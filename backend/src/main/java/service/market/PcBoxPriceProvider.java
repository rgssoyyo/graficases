package com.graficases.backend.service.market;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * PCBox (VTEX) bloquea el endpoint JSON para bots, pero expone un ItemList JSON-LD
 * en el HTML del buscador.
 */
@Component
public class PcBoxPriceProvider implements GpuPriceProvider {

    private static final String STORE = "PCBox";
    private static final String CURRENCY = "EUR";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";
    private static final int MAX_PAGES = 4;

    private static final List<String> GPU_HINTS = List.of(
            "TARJETA", "GRAFICA", "GPU", "GEFORCE", "RADEON", "ARC", "GRAPHICSCARD"
    );

    private static final List<String> NON_GPU_HINTS = List.of(
            "PORTATIL", "LAPTOP", "NOTEBOOK", "ORDENADOR", "SOBREMESA", "WORKSTATION",
            "ALLINONE", "MINIPC", "SERVIDOR", "CPU", "TABLET"
    );

    private static final List<String> NON_GPU_URL_HINTS = List.of(
            "portatil", "ordenador", "sobremesa", "workstation", "all-in-one",
            "allinone", "mini-pc", "cpu-", "servidor", "tablet"
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
        BigDecimal best = null;
        String bestUrl = null;

        for (int page = 1; page <= MAX_PAGES; page++) {
            String url = "https://www.pcbox.com/buscar/" + encode(spec.query())
                    + "?map=ft"
                    + "&page=" + page;

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            PageScanResult result = scanPage(doc, spec);
            if (!result.sawResults()) {
                break;
            }

            if (result.quote().isEmpty()) {
                continue;
            }

            PriceQuote quote = result.quote().get();
            if (best == null || quote.price().compareTo(best) < 0) {
                best = quote.price();
                bestUrl = quote.productUrl();
            }
        }

        if (best == null) {
            return Optional.empty();
        }

        return Optional.of(new PriceQuote(best, CURRENCY, STORE, bestUrl, Instant.now()));
    }

    private PageScanResult scanPage(Document doc, GpuMatchSpec spec) {
        BigDecimal best = null;
        String bestUrl = "";
        boolean sawResults = false;

        for (Element script : doc.select("script[type=application/ld+json]")) {
            String json = script.data();
            if (json == null || json.isBlank()) {
                continue;
            }

            JsonNode root;
            try {
                root = objectMapper.readTree(json);
            } catch (Exception ignored) {
                continue;
            }

            if (!"ItemList".equalsIgnoreCase(text(root, "@type"))) {
                continue;
            }

            JsonNode items = root.path("itemListElement");
            if (!items.isArray()) {
                continue;
            }
            if (!items.isEmpty()) {
                sawResults = true;
            }

            for (JsonNode wrapper : items) {
                JsonNode item = wrapper.path("item");
                if (item.isMissingNode() || item.isNull()) {
                    continue;
                }

                String title = decodeHtmlEntities(text(item, "name"));
                String productUrl = text(item, "@id");

                if (!isLikelyGpuItem(title, productUrl, spec)) {
                    continue;
                }

                BigDecimal price = readLowestInStockPrice(item);
                if (price == null) {
                    continue;
                }

                if (best == null || price.compareTo(best) < 0) {
                    best = price;
                    bestUrl = productUrl;
                }
            }
        }

        if (best == null) {
            return new PageScanResult(sawResults, Optional.empty());
        }

        return new PageScanResult(
                sawResults,
                Optional.of(new PriceQuote(best, CURRENCY, STORE, bestUrl, Instant.now()))
        );
    }

    private static boolean isLikelyGpuItem(String title, String productUrl, GpuMatchSpec spec) {
        if (title == null || title.isBlank()) {
            return false;
        }
        if (!spec.matchesTitle(title)) {
            return false;
        }

        String compactTitle = GpuMatchSpec.toCompact(title);
        String lowerUrl = productUrl == null ? "" : productUrl.toLowerCase(Locale.ROOT);

        if (containsAny(compactTitle, NON_GPU_HINTS)) {
            return false;
        }
        if (containsAny(lowerUrl, NON_GPU_URL_HINTS)) {
            return false;
        }

        return lowerUrl.contains("tarjeta-grafica") || containsAny(compactTitle, GPU_HINTS);
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

    private static BigDecimal readLowestInStockPrice(JsonNode item) {
        JsonNode offersRoot = item.path("offers");
        if (offersRoot.isMissingNode() || offersRoot.isNull()) {
            return null;
        }

        BigDecimal best = null;

        JsonNode offers = offersRoot.path("offers");
        if (offers.isArray()) {
            for (JsonNode offer : offers) {
                if (!isInStock(offer)) {
                    continue;
                }
                BigDecimal price = asBigDecimal(offer.get("price"));
                if (price == null) {
                    continue;
                }
                if (best == null || price.compareTo(best) < 0) {
                    best = price;
                }
            }
        }

        if (best != null) {
            return best;
        }

        // Fallback para casos en los que solo venga aggregate offer sin lista interna.
        if (isInStock(offersRoot)) {
            BigDecimal direct = asBigDecimal(offersRoot.get("price"));
            if (direct != null) {
                return direct;
            }

            BigDecimal low = asBigDecimal(offersRoot.get("lowPrice"));
            if (low != null) {
                return low;
            }
        }

        return null;
    }

    private static boolean isInStock(JsonNode offerNode) {
        String availability = text(offerNode, "availability").toLowerCase(Locale.ROOT);
        return availability.contains("instock");
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

    private static String decodeHtmlEntities(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return Parser.unescapeEntities(raw, false);
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

    private record PageScanResult(boolean sawResults, Optional<PriceQuote> quote) {
    }
}
