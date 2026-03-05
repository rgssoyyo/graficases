package com.graficases.backend.model;

/**
 * Modelo de datos simple para una CPU.
 * Incluye nombre y rendimiento (valor relativo fijo).
 */
public class CPU {

    /** Nombre visible de la CPU (ej. "Ryzen 9 9950X3D"). */
    private String nombre;

    /** Valor de rendimiento base (puede incluir decimales). */
    private double rendimiento;

    /**
     * Constructor para crear una CPU con nombre y rendimiento.
     */
    public CPU(String nombre, double rendimiento) {
        this.nombre = nombre;
        this.rendimiento = rendimiento;
    }

    /**
     * @return nombre de la CPU.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return rendimiento base de la CPU.
     */
    public double getRendimiento() {
        return rendimiento;
    }
}
