package com.graficases.backend.service.market;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Neobyte (PrestaShop) expone resultados de busqueda en HTML renderizado server-side.
 */
@Component
public class NeobytePriceProvider implements GpuPriceProvider {

    private static final String STORE = "Neobyte";
    private static final String CURRENCY = "EUR";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";

    @Override
    public String storeName() {
        return STORE;
    }

    @Override
    public Optional<PriceQuote> findLowestPrice(GpuMatchSpec spec) throws Exception {
        String url = "https://www.neobyte.es/buscar?controller=search"
                + "&orderby=position&orderway=desc"
                + "&search_query=" + encode(spec.query())
                + "&submit_search=";

        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true)
                .get();

        BigDecimal best = null;
        String bestUrl = null;

        for (Element product : doc.select("article.js-product-miniature")) {
            Element link = product.selectFirst(".product-title a[href]");
            if (link == null) {
                continue;
            }
            String title = link.text();
            String href = link.absUrl("href");
            if (href.isBlank()) {
                href = link.attr("href");
            }

            // Filtrar a "tarjeta grafica" para evitar PCs / portatiles, etc.
            String hrefLower = href.toLowerCase();
            if (!hrefLower.contains("tarjeta-grafica") && !title.toLowerCase().contains("tarjeta")) {
                continue;
            }

            if (!spec.matchesTitle(title)) {
                continue;
            }

            if (!isInStock(product)) {
                continue;
            }

            Element priceEl = product.selectFirst(".product-price-and-shipping .product-price");
            if (priceEl == null) {
                continue;
            }

            BigDecimal price = parsePrice(priceEl);
            if (price == null) {
                continue;
            }

            if (best == null || price.compareTo(best) < 0) {
                best = price;
                bestUrl = href;
            }
        }

        if (best == null) {
            return Optional.empty();
        }

        return Optional.of(new PriceQuote(best, CURRENCY, STORE, bestUrl, Instant.now()));
    }

    private static boolean isInStock(Element product) {
        Element badge = product.selectFirst(".product-availability .badge");
        if (badge == null) {
            return false;
        }

        String klass = badge.className().toLowerCase();
        if (klass.contains("product-available")) {
            return true;
        }

        String text = badge.text().toLowerCase();
        return text.contains("stock") || text.contains("disponible") || text.contains("recibelo");
    }

    private static BigDecimal parsePrice(Element priceEl) {
        String content = priceEl.attr("content");
        if (content != null && !content.isBlank()) {
            try {
                return new BigDecimal(content.trim());
            } catch (NumberFormatException ignored) {
                // fallback a texto visible
            }
        }

        String raw = priceEl.text();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        // 1.699,89 € -> 1699.89
        String cleaned = raw
                .replace("\u00A0", " ")
                .replace("€", "")
                .trim();
        cleaned = cleaned.replace(".", "").replace(",", ".");
        cleaned = cleaned.replaceAll("[^0-9.]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String encode(String raw) {
        if (raw == null) {
            return "";
        }
        return java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8);
    }
}

