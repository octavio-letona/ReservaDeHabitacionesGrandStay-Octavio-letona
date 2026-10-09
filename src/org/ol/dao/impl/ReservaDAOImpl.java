package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ol.dao.ReservaDAO;
import org.ol.exception.DBException;
import org.ol.model.Reserva;
import org.ol.util.Conexion;

public class ReservaDAOImpl implements ReservaDAO {
    private static final String INSERTAR = "{call sp_insertarreserva(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizarreserva(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminarreserva(?)}";
    private static final String COLUMNAS = "id_reserva, id_huesped, id_habitacion, fecha_reserva, fecha_entrada, fecha_salida, cantidad_huespedes, tarifa_aplicada, deposito_previo, deposito_pagado, fecha_checkin, fecha_checkout, estado_reserva";

    @Override public boolean crear(Reserva r) {
        Objects.requireNonNull(r, "La reserva no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(INSERTAR)) {
            s.setInt(1, r.getIdHuesped()); s.setInt(2, r.getIdHabitacion());
            s.setDate(3, Objects.requireNonNull(r.getFechaEntrada())); s.setDate(4, Objects.requireNonNull(r.getFechaSalida()));
            s.setInt(5, r.getCantidadHuespedes()); s.setDouble(6, Objects.requireNonNull(r.getTarifaAplicada()));
            s.setDouble(7, Objects.requireNonNull(r.getDepositoPrevio())); s.setBoolean(8, r.isDepositoPagado());
            s.setString(9, r.getEstadoReserva()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo crear la reserva", e); }
    }

    @Override public boolean actualizar(Reserva r) {
        Objects.requireNonNull(r, "La reserva no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ACTUALIZAR)) {
            s.setInt(1, r.getIdReserva()); s.setInt(2, r.getIdHuesped()); s.setInt(3, r.getIdHabitacion());
            s.setDate(4, Objects.requireNonNull(r.getFechaEntrada())); s.setDate(5, Objects.requireNonNull(r.getFechaSalida()));
            s.setInt(6, r.getCantidadHuespedes()); s.setDouble(7, Objects.requireNonNull(r.getTarifaAplicada()));
            s.setDouble(8, Objects.requireNonNull(r.getDepositoPrevio())); s.setBoolean(9, r.isDepositoPagado());
            if (r.getFechaCheckin() == null) s.setNull(10, Types.TIMESTAMP); else s.setTimestamp(10, r.getFechaCheckin());
            if (r.getFechaCheckout() == null) s.setNull(11, Types.TIMESTAMP); else s.setTimestamp(11, r.getFechaCheckout());
            s.setString(12, r.getEstadoReserva()); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo actualizar la reserva", e); }
    }

    @Override public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id de reserva no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ELIMINAR)) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo eliminar la reserva", e); }
    }

    @Override public Reserva buscarPorId(Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM reserva WHERE id_reserva = ?")) {
            s.setInt(1, id); try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar la reserva", e); }
    }

    @Override public List<Reserva> buscarPorHuesped(int id) { return listar("id_huesped = ?", id); }
    @Override public List<Reserva> buscarPorHabitacion(int id) { return listar("id_habitacion = ?", id); }
    @Override public List<Reserva> buscarPorEstado(String estado) {
        Objects.requireNonNull(estado, "El estado no puede ser null"); return listar("estado_reserva = ?", estado);
    }

    @Override public List<Reserva> buscarPorRangoFechas(Date entrada, Date salida) {
        Objects.requireNonNull(entrada, "La fecha de entrada no puede ser null");
        Objects.requireNonNull(salida, "La fecha de salida no puede ser null"); List<Reserva> items = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM reserva WHERE fecha_entrada <= ? AND fecha_salida >= ? ORDER BY fecha_entrada";
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setDate(1, salida); s.setDate(2, entrada); try (ResultSet r = s.executeQuery()) { while (r.next()) items.add(mapear(r)); }
            return items;
        } catch (SQLException e) { throw new DBException("No se pudieron buscar reservas por rango de fechas", e); }
    }

    private List<Reserva> listar(String condicion, Object valor) {
        List<Reserva> items = new ArrayList<>(); String sql = "SELECT " + COLUMNAS + " FROM reserva WHERE " + condicion + " ORDER BY fecha_entrada";
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            if (valor instanceof Integer) s.setInt(1, (Integer) valor); else s.setString(1, (String) valor);
            try (ResultSet r = s.executeQuery()) { while (r.next()) items.add(mapear(r)); } return items;
        } catch (SQLException e) { throw new DBException("No se pudieron consultar las reservas", e); }
    }

    @Override public ArrayList<Reserva> listarTodos() {
        ArrayList<Reserva> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM reserva ORDER BY fecha_entrada"); ResultSet r = s.executeQuery()) {
            while (r.next()) items.add(mapear(r)); return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar las reservas", e); }
    }

    private Reserva mapear(ResultSet r) throws SQLException {
        return new Reserva(r.getInt("id_reserva"), r.getInt("id_huesped"), r.getInt("id_habitacion"),
                r.getTimestamp("fecha_reserva"), r.getDate("fecha_entrada"), r.getDate("fecha_salida"),
                r.getInt("cantidad_huespedes"), r.getDouble("tarifa_aplicada"), r.getDouble("deposito_previo"),
                r.getBoolean("deposito_pagado"), r.getTimestamp("fecha_checkin"), r.getTimestamp("fecha_checkout"),
                r.getString("estado_reserva"));
    }
}
