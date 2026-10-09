package org.ol.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.ol.dao.impl.HabitacionDAOImpl;
import org.ol.dao.impl.HuespedDAOImpl;
import org.ol.dao.impl.ReservaDAOImpl;
import org.ol.exception.AppException;
import org.ol.model.Habitacion;
import org.ol.model.Huesped;
import org.ol.model.Reserva;
import org.ol.service.CheckInService;

public class CheckInController implements Initializable {

    @FXML private TextField reservationIdField;
    @FXML private Button checkInButton;
    @FXML private Label feedbackLabel;
    @FXML private Label guestLabel;
    @FXML private Label roomLabel;
    @FXML private Label datesLabel;
    @FXML private Label rateLabel;
    @FXML private Label depositLabel;
    @FXML private Label statusLabel;
    @FXML private Label guestsLabel;

    private final ReservaDAOImpl reservaDAO    = new ReservaDAOImpl();
    private final HuespedDAOImpl  huespedDAO   = new HuespedDAOImpl();
    private final HabitacionDAOImpl habitacionDAO = new HabitacionDAOImpl();
    private final CheckInService checkInService = new CheckInService();

    private Reserva currentReservation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkInButton.setDisable(true);
    }

    @FXML
    private void lookupReservation() {
        clearSummary();
        String raw = reservationIdField.getText() == null ? "" : reservationIdField.getText().trim();
        int id;
        try {
            id = Integer.parseInt(raw);
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            feedbackLabel.setText("Escribe un ID de reserva válido (número positivo).");
            feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
            return;
        }

        try {
            currentReservation = reservaDAO.buscarPorId(id);
            if (currentReservation == null) {
                feedbackLabel.setText("No existe una reserva con ID " + id + ".");
                feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
                return;
            }

            Huesped guest = huespedDAO.buscarPorId(currentReservation.getIdHuesped());
            Habitacion room = habitacionDAO.buscarPorId(currentReservation.getIdHabitacion());
            if (guest == null || room == null) {
                feedbackLabel.setText("Datos de huésped o habitación incompletos en la reserva.");
                feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
                currentReservation = null;
                return;
            }

            guestLabel.setText(guest.getNombreHuesped() + " " + guest.getApellidoHuesped());
            roomLabel.setText(room.getNumeroHabitacion() + " (Piso " + room.getPiso() + ")");
            datesLabel.setText(currentReservation.getFechaEntrada() + " → " + currentReservation.getFechaSalida());
            rateLabel.setText(money(currentReservation.getTarifaAplicada()) + " / noche");
            guestsLabel.setText(String.valueOf(currentReservation.getCantidadHuespedes()));
            statusLabel.setText(currentReservation.getEstadoReserva());

            boolean depositoPagado = currentReservation.isDepositoPagado();
            if (depositoPagado) {
                depositLabel.setText("✔ Pagado: " + money(currentReservation.getDepositoPrevio()));
                depositLabel.getStyleClass().setAll("summary-value", "deposit-ok");
            } else {
                depositLabel.setText("✖ No pagado — requerido para check-in");
                depositLabel.getStyleClass().setAll("summary-value", "deposit-error");
            }

            String estado = currentReservation.getEstadoReserva();
            boolean canCheckIn = ("confirmada".equalsIgnoreCase(estado) || "pendiente".equalsIgnoreCase(estado))
                    && depositoPagado;
            checkInButton.setDisable(!canCheckIn);

            if ("check_in".equalsIgnoreCase(estado)) {
                feedbackLabel.setText("Esta reserva ya tiene check-in registrado.");
                feedbackLabel.getStyleClass().setAll("feedback", "feedback-warning");
            } else if ("check_out".equalsIgnoreCase(estado) || "cancelada".equalsIgnoreCase(estado)) {
                feedbackLabel.setText("No se puede hacer check-in: la reserva está en estado «" + estado + "».");
                feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
            } else if (!depositoPagado) {
                feedbackLabel.setText("El depósito no ha sido pagado. Registra el pago antes del check-in.");
                feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
            } else {
                feedbackLabel.setText("Reserva lista para check-in.");
                feedbackLabel.getStyleClass().setAll("feedback", "feedback-ok");
            }

        } catch (RuntimeException e) {
            currentReservation = null;
            feedbackLabel.setText("Error al consultar la reserva. Revisa la conexión con la base de datos.");
            feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
        }
    }

    @FXML
    private void confirmCheckIn() {
        if (currentReservation == null || checkInButton.isDisabled()) return;
        try {
            Reserva updated = checkInService.realizarCheckIn(currentReservation.getIdReserva());
            statusLabel.setText(updated.getEstadoReserva());
            feedbackLabel.setText("✔ Check-in registrado exitosamente. Habitación marcada como ocupada.");
            feedbackLabel.getStyleClass().setAll("feedback", "feedback-ok");
            checkInButton.setDisable(true);
            currentReservation = updated;
        } catch (AppException e) {
            feedbackLabel.setText(e.getMessage() == null ? "No se pudo completar el check-in." : e.getMessage());
            feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
        } catch (RuntimeException e) {
            feedbackLabel.setText("Error inesperado al registrar check-in. Revisa la conexión con la base de datos.");
            feedbackLabel.getStyleClass().setAll("feedback", "feedback-error");
        }
    }

    private void clearSummary() {
        currentReservation = null;
        checkInButton.setDisable(true);
        guestLabel.setText("—");
        roomLabel.setText("—");
        datesLabel.setText("—");
        rateLabel.setText("—");
        depositLabel.setText("—");
        statusLabel.setText("—");
        guestsLabel.setText("—");
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().setAll("feedback");
    }

    private String money(Double amount) { return money(amount == null ? 0 : amount); }
    private String money(double amount) { return String.format(java.util.Locale.US, "Q %.2f", amount); }
}
