package com.graficases.backend.model;

/**
 * Modelo de datos simple para una GPU.
 * Incluye nombre, rendimiento (valor relativo fijo) y MSRP (precio de lanzamiento).
 */
public class GPU {

    /** Nombre visible de la GPU (ej. "GeForce RTX 4090"). */
    private String nombre;

    /** Valor de rendimiento base (entero). */
    private int rendimiento;

    /** MSRP (precio de lanzamiento) en USD. */
    private int msrp;

    /**
     * Constructor para crear una GPU con nombre, rendimiento y MSRP.
     */
    public GPU(String nombre, int rendimiento, int msrp) {
        this.nombre = nombre;
        this.rendimiento = rendimiento;
        this.msrp = msrp;
    }

    /**
     * @return nombre de la GPU.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return rendimiento base de la GPU.
     */
    public int getRendimiento() {
        return rendimiento;
    }

    /**
     * @return MSRP (precio de lanzamiento) en USD.
     */
    public int getMsrp() {
        return msrp;
    }
}
