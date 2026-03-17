package com.graficases.backend.service.market;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PCComponentes bloquea parte del scraping con Cloudflare.
 * Intentamos primero el endpoint JSON y, si falla, usamos Brave para descubrir fichas
 * y r.jina.ai para leer una ficha accesible y extraer precio desde el propio producto,
 * sus variantes o los articulos similares.
 */
@Component
public class PcComponentesPriceProvider implements GpuPriceProvider {

    private static final String STORE = "PCComponentes";
    private static final String CURRENCY = "EUR";
    private static final String BRAVE_SEARCH_URL = "https://search.brave.com/search?q=";
    private static final String JINA_PROXY_URL = "https://r.jina.ai/http://";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123 Safari/537.36";

    private static final String GPU_FAMILY_ID = "6"; // Tarjetas Graficas
    private static final int PAGE_SIZE = 40;
    private static final int MAX_PAGES = 4;
    private static final int API_TIMEOUT_MS = 3500;
    private static final int SEARCH_TIMEOUT_MS = 4000;
    private static final int JINA_TIMEOUT_MS = 4500;
    private static final int MAX_CANDIDATE_URLS = 8;
    private static final int MAX_JINA_PAGES = 4;

    private static final Pattern BRAVE_RESULT_URL_PATTERN =
            Pattern.compile("https://www\\.pccomponentes\\.com/[^\"'&<>\\s)]+");
    private static final Pattern MARKDOWN_PRODUCT_URL_PATTERN =
            Pattern.compile("\\((https?://www\\.pccomponentes\\.com/[^\\s)]+)");
    private static final Pattern LINK_TITLE_PATTERN = Pattern.compile("\"([^\"]+)\"\\)");
    private static final Pattern SELLER_PATTERN =
            Pattern.compile("Vendido(?:\\s+y\\s+enviado)?\\s+por\\s+([^\\]\\n]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_PATTERN =
            Pattern.compile("(?<!\\d)(\\d{1,3}(?:\\.\\d{3})*(?:,\\d{1,2})?|\\d+(?:,\\d{1,2})?)(?=\\s*€)");

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
        Optional<PriceQuote> apiQuote = findLowestPriceFromApi(spec);
        if (apiQuote.isPresent()) {
            return apiQuote;
        }

        PriceQuote bestFromPages = null;
        int checkedPages = 0;

        for (String candidateUrl : discoverCandidateProductUrls(spec)) {
            if (checkedPages >= MAX_JINA_PAGES) {
                break;
            }

            Optional<String> markdownOpt = fetchJinaMarkdown(candidateUrl);
            if (markdownOpt.isEmpty()) {
                continue;
            }

            checkedPages++;
            Optional<PriceQuote> pageQuote = findLowestPriceInMarkdown(markdownOpt.get(), spec, candidateUrl);
            if (pageQuote.isEmpty()) {
                continue;
            }

            if (bestFromPages == null || pageQuote.get().price().compareTo(bestFromPages.price()) < 0) {
                bestFromPages = pageQuote.get();
            }

            // Una ficha accesible suele traer variantes y articulos similares del mismo modelo.
            break;
        }

        return Optional.ofNullable(bestFromPages);
    }

    private Optional<PriceQuote> findLowestPriceFromApi(GpuMatchSpec spec) throws Exception {
        PriceQuote bestNonNew = null;

        for (int page = 1; page <= MAX_PAGES; page++) {
            Optional<JsonNode> rootOpt = fetchJson(buildSearchUrl(spec, page));
            if (rootOpt.isEmpty()) {
                return Optional.ofNullable(bestNonNew);
            }

            JsonNode root = rootOpt.get();
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

    List<String> discoverCandidateProductUrls(GpuMatchSpec spec) throws Exception {
        LinkedHashSet<String> urls = new LinkedHashSet<>();

        for (String query : buildBraveQueries(spec)) {
            String html = fetchBraveSearchHtml(query);
            if (html.isBlank()) {
                continue;
            }

            urls.addAll(extractCandidateProductUrls(html));
            if (urls.size() >= MAX_CANDIDATE_URLS) {
                break;
            }
        }

        return new ArrayList<>(urls).subList(0, Math.min(urls.size(), MAX_CANDIDATE_URLS));
    }

    List<String> extractCandidateProductUrls(String html) {
        if (html == null || html.isBlank()) {
            return List.of();
        }

        LinkedHashSet<String> urls = new LinkedHashSet<>();
        Matcher matcher = BRAVE_RESULT_URL_PATTERN.matcher(html);
        while (matcher.find()) {
            String normalized = normalizeProductUrl(matcher.group(0));
            if (isLikelyProductUrl(normalized)) {
                urls.add(normalized);
            }
        }

        return new ArrayList<>(urls);
    }

    Optional<PriceQuote> findLowestPriceInMarkdown(String markdown, GpuMatchSpec spec, String sourceUrl) {
        if (markdown == null || markdown.isBlank() || isCloudflareChallenge(markdown)) {
            return Optional.empty();
        }

        PriceQuote best = null;
        Set<String> seenUrls = new LinkedHashSet<>();

        for (String line : markdown.split("\\R")) {
            ProductLineCandidate candidate = parseMarkdownCandidate(line);
            if (candidate == null) {
                continue;
            }

            if (!isLikelyProductUrl(candidate.productUrl())) {
                continue;
            }
            if (sellerLooksExternal(line)) {
                continue;
            }
            if (!spec.matchesTitle(candidate.title())) {
                continue;
            }
            if (!seenUrls.add(candidate.productUrl())) {
                continue;
            }

            PriceQuote quote = new PriceQuote(candidate.price(), CURRENCY, STORE, candidate.productUrl(), Instant.now());
            if (best == null || quote.price().compareTo(best.price()) < 0) {
                best = quote;
            }
        }

        if (best != null) {
            return Optional.of(best);
        }

        return findPrimaryProductQuote(markdown, spec, sourceUrl);
    }

    private static String buildSearchUrl(GpuMatchSpec spec, int page) {
        return "https://www.pccomponentes.com/api/articles/"
                + "?query=" + encode(spec.query())
                + "&families=" + GPU_FAMILY_ID
                + "&sort=price_asc"
                + "&pageSize=" + PAGE_SIZE
                + "&page=" + page;
    }

    private Optional<JsonNode> fetchJson(String url) throws Exception {
        Connection.Response response = Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .header("Referer", "https://www.pccomponentes.com/buscar/")
                .timeout(API_TIMEOUT_MS)
                .followRedirects(true)
                .execute();

        if (response.statusCode() >= 400) {
            return Optional.empty();
        }

        String body = response.body();
        if (isCloudflareChallenge(body)) {
            return Optional.empty();
        }
        if (!looksLikeJson(response.contentType(), body)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readTree(body));
    }

    private String fetchBraveSearchHtml(String query) throws Exception {
        String url = BRAVE_SEARCH_URL + encode(query);
        Connection.Response response = Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .timeout(SEARCH_TIMEOUT_MS)
                .followRedirects(true)
                .execute();

        if (response.statusCode() >= 400) {
            return "";
        }

        return response.body();
    }

    private Optional<String> fetchJinaMarkdown(String productUrl) throws Exception {
        String proxiedUrl = JINA_PROXY_URL + normalizeProductUrl(productUrl);
        Connection.Response response = Jsoup.connect(proxiedUrl)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .header("Accept", "text/plain, text/markdown, */*")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .timeout(JINA_TIMEOUT_MS)
                .followRedirects(true)
                .execute();

        if (response.statusCode() >= 400) {
            return Optional.empty();
        }

        String body = response.body();
        if (isCloudflareChallenge(body)) {
            return Optional.empty();
        }

        return Optional.of(body);
    }

    private static List<String> buildBraveQueries(GpuMatchSpec spec) {
        String query = spec.query().trim();
        String nombre = spec.nombre().trim();
        return List.of(
                "site:pccomponentes.com \"" + query + "\" \"tarjeta grafica\"",
                "site:pccomponentes.com \"" + nombre + "\""
        );
    }

    private static ProductLineCandidate parseMarkdownCandidate(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String productUrl = lastMatch(MARKDOWN_PRODUCT_URL_PATTERN, line);
        if (productUrl.isBlank()) {
            return null;
        }

        BigDecimal price = firstPrice(line);
        if (price == null) {
            return null;
        }

        String title = lastMatch(LINK_TITLE_PATTERN, line);
        if (title.isBlank()) {
            title = line;
        }

        return new ProductLineCandidate(title.trim(), normalizeProductUrl(productUrl), price);
    }

    private static Optional<PriceQuote> findPrimaryProductQuote(String markdown, GpuMatchSpec spec, String sourceUrl) {
        String[] lines = markdown.split("\\R");
        String primaryTitle = extractPrimaryTitle(lines);
        if (primaryTitle.isBlank() || !spec.matchesTitle(primaryTitle)) {
            return Optional.empty();
        }

        int anchorIndex = findLineIndex(lines, primaryTitle);
        if (anchorIndex < 0) {
            anchorIndex = 0;
        }

        for (int i = anchorIndex; i < Math.min(lines.length, anchorIndex + 12); i++) {
            BigDecimal price = firstPrice(lines[i]);
            if (price == null) {
                continue;
            }

            return Optional.of(new PriceQuote(
                    price,
                    CURRENCY,
                    STORE,
                    normalizeProductUrl(sourceUrl),
                    Instant.now()
            ));
        }

        return Optional.empty();
    }

    private static String extractPrimaryTitle(String[] lines) {
        for (String line : lines) {
            if (line == null) {
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            if (trimmed.startsWith("Title: ")) {
                trimmed = trimmed.substring("Title: ".length()).trim();
            }
            if (trimmed.endsWith("| PcComponentes.com")) {
                return trimmed.substring(0, trimmed.length() - "| PcComponentes.com".length()).trim();
            }
        }
        return "";
    }

    private static int findLineIndex(String[] lines, String needle) {
        int found = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && lines[i].contains(needle)) {
                found = i;
            }
        }
        return found;
    }

    private static boolean sellerLooksExternal(String line) {
        Matcher matcher = SELLER_PATTERN.matcher(line);
        if (!matcher.find()) {
            return false;
        }

        String seller = matcher.group(1).trim();
        return !seller.toLowerCase(Locale.ROOT).contains("pccomponentes");
    }

    private static BigDecimal firstPrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Matcher matcher = PRICE_PATTERN.matcher(raw);
        if (!matcher.find()) {
            return null;
        }

        String normalized = matcher.group(1)
                .replace(".", "")
                .replace(',', '.');
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String lastMatch(Pattern pattern, String raw) {
        Matcher matcher = pattern.matcher(raw);
        String value = "";
        while (matcher.find()) {
            value = matcher.group(1);
        }
        return value;
    }

    private static boolean isLikelyProductUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String lower = url.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("https://www.pccomponentes.com/")) {
            return false;
        }
        if (lower.contains("/opiniones/") || lower.contains("/wiki/") || lower.contains("/buscar")) {
            return false;
        }

        String slug = lower.substring("https://www.pccomponentes.com/".length());
        if (slug.contains("/") || slug.isBlank()) {
            return false;
        }
        if (slug.startsWith("tarjetas-graficas")) {
            return false;
        }

        return slug.contains("tarjeta-grafica")
                || slug.contains("grafica")
                || slug.contains("geforce")
                || slug.contains("radeon")
                || slug.contains("arc");
    }

    private static String normalizeProductUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }

        String normalized = url.trim()
                .replace("http://www.pccomponentes.com/", "https://www.pccomponentes.com/")
                .replace("&amp;", "&");

        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        return normalized;
    }

    private static boolean looksLikeJson(String contentType, String body) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("json")) {
            return true;
        }
        if (body == null) {
            return false;
        }
        String trimmed = body.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    static boolean isCloudflareChallenge(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }

        String lower = body.toLowerCase(Locale.ROOT);
        return lower.contains("just a moment")
                || lower.contains("attention required")
                || lower.contains("challenge-platform")
                || lower.contains("cf-chl")
                || lower.contains("turnstile")
                || lower.contains("performing security verification")
                || lower.contains("security service to protect against malicious bots");
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
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("");
    }

    private static String encode(String raw) {
        if (raw == null) {
            return "";
        }
        return java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8);
    }

    private record ProductLineCandidate(String title, String productUrl, BigDecimal price) {
    }
}
