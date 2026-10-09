package org.ol.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());

    public static void handleException(Exception e) {
        if (e instanceof DBException) {
            LOGGER.log(Level.SEVERE, "Error de base de datos: " + e.getMessage(), e);
        } else if (e instanceof AppException) {
            LOGGER.log(Level.WARNING, "Error de aplicacion: " + e.getMessage(), e);
        } else {
            LOGGER.log(Level.SEVERE, "Error inesperado: " + e.getMessage(), e);
        }
    }
}
