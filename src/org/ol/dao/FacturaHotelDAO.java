/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.ol.dao;

import org.ol.model.FacturaHotel;

public interface FacturaHotelDAO extends Crud<FacturaHotel, Integer> {
    FacturaHotel obtenerPorIdReserva(int idReserva);
}