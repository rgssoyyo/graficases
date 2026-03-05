package com.graficases.backend.controller;

import com.graficases.backend.dto.GpuItemDto;
import com.graficases.backend.service.GpuService;
import com.graficases.backend.service.market.GpuMarketInfo;
import com.graficases.backend.service.market.GpuMarketStoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para exponer endpoints de GPUs.
 * Este es el punto de entrada HTTP de la API.
 */
@RestController
@RequestMapping("/gpus")
public class GpuController {

    /**
     * Servicio que contiene los datos y la logica de negocio.
     * Se inyecta por constructor (inyeccion de dependencias).
     */
    private final GpuService gpuService;
    private final GpuMarketStoreService marketStoreService;

    /**
     * Constructor usado por Spring para inyectar el servicio.
     */
    public GpuController(GpuService gpuService, GpuMarketStoreService marketStoreService) {
        this.gpuService = gpuService;
        this.marketStoreService = marketStoreService;
    }

    /**
     * GET /gpus
     * Devuelve la lista completa de GPUs en crudo.
     */
    @GetMapping
    public List<GpuItemDto> obtenerGpus() {
        return gpuService.obtenerGpusConMarket();
    }

    /**
     * GET /gpus/relative/{nombreBase}
     * Devuelve la lista de GPUs con rendimiento relativo a la base indicada.
     */
    @GetMapping("/relative/{nombreBase}")
    public List<String> rendimientoRelativo(@PathVariable String nombreBase) {
        return gpuService.calcularRelativo(nombreBase);
    }

    /**
     * GET /gpus/market/{nombre}
     * Devuelve texto de disponibilidad/precio para mostrar en el listado.
     */
    @GetMapping("/market/{nombre}")
    public GpuMarketInfo marketInfo(@PathVariable String nombre) {
        return marketStoreService.getStoredOrDefault(nombre);
    }

    /**
     * POST /gpus/market/{nombre}/refresh
     * Fuerza scraping de esa GPU y lo guarda en la DB.
     */
    @PostMapping("/market/{nombre}/refresh")
    public GpuMarketInfo refreshMarketInfo(@PathVariable String nombre) {
        return marketStoreService.refreshAndStore(nombre);
    }

    /**
     * POST /gpus/market/refresh-all?concurrency=6
     * Refresca todas las GPUs y las guarda en la DB.
     * Esto puede tardar unos minutos; usarlo como operacion manual/administrativa.
     */
    @PostMapping("/market/refresh-all")
    public List<GpuMarketInfo> refreshAll(@RequestParam(defaultValue = "6") int concurrency) {
        return marketStoreService.refreshAllAndStore(gpuService.obtenerNombresGpus(), concurrency);
    }
}
