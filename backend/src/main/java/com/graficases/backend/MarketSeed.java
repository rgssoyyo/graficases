package com.graficases.backend;

import com.graficases.backend.service.GpuService;
import com.graficases.backend.service.market.GpuMarketInfo;
import com.graficases.backend.service.market.GpuMarketStatus;
import com.graficases.backend.service.market.GpuMarketStoreService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * Ejecutable para poblar/actualizar la base de datos de precios "una vez",
 * sin levantar el servidor web.
 *
 * Uso:
 * mvn -DskipTests spring-boot:run -Dspring-boot.run.mainClass=com.graficases.backend.MarketSeed
 */
public class MarketSeed {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BackendApplication.class)
                .web(WebApplicationType.NONE)
                .run(args)) {

            GpuService gpuService = ctx.getBean(GpuService.class);
            GpuMarketStoreService storeService = ctx.getBean(GpuMarketStoreService.class);

            List<String> nombres = gpuService.obtenerNombresGpus();
            int concurrency = 6;

            System.out.printf("Refrescando %d GPUs (concurrency=%d)...%n", nombres.size(), concurrency);
            List<GpuMarketInfo> results = storeService.refreshAllAndStore(nombres, concurrency);

            long available = results.stream().filter(r -> r.status() == GpuMarketStatus.AVAILABLE).count();
            long unavailable = results.stream().filter(r -> r.status() == GpuMarketStatus.UNAVAILABLE).count();
            long error = results.stream().filter(r -> r.status() == GpuMarketStatus.ERROR).count();

            System.out.printf("Listo. AVAILABLE=%d UNAVAILABLE=%d ERROR=%d%n", available, unavailable, error);
        }
    }
}

