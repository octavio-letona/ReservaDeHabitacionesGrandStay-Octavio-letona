/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.ol.dao;

import java.util.List;
import org.ol.model.Huesped;

public interface HuespedDAO extends Crud<Huesped, Integer> {
    Huesped buscarPorDocumento(String documentoIdentificacion);
    List<Huesped> buscarPorApellido(String apellidoHuesped);
}