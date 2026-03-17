package com.graficases.backend.service.market;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * PCBox expone un endpoint VTEX estable para busqueda de catalogo con orden por precio.
 */
@Component
public class PcBoxPriceProvider implements GpuPriceProvider {

    private static final String STORE = "PCBox";
    private static final String CURRENCY = "EUR";
    private static final String SEARCH_ENDPOINT =
            "https://www.pcbox.com/api/io/_v/api/intelligent-search/product_search/trade-policy/1";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";
    private static final int PAGE_SIZE = 24;
    private static final int MAX_PAGES = 3;

    private static final List<String> GPU_HINTS = List.of(
            "TARJETA", "GRAFICA", "GPU", "GEFORCE", "RADEON", "ARC", "GRAPHICSCARD"
    );

    private static final List<String> NON_GPU_HINTS = List.of(
            "PORTATIL", "LAPTOP", "NOTEBOOK", "ORDENADOR", "SOBREMESA", "WORKSTATION",
            "ALLINONE", "MINIPC", "SERVIDOR", "CPU", "TABLET", "MONITOR"
    );

    private static final List<String> NON_GPU_URL_HINTS = List.of(
            "portatil", "ordenador", "sobremesa", "workstation", "all-in-one",
            "allinone", "mini-pc", "cpu-", "servidor", "tablet", "monitor"
    );

    private final ObjectMapper objectMapper;

    public PcBoxPriceProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String storeName() {
        return STORE;
    }

    @Override
    public Optional<PriceQuote> findLowestPrice(GpuMatchSpec spec) throws Exception {
        int totalPages = MAX_PAGES;

        for (int page = 1; page <= totalPages; page++) {
            JsonNode root = fetchSearchPage(spec, page);
            JsonNode products = root.path("products");
            if (!products.isArray() || products.isEmpty()) {
                break;
            }

            PriceQuote firstMatch = findFirstMatchingQuote(products, spec);
            if (firstMatch != null) {
                return Optional.of(firstMatch);
            }

            int discoveredPages = root.path("pagination").path("count").asInt(0);
            if (discoveredPages > 0) {
                totalPages = Math.min(MAX_PAGES, discoveredPages);
            }
        }

        return Optional.empty();
    }

    private JsonNode fetchSearchPage(GpuMatchSpec spec, int page) throws Exception {
        String url = SEARCH_ENDPOINT
                + "?query=" + encode(spec.query())
                + "&page=" + page
                + "&count=" + PAGE_SIZE
                + "&sort=price:asc";

        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .userAgent(USER_AGENT)
                .header("Accept", "application/json")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .timeout(9000)
                .followRedirects(true)
                .execute();

        if (response.statusCode() >= 400) {
            throw new IllegalStateException("PCBox devolvio HTTP " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    PriceQuote findFirstMatchingQuote(JsonNode products, GpuMatchSpec spec) {
        for (JsonNode product : products) {
            String title = firstNonBlank(
                    text(product, "productName"),
                    text(product, "productTitle"),
                    text(product, "description")
            );

            String productUrl = absoluteUrl(text(product, "link"));
            if (!isLikelyGpuProduct(title, productUrl, product, spec)) {
                continue;
            }

            BigDecimal price = readLowestInStockPrice(product);
            if (price == null) {
                continue;
            }

            return new PriceQuote(price, CURRENCY, STORE, productUrl, Instant.now());
        }

        return null;
    }

    private static boolean isLikelyGpuProduct(String title, String productUrl, JsonNode product, GpuMatchSpec spec) {
        if (title == null || title.isBlank()) {
            return false;
        }
        if (!spec.matchesTitle(title)) {
            return false;
        }

        String compactTitle = GpuMatchSpec.toCompact(title);
        String lowerUrl = productUrl == null ? "" : productUrl.toLowerCase(Locale.ROOT);
        String compactCategories = compactCategories(product.path("categories"));

        if (containsAny(compactTitle, NON_GPU_HINTS)) {
            return false;
        }
        if (containsAny(lowerUrl, NON_GPU_URL_HINTS)) {
            return false;
        }

        if (compactCategories.contains("TARJETASGRAFICAS") || compactCategories.contains("TARJETAGRAFICA")) {
            return true;
        }

        return lowerUrl.contains("tarjeta-grafica") || containsAny(compactTitle, GPU_HINTS);
    }

    private static BigDecimal readLowestInStockPrice(JsonNode product) {
        BigDecimal best = null;

        JsonNode items = product.path("items");
        if (!items.isArray()) {
            return null;
        }

        for (JsonNode item : items) {
            JsonNode sellers = item.path("sellers");
            if (!sellers.isArray()) {
                continue;
            }

            for (JsonNode seller : sellers) {
                JsonNode offer = seller.path("commertialOffer");
                if (!isAvailable(offer)) {
                    continue;
                }

                BigDecimal price = firstPrice(
                        offer.get("Price"),
                        offer.get("spotPrice"),
                        offer.get("PriceWithoutDiscount"),
                        offer.get("ListPrice")
                );
                if (price == null) {
                    continue;
                }

                if (best == null || price.compareTo(best) < 0) {
                    best = price;
                }
            }
        }

        return best;
    }

    private static boolean isAvailable(JsonNode offer) {
        if (offer == null || offer.isMissingNode() || offer.isNull()) {
            return false;
        }

        JsonNode quantity = offer.get("AvailableQuantity");
        return quantity != null && quantity.isNumber() && quantity.asInt(0) > 0;
    }

    private static BigDecimal firstPrice(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            BigDecimal parsed = asBigDecimal(node);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static BigDecimal asBigDecimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }

        String raw = node.asText("").trim();
        if (raw.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String compactCategories(JsonNode categoriesNode) {
        if (!categoriesNode.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode category : categoriesNode) {
            sb.append(GpuMatchSpec.toCompact(category.asText("")));
        }
        return sb.toString();
    }

    private static boolean containsAny(String text, List<String> tokens) {
        if (text == null || text.isBlank()) {
            return false;
        }
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String absoluteUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.startsWith("http") ? raw : "https://www.pcbox.com" + raw;
    }

    private static String encode(String raw) {
        if (raw == null) {
            return "";
        }
        return java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8);
    }
}
