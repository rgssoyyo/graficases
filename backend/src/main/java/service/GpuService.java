package com.graficases.backend.service;

import com.graficases.backend.data.GpuDatabase;
import com.graficases.backend.dto.GpuItemDto;
import com.graficases.backend.model.GPU;
import com.graficases.backend.service.market.GpuMarketInfo;
import com.graficases.backend.service.market.GpuMarketStoreService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio de negocio para GPUs.
 * Contiene la logica para consultar el dataset y calcular rendimiento relativo.
 */
@Service
public class GpuService {

    private final GpuDatabase gpuDatabase;
    private final GpuMarketStoreService marketStoreService;

    public GpuService(GpuDatabase gpuDatabase, GpuMarketStoreService marketStoreService) {
        this.gpuDatabase = gpuDatabase;
        this.marketStoreService = marketStoreService;
    }

    public List<GPU> obtenerGpus() {
        return gpuDatabase.getAll();
    }

    public List<String> obtenerNombresGpus() {
        return gpuDatabase.getAll().stream().map(GPU::getNombre).toList();
    }

    public List<GpuItemDto> obtenerGpusConMarket() {
        List<GPU> gpus = gpuDatabase.getAll();
        List<String> nombres = gpus.stream().map(GPU::getNombre).toList();
        Map<String, GpuMarketInfo> marketByName = marketStoreService.getStoredByNombre(nombres);

        return gpus.stream()
                .map(gpu -> new GpuItemDto(
                        gpu.getNombre(),
                        gpu.getRendimiento(),
                        gpu.getMsrp(),
                        marketByName.getOrDefault(gpu.getNombre(), marketStoreService.defaultInfo(gpu.getNombre()))
                ))
                .toList();
    }

    public List<String> calcularRelativo(String nombreBase) {

        if (nombreBase == null || nombreBase.isBlank()) {
            return List.of("GPU base no indicada");
        }

        List<GPU> gpus = gpuDatabase.getAll();

        // Buscar la GPU base por nombre ignorando mayusculas/minusculas.
        GPU base = gpus.stream()
                .filter(g -> g.getNombre().equalsIgnoreCase(nombreBase))
                .findFirst()
                .orElse(null);

        if (base == null) {
            return List.of("GPU base no encontrada");
        }

        if (base.getRendimiento() <= 0) {
            return List.of("GPU base sin rendimiento valido");
        }

        List<String> resultado = new ArrayList<>(gpus.size());

        for (GPU gpu : gpus) {
            double relativo = (gpu.getRendimiento() * 100.0) / base.getRendimiento();
            resultado.add(gpu.getNombre() + " -> " + Math.round(relativo) + "%");
        }

        return resultado;
    }
}
