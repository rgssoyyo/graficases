package com.graficases.backend.dto;

import com.graficases.backend.service.market.GpuMarketInfo;

/**
 * DTO del listado base de GPUs (incluye snapshot de mercado).
 */
public record GpuItemDto(
        String nombre,
        int rendimiento,
        int msrp,
        GpuMarketInfo market
) {
}
