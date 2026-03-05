package com.graficases.backend.service.market;

import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Orquesta proveedores de precio y cachea resultados para no golpear las tiendas en cada render.
 */
@Service
public class GpuMarketInfoService {

    private static final Duration TTL = Duration.ofHours(6);
    private static final Duration PROVIDER_TIMEOUT = Duration.ofSeconds(12);

    private final List<GpuPriceProvider> providers;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    public GpuMarketInfoService(List<GpuPriceProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    public GpuMarketInfo getMarketInfo(String nombreGpu) {
        if (nombreGpu == null || nombreGpu.isBlank()) {
            return new GpuMarketInfo(
                    "",
                    GpuMarketStatus.ERROR,
                    "error consultando",
                    null,
                    null,
                    null,
                    null,
                    Instant.now()
            );
        }

        String key = nombreGpu.trim().toLowerCase(Locale.ROOT);
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.info();
        }

        Object lock = locks.computeIfAbsent(key, ignored -> new Object());
        synchronized (lock) {
            entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                return entry.info();
            }

            GpuMarketInfo fresh = fetchMarketInfo(nombreGpu);
            cache.put(key, new CacheEntry(fresh, Instant.now()));
            return fresh;
        }
    }

    /**
     * Fuerza una consulta "live" ignorando el cache TTL y lo actualiza.
     * Se usa para el boton de "Actualizar precio".
     */
    public GpuMarketInfo refreshMarketInfo(String nombreGpu) {
        if (nombreGpu == null || nombreGpu.isBlank()) {
            return new GpuMarketInfo(
                    "",
                    GpuMarketStatus.ERROR,
                    "error consultando",
                    null,
                    null,
                    null,
                    null,
                    Instant.now()
            );
        }

        String key = nombreGpu.trim().toLowerCase(Locale.ROOT);
        GpuMarketInfo fresh = fetchMarketInfo(nombreGpu);
        cache.put(key, new CacheEntry(fresh, Instant.now()));
        return fresh;
    }

    private GpuMarketInfo fetchMarketInfo(String nombreGpu) {
        GpuMatchSpec spec;
        try {
            spec = GpuMatchSpec.fromNombre(nombreGpu);
        } catch (RuntimeException ex) {
            return new GpuMarketInfo(
                    nombreGpu,
                    GpuMarketStatus.ERROR,
                    "error consultando",
                    null,
                    null,
                    null,
                    null,
                    Instant.now()
            );
        }

        int errors = 0;
        PriceQuote best = null;

        List<Future<ProviderResult>> futures = providers.stream()
                .map(provider -> executor.submit(() -> {
                    try {
                        Optional<PriceQuote> quote = provider.findLowestPrice(spec);
                        return new ProviderResult(provider.storeName(), quote, null);
                    } catch (Exception ex) {
                        return new ProviderResult(provider.storeName(), Optional.empty(), ex);
                    }
                }))
                .toList();

        for (Future<ProviderResult> future : futures) {
            ProviderResult result;
            try {
                result = future.get(PROVIDER_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                future.cancel(true);
                errors++;
                continue;
            }

            if (result.error() != null) {
                errors++;
                continue;
            }

            Optional<PriceQuote> quote = result.quote();
            if (quote.isEmpty()) {
                continue;
            }

            if (best == null || quote.get().price().compareTo(best.price()) < 0) {
                best = quote.get();
            }
        }

        if (best != null) {
            String formatted = formatPrice(best.price(), best.currency());
            return new GpuMarketInfo(
                    nombreGpu,
                    GpuMarketStatus.AVAILABLE,
                    "disponible a partir de " + formatted,
                    best.price(),
                    best.currency(),
                    best.store(),
                    best.productUrl(),
                    best.checkedAt()
            );
        }

        if (errors >= providers.size()) {
            return new GpuMarketInfo(
                    nombreGpu,
                    GpuMarketStatus.ERROR,
                    "error consultando",
                    null,
                    null,
                    null,
                    null,
                    Instant.now()
            );
        }

        return new GpuMarketInfo(
                nombreGpu,
                GpuMarketStatus.UNAVAILABLE,
                "descontinuada/no disponible",
                null,
                null,
                null,
                null,
                Instant.now()
        );
    }

    private static String formatPrice(BigDecimal price, String currency) {
        if (price == null) {
            return "";
        }

        // Si en el futuro se agregan mas tiendas/monedas, aqui se puede extender.
        boolean isEur = currency == null || currency.isBlank() || currency.equalsIgnoreCase("EUR");

        NumberFormat number = NumberFormat.getNumberInstance(new Locale("es", "ES"));
        number.setMinimumFractionDigits(2);
        number.setMaximumFractionDigits(2);
        String value = number.format(price);

        return isEur ? value + " €" : value + " " + currency;
    }

    private record CacheEntry(GpuMarketInfo info, Instant createdAt) {
        boolean isExpired() {
            return createdAt.plus(TTL).isBefore(Instant.now());
        }
    }

    private record ProviderResult(String store, Optional<PriceQuote> quote, Exception error) {
    }
}
