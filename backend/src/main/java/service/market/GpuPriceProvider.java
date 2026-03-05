package com.graficases.backend.service.market;

import java.util.Optional;

/**
 * Proveedor de precios (una tienda).
 */
public interface GpuPriceProvider {

    String storeName();

    Optional<PriceQuote> findLowestPrice(GpuMatchSpec spec) throws Exception;
}

