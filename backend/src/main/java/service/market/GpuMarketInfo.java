package com.graficases.backend.service.market;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Informacion "lista para UI" sobre disponibilidad/precio de una GPU.
 */
public record GpuMarketInfo(
        String nombre,
        GpuMarketStatus status,
        String texto,
        BigDecimal precio,
        String moneda,
        String fuente,
        String url,
        Instant checkedAt
) {
}

