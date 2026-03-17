package com.graficases.backend.service.market;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Coolmod devuelve resultados de busqueda server-side en /?s=..., suficientes para scraping simple.
 */
@Component
public class CoolmodPriceProvider implements GpuPriceProvider {

    private static final String STORE = "Coolmod";
    private static final String CURRENCY = "EUR";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";
    private static final int MAX_PAGES = 2;

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

    @Override
    public String storeName() {
        return STORE;
    }

    @Override
    public Optional<PriceQuote> findLowestPrice(GpuMatchSpec spec) throws Exception {
        BigDecimal best = null;
        String bestUrl = null;
        Set<String> seenUrls = new HashSet<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            Document doc = fetchSearchPage(spec, page);
            PageScanResult result = scanPage(doc, spec, seenUrls);
            if (!result.sawProducts()) {
                break;
            }
            if (page > 1 && result.newProducts() == 0) {
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

    Optional<PriceQuote> findLowestPriceOnPage(Document doc, GpuMatchSpec spec) {
        return scanPage(doc, spec, new HashSet<>()).quote();
    }

    private Document fetchSearchPage(GpuMatchSpec spec, int page) throws Exception {
        String url = "https://www.coolmod.com/?s=" + encode(spec.query());
        if (page > 1) {
            url += "&page=" + page;
        }

        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true)
                .get();
    }

    private PageScanResult scanPage(Document doc, GpuMatchSpec spec, Set<String> seenUrls) {
        BigDecimal best = null;
        String bestUrl = "";
        boolean sawProducts = false;
        int newProducts = 0;

        for (Element product : doc.select("article.product-card")) {
            sawProducts = true;

            Element link = product.selectFirst(".card-title a[href]");
            if (link == null) {
                link = product.selectFirst("figure a[href]");
            }
            if (link == null) {
                continue;
            }

            String productUrl = link.absUrl("href");
            if (productUrl.isBlank()) {
                productUrl = absoluteUrl(link.attr("href"));
            }
            if (!productUrl.isBlank() && !seenUrls.add(productUrl)) {
                continue;
            }
            newProducts++;

            String title = firstNonBlank(
                    link.text(),
                    link.attr("data-itemname"),
                    product.attr("data-itemname")
            );
            String category = firstNonBlank(
                    link.attr("data-itemcategory2"),
                    product.select(".card-text").stream()
                            .map(Element::text)
                            .filter(text -> !text.isBlank())
                            .findFirst()
                            .orElse("")
            );

            if (!isLikelyGpuProduct(title, category, productUrl, spec)) {
                continue;
            }
            if (!isInStock(product)) {
                continue;
            }

            BigDecimal price = readPrice(product, link);
            if (price == null) {
                continue;
            }

            if (best == null || price.compareTo(best) < 0) {
                best = price;
                bestUrl = productUrl;
            }
        }

        if (best == null) {
            return new PageScanResult(sawProducts, newProducts, Optional.empty());
        }

        return new PageScanResult(
                sawProducts,
                newProducts,
                Optional.of(new PriceQuote(best, CURRENCY, STORE, bestUrl, Instant.now()))
        );
    }

    private static boolean isLikelyGpuProduct(String title, String category, String productUrl, GpuMatchSpec spec) {
        if (title == null || title.isBlank()) {
            return false;
        }
        if (!spec.matchesTitle(title)) {
            return false;
        }

        String compactTitle = GpuMatchSpec.toCompact(title);
        String compactCategory = GpuMatchSpec.toCompact(category);
        String lowerUrl = productUrl == null ? "" : productUrl.toLowerCase(Locale.ROOT);

        if (containsAny(compactTitle, NON_GPU_HINTS)) {
            return false;
        }
        if (containsAny(lowerUrl, NON_GPU_URL_HINTS)) {
            return false;
        }

        if (compactCategory.contains("TARJETASGRAFICAS") || compactCategory.contains("TARJETAGRAFICA")) {
            return true;
        }

        return lowerUrl.contains("grafica") || containsAny(compactTitle, GPU_HINTS);
    }

    private static boolean isInStock(Element product) {
        Element addToCart = product.selectFirst(".card-actions .add-to-cart, .card-actions button[data-id]");
        if (addToCart != null) {
            String klass = addToCart.className().toLowerCase(Locale.ROOT);
            return !addToCart.hasAttr("disabled") && !klass.contains("disabled");
        }

        String lowerText = product.text().toLowerCase(Locale.ROOT);
        if (lowerText.contains("agotado") || lowerText.contains("sin stock") || lowerText.contains("no disponible")) {
            return false;
        }

        return lowerText.contains("recibelo");
    }

    private static BigDecimal readPrice(Element product, Element link) {
        String dataPrice = firstNonBlank(
                link.attr("data-itemprice"),
                product.selectFirst("a[data-itemprice]") != null
                        ? product.selectFirst("a[data-itemprice]").attr("data-itemprice")
                        : ""
        );
        BigDecimal dataPriceParsed = parseDecimalPrice(dataPrice);
        if (dataPriceParsed != null) {
            return dataPriceParsed;
        }

        Element intPrice = product.selectFirst(".product_price.int_price");
        Element decPrice = product.selectFirst(".dec_price");
        if (intPrice == null) {
            return null;
        }

        String intPart = digitsOnly(intPrice.text());
        String decPart = decPrice == null ? "00" : digitsOnly(decPrice.text());
        if (intPart.isBlank()) {
            return null;
        }
        if (decPart.isBlank()) {
            decPart = "00";
        }

        try {
            return new BigDecimal(intPart + "." + decPart);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static BigDecimal parseDecimalPrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String digitsOnly(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[^0-9]", "");
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
        return raw.startsWith("http") ? raw : "https://www.coolmod.com" + raw;
    }

    private static String encode(String raw) {
        if (raw == null) {
            return "";
        }
        return java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8);
    }

    private record PageScanResult(boolean sawProducts, int newProducts, Optional<PriceQuote> quote) {
    }
}
