package org.ol.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import org.ol.dao.impl.ReservaDAOImpl;
import org.ol.exception.AppException;
import org.ol.exception.DBException;
import org.ol.model.Reserva;
import org.ol.util.Conexion;

/**
 * Ejecuta el proceso de check-in de una reserva como una transacción de BD.
 * Valida: estado 'confirmada', depósito pagado y actualiza fecha_checkin.
 */
public class CheckInService {

    private static final String CHECKIN_SQL =
            "UPDATE reserva "
            + "SET estado_reserva = 'check_in', fecha_checkin = CURRENT_TIMESTAMP() "
            + "WHERE id_reserva = ? AND estado_reserva IN ('confirmada','pendiente') "
            + "AND deposito_pagado = TRUE";

    private static final String UPDATE_ROOM_SQL =
            "UPDATE habitacion SET estado_habitacion = 'ocupada' "
            + "WHERE id_habitacion = ("
            + "  SELECT id_habitacion FROM reserva WHERE id_reserva = ?)";

    private final ReservaDAOImpl reservaDAO = new ReservaDAOImpl();

    /**
     * Realiza el check-in de una reserva.
     *
     * @param idReserva ID de la reserva a procesar
     * @return la reserva actualizada con estado check_in
     * @throws AppException si la reserva no existe, ya tiene check-in, o el depósito no fue pagado
     */
    public Reserva realizarCheckIn(int idReserva) {
        if (idReserva <= 0) throw new IllegalArgumentException("El ID de reserva debe ser positivo.");

        Reserva reserva = reservaDAO.buscarPorId(idReserva);
        if (reserva == null) throw new AppException("No existe una reserva con ID " + idReserva + ".");

        // Validaciones de negocio
        String estado = reserva.getEstadoReserva();
        if ("check_in".equalsIgnoreCase(estado)) {
            throw new AppException("La reserva ya tiene check-in registrado.");
        }
        if ("check_out".equalsIgnoreCase(estado) || "cancelada".equalsIgnoreCase(estado)) {
            throw new AppException("No se puede hacer check-in a una reserva en estado «" + estado + "».");
        }
        if (!reserva.isDepositoPagado()) {
            throw new AppException(
                    "El depósito previo no ha sido pagado. "
                    + "Registra el pago del depósito antes de realizar el check-in.");
        }

        try (Connection conn = Conexion.getInstancia().conectar()) {
            conn.setAutoCommit(false);
            try {
                // Actualizar estado de la reserva y fecha_checkin
                try (CallableStatement st = conn.prepareCall(CHECKIN_SQL)) {
                    st.setInt(1, idReserva);
                    int rows = st.executeUpdate();
                    if (rows == 0) {
                        throw new AppException(
                                "No se pudo registrar el check-in. Verifica el estado y depósito de la reserva.");
                    }
                }
                // Marcar la habitación como ocupada
                try (CallableStatement st = conn.prepareCall(UPDATE_ROOM_SQL)) {
                    st.setInt(1, idReserva);
                    st.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DBException("Error de base de datos al realizar check-in de la reserva " + idReserva, e);
        }

        Reserva updated = reservaDAO.buscarPorId(idReserva);
        if (updated == null) throw new AppException("Check-in realizado, pero no se pudo recuperar la reserva.");
        return updated;
    }
}
