package com.graficases.backend.service.market;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GpuMarketProviderParsingTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void pcBoxSkipsLaptopsAndKeepsDedicatedGpuResults() throws Exception {
        PcBoxPriceProvider provider = new PcBoxPriceProvider(objectMapper);
        GpuMatchSpec spec = GpuMatchSpec.fromNombre("GeForce RTX 5080");

        JsonNode products = objectMapper.readTree("""
                [
                  {
                    "productName": "PORTATIL ASUS ROG RTX 5080 16GB",
                    "link": "/portatil-asus-rtx-5080/p",
                    "categories": ["/Gaming/Portatiles Gaming/"],
                    "items": [
                      {
                        "sellers": [
                          {
                            "commertialOffer": {
                              "AvailableQuantity": 3,
                              "Price": 2999.99
                            }
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "productName": "Tarjeta Grafica INNO3D GeForce RTX 5080 X3 16GB GDDR7",
                    "link": "/tarjeta-grafica-inno3d-rtx-5080/p",
                    "categories": ["/Componentes de ordenador/Tarjetas Graficas/"],
                    "items": [
                      {
                        "sellers": [
                          {
                            "commertialOffer": {
                              "AvailableQuantity": 5,
                              "Price": 1359.87
                            }
                          }
                        ]
                      }
                    ]
                  }
                ]
                """);

        PriceQuote quote = provider.findFirstMatchingQuote(products, spec);

        assertNotNull(quote);
        assertEquals(0, quote.price().compareTo(new BigDecimal("1359.87")));
        assertEquals("https://www.pcbox.com/tarjeta-grafica-inno3d-rtx-5080/p", quote.productUrl());
    }

    @Test
    void coolmodExtractsPriceFromSearchCards() {
        CoolmodPriceProvider provider = new CoolmodPriceProvider();
        GpuMatchSpec spec = GpuMatchSpec.fromNombre("GeForce RTX 5080");

        Document doc = Jsoup.parse("""
                <section>
                  <article class="product-card card">
                    <figure>
                      <a href="/portatil-gaming-rtx-5080/" data-itemprice="2499.95">
                        <img alt="Portatil Gaming RTX 5080">
                      </a>
                    </figure>
                    <div class="card-body">
                      <p class="card-title"><a href="/portatil-gaming-rtx-5080/">Portatil Gaming RTX 5080 16GB</a></p>
                      <p class="card-text text-xs">Portatiles Gaming</p>
                    </div>
                    <div class="card-actions">
                      <button class="btn add-to-cart" data-id="1">Anadir</button>
                    </div>
                  </article>
                  <article class="product-card card">
                    <figure>
                      <a href="/zotac-rtx-5080-solid-oc/" data-itemprice="1400.30" data-itemcategory2="Tarjetas Graficas">
                        <img alt="Tarjeta Grafica Zotac RTX 5080">
                      </a>
                    </figure>
                    <div class="card-body">
                      <p class="card-title">
                        <a href="/zotac-rtx-5080-solid-oc/">Tarjeta Grafica Zotac Gaming GeForce RTX 5080 SOLID OC 16GB GDDR7</a>
                      </p>
                      <p class="card-text text-xs">Tarjetas Graficas</p>
                    </div>
                    <div class="card-actions">
                      <button class="btn add-to-cart" data-id="2">Anadir</button>
                    </div>
                  </article>
                </section>
                """, "https://www.coolmod.com/");

        Optional<PriceQuote> quote = provider.findLowestPriceOnPage(doc, spec);

        assertTrue(quote.isPresent());
        assertEquals(0, quote.get().price().compareTo(new BigDecimal("1400.30")));
        assertEquals("https://www.coolmod.com/zotac-rtx-5080-solid-oc/", quote.get().productUrl());
    }

    @Test
    void pcComponentesDetectsCloudflareChallengeHtml() {
        assertTrue(PcComponentesPriceProvider.isCloudflareChallenge("""
                <html>
                  <head><title>Just a moment...</title></head>
                  <body>
                    <div id="challenge-platform"></div>
                    <input name="cf-turnstile-response">
                  </body>
                </html>
                """));

        assertTrue(PcComponentesPriceProvider.isCloudflareChallenge("""
                Title: Just a moment...

                Performing security verification

                This website uses a security service to protect against malicious bots.
                """));

        assertFalse(PcComponentesPriceProvider.isCloudflareChallenge("{\"articles\":[]}"));
    }

    @Test
    void pcComponentesExtractsProductUrlsFromBraveHtml() {
        PcComponentesPriceProvider provider = new PcComponentesPriceProvider(objectMapper);

        List<String> urls = provider.extractCandidateProductUrls("""
                <html>
                  <body>
                    <script>
                      "https://www.pccomponentes.com/tarjeta-grafica-msi-geforce-rtx-5080-gaming-trio-oc-16gb-gddr7-reflex-2-rtx-ai-dlss4"
                      "https://www.pccomponentes.com/tarjetas-graficas-amd"
                      "https://www.pccomponentes.com/opiniones/tarjeta-grafica-msi-geforce-rtx-5080-gaming-trio-oc-16gb-gddr7-reflex-2-rtx-ai-dlss4"
                      "https://www.pccomponentes.com/tarjeta-grafica-asus-prime-geforce-rtx-5080-16gb-gddr7-reflex-2-rtx-ai-dlss4"
                    </script>
                  </body>
                </html>
                """);

        assertIterableEquals(List.of(
                "https://www.pccomponentes.com/tarjeta-grafica-msi-geforce-rtx-5080-gaming-trio-oc-16gb-gddr7-reflex-2-rtx-ai-dlss4",
                "https://www.pccomponentes.com/tarjeta-grafica-asus-prime-geforce-rtx-5080-16gb-gddr7-reflex-2-rtx-ai-dlss4"
        ), urls);
    }

    @Test
    void pcComponentesExtractsLowestQuoteFromJinaMarkdown() {
        PcComponentesPriceProvider provider = new PcComponentesPriceProvider(objectMapper);
        GpuMatchSpec spec = GpuMatchSpec.fromNombre("Radeon RX 9070 XT 16GB");

        Optional<PriceQuote> quote = provider.findLowestPriceInMarkdown("""
                Title: ASUS TUF Gaming AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4 | PcComponentes.com

                ASUS TUF Gaming AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4 Recíbelo mañana
                849,00€PVPR 934,90€
                Vendido y enviado por PcComponentes

                *   TUF Gaming OC 849€[](http://www.pccomponentes.com/tarjeta-grafica-asus-tuf-gaming-amd-radeon-rx-9070-xt-oc-16gb-gddr6-fsr-4 "ASUS TUF Gaming AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4")
                *   PRIME OC 749€[](http://www.pccomponentes.com/tarjeta-grafica-asus-prime-amd-radeon-rx-9070-xt-oc-16gb-gddr6-fsr-4 "ASUS PRIME AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4")
                [![Image: Sapphire NITRO+ AMD Radeon RX 9070 XT Gaming OC 16GB GDDR6 FSR 4](https://thumb.pccomponentes.com/foo.jpg) ### Sapphire NITRO+ AMD Radeon RX 9070 XT Gaming OC 16GB GDDR6 FSR 4 769€ 4,6/5 447 opiniones Vendido y enviado por PcComponentes Envío gratis. Entrega mañana](https://www.pccomponentes.com/tarjeta-grafica-sapphire-nitro-amd-radeon-rx-9070-xt-gaming-oc-16gb-gddr6-fsr-4 "Sapphire NITRO+ AMD Radeon RX 9070 XT Gaming OC 16GB GDDR6 FSR 4")
                [![Image: ASUS PRIME AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4](https://thumb.pccomponentes.com/foo.jpg) ### ASUS PRIME AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4 699€ 4,6/5 447 opiniones Vendido y enviado por TiendaExterna Entrega mañana](https://www.pccomponentes.com/tarjeta-grafica-asus-prime-amd-radeon-rx-9070-xt-oc-16gb-gddr6-fsr-4 "ASUS PRIME AMD Radeon RX 9070 XT OC 16GB GDDR6 FSR 4")
                """, spec, "https://www.pccomponentes.com/tarjeta-grafica-asus-tuf-gaming-amd-radeon-rx-9070-xt-oc-16gb-gddr6-fsr-4");

        assertTrue(quote.isPresent());
        assertEquals(0, quote.get().price().compareTo(new BigDecimal("749")));
        assertEquals(
                "https://www.pccomponentes.com/tarjeta-grafica-asus-prime-amd-radeon-rx-9070-xt-oc-16gb-gddr6-fsr-4",
                quote.get().productUrl()
        );
    }
}
