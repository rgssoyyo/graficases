package com.graficases.backend.data;

import com.graficases.backend.model.GPU;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory dataset for GPUs.
 */
@Component
public class GpuDatabase {

    private final List<GPU> gpus;

    public GpuDatabase() {
        List<GPU> items = new ArrayList<>();

        // Dataset GPUs (referencia: GeForce RTX 5090 = 100).
        items.add(new GPU("GeForce RTX 5090", 100, 1999));
        items.add(new GPU("GeForce RTX 4090", 76, 1599));
        items.add(new GPU("GeForce RTX 5080", 66, 999));
        items.add(new GPU("GeForce RTX 4080 SUPER", 59, 999));
        items.add(new GPU("GeForce RTX 4080", 58, 1199));
        items.add(new GPU("Radeon RX 7900 XTX", 58, 999));
        items.add(new GPU("GeForce RTX 5070 Ti", 57, 749));
        items.add(new GPU("Radeon RX 9070 XT", 55, 699));
        items.add(new GPU("Radeon RX 7900 XT", 50, 899));
        items.add(new GPU("GeForce RTX 3090 Ti", 49, 1999));
        items.add(new GPU("GeForce RTX 4070 Ti SUPER", 49, 799));
        items.add(new GPU("Radeon RX 9070", 49, 549));
        items.add(new GPU("GeForce RTX 4070 Ti", 45, 799));
        items.add(new GPU("GeForce RTX 5070", 45, 549));
        items.add(new GPU("GeForce RTX 3090", 44, 1499));
        items.add(new GPU("GeForce RTX 3080 Ti", 43, 1199));
        items.add(new GPU("GeForce RTX 4070 SUPER", 42, 599));
        items.add(new GPU("Radeon RX 7900 GRE", 41, 549));
        items.add(new GPU("Radeon RX 6950 XT", 41, 1099));
        items.add(new GPU("GeForce RTX 4070", 40, 599));
        items.add(new GPU("Radeon RX 6900 XT", 40, 999));
        items.add(new GPU("GeForce RTX 3080", 39, 699));
        items.add(new GPU("Radeon RX 7800 XT", 39, 499));
        items.add(new GPU("Radeon RX 6800 XT", 38, 649));
        items.add(new GPU("GeForce RTX 5060 Ti 16 GB", 35, 499));
        items.add(new GPU("GeForce RTX 5060 Ti 8 GB", 35, 399));
        items.add(new GPU("Radeon RX 7700 XT", 34, 449));
        items.add(new GPU("GeForce RTX 3070 Ti", 33, 599));
        items.add(new GPU("Radeon RX 6800", 32, 579));
        items.add(new GPU("GeForce RTX 3070", 31, 499));
        items.add(new GPU("GeForce RTX 2080 Ti", 31, 999));
        items.add(new GPU("GeForce RTX 4060 Ti 8 GB", 31, 399));
        items.add(new GPU("GeForce RTX 5060", 30, 299));
        items.add(new GPU("Radeon RX 6750 XT", 29, 549));
        items.add(new GPU("GeForce RTX 3060 Ti", 27, 399));
        items.add(new GPU("Radeon RX 6700 XT", 27, 479));
        items.add(new GPU("GeForce RTX 2080 SUPER", 25, 699));
        items.add(new GPU("Radeon RX 7600 XT", 24, 329));
        items.add(new GPU("GeForce RTX 4060", 24, 299));
        items.add(new GPU("Arc B580", 24, 249));
        items.add(new GPU("GeForce RTX 5050", 24, 249));
        items.add(new GPU("GeForce RTX 2080", 24, 699));
        items.add(new GPU("Radeon RX 6650 XT", 23, 399));
        items.add(new GPU("Radeon RX 7600", 23, 269));
        items.add(new GPU("GeForce RTX 2070 SUPER", 22, 499));
        items.add(new GPU("GeForce GTX 1080 Ti", 22, 699));
        items.add(new GPU("Arc A770", 22, 329));
        items.add(new GPU("GeForce RTX 3060 12 GB", 21, 329));
        items.add(new GPU("Radeon RX 6600 XT", 21, 379));
        items.add(new GPU("Radeon VII", 21, 699));
        items.add(new GPU("Arc A750", 20, 289));
        items.add(new GPU("Radeon RX 5700 XT", 20, 399));
        items.add(new GPU("GeForce RTX 2070", 19, 499));
        items.add(new GPU("Radeon RX 6600", 18, 329));
        items.add(new GPU("Arc A580", 18, 179));
        items.add(new GPU("GeForce RTX 2060 SUPER", 18, 399));
        items.add(new GPU("Radeon RX Vega 64", 17, 499));
        items.add(new GPU("GeForce RTX 2060", 17, 349));
        items.add(new GPU("Radeon RX 5700", 17, 349));
        items.add(new GPU("GeForce GTX 1080", 16, 599));
        items.add(new GPU("GeForce GTX 1070 Ti", 16, 449));
        items.add(new GPU("Radeon RX 5600 XT", 16, 279));
        items.add(new GPU("Radeon RX Vega 56", 15, 399));
        items.add(new GPU("GeForce GTX 1070", 14, 379));
        items.add(new GPU("GeForce GTX 1660 SUPER", 14, 229));
        items.add(new GPU("GeForce GTX 1660 Ti", 14, 279));
        items.add(new GPU("GeForce GTX 980 Ti", 13, 649));
        items.add(new GPU("GeForce RTX 3050 8 GB", 13, 249));
        items.add(new GPU("Radeon R9 FURY X", 13, 649));
        items.add(new GPU("GeForce GTX 1660", 13, 219));
        items.add(new GPU("Radeon RX 590", 12, 279));
        items.add(new GPU("Radeon R9 FURY", 12, 549));
        items.add(new GPU("GeForce GTX 980", 11, 549));
        items.add(new GPU("GeForce GTX 1650 SUPER", 11, 159));
        items.add(new GPU("Radeon RX 6500 XT", 11, 199));
        items.add(new GPU("Radeon RX 5500 XT", 11, 199));
        items.add(new GPU("Radeon RX 580", 11, 229));
        items.add(new GPU("GeForce GTX 1060 6 GB", 11, 249));
        items.add(new GPU("GeForce GTX 970", 10, 329));
        items.add(new GPU("Radeon RX 570", 10, 169));
        items.add(new GPU("GeForce GTX 780 Ti", 10, 699));
        items.add(new GPU("GeForce GTX 1650", 8, 149));
        items.add(new GPU("Radeon RX 6400", 8, 159));
        items.add(new GPU("Arc A380", 8, 139));
        items.add(new GPU("GeForce GTX 1050 Ti", 7, 139));
        items.add(new GPU("GeForce GT 1030", 3, 79));
        items.add(new GPU("GeForce 210", 0, 49));

        this.gpus = List.copyOf(items);
    }

    public List<GPU> getAll() {
        return gpus;
    }
}
