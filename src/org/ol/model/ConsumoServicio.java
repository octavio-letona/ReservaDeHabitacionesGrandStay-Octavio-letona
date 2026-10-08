package org.ol.model;

import java.sql.Timestamp;

public class ConsumoServicio {
    private int idConsumo;
    private int idReserva;
    private String tipoServicio;
    private String descripcion;
    private int cantidad;
    private Double precioUnitario;
    private Timestamp fechaConsumo;

    public ConsumoServicio() {
    }

    public ConsumoServicio(int idConsumo, int idReserva, String tipoServicio, String descripcion, int cantidad, Double precioUnitario, Timestamp fechaConsumo) {
        this.idConsumo = idConsumo;
        this.idReserva = idReserva;
        this.tipoServicio = tipoServicio;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.fechaConsumo = fechaConsumo;
    }

    public int getIdConsumo() {
        return idConsumo;
    }

    public void setIdConsumo(int idConsumo) {
        this.idConsumo = idConsumo;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Timestamp getFechaConsumo() {
        return fechaConsumo;
    }

    public void setFechaConsumo(Timestamp fechaConsumo) {
        this.fechaConsumo = fechaConsumo;
    }

    @Override
    public String toString() {
        return "ConsumoServicio{" +
                "idConsumo=" + idConsumo +
                ", idReserva=" + idReserva +
                ", tipoServicio='" + tipoServicio + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", fechaConsumo=" + fechaConsumo +
                '}';
    }
}
