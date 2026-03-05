package com.graficases.backend.service.market;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Resultado de una busqueda de precio en una tienda concreta.
 */
public record PriceQuote(
        BigDecimal price,
        String currency,
        String store,
        String productUrl,
        Instant checkedAt
) {
}

