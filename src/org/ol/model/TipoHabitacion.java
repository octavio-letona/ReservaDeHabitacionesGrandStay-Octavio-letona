package org.ol.model;

public class TipoHabitacion {
    private int idTipoHabitacion;
    private String nombreTipo;
    private String descripcion;
    private int capacidadPersonas;
    private Double tarifaNoche;

    public TipoHabitacion() {
    }

    public TipoHabitacion(int idTipoHabitacion, String nombreTipo, String descripcion, int capacidadPersonas, Double tarifaNoche) {
        this.idTipoHabitacion = idTipoHabitacion;
        this.nombreTipo = nombreTipo;
        this.descripcion = descripcion;
        this.capacidadPersonas = capacidadPersonas;
        this.tarifaNoche = tarifaNoche;
    }

    public int getIdTipoHabitacion() {
        return idTipoHabitacion;
    }

    public void setIdTipoHabitacion(int idTipoHabitacion) {
        this.idTipoHabitacion = idTipoHabitacion;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCapacidadPersonas() {
        return capacidadPersonas;
    }

    public void setCapacidadPersonas(int capacidadPersonas) {
        this.capacidadPersonas = capacidadPersonas;
    }

    public Double getTarifaNoche() {
        return tarifaNoche;
    }

    public void setTarifaNoche(Double tarifaNoche) {
        this.tarifaNoche = tarifaNoche;
    }

    @Override
    public String toString() {
        return "TipoHabitacion{" +
                "idTipoHabitacion=" + idTipoHabitacion +
                ", nombreTipo='" + nombreTipo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", capacidadPersonas=" + capacidadPersonas +
                ", tarifaNoche=" + tarifaNoche +
                '}';
    }
}
