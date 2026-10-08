/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.ol.dao;

import java.util.ArrayList;

public interface Crud<T, K> {
    boolean crear(T entidad);
    boolean actualizar(T entidad);
    boolean eliminar(K id);
    T buscarPorId(K id);
    ArrayList<T> listarTodos();
}