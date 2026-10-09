package org.ol.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import org.ol.dao.ConsumoServicioDAO;
import org.ol.exception.DBException;
import org.ol.model.ConsumoServicio;
import org.ol.util.Conexion;

/** Implementación JDBC del acceso a datos de consumos de servicio. */
public class ConsumoServicioDAOImpl implements ConsumoServicioDAO {

    private static final String INSERTAR = "{call sp_insertarconsumoservicio(?, ?, ?, ?, ?)}";
    private static final String ACTUALIZAR = "{call sp_actualizarconsumoservicio(?, ?, ?, ?, ?, ?)}";
    private static final String ELIMINAR = "{call sp_eliminarconsumoservicio(?)}";
    private static final String BUSCAR_POR_ID =
            "SELECT id_consumo, id_reserva, tipo_servicio, descripcion, cantidad, "
            + "precio_unitario, fecha_consumo FROM consumo_servicio WHERE id_consumo = ?";
    private static final String LISTAR_TODOS =
            "SELECT id_consumo, id_reserva, tipo_servicio, descripcion, cantidad, "
            + "precio_unitario, fecha_consumo FROM consumo_servicio ORDER BY id_consumo";

    @Override
    public boolean crear(ConsumoServicio consumo) {
        Objects.requireNonNull(consumo, "El consumo no puede ser null");
        try (Connection connection = Conexion.getInstancia().conectar();
                CallableStatement statement = connection.prepareCall(INSERTAR)) {
            statement.setInt(1, consumo.getIdReserva());
            statement.setString(2, consumo.getTipoServicio());
            statement.setString(3, consumo.getDescripcion());
            statement.setInt(4, consumo.getCantidad());
            statement.setDouble(5, Objects.requireNonNull(
                    consumo.getPrecioUnitario(), "El precio unitario no puede ser null"));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException("No se pudo crear el consumo de servicio", e);
        }
    }

    @Override
    public boolean actualizar(ConsumoServicio consumo) {
        Objects.requireNonNull(consumo, "El consumo no puede ser null");
        try (Connection connection = Conexion.getInstancia().conectar();
                CallableStatement statement = connection.prepareCall(ACTUALIZAR)) {
            statement.setInt(1, consumo.getIdConsumo());
            statement.setInt(2, consumo.getIdReserva());
            statement.setString(3, consumo.getTipoServicio());
            statement.setString(4, consumo.getDescripcion());
            statement.setInt(5, consumo.getCantidad());
            statement.setDouble(6, Objects.requireNonNull(
                    consumo.getPrecioUnitario(), "El precio unitario no puede ser null"));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException("No se pudo actualizar el consumo de servicio", e);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        Objects.requireNonNull(id, "El id del consumo no puede ser null");
        try (Connection connection = Conexion.getInstancia().conectar();
                CallableStatement statement = connection.prepareCall(ELIMINAR)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException("No se pudo eliminar el consumo de servicio con id " + id, e);
        }
    }

    @Override
    public ConsumoServicio buscarPorId(Integer id) {
        Objects.requireNonNull(id, "El id del consumo no puede ser null");
        try (Connection connection = Conexion.getInstancia().conectar();
                PreparedStatement statement = connection.prepareStatement(BUSCAR_POR_ID)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapear(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new DBException("No se pudo buscar el consumo de servicio con id " + id, e);
        }
    }

    @Override
    public ArrayList<ConsumoServicio> listarTodos() {
        ArrayList<ConsumoServicio> consumos = new ArrayList<>();
        try (Connection connection = Conexion.getInstancia().conectar();
                PreparedStatement statement = connection.prepareStatement(LISTAR_TODOS);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                consumos.add(mapear(resultSet));
            }
            return consumos;
        } catch (SQLException e) {
            throw new DBException("No se pudieron listar los consumos de servicio", e);
        }
    }

    private ConsumoServicio mapear(ResultSet resultSet) throws SQLException {
        return new ConsumoServicio(
                resultSet.getInt("id_consumo"),
                resultSet.getInt("id_reserva"),
                resultSet.getString("tipo_servicio"),
                resultSet.getString("descripcion"),
                resultSet.getInt("cantidad"),
                resultSet.getDouble("precio_unitario"),
                resultSet.getTimestamp("fecha_consumo"));
    }
}
