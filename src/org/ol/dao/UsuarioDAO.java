/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.ol.dao;

import java.util.List;
import org.ol.model.Usuario;
public interface UsuarioDAO extends Crud<Usuario, Integer> {
    Usuario iniciarSesion(String username, String passwordHash);
    Usuario buscarPorUsername(String username);
    Usuario buscarPorEmail(String email);
    boolean cambiarPassword(int idUsuario, String passwordHash);
    boolean desactivarUsuario(int idUsuario);
    List<Usuario> listarActivos();
}