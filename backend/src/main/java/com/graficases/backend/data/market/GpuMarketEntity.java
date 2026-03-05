package com.graficases.backend.data.market;

import com.graficases.backend.service.market.GpuMarketInfo;
import com.graficases.backend.service.market.GpuMarketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Snapshot persistido de disponibilidad/precio por GPU.
 *
 * Nota: usamos el nombre canonico del dataset como PK para simplificar el join.
 */
@Entity
@Table(name = "gpu_market")
public class GpuMarketEntity {

    @Id
    @Column(name = "nombre", nullable = false, length = 140)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GpuMarketStatus status;

    @Column(name = "texto", nullable = false, length = 240)
    private String texto;

    @Column(name = "precio", precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(name = "moneda", length = 8)
    private String moneda;

    @Column(name = "fuente", length = 40)
    private String fuente;

    @Column(name = "url", length = 900)
    private String url;

    @Column(name = "checked_at")
    private Instant checkedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GpuMarketEntity() {
        // JPA
    }

    public GpuMarketEntity(String nombre, GpuMarketStatus status, String texto) {
        this.nombre = nombre;
        this.status = status;
        this.texto = texto;
        this.updatedAt = Instant.now();
    }

    public static GpuMarketEntity fromInfo(GpuMarketInfo info) {
        GpuMarketEntity entity = new GpuMarketEntity();
        entity.nombre = info.nombre();
        entity.status = info.status();
        entity.texto = info.texto();
        entity.precio = info.precio();
        entity.moneda = info.moneda();
        entity.fuente = info.fuente();
        entity.url = info.url();
        entity.checkedAt = info.checkedAt();
        entity.updatedAt = Instant.now();
        return entity;
    }

    public GpuMarketInfo toInfo() {
        return new GpuMarketInfo(
                nombre,
                status,
                texto,
                precio,
                moneda,
                fuente,
                url,
                checkedAt
        );
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public GpuMarketStatus getStatus() {
        return status;
    }

    public void setStatus(GpuMarketStatus status) {
        this.status = status;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(Instant checkedAt) {
        this.checkedAt = checkedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

