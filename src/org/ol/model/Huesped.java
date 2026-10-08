package org.ol.model;

public class Huesped {
    private int idHuesped;
    private String documentoIdentificacion;
    private String nombreHuesped;
    private String apellidoHuesped;
    private String telefonoHuesped;
    private String correoElectronico;
    private String nacionalidad;

    public Huesped() {
    }

    public Huesped(int idHuesped, String documentoIdentificacion, String nombreHuesped, String apellidoHuesped, String telefonoHuesped, String correoElectronico, String nacionalidad) {
        this.idHuesped = idHuesped;
        this.documentoIdentificacion = documentoIdentificacion;
        this.nombreHuesped = nombreHuesped;
        this.apellidoHuesped = apellidoHuesped;
        this.telefonoHuesped = telefonoHuesped;
        this.correoElectronico = correoElectronico;
        this.nacionalidad = nacionalidad;
    }

    public int getIdHuesped() {
        return idHuesped;
    }

    public void setIdHuesped(int idHuesped) {
        this.idHuesped = idHuesped;
    }

    public String getDocumentoIdentificacion() {
        return documentoIdentificacion;
    }

    public void setDocumentoIdentificacion(String documentoIdentificacion) {
        this.documentoIdentificacion = documentoIdentificacion;
    }

    public String getNombreHuesped() {
        return nombreHuesped;
    }

    public void setNombreHuesped(String nombreHuesped) {
        this.nombreHuesped = nombreHuesped;
    }

    public String getApellidoHuesped() {
        return apellidoHuesped;
    }

    public void setApellidoHuesped(String apellidoHuesped) {
        this.apellidoHuesped = apellidoHuesped;
    }

    public String getTelefonoHuesped() {
        return telefonoHuesped;
    }

    public void setTelefonoHuesped(String telefonoHuesped) {
        this.telefonoHuesped = telefonoHuesped;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    @Override
    public String toString() {
        return "Huesped{" +
                "idHuesped=" + idHuesped +
                ", documentoIdentificacion='" + documentoIdentificacion + '\'' +
                ", nombreHuesped='" + nombreHuesped + '\'' +
                ", apellidoHuesped='" + apellidoHuesped + '\'' +
                ", telefonoHuesped='" + telefonoHuesped + '\'' +
                ", correoElectronico='" + correoElectronico + '\'' +
                ", nacionalidad='" + nacionalidad + '\'' +
                '}';
    }
}
