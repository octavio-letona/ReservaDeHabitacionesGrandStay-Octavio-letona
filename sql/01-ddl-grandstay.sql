drop database if exists grandstay_in4cm;
create database if not exists grandstay_in4cm;
use grandstay_in4cm;

create table usuario(
    id_usuario int primary key auto_increment,
    username varchar(50) not null,
    email varchar(100) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    password_hash varchar(255) not null,
    rol varchar(30) not null,
    activo boolean not null default true,
    fecha_creacion timestamp not null default current_timestamp,
    constraint uq_usuario_username unique (username),
    constraint uq_usuario_email unique (email)
);


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


delimiter $$

create procedure sp_insertartipohabitacion(
    in _nombre_tipo varchar(60),
    in _descripcion varchar(255),
    in _capacidad_personas tinyint,
    in _tarifa_noche decimal(10,2)
)
begin
    insert into tipo_habitacion(nombre_tipo, descripcion, capacidad_personas, tarifa_noche)
    values (_nombre_tipo, _descripcion, _capacidad_personas, _tarifa_noche);
end $$

create procedure sp_actualizartipohabitacion(
    in _id_tipo_habitacion int,
    in _nombre_tipo varchar(60),
    in _descripcion varchar(255),
    in _capacidad_personas tinyint,
    in _tarifa_noche decimal(10,2)
)
begin
    update tipo_habitacion
    set nombre_tipo = _nombre_tipo,
        descripcion = _descripcion,
        capacidad_personas = _capacidad_personas,
        tarifa_noche = _tarifa_noche
    where id_tipo_habitacion = _id_tipo_habitacion;
end $$

create procedure sp_eliminartipohabitacion(
    in _id_tipo_habitacion int
)
begin
    delete from tipo_habitacion where id_tipo_habitacion = _id_tipo_habitacion;
end $$

delimiter ;

delimiter $$

create procedure sp_insertarhabitacion(
    in _numero_habitacion varchar(10),
    in _piso tinyint,
    in _estado_habitacion varchar(20),
    in _id_tipo_habitacion int
)
begin
    insert into habitacion(numero_habitacion, piso, estado_habitacion, id_tipo_habitacion)
    values (_numero_habitacion, _piso, _estado_habitacion, _id_tipo_habitacion);
end $$

create procedure sp_actualizarhabitacion(
    in _id_habitacion int,
    in _numero_habitacion varchar(10),
    in _piso tinyint,
    in _estado_habitacion varchar(20),
    in _id_tipo_habitacion int
)
begin
    update habitacion
    set numero_habitacion = _numero_habitacion,
        piso = _piso,
        estado_habitacion = _estado_habitacion,
        id_tipo_habitacion = _id_tipo_habitacion
    where id_habitacion = _id_habitacion;
end $$

create procedure sp_eliminarhabitacion(
    in _id_habitacion int
)
begin
    delete from habitacion where id_habitacion = _id_habitacion;
end $$

delimiter ;

delimiter $$

create procedure sp_insertarhuesped(
    in _documento_identificacion varchar(20),
    in _nombre_huesped varchar(100),
    in _apellido_huesped varchar(100),
    in _telefono_huesped varchar(15),
    in _correo_electronico varchar(100),
    in _nacionalidad varchar(60)
)
begin
    insert into huesped(documento_identificacion, nombre_huesped, apellido_huesped, telefono_huesped, correo_electronico, nacionalidad)
    values (_documento_identificacion, _nombre_huesped, _apellido_huesped, _telefono_huesped, _correo_electronico, _nacionalidad);
end $$

create procedure sp_actualizarhuesped(
    in _id_huesped int,
    in _documento_identificacion varchar(20),
    in _nombre_huesped varchar(100),
    in _apellido_huesped varchar(100),
    in _telefono_huesped varchar(15),
    in _correo_electronico varchar(100),
    in _nacionalidad varchar(60)
)
begin
    update huesped
    set documento_identificacion = _documento_identificacion,
        nombre_huesped = _nombre_huesped,
        apellido_huesped = _apellido_huesped,
        telefono_huesped = _telefono_huesped,
        correo_electronico = _correo_electronico,
        nacionalidad = _nacionalidad
    where id_huesped = _id_huesped;
end $$

create procedure sp_eliminarhuesped(
    in _id_huesped int
)
begin
    delete from huesped where id_huesped = _id_huesped;
end $$

delimiter ;

delimiter $$

create procedure sp_insertarreserva(
    in _id_huesped int,
    in _id_habitacion int,
    in _fecha_entrada date,
    in _fecha_salida date,
    in _cantidad_huespedes tinyint,
    in _tarifa_aplicada decimal(10,2),
    in _deposito_previo decimal(10,2),
    in _deposito_pagado boolean,
    in _estado_reserva varchar(20)
)
begin
    -- se omiten fecha_reserva, fecha_checkin y fecha_checkout
    insert into reserva(id_huesped, id_habitacion, fecha_entrada, fecha_salida, cantidad_huespedes, tarifa_aplicada, deposito_previo, deposito_pagado, estado_reserva)
    values (_id_huesped, _id_habitacion, _fecha_entrada, _fecha_salida, _cantidad_huespedes, _tarifa_aplicada, _deposito_previo, _deposito_pagado, _estado_reserva);
end $$

create procedure sp_actualizarreserva(
    in _id_reserva int,
    in _id_huesped int,
    in _id_habitacion int,
    in _fecha_entrada date,
    in _fecha_salida date,
    in _cantidad_huespedes tinyint,
    in _tarifa_aplicada decimal(10,2),
    in _deposito_previo decimal(10,2),
    in _deposito_pagado boolean,
    in _fecha_checkin datetime,
    in _fecha_checkout datetime,
    in _estado_reserva varchar(20)
)
begin
    update reserva
    set id_huesped = _id_huesped,
        id_habitacion = _id_habitacion,
        fecha_entrada = _fecha_entrada,
        fecha_salida = _fecha_salida,
        cantidad_huespedes = _cantidad_huespedes,
        tarifa_aplicada = _tarifa_aplicada,
        deposito_previo = _deposito_previo,
        deposito_pagado = _deposito_pagado,
        fecha_checkin = _fecha_checkin,
        fecha_checkout = _fecha_checkout,
        estado_reserva = _estado_reserva
    where id_reserva = _id_reserva;
end $$

create procedure sp_eliminarreserva(
    in _id_reserva int
)
begin
    delete from reserva where id_reserva = _id_reserva;
end $$

delimiter ;

-- =============================================================================
-- 5. crud: consumo_servicio
-- =============================================================================
delimiter $$

create procedure sp_insertarconsumoservicio(
    in _id_reserva int,
    in _tipo_servicio varchar(20),
    in _descripcion varchar(100),
    in _cantidad int,
    in _precio_unitario decimal(8,2)
)
begin
    -- se omite fecha_consumo para que tome el current_timestamp por defecto
    insert into consumo_servicio(id_reserva, tipo_servicio, descripcion, cantidad, precio_unitario)
    values (_id_reserva, _tipo_servicio, _descripcion, _cantidad, _precio_unitario);
end $$

create procedure sp_actualizarconsumoservicio(
    in _id_consumo int,
    in _id_reserva int,
    in _tipo_servicio varchar(20),
    in _descripcion varchar(100),
    in _cantidad int,
    in _precio_unitario decimal(8,2)
)
begin
    update consumo_servicio
    set id_reserva = _id_reserva,
        tipo_servicio = _tipo_servicio,
        descripcion = _descripcion,
        cantidad = _cantidad,
        precio_unitario = _precio_unitario
    where id_consumo = _id_consumo;
end $$

create procedure sp_eliminarconsumoservicio(
    in _id_consumo int
)
begin
    delete from consumo_servicio where id_consumo = _id_consumo;
end $$

delimiter ;

-- =============================================================================
-- 6. crud: factura_hotel
-- =============================================================================
delimiter $$

create procedure sp_insertarfacturahotel(
    in _id_reserva int,
    in _dias_hospedados int,
    in _subtotal_hospedaje decimal(10,2),
    in _subtotal_consumos decimal(10,2),
    in _deposito_aplicado decimal(10,2),
    in _total_factura decimal(10,2),
    in _estado_pago varchar(20)
)
begin
    -- se omite fecha_emision para que tome el current_timestamp por defecto
    insert into factura_hotel(id_reserva, dias_hospedados, subtotal_hospedaje, subtotal_consumos, deposito_aplicado, total_factura, estado_pago)
    values (_id_reserva, _dias_hospedados, _subtotal_hospedaje, _subtotal_consumos, _deposito_aplicado, _total_factura, _estado_pago);
end $$

create procedure sp_actualizarfacturahotel(
    in _id_factura int,
    in _id_reserva int,
    in _dias_hospedados int,
    in _subtotal_hospedaje decimal(10,2),
    in _subtotal_consumos decimal(10,2),
    in _deposito_aplicado decimal(10,2),
    in _total_factura decimal(10,2),
    in _estado_pago varchar(20)
)
begin
    update factura_hotel
    set id_reserva = _id_reserva,
        dias_hospedados = _dias_hospedados,
        subtotal_hospedaje = _subtotal_hospedaje,
        subtotal_consumos = _subtotal_consumos,
        deposito_aplicado = _deposito_aplicado,
        total_factura = _total_factura,
        estado_pago = _estado_pago
    where id_factura = _id_factura;
end $$

create procedure sp_eliminarfacturahotel(
    in _id_factura int
)
begin
    delete from factura_hotel where id_factura = _id_factura;
end $$

delimiter ;

-- =============================================================================
-- crud: usuario
-- =============================================================================
delimiter $$

create procedure sp_insertarusuario(
    in _username varchar(50),
    in _email varchar(100),
    in _first_name varchar(100),
    in _last_name varchar(100),
    in _password_hash varchar(255),
    in _rol varchar(30),
    in _activo boolean
)
begin
    insert into usuario(username, email, first_name, last_name, password_hash, rol, activo)
    values (_username, _email, _first_name, _last_name, _password_hash, _rol, _activo);
end $$

create procedure sp_actualizarusuario(
    in _id_usuario int,
    in _username varchar(50),
    in _email varchar(100),
    in _first_name varchar(100),
    in _last_name varchar(100),
    in _password_hash varchar(255),
    in _rol varchar(30),
    in _activo boolean
)
begin
    update usuario
    set username = _username,
        email = _email,
        first_name = _first_name,
        last_name = _last_name,
        password_hash = _password_hash,
        rol = _rol,
        activo = _activo
    where id_usuario = _id_usuario;
end $$

create procedure sp_eliminarusuario(in _id_usuario int)
begin
    delete from usuario where id_usuario = _id_usuario;
end $$

create procedure sp_cambiarpasswordusuario(in _id_usuario int, in _password_hash varchar(255))
begin
    update usuario set password_hash = _password_hash where id_usuario = _id_usuario;
end $$

create procedure sp_desactivarusuario(in _id_usuario int)
begin
    update usuario set activo = false where id_usuario = _id_usuario and activo = true;
end $$

create procedure sp_realizarcheckout(in _id_reserva int)
begin
    declare v_not_found boolean default false;
    declare v_id_habitacion int;
    declare v_fecha_entrada date;
    declare v_tarifa_aplicada decimal(10,2);
    declare v_deposito_previo decimal(10,2);
    declare v_deposito_pagado boolean;
    declare v_estado_reserva varchar(20);
    declare v_factura_existente int default 0;
    declare v_dias_hospedados int;
    declare v_subtotal_hospedaje decimal(10,2);
    declare v_subtotal_consumos decimal(10,2);
    declare v_deposito_aplicado decimal(10,2);
    declare v_total_factura decimal(10,2);
    declare continue handler for not found set v_not_found = true;
    declare exit handler for sqlexception
    begin
        rollback;
        resignal;
    end;

    start transaction;

    select id_habitacion, fecha_entrada, tarifa_aplicada, deposito_previo, deposito_pagado, estado_reserva
    into v_id_habitacion, v_fecha_entrada, v_tarifa_aplicada, v_deposito_previo, v_deposito_pagado, v_estado_reserva
    from reserva
    where id_reserva = _id_reserva
    for update;

    if v_not_found then
        signal sqlstate '45000' set message_text = 'La reserva no existe';
    end if;
    if v_estado_reserva <> 'check_in' then
        signal sqlstate '45000' set message_text = 'La reserva no está registrada como check-in';
    end if;

    select count(*) into v_factura_existente from factura_hotel where id_reserva = _id_reserva;
    if v_factura_existente > 0 then
        signal sqlstate '45000' set message_text = 'La reserva ya tiene una factura';
    end if;

    set v_dias_hospedados = greatest(datediff(current_date(), v_fecha_entrada), 1);
    set v_subtotal_hospedaje = v_dias_hospedados * v_tarifa_aplicada;
    select coalesce(sum(cantidad * precio_unitario), 0.00)
    into v_subtotal_consumos
    from consumo_servicio
    where id_reserva = _id_reserva;
    set v_deposito_aplicado = if(v_deposito_pagado, least(v_deposito_previo,
            v_subtotal_hospedaje + v_subtotal_consumos), 0.00);
    set v_total_factura = v_subtotal_hospedaje + v_subtotal_consumos - v_deposito_aplicado;

    insert into factura_hotel(id_reserva, dias_hospedados, subtotal_hospedaje, subtotal_consumos,
            deposito_aplicado, total_factura, estado_pago)
    values (_id_reserva, v_dias_hospedados, v_subtotal_hospedaje, v_subtotal_consumos,
            v_deposito_aplicado, v_total_factura, if(v_total_factura = 0, 'pagada', 'pendiente'));

    update reserva
    set fecha_checkout = current_timestamp(), estado_reserva = 'check_out'
    where id_reserva = _id_reserva;
    update habitacion set estado_habitacion = 'limpieza' where id_habitacion = v_id_habitacion;

    commit;
end $$

delimiter ;

-- =============================================================================
-- vistas de consulta
-- =============================================================================

-- vista para listar tipos de habitacion
create or replace view vw_lista_tipos_habitacion as
select
    id_tipo_habitacion as 'id tipo',
    nombre_tipo as 'tipo de habitación',
    descripcion as 'descripción',
    capacidad_personas as 'capacidad',
    tarifa_noche as 'tarifa por noche'
from tipo_habitacion;

-- vista para listar huespedes
create or replace view vw_lista_huespedes as
select
    id_huesped as 'id huésped',
    documento_identificacion as 'documento',
    concat(nombre_huesped, ' ', apellido_huesped) as 'huésped',
    telefono_huesped as 'teléfono',
    correo_electronico as 'correo electrónico',
    nacionalidad as 'nacionalidad'
from huesped;

-- vista para listar habitaciones (une con tipo_habitacion)
create or replace view vw_lista_habitaciones as
select
    h.id_habitacion as 'id habitación',
    h.numero_habitacion as 'número',
    h.piso as 'piso',
    t.nombre_tipo as 'tipo de habitación',
    t.tarifa_noche as 'tarifa por noche',
    h.estado_habitacion as 'estado'
from habitacion h
inner join tipo_habitacion t on h.id_tipo_habitacion = t.id_tipo_habitacion;

-- vista para listar reservas (une con huesped y habitacion)
create or replace view vw_lista_reservas as
select
    r.id_reserva as 'no. reserva',
    concat(hu.nombre_huesped, ' ', hu.apellido_huesped) as 'huésped',
    ha.numero_habitacion as 'habitación',
    r.fecha_entrada as 'entrada',
    r.fecha_salida as 'salida',
    r.cantidad_huespedes as 'huéspedes',
    r.tarifa_aplicada as 'tarifa aplicada',
    r.deposito_previo as 'depósito previo',
    r.deposito_pagado as 'depósito pagado',
    r.fecha_checkin as 'check-in',
    r.fecha_checkout as 'check-out',
    r.estado_reserva as 'estado'
from reserva r
inner join huesped hu on r.id_huesped = hu.id_huesped
inner join habitacion ha on r.id_habitacion = ha.id_habitacion;

-- vista para listar consumos de servicio (une con reserva y huesped)
create or replace view vw_lista_consumos as
select
    c.id_consumo as 'id consumo',
    c.id_reserva as 'no. reserva',
    concat(hu.nombre_huesped, ' ', hu.apellido_huesped) as 'huésped',
    c.tipo_servicio as 'servicio',
    c.descripcion as 'descripción',
    c.cantidad as 'cantidad',
    c.precio_unitario as 'precio unitario',
    (c.cantidad * c.precio_unitario) as 'subtotal',
    c.fecha_consumo as 'fecha/hora'
from consumo_servicio c
inner join reserva r on c.id_reserva = r.id_reserva
inner join huesped hu on r.id_huesped = hu.id_huesped;

-- vista para listar facturas del hotel (une con reserva, huesped y habitacion)
create or replace view vw_lista_facturas as
select
    f.id_factura as 'no. factura',
    f.fecha_emision as 'fecha de emisión',
    concat(hu.nombre_huesped, ' ', hu.apellido_huesped) as 'huésped',
    ha.numero_habitacion as 'habitación',
    f.dias_hospedados as 'días hospedados',
    f.subtotal_hospedaje as 'subtotal hospedaje',
    f.subtotal_consumos as 'subtotal consumos',
    f.deposito_aplicado as 'depósito aplicado',
    f.total_factura as 'total',
    f.estado_pago as 'estado de pago'
from factura_hotel f
inner join reserva r on f.id_reserva = r.id_reserva
inner join huesped hu on r.id_huesped = hu.id_huesped
inner join habitacion ha on r.id_habitacion = ha.id_habitacion;
