package org.ol.dao;

import java.util.List;
import org.ol.model.ConsumoServicio;

public interface ConsumoServicioDAO extends Crud<ConsumoServicio, Integer> {

    /** Devuelve todos los consumos asociados a una reserva específica. */
    List<ConsumoServicio> listarPorReserva(int idReserva);
}