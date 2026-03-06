package com.graficases.backend.service.market;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * PCComponentes expone un endpoint JSON de búsqueda (mismo que usa el frontend).
 *
 * Nota: el buscador es "fuzzy" y con queries cortas puede devolver muchos modelos cercanos.
 * Para evitar paginar mucho, intentamos resolver el filtro "Gráfica Series" (filter_group_5) y luego
 * pedimos resultados ordenados por precio ascendente.
 */
@Component
public class PcComponentesPriceProvider implements GpuPriceProvider {

    private static final String STORE = "PCComponentes";
    private static final String CURRENCY = "EUR";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";

    private static final String GPU_FAMILY_ID = "6"; // Tarjetas Graficas
    private static final int PAGE_SIZE = 40;
    private static final int MAX_PAGES_WITH_SERIES = 3;
    private static final int MAX_PAGES_FALLBACK = 8;

    // filter_group_5[] en URL-encoding.
    private static final String SERIES_FILTER_PARAM = "filter_group_5%5B%5D";

    private final ObjectMapper objectMapper;

    public PcComponentesPriceProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String storeName() {
        return STORE;
    }

    @Override
    public Optional<PriceQuote> findLowestPrice(GpuMatchSpec spec) throws Exception {
        Optional<String> seriesId = resolveSeriesId(spec);

        PriceQuote bestNonNew = null;
        int maxPages = seriesId.isPresent() ? MAX_PAGES_WITH_SERIES : MAX_PAGES_FALLBACK;

        for (int page = 1; page <= maxPages; page++) {
            String url = buildSearchUrl(spec, seriesId.orElse(null), page);
            JsonNode root = fetchJson(url);
            JsonNode articles = root.path("articles");
            if (!articles.isArray() || articles.isEmpty()) {
                break;
            }

            for (JsonNode article : articles) {
                String title = text(article, "name");
                if (!spec.matchesTitle(title)) {
                    continue;
                }

                if (!isAvailable(article)) {
                    continue;
                }

                BigDecimal price = readPrice(article);
                if (price == null) {
                    continue;
                }

                String slug = text(article, "slug");
                String productUrl = slug.isBlank() ? "" : ("https://www.pccomponentes.com/" + slug);

                PriceQuote quote = new PriceQuote(price, CURRENCY, STORE, productUrl, Instant.now());
                if (isNewArticle(article, title)) {
                    // Ordenado por precio asc => el primer match nuevo y disponible es el más barato.
                    return Optional.of(quote);
                }

                if (bestNonNew == null) {
                    bestNonNew = quote;
                }
            }

            int totalPages = root.path("totalPages").asInt(0);
            if (totalPages > 0 && page >= totalPages) {
                break;
            }
        }

        return Optional.ofNullable(bestNonNew);
    }

    private Optional<String> resolveSeriesId(GpuMatchSpec spec) {
        String url = "https://www.pccomponentes.com/api/articles/"
                + "?query=" + encode(spec.query())
                + "&families=" + GPU_FAMILY_ID
                + "&sort=relevance"
                + "&pageSize=1"
                + "&page=1";

        JsonNode root;
        try {
            root = fetchJson(url);
        } catch (Exception ignored) {
            return Optional.empty();
        }

        JsonNode values = root.path("filters").path("filter_group_5").path("values");
        if (!values.isArray() || values.isEmpty()) {
            return Optional.empty();
        }

        String modelFull = spec.modelFullCompact();
        String modelBase = spec.modelBaseCompact();
        if ((modelFull == null || modelFull.isBlank()) && (modelBase == null || modelBase.isBlank())) {
            return Optional.empty();
        }

        String bestId = null;
        int bestScore = 0;

        for (JsonNode value : values) {
            String id = text(value, "id");
            if (id.isBlank()) {
                continue;
            }
            String name = text(value, "name");
            if (name.isBlank()) {
                continue;
            }

            String compact = GpuMatchSpec.toCompact(name);
            int score = 0;
            if (modelFull != null && !modelFull.isBlank() && compact.contains(modelFull)) {
                score += 1000;
            }
            if (modelBase != null && !modelBase.isBlank() && compact.contains(modelBase)) {
                score += 500;
            }

            // Penalizar entradas genéricas tipo "Grafica NVIDIA" cuando sea posible.
            if (compact.equals("GRAFICANVIDIA") || compact.equals("GRAFICAAMD") || compact.equals("GRAFICAINTEL")) {
                score -= 50;
            }

            if (score > bestScore) {
                bestScore = score;
                bestId = id;
            }
        }

        return bestScore > 0 ? Optional.ofNullable(bestId) : Optional.empty();
    }

    private static String buildSearchUrl(GpuMatchSpec spec, String seriesId, int page) {
        StringBuilder sb = new StringBuilder("https://www.pccomponentes.com/api/articles/");
        sb.append("?query=").append(encode(spec.query()));
        sb.append("&families=").append(GPU_FAMILY_ID);
        sb.append("&sort=price_asc");
        sb.append("&pageSize=").append(PAGE_SIZE);
        sb.append("&page=").append(page);
        if (seriesId != null && !seriesId.isBlank()) {
            sb.append("&").append(SERIES_FILTER_PARAM).append("=").append(encode(seriesId));
        }
        return sb.toString();
    }

    private JsonNode fetchJson(String url) throws Exception {
        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .header("Accept", "application/json")
                .timeout(9000)
                .followRedirects(true)
                .execute();
        return objectMapper.readTree(response.body());
    }

    private static boolean isAvailable(JsonNode article) {
        int availabilityCode = article.path("delivery").path("availabilityCode").asInt(0);
        if (availabilityCode == 1) {
            return true;
        }

        JsonNode stock = article.get("stock");
        return stock != null && stock.isNumber() && stock.asInt(0) > 0;
    }

    private static boolean isNewArticle(JsonNode article, String title) {
        String objectId = text(article, "objectId");
        if (!objectId.isBlank()) {
            return objectId.toLowerCase(Locale.ROOT).endsWith("#new");
        }
        return title == null || !title.toLowerCase(Locale.ROOT).contains("reacond");
    }

    private static BigDecimal readPrice(JsonNode article) {
        JsonNode promo = article.get("promotionalPrice");
        if (promo != null && promo.isNumber()) {
            return promo.decimalValue();
        }

        JsonNode price = article.get("price");
        if (price != null && price.isNumber()) {
            return price.decimalValue();
        }

        JsonNode original = article.get("originalPrice");
        if (original != null && original.isNumber()) {
            return original.decimalValue();
        }

        JsonNode pricingPrice = article.path("pricing").path("price");
        if (pricingPrice.isNumber()) {
            return pricingPrice.decimalValue();
        }

        return null;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText("");
    }

    private static String encode(String raw) {
        if (raw == null) {
            return "";
        }
        return java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8);
    }
}

