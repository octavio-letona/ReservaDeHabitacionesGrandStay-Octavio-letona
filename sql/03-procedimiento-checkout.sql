use grandstay_in4cm;

drop procedure if exists sp_realizarcheckout;
delimiter $$

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
