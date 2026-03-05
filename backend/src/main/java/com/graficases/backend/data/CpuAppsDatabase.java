package com.graficases.backend.data;

import com.graficases.backend.model.CPU;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory dataset for CPU performance in applications.
 * Kept separate from CPU gaming performance on purpose.
 */
@Component
public class CpuAppsDatabase {

    private final List<CPU> cpus;

    public CpuAppsDatabase() {
        List<CPU> items = new ArrayList<>();

        // Dataset aplicaciones (referencia: Ryzen 7 9800X3D = 100).
        items.add(new CPU("Ryzen 9 9950X3D", 128.3));
        items.add(new CPU("Ryzen 9 9950X", 125.1));
        items.add(new CPU("Core Ultra 9 285K", 121.0));
        items.add(new CPU("Ryzen 9 7950X", 120.9));
        items.add(new CPU("Core i9-14900K", 119.5));
        items.add(new CPU("Core i9-13900K", 116.6));
        items.add(new CPU("Ryzen 9 7950X3D", 116.3));
        items.add(new CPU("Core Ultra 7 265K", 113.3));
        items.add(new CPU("Ryzen 9 9900X", 112.0));
        items.add(new CPU("Core i7-14700K", 110.2));
        items.add(new CPU("Ryzen 9 7900X", 107.1));
        items.add(new CPU("Core i7-13700K", 103.6));
        items.add(new CPU("Ryzen 7 9850X3D", 102.3));
        items.add(new CPU("Ryzen 7 9800X3D", 100.0));
        items.add(new CPU("Ryzen 9 7900", 98.5));
        items.add(new CPU("Core Ultra 5 245K", 95.6));
        items.add(new CPU("Core i9-12900K", 95.6));
        items.add(new CPU("Ryzen 7 9700X", 94.1));
        items.add(new CPU("Core i5-14600K", 93.3));
        items.add(new CPU("Ryzen 7 7700X", 90.7));
        items.add(new CPU("Core i5-13600K", 89.5));
        items.add(new CPU("Ryzen 9 5950X", 89.3));
        items.add(new CPU("Ryzen 7 7700", 87.4));
        items.add(new CPU("Core i7-12700K", 86.7));
        items.add(new CPU("Ryzen 7 7800X3D", 85.1));
        items.add(new CPU("Ryzen 5 9600X", 83.1));
        items.add(new CPU("Ryzen 9 5900X", 82.0));
        items.add(new CPU("Ryzen 5 7600X", 77.4));
        items.add(new CPU("Core i5-12600K", 75.1));
        items.add(new CPU("Ryzen 5 7600", 73.9));
        items.add(new CPU("Core i9-11900K", 68.9));
        items.add(new CPU("Ryzen 7 5800X", 68.3));
        items.add(new CPU("Ryzen 7 5800X3D", 68.1));
        items.add(new CPU("Ryzen 9 3900X", 67.9));
        items.add(new CPU("Core i5-13400F", 67.6));
        items.add(new CPU("Core i7-11700KF", 65.6));
        items.add(new CPU("Ryzen 7 5700X", 64.9));
        items.add(new CPU("Ryzen 7 5700G", 62.5));
        items.add(new CPU("Ryzen 5 8500G", 60.8));
        items.add(new CPU("Core i5-12400F", 58.4));
        items.add(new CPU("Ryzen 5 5600X", 58.1));
        items.add(new CPU("Core i5-11600K", 57.5));
        items.add(new CPU("Ryzen 7 3700X", 56.1));
        items.add(new CPU("Core i3-14100", 49.7));
        items.add(new CPU("Ryzen 5 3600", 47.3));
        items.add(new CPU("Core i3-12100F", 45.0));
        items.add(new CPU("Core i5-11400F", 44.6));
        items.add(new CPU("Ryzen 7 2700X", 43.6));
        items.add(new CPU("Ryzen 3 3300X", 38.3));

        this.cpus = List.copyOf(items);
    }

    public List<CPU> getAll() {
        return cpus;
    }
}

