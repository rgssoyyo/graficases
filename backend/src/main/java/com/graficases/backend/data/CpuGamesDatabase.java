package com.graficases.backend.data;

import com.graficases.backend.model.CPU;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory dataset for CPU performance in games.
 * Kept separate from CPU application performance on purpose.
 */
@Component
public class CpuGamesDatabase {

    private final List<CPU> cpus;

    public CpuGamesDatabase() {
        List<CPU> items = new ArrayList<>();

        // Dataset juegos (referencia: Ryzen 7 9850X3D = 100).
        items.add(new CPU("Ryzen 7 9850X3D", 100.0));
        items.add(new CPU("Ryzen 7 9800X3D", 98.6));
        items.add(new CPU("Ryzen 9 9950X3D", 92.9));
        items.add(new CPU("Ryzen 7 7800X3D", 88.7));
        items.add(new CPU("Ryzen 9 9950X", 83.7));
        items.add(new CPU("Ryzen 7 9700X", 82.3));
        items.add(new CPU("Core i9-14900K", 81.8));
        items.add(new CPU("Core Ultra 9 285K", 81.7));
        items.add(new CPU("Core i7-14700K", 80.5));
        items.add(new CPU("Ryzen 5 9600X", 79.7));
        items.add(new CPU("Core Ultra 7 265K", 79.0));
        items.add(new CPU("Ryzen 9 7950X", 77.8));
        items.add(new CPU("Ryzen 9 7950X3D", 86.1));
        items.add(new CPU("Core i9-13900K", 85.8));
        items.add(new CPU("Core i7-13700K", 83.3));
        items.add(new CPU("Ryzen 9 9900X", 81.4));
        items.add(new CPU("Core i5-14600K", 79.9));
        items.add(new CPU("Ryzen 7 7700X", 79.7));
        items.add(new CPU("Ryzen 9 7900X", 79.3));
        items.add(new CPU("Ryzen 9 7900", 79.3));
        items.add(new CPU("Ryzen 7 7700", 78.6));
        items.add(new CPU("Ryzen 5 7600X", 77.0));
        items.add(new CPU("Core i5-13600K", 76.9));
        items.add(new CPU("Core i9-12900K", 76.6));
        items.add(new CPU("Ryzen 7 5800X3D", 76.5));
        items.add(new CPU("Core Ultra 5 245K", 76.3));
        items.add(new CPU("Ryzen 5 7600", 75.3));
        items.add(new CPU("Core i7-12700K", 73.9));
        items.add(new CPU("Core i5-12600K", 68.2));
        items.add(new CPU("Ryzen 9 5950X", 68.1));
        items.add(new CPU("Ryzen 9 5900X", 67.4));
        items.add(new CPU("Core i9-11900K", 64.6));
        items.add(new CPU("Core i5-13400F", 64.2));
        items.add(new CPU("Ryzen 7 5800X", 63.4));
        items.add(new CPU("Core i7-11700KF", 63.6));
        items.add(new CPU("Core i5-12400F", 63.3));
        items.add(new CPU("Ryzen 7 5700X", 62.4));
        items.add(new CPU("Ryzen 5 5600X", 62.5));
        items.add(new CPU("Core i5-11600K", 60.5));
        items.add(new CPU("Ryzen 5 8500G", 59.8));
        items.add(new CPU("Core i3-14100", 59.6));
        items.add(new CPU("Ryzen 7 5700G", 55.9));
        items.add(new CPU("Core i3-12100F", 55.8));
        items.add(new CPU("Ryzen 9 3900X", 54.0));
        items.add(new CPU("Ryzen 7 3700X", 53.0));
        items.add(new CPU("Ryzen 5 3600", 49.7));
        items.add(new CPU("Core i5-11400F", 50.1));
        items.add(new CPU("Ryzen 3 3300X", 47.7));
        items.add(new CPU("Ryzen 7 2700X", 40.0));

        this.cpus = List.copyOf(items);
    }

    public List<CPU> getAll() {
        return cpus;
    }
}

