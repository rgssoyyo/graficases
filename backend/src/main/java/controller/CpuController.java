package com.graficases.backend.controller;

import com.graficases.backend.model.CPU;
import com.graficases.backend.service.CpuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para exponer endpoints de CPUs.
 * Incluye categorias separadas para aplicaciones y juegos.
 */
@RestController
@RequestMapping("/cpus")
public class CpuController {

    private final CpuService cpuService;

    public CpuController(CpuService cpuService) {
        this.cpuService = cpuService;
    }

    /**
     * GET /cpus/apps
     * Devuelve la lista completa de CPUs para aplicaciones.
     */
    @GetMapping("/apps")
    public List<CPU> obtenerCpusAplicaciones() {
        return cpuService.obtenerCpusAplicaciones();
    }

    /**
     * GET /cpus/games
     * Devuelve la lista completa de CPUs para juegos.
     */
    @GetMapping("/games")
    public List<CPU> obtenerCpusJuegos() {
        return cpuService.obtenerCpusJuegos();
    }

    /**
     * GET /cpus/apps/relative/{nombreBase}
     * Devuelve rendimiento relativo de CPUs (apps) respecto a la base indicada.
     */
    @GetMapping("/apps/relative/{nombreBase}")
    public List<String> rendimientoRelativoApps(@PathVariable String nombreBase) {
        return cpuService.calcularRelativoAplicaciones(nombreBase);
    }

    /**
     * GET /cpus/games/relative/{nombreBase}
     * Devuelve rendimiento relativo de CPUs (juegos) respecto a la base indicada.
     */
    @GetMapping("/games/relative/{nombreBase}")
    public List<String> rendimientoRelativoGames(@PathVariable String nombreBase) {
        return cpuService.calcularRelativoJuegos(nombreBase);
    }
}
