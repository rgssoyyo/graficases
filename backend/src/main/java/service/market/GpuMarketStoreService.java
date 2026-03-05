package com.graficases.backend.service.market;

import com.graficases.backend.data.market.GpuMarketEntity;
import com.graficases.backend.data.market.GpuMarketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * Acceso a snapshot persistido de precios + operaciones de "refresh" (scraping).
 */
@Service
public class GpuMarketStoreService {

    private final GpuMarketRepository repository;
    private final GpuMarketInfoService fetchService;

    public GpuMarketStoreService(GpuMarketRepository repository, GpuMarketInfoService fetchService) {
        this.repository = repository;
        this.fetchService = fetchService;
    }

    public Map<String, GpuMarketInfo> getStoredByNombre(List<String> nombres) {
        if (nombres == null || nombres.isEmpty()) {
            return Map.of();
        }

        List<GpuMarketEntity> stored = repository.findAllById(nombres);
        Map<String, GpuMarketInfo> out = new HashMap<>(stored.size());
        for (GpuMarketEntity entity : stored) {
            out.put(entity.getNombre(), entity.toInfo());
        }
        return out;
    }

    public GpuMarketInfo getStoredOrDefault(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return new GpuMarketInfo(
                    "",
                    GpuMarketStatus.UNAVAILABLE,
                    "sin datos",
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return repository.findById(nombre)
                .map(GpuMarketEntity::toInfo)
                .orElseGet(() -> defaultInfo(nombre));
    }

    public GpuMarketInfo defaultInfo(String nombre) {
        return new GpuMarketInfo(
                nombre,
                GpuMarketStatus.UNAVAILABLE,
                "sin datos",
                null,
                null,
                null,
                null,
                null
        );
    }

    public GpuMarketInfo refreshAndStore(String nombre) {
        GpuMarketInfo live = fetchService.refreshMarketInfo(nombre);
        repository.save(GpuMarketEntity.fromInfo(live));
        return live;
    }

    /**
     * Refresca y persiste todas las GPUs, con un limite de concurrencia para no saturar las tiendas.
     */
    public List<GpuMarketInfo> refreshAllAndStore(List<String> nombres, int maxConcurrent) {
        if (nombres == null || nombres.isEmpty()) {
            return List.of();
        }
        int concurrency = Math.max(1, maxConcurrent);

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        Semaphore sem = new Semaphore(concurrency);
        try {
            List<Future<GpuMarketInfo>> futures = nombres.stream()
                    .map(nombre -> executor.submit(() -> {
                        sem.acquire();
                        try {
                            return refreshAndStore(nombre);
                        } finally {
                            sem.release();
                        }
                    }))
                    .toList();

            // Mantener orden del dataset.
            List<GpuMarketInfo> results = new ArrayList<>(nombres.size());
            for (int i = 0; i < futures.size(); i++) {
                try {
                    results.add(futures.get(i).get());
                } catch (Exception ex) {
                    results.add(new GpuMarketInfo(
                            nombres.get(i),
                            GpuMarketStatus.ERROR,
                            "error consultando",
                            null,
                            null,
                            null,
                            null,
                            Instant.now()
                    ));
                }
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }
}
