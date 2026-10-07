drop database if exists grandstay_in4cm;
create database if not exists grandstay_in4cm;
use grandstay_in4cm;

-- =============================================================================
-- creacion de tablas
-- =============================================================================

-- tipo_habitacion (tipos de suite y su tarifa base)
create table tipo_habitacion(
    id_tipo_habitacion int primary key auto_increment,
    nombre_tipo varchar(60) not null,
    descripcion varchar(255),
    capacidad_personas tinyint not null,
    tarifa_noche decimal(10,2) not null,
    constraint uq_nombre_tipo unique (nombre_tipo),
    constraint ck_capacidad_personas check (capacidad_personas > 0),
    constraint ck_tarifa_noche check (tarifa_noche >= 0)
);

-- habitacion
create table habitacion(
    id_habitacion int primary key auto_increment,
    numero_habitacion varchar(10) not null,
    piso tinyint not null,
    estado_habitacion enum('disponible','ocupada','limpieza','mantenimiento') not null default 'disponible',
    id_tipo_habitacion int not null,
    constraint uq_numero_habitacion unique (numero_habitacion)
);

-- huesped
create table huesped(
    id_huesped int primary key auto_increment,
    documento_identificacion varchar(20) not null,
    nombre_huesped varchar(100) not null,
    apellido_huesped varchar(100) not null,
    telefono_huesped varchar(15),
    correo_electronico varchar(100),
    nacionalidad varchar(60),
    constraint uq_documento_identificacion unique (documento_identificacion)
);

-- reserva (incluye datos de check-in, check-out y deposito previo)
create table reserva(
    id_reserva int primary key auto_increment,
    id_huesped int not null,
    id_habitacion int not null,
    fecha_reserva timestamp default current_timestamp,
    fecha_entrada date not null,
    fecha_salida date not null,
    cantidad_huespedes tinyint not null default 1,
    tarifa_aplicada decimal(10,2) not null,
    deposito_previo decimal(10,2) not null default 0.00,
    deposito_pagado boolean not null default false,
    fecha_checkin datetime,
    fecha_checkout datetime,
    estado_reserva enum('pendiente','confirmada','check_in','check_out','cancelada') not null default 'pendiente',
    constraint ck_fechas_reserva check (fecha_salida > fecha_entrada),
    constraint ck_cantidad_huespedes check (cantidad_huespedes > 0),
    constraint ck_tarifa_aplicada check (tarifa_aplicada >= 0),
    constraint ck_deposito_previo check (deposito_previo >= 0)
);

-- consumo_servicio (consumos de minibar y otros servicios cargados a la reserva)
create table consumo_servicio(
    id_consumo int primary key auto_increment,
    id_reserva int not null,
    tipo_servicio enum('minibar','restaurante','lavanderia','spa','otro') not null default 'minibar',
    descripcion varchar(100) not null,
    cantidad int not null default 1,
    precio_unitario decimal(8,2) not null,
    fecha_consumo timestamp default current_timestamp,
    constraint ck_cantidad_consumo check (cantidad > 0),
    constraint ck_precio_unitario check (precio_unitario >= 0)
);

-- factura_hotel (se genera al hacer el check-out)
create table factura_hotel(
    id_factura int primary key auto_increment,
    id_reserva int not null,
    fecha_emision timestamp default current_timestamp,
    dias_hospedados int not null,
    subtotal_hospedaje decimal(10,2) not null,
    subtotal_consumos decimal(10,2) not null default 0.00,
    deposito_aplicado decimal(10,2) not null default 0.00,
    total_factura decimal(10,2) not null,
    estado_pago enum('pendiente','pagada','anulada') not null default 'pendiente',
    constraint uq_factura_reserva unique (id_reserva),
    constraint ck_dias_hospedados check (dias_hospedados > 0)
);

-- =============================================================================
-- agregamos las relaciones a las tablas (llaves foraneas)
-- =============================================================================

alter table habitacion
add constraint fk_habitacion_tipo foreign key (id_tipo_habitacion)
    references tipo_habitacion(id_tipo_habitacion)
    on delete restrict
    on update cascade;

alter table reserva
add constraint fk_reserva_huesped foreign key (id_huesped)
    references huesped(id_huesped)
    on delete restrict
    on update cascade,
add constraint fk_reserva_habitacion foreign key (id_habitacion)
    references habitacion(id_habitacion)
    on delete restrict
    on update cascade;

alter table consumo_servicio
add constraint fk_consumo_reserva foreign key (id_reserva)
    references reserva(id_reserva)
    on delete cascade
    on update cascade;

alter table factura_hotel
add constraint fk_factura_reserva foreign key (id_reserva)
    references reserva(id_reserva)
    on delete restrict
    on update cascade;

-- =============================================================================
-- indices para acelerar busquedas frecuentes
-- =============================================================================

create index idx_habitacion_estado on habitacion(estado_habitacion);
create index idx_habitacion_tipo on habitacion(id_tipo_habitacion);

create index idx_huesped_apellido on huesped(apellido_huesped, nombre_huesped);

create index idx_reserva_huesped on reserva(id_huesped);
create index idx_reserva_habitacion on reserva(id_habitacion);
create index idx_reserva_fechas on reserva(fecha_entrada, fecha_salida);
create index idx_reserva_estado on reserva(estado_reserva);

create index idx_consumo_reserva on consumo_servicio(id_reserva);
create index idx_consumo_fecha on consumo_servicio(fecha_consumo);

create index idx_factura_estado_pago on factura_hotel(estado_pago);
