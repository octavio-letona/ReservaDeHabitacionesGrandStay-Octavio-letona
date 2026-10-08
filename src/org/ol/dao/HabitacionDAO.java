/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.ol.dao;

/**
 *
 * @author octavio 
 */
import java.util.List;
import org.ol.model.Habitacion;

public interface HabitacionDAO extends Crud<Habitacion, Integer> {
    
    Habitacion buscarPorNumero(String numeroHabitacion);
    

    List<Habitacion> listarPorEstado(String estadoHabitacion);
    List<Habitacion> listarPorTipo(int idTipoHabitacion);
}