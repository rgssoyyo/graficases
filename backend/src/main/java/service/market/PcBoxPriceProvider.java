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
 * PCBox (VTEX) tiene un endpoint publico de busqueda en JSON.
 */
@Component
public class PcBoxPriceProvider implements GpuPriceProvider {

    private static final String STORE = "PCBox";
    private static final String CURRENCY = "EUR";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";

    // Limite del endpoint: _to no puede ser 50 (devuelve 400). 49 suele funcionar.
    private static final int FROM = 0;
    private static final int TO = 49;

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
        String url = "https://www.pcbox.com/api/catalog_system/pub/products/search"
                + "?ft=" + encode(spec.query())
                + "&_from=" + FROM
                + "&_to=" + TO
                + "&O=OrderByPriceASC";

        Connection.Response response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .timeout(8000)
                .followRedirects(true)
                .execute();

        JsonNode root = objectMapper.readTree(response.body());
        if (!root.isArray()) {
            return Optional.empty();
        }

        BigDecimal best = null;
        String bestUrl = null;

        for (JsonNode product : root) {
            if (!isGpuCategory(product)) {
                continue;
            }

            String title = text(product, "productName");
            if (!spec.matchesTitle(title)) {
                continue;
            }

            JsonNode items = product.get("items");
            if (items == null || !items.isArray()) {
                continue;
            }

            for (JsonNode item : items) {
                JsonNode sellers = item.get("sellers");
                if (sellers == null || !sellers.isArray()) {
                    continue;
                }

                for (JsonNode seller : sellers) {
                    JsonNode offer = seller.get("commertialOffer");
                    if (offer == null || offer.isNull()) {
                        continue;
                    }
                    boolean isAvailable = offer.path("IsAvailable").asBoolean(false);
                    if (!isAvailable) {
                        continue;
                    }
                    JsonNode priceNode = offer.get("Price");
                    if (priceNode == null || !priceNode.isNumber()) {
                        continue;
                    }
                    BigDecimal price = priceNode.decimalValue();
                    if (best == null || price.compareTo(best) < 0) {
                        best = price;
                        bestUrl = text(product, "link");
                    }
                }
            }
        }

        if (best == null) {
            return Optional.empty();
        }

        return Optional.of(new PriceQuote(best, CURRENCY, STORE, bestUrl, Instant.now()));
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText("");
    }

    private static boolean isGpuCategory(JsonNode product) {
        JsonNode categories = product.get("categories");
        if (categories == null || !categories.isArray()) {
            return false;
        }

        for (JsonNode category : categories) {
            String value = category.asText("");
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.contains("tarjetas gr")) {
                return true;
            }
        }
        return false;
    }

    private static String encode(String raw) {
        if (raw == null) {
            return "";
        }
        return java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8);
    }
}
