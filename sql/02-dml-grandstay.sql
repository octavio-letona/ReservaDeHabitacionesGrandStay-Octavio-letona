use grandstay_in4cm;

-- =============================================================================
-- usuarios semilla (se insertan con la contraseña en texto plano)
-- La aplicación (UsuarioDAOImpl.iniciarSesion) se encargará de hashearlas 
-- la primera vez que inicien sesión.
-- =============================================================================
insert into usuario(username, email, first_name, last_name, password_hash, rol, activo) values
('admin',       'admin@grandstay.com',    'Administrador', 'GrandStay', 'Admin1234!', 'administrador', true),
('recepcion01', 'recep@grandstay.com',    'Recepción',     'GrandStay', 'Recep1234!', 'recepcionista', true),
('housekeep01', 'housekeep@grandstay.com','Housekeeping',  'GrandStay', 'House1234!', 'housekeeping',  true);


insert into tipo_habitacion(nombre_tipo, descripcion, capacidad_personas, tarifa_noche) values
('suite sencilla', 'cama individual, escritorio y baño privado', 1, 450.00),
('suite doble', 'dos camas dobles, minibar y vista a la ciudad', 2, 650.00),
('suite junior', 'cama king, sala pequeña y balcón', 2, 900.00),
('suite familiar', 'dos habitaciones conectadas, sala y minibar', 4, 1250.00),
('suite presidencial', 'suite de lujo con jacuzzi, sala, comedor y terraza privada', 2, 2200.00);

insert into habitacion(numero_habitacion, piso, estado_habitacion, id_tipo_habitacion) values
('101', 1, 'disponible', 1),
('102', 1, 'limpieza', 1),
('201', 2, 'ocupada', 2),
('202', 2, 'disponible', 2),
('301', 3, 'ocupada', 3),
('302', 3, 'mantenimiento', 3),
('401', 4, 'disponible', 4),
('501', 5, 'disponible', 5);


insert into huesped(documento_identificacion, nombre_huesped, apellido_huesped, telefono_huesped, correo_electronico, nacionalidad) values
('2456789012301', 'Carlos', 'Méndez Ruiz', '5512-3401', 'carlos.mendez@correo.com', 'Guatemalteca'),
('1987654321012', 'María Fernanda', 'López Girón', '5523-8890', 'mf.lopez@correo.com', 'Guatemalteca'),
('US7845123', 'John', 'Anderson', '+1 305-555-0142', 'j.anderson@mailbox.com', 'Estadounidense'),
('2234567890101', 'Ana Lucía', 'Castillo Morales', '4155-7721', 'ana.castillo@correo.com', 'Guatemalteca'),
('ar4521897', 'Sofía', 'Ramírez Díaz', '+54 11 55550198', 'sofia.ramirez@mailbox.com', 'Argentina'),
('1765432109801', 'Luis', 'Pérez Gómez', '5588-4412', 'luis.perez@correo.com', 'Guatemalteca');


insert into reserva(id_huesped, id_habitacion, fecha_entrada, fecha_salida, cantidad_huespedes, tarifa_aplicada, deposito_previo, deposito_pagado, fecha_checkin, fecha_checkout, estado_reserva) values
(1, 2, '2026-10-01', '2026-10-04', 1, 450.00, 450.00, true, '2026-10-01 14:30:00', '2026-10-04 11:00:00', 'check_out'),
(2, 1, '2026-09-28', '2026-09-30', 1, 450.00, 450.00, true, '2026-09-28 15:10:00', '2026-09-30 10:45:00', 'check_out'),
(3, 4, '2026-09-20', '2026-09-23', 2, 650.00, 650.00, true, '2026-09-20 13:40:00', '2026-09-23 11:30:00', 'check_out'),
(4, 7, '2026-09-25', '2026-09-27', 3, 1250.00, 1250.00, true, '2026-09-25 16:00:00', '2026-09-27 12:00:00', 'check_out'),
(5, 8, '2026-09-30', '2026-10-02', 2, 2200.00, 2200.00, true, '2026-09-30 17:15:00', '2026-10-02 10:30:00', 'check_out'),
(3, 3, '2026-10-05', '2026-10-09', 2, 650.00, 650.00, true, '2026-10-05 13:50:00', null, 'check_in'),
(4, 5, '2026-10-06', '2026-10-08', 2, 900.00, 900.00, true, '2026-10-06 16:20:00', null, 'check_in'),
(6, 7, '2026-10-12', '2026-10-15', 4, 1250.00, 1250.00, true, null, null, 'confirmada'),
(1, 8, '2026-10-20', '2026-10-22', 2, 2200.00, 2200.00, false, null, null, 'pendiente'),
(2, 1, '2026-10-10', '2026-10-12', 1, 450.00, 0.00, false, null, null, 'cancelada');

insert into consumo_servicio(id_reserva, tipo_servicio, descripcion, cantidad, precio_unitario) values
(1, 'minibar', 'agua purificada', 2, 15.00),
(1, 'minibar', 'barra de chocolate', 1, 20.00),
(1, 'lavanderia', 'lavado de camisas', 3, 35.00),
(2, 'minibar', 'cerveza nacional', 2, 30.00),
(2, 'restaurante', 'desayuno continental', 2, 85.00),
(3, 'minibar', 'jugo de naranja', 2, 18.00),
(3, 'minibar', 'snack de nueces', 1, 28.00),
(4, 'spa', 'masaje relajante', 2, 350.00),
(4, 'minibar', 'botella de vino', 1, 280.00),
(5, 'restaurante', 'cena romántica', 1, 520.00),
(5, 'minibar', 'champagne', 1, 450.00),
(6, 'minibar', 'jugo de naranja', 2, 18.00),
(7, 'minibar', 'botella de vino', 1, 280.00);

insert into factura_hotel(id_reserva, dias_hospedados, subtotal_hospedaje, subtotal_consumos, deposito_aplicado, total_factura, estado_pago) values
(1, 3, 1350.00, 155.00, 450.00, 1055.00, 'pagada'),
(2, 2, 900.00, 230.00, 450.00, 680.00, 'pagada'),
(3, 3, 1950.00, 64.00, 650.00, 1364.00, 'pagada'),
(4, 2, 2500.00, 980.00, 1250.00, 2230.00, 'pendiente'),
(5, 2, 4400.00, 970.00, 2200.00, 3170.00, 'pagada');

select * from vw_lista_tipos_habitacion;
select * from vw_lista_habitaciones;
select * from vw_lista_huespedes;
select * from vw_lista_reservas;
select * from vw_lista_consumos;
select * from vw_lista_facturas;