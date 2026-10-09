package org.ol.manager;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/** Central role policy for navigation and access to hotel modules. */
public final class RolePermissions {
    public enum Module { DASHBOARD, TIPOS_HABITACION, HABITACIONES, HUESPEDES, RESERVAS, CONSUMOS, FACTURAS, USUARIOS }

    private RolePermissions() { }

    public static Set<Module> forRole(String role) {
        String normalized = normalize(role);
        if (normalized.equals("ADMIN") || normalized.equals("ADMINISTRADOR") || normalized.equals("GERENTE")) {
            return EnumSet.allOf(Module.class);
        }
        if (normalized.equals("RECEPCIONISTA") || normalized.equals("RECEPCION")) {
            return EnumSet.of(Module.DASHBOARD, Module.TIPOS_HABITACION, Module.HABITACIONES,
                    Module.HUESPEDES, Module.RESERVAS, Module.CONSUMOS, Module.FACTURAS);
        }
        if (normalized.equals("CAJERO") || normalized.equals("CONTADOR")) {
            return EnumSet.of(Module.DASHBOARD, Module.RESERVAS, Module.CONSUMOS, Module.FACTURAS);
        }
        if (normalized.equals("AUDITOR")) {
            return EnumSet.of(Module.DASHBOARD, Module.TIPOS_HABITACION, Module.HABITACIONES,
                    Module.HUESPEDES, Module.RESERVAS, Module.CONSUMOS, Module.FACTURAS);
        }
        return EnumSet.noneOf(Module.class);
    }

    public static boolean allows(String role, Module module) {
        return forRole(role).contains(module);
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
    }
}
