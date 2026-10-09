package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import org.ol.dao.FacturaHotelDAO;
import org.ol.exception.DBException;
import org.ol.model.FacturaHotel;
import org.ol.util.Conexion;

public class FacturaHotelDAOImpl implements FacturaHotelDAO {
    private static final String INSERTAR = "{call sp_insertarfacturahotel(?, ?, ?, ?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizarfacturahotel(?, ?, ?, ?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminarfacturahotel(?)}";
    private static final String COLUMNAS = "id_factura, id_reserva, fecha_emision, dias_hospedados, subtotal_hospedaje, subtotal_consumos, deposito_aplicado, total_factura, estado_pago";

    @Override public boolean crear(FacturaHotel f) {
        Objects.requireNonNull(f, "La factura no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(INSERTAR)) {
            s.setInt(1, f.getIdReserva()); s.setInt(2, f.getDiasHospedados());
            s.setDouble(3, Objects.requireNonNull(f.getSubtotalHospedaje()));
            s.setDouble(4, Objects.requireNonNull(f.getSubtotalConsumos()));
            s.setDouble(5, Objects.requireNonNull(f.getDepositoAplicado()));
            s.setDouble(6, Objects.requireNonNull(f.getTotalFactura())); s.setString(7, f.getEstadoPago());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo crear la factura", e); }
    }

    @Override public boolean actualizar(FacturaHotel f) {
        Objects.requireNonNull(f, "La factura no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ACTUALIZAR)) {
            s.setInt(1, f.getIdFactura()); s.setInt(2, f.getIdReserva()); s.setInt(3, f.getDiasHospedados());
            s.setDouble(4, Objects.requireNonNull(f.getSubtotalHospedaje()));
            s.setDouble(5, Objects.requireNonNull(f.getSubtotalConsumos()));
            s.setDouble(6, Objects.requireNonNull(f.getDepositoAplicado()));
            s.setDouble(7, Objects.requireNonNull(f.getTotalFactura())); s.setString(8, f.getEstadoPago());
            return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo actualizar la factura", e); }
    }

    @Override public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id de factura no puede ser null");
        try (Connection c = Conexion.getInstancia().conectar(); CallableStatement s = c.prepareCall(ELIMINAR)) {
            s.setInt(1, id); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DBException("No se pudo eliminar la factura", e); }
    }

    @Override public FacturaHotel buscarPorId(Integer id) {
        Objects.requireNonNull(id, "El id de factura no puede ser null");
        return buscar("SELECT " + COLUMNAS + " FROM factura_hotel WHERE id_factura = ?", id);
    }

    @Override public FacturaHotel obtenerPorIdReserva(int idReserva) {
        return buscar("SELECT " + COLUMNAS + " FROM factura_hotel WHERE id_reserva = ?", idReserva);
    }

    private FacturaHotel buscar(String sql, int id) {
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id); try (ResultSet r = s.executeQuery()) { return r.next() ? mapear(r) : null; }
        } catch (SQLException e) { throw new DBException("No se pudo buscar la factura", e); }
    }

    @Override public ArrayList<FacturaHotel> listarTodos() {
        ArrayList<FacturaHotel> items = new ArrayList<>();
        try (Connection c = Conexion.getInstancia().conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT " + COLUMNAS + " FROM factura_hotel ORDER BY id_factura"); ResultSet r = s.executeQuery()) {
            while (r.next()) items.add(mapear(r)); return items;
        } catch (SQLException e) { throw new DBException("No se pudieron listar las facturas", e); }
    }

    private FacturaHotel mapear(ResultSet r) throws SQLException {
        return new FacturaHotel(r.getInt("id_factura"), r.getInt("id_reserva"), r.getTimestamp("fecha_emision"),
                r.getInt("dias_hospedados"), r.getDouble("subtotal_hospedaje"), r.getDouble("subtotal_consumos"),
                r.getDouble("deposito_aplicado"), r.getDouble("total_factura"), r.getString("estado_pago"));
    }
}
