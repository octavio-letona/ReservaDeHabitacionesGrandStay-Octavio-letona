# Reserva de Habitaciones GrandStay

## Descripción

Sistema de gestión hotelera desarrollado en JavaFX y MySQL para la administración de habitaciones, reservas, check-in, check-out, consumos de servicios y facturación.

## Tecnologías Utilizadas

- Java
- JavaFX
- MySQL
- JDBC
- DAO Pattern
- Stored Procedures

## Roles del Sistema

### Recepcionista
- Registrar huéspedes
- Gestionar reservas
- Realizar check-in
- Realizar check-out
- Registrar consumos

### Administrador
- Gestionar tarifas
- Gestionar promociones
- Administrar habitaciones

### Housekeeping
- Actualizar estado de habitaciones

## Entidades del Modelo

- Habitacion
- TipoHabitacion
- Huesped
- Reserva
- ConsumoServicio
- FacturaHotel
- Usuario

## Credenciales por Defecto MySQL

```properties
mysql.host=localhost
mysql.port=3306
mysql.database=grandstay
mysql.username=root
mysql.password=root
```

## Seguridad

Las contraseñas se almacenan utilizando hash SHA-1.

## Compilación

### NetBeans

1. Abrir el proyecto en NetBeans.
2. Configurar la conexión a MySQL.
3. Ejecutar los scripts de base de datos.
4. Ejecutar el proyecto.

### Maven

```bash
mvn clean install
```

## Despliegue

1. Instalar MySQL Server.
2. Crear la base de datos.
3. Ejecutar los scripts SQL del proyecto.
4. Configurar el archivo `db.properties`.
5. Compilar y ejecutar la aplicación.

## Flujo Principal

1. Registrar huésped.
2. Crear reserva.
3. Realizar check-in.
4. Registrar consumos.
5. Realizar check-out.
6. Generar factura.
7. Liberar habitación.

## Estructura del Proyecto

```text
org.ol.controller
org.ol.dao
org.ol.dao.impl
org.ol.model
org.ol.service
org.ol.exception
org.ol.manager
```
![Diagrama ER](diagrama.png)


