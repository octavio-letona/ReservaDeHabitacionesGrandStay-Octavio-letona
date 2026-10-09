package org.ol.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import org.ol.dao.impl.FacturaHotelDAOImpl;
import org.ol.exception.AppException;
import org.ol.exception.DBException;
import org.ol.model.FacturaHotel;
import org.ol.util.Conexion;

/** Executes the hotel checkout as one database transaction. */
public class CheckoutService {
    private static final String CHECKOUT = "{call sp_realizarcheckout(?)}";
    private final FacturaHotelDAOImpl facturaDAO = new FacturaHotelDAOImpl();

    public FacturaHotel realizarCheckout(int idReserva) {
        if (idReserva <= 0) throw new IllegalArgumentException("El ID de reserva debe ser positivo.");
        try (Connection connection = Conexion.getInstancia().conectar();
                CallableStatement statement = connection.prepareCall(CHECKOUT)) {
            statement.setInt(1, idReserva);
            statement.execute();
        } catch (SQLException e) {
            throw new DBException("No se pudo completar el checkout de la reserva " + idReserva, e);
        }
        FacturaHotel invoice = facturaDAO.obtenerPorIdReserva(idReserva);
        if (invoice == null) throw new AppException("El checkout terminó, pero no se pudo recuperar la factura.");
        return invoice;
    }
}
