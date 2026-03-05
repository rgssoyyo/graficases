package com.graficases.backend.service;

import com.graficases.backend.data.CpuAppsDatabase;
import com.graficases.backend.data.CpuGamesDatabase;
import com.graficases.backend.model.CPU;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Servicio de negocio para CPUs.
 * Contiene la logica para consultar datasets y calcular rendimiento relativo.
 */
@Service
public class CpuService {

    private final CpuAppsDatabase cpuAppsDatabase;
    private final CpuGamesDatabase cpuGamesDatabase;

    public CpuService(CpuAppsDatabase cpuAppsDatabase, CpuGamesDatabase cpuGamesDatabase) {
        this.cpuAppsDatabase = cpuAppsDatabase;
        this.cpuGamesDatabase = cpuGamesDatabase;
    }

    public List<CPU> obtenerCpusAplicaciones() {
        return cpuAppsDatabase.getAll();
    }

    public List<CPU> obtenerCpusJuegos() {
        return cpuGamesDatabase.getAll();
    }

    public List<String> calcularRelativoAplicaciones(String nombreBase) {
        return calcularRelativo(cpuAppsDatabase.getAll(), nombreBase);
    }

    public List<String> calcularRelativoJuegos(String nombreBase) {
        return calcularRelativo(cpuGamesDatabase.getAll(), nombreBase);
    }

    private List<String> calcularRelativo(List<CPU> lista, String nombreBase) {

        if (lista.isEmpty()) {
            return List.of("Listado vacio");
        }

        if (nombreBase == null || nombreBase.isBlank()) {
            return List.of("CPU base no indicada");
        }

        CPU base = lista.stream()
                .filter(cpu -> cpu.getNombre().equalsIgnoreCase(nombreBase))
                .findFirst()
                .orElse(null);

        if (base == null) {
            return List.of("CPU base no encontrada");
        }

        if (base.getRendimiento() <= 0) {
            return List.of("CPU base sin rendimiento valido");
        }
        List<String> cosa =  lista.stream()
                .sorted((a, b) -> {
                    double relA = (a.getRendimiento() * 100.0) / base.getRendimiento();
                    double relB = (b.getRendimiento() * 100.0) / base.getRendimiento();
                    return Double.compare(relB, relA); // descendente
                })
                .map(cpu -> {
                    double relativo = (cpu.getRendimiento() * 100.0) / base.getRendimiento();
                    String percent = String.format(Locale.US, "%.1f%%", relativo);
                    return cpu.getNombre() + " -> " + percent;
                })
                .toList();
        return cosa;
    }
}