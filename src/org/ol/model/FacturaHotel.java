package org.ol.model;

import java.sql.Timestamp;

public class FacturaHotel {
    private int idFactura;
    private int idReserva;
    private Timestamp fechaEmision;
    private int diasHospedados;
    private Double subtotalHospedaje;
    private Double subtotalConsumos;
    private Double depositoAplicado;
    private Double totalFactura;
    private String estadoPago;

    public FacturaHotel() {
    }

    public FacturaHotel(int idFactura, int idReserva, Timestamp fechaEmision, int diasHospedados, Double subtotalHospedaje, Double subtotalConsumos, Double depositoAplicado, Double totalFactura, String estadoPago) {
        this.idFactura = idFactura;
        this.idReserva = idReserva;
        this.fechaEmision = fechaEmision;
        this.diasHospedados = diasHospedados;
        this.subtotalHospedaje = subtotalHospedaje;
        this.subtotalConsumos = subtotalConsumos;
        this.depositoAplicado = depositoAplicado;
        this.totalFactura = totalFactura;
        this.estadoPago = estadoPago;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public Timestamp getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Timestamp fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public int getDiasHospedados() {
        return diasHospedados;
    }

    public void setDiasHospedados(int diasHospedados) {
        this.diasHospedados = diasHospedados;
    }

    public Double getSubtotalHospedaje() {
        return subtotalHospedaje;
    }

    public void setSubtotalHospedaje(Double subtotalHospedaje) {
        this.subtotalHospedaje = subtotalHospedaje;
    }

    public Double getSubtotalConsumos() {
        return subtotalConsumos;
    }

    public void setSubtotalConsumos(Double subtotalConsumos) {
        this.subtotalConsumos = subtotalConsumos;
    }

    public Double getDepositoAplicado() {
        return depositoAplicado;
    }

    public void setDepositoAplicado(Double depositoAplicado) {
        this.depositoAplicado = depositoAplicado;
    }

    public Double getTotalFactura() {
        return totalFactura;
    }

    public void setTotalFactura(Double totalFactura) {
        this.totalFactura = totalFactura;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    @Override
    public String toString() {
        return "FacturaHotel{" +
                "idFactura=" + idFactura +
                ", idReserva=" + idReserva +
                ", fechaEmision=" + fechaEmision +
                ", diasHospedados=" + diasHospedados +
                ", subtotalHospedaje=" + subtotalHospedaje +
                ", subtotalConsumos=" + subtotalConsumos +
                ", depositoAplicado=" + depositoAplicado +
                ", totalFactura=" + totalFactura +
                ", estadoPago='" + estadoPago + '\'' +
                '}';
    }
}
