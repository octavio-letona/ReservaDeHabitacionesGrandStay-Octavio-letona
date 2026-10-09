package org.ol.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.ol.dao.impl.ConsumoServicioDAOImpl;
import org.ol.dao.impl.HabitacionDAOImpl;
import org.ol.dao.impl.HuespedDAOImpl;
import org.ol.dao.impl.ReservaDAOImpl;
import org.ol.model.ConsumoServicio;
import org.ol.model.FacturaHotel;
import org.ol.model.Habitacion;
import org.ol.model.Huesped;
import org.ol.model.Reserva;
import org.ol.service.CheckoutService;

public class CheckoutController implements Initializable {
    @FXML private TextField reservationIdField;
    @FXML private Button checkoutButton;
    @FXML private Label feedbackLabel;
    @FXML private Label guestLabel;
    @FXML private Label roomLabel;
    @FXML private Label datesLabel;
    @FXML private Label rateLabel;
    @FXML private Label depositLabel;
    @FXML private Label nightsLabel;
    @FXML private Label staySubtotalLabel;
    @FXML private Label consumptionSubtotalLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<ConsumoServicio> consumptionTable;

    private final ReservaDAOImpl reservaDAO = new ReservaDAOImpl();
    private final HuespedDAOImpl huespedDAO = new HuespedDAOImpl();
    private final HabitacionDAOImpl habitacionDAO = new HabitacionDAOImpl();
    private final ConsumoServicioDAOImpl consumoDAO = new ConsumoServicioDAOImpl();
    private final CheckoutService checkoutService = new CheckoutService();
    private final ObservableList<ConsumoServicio> consumptions = FXCollections.observableArrayList();
    private Reserva currentReservation;
    private double previewTotal;

    @Override public void initialize(URL location, ResourceBundle resources) {
        TableColumn<ConsumoServicio, String> type = new TableColumn<>("Servicio");
        type.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTipoServicio()));
        TableColumn<ConsumoServicio, String> description = new TableColumn<>("Descripción");
        description.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescripcion()));
        TableColumn<ConsumoServicio, Integer> quantity = new TableColumn<>("Cantidad");
        quantity.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCantidad()));
        TableColumn<ConsumoServicio, Double> price = new TableColumn<>("Precio unitario");
        price.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrecioUnitario()));
        TableColumn<ConsumoServicio, Double> subtotal = new TableColumn<>("Subtotal");
        subtotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getCantidad() * data.getValue().getPrecioUnitario()));
        consumptionTable.getColumns().addAll(List.of(type, description, quantity, price, subtotal));
        consumptionTable.setItems(consumptions);
        checkoutButton.setDisable(true);
    }

    @FXML private void lookupReservation() {
        clearSummary();
        try {
            int id = Integer.parseInt(reservationIdField.getText().trim());
            if (id <= 0) throw new NumberFormatException();
            currentReservation = reservaDAO.buscarPorId(id);
            if (currentReservation == null) {
                feedbackLabel.setText("No existe una reserva con ese ID.");
                return;
            }
            Huesped guest = huespedDAO.buscarPorId(currentReservation.getIdHuesped());
            Habitacion room = habitacionDAO.buscarPorId(currentReservation.getIdHabitacion());
            if (guest == null || room == null) {
                feedbackLabel.setText("La reserva tiene datos de huésped o habitación incompletos.");
                currentReservation = null;
                return;
            }
            guestLabel.setText(guest.getNombreHuesped() + " " + guest.getApellidoHuesped());
            roomLabel.setText(room.getNumeroHabitacion());
            datesLabel.setText(currentReservation.getFechaEntrada() + " — " + currentReservation.getFechaSalida());
            rateLabel.setText(money(currentReservation.getTarifaAplicada()) + " / noche");
            depositLabel.setText(currentReservation.isDepositoPagado()
                    ? "Pagado: " + money(currentReservation.getDepositoPrevio()) : "Sin depósito pagado");

            List<ConsumoServicio> matching = consumoDAO.listarPorReserva(id);
            consumptions.setAll(matching);
            renderPreview(matching);
            boolean canCheckout = "check_in".equalsIgnoreCase(currentReservation.getEstadoReserva());
            checkoutButton.setDisable(!canCheckout);
            feedbackLabel.setText(canCheckout ? "Reserva lista para checkout." :
                    "El checkout solo está disponible para reservas en estado check_in.");
        } catch (NumberFormatException e) {
            feedbackLabel.setText("Escribe un ID de reserva válido.");
        } catch (RuntimeException e) {
            currentReservation = null;
            feedbackLabel.setText("No se pudo consultar la reserva. Revisa la conexión con la base de datos.");
        }
    }

    private void renderPreview(List<ConsumoServicio> items) {
        LocalDate checkIn = currentReservation.getFechaEntrada().toLocalDate();
        long nights = Math.max(1, ChronoUnit.DAYS.between(checkIn, LocalDate.now()));
        double stay = nights * currentReservation.getTarifaAplicada();
        double consumption = items.stream().mapToDouble(item -> item.getCantidad() * item.getPrecioUnitario()).sum();
        double deposit = currentReservation.isDepositoPagado()
                ? Math.min(currentReservation.getDepositoPrevio(), stay + consumption) : 0;
        previewTotal = stay + consumption - deposit;
        nightsLabel.setText(Long.toString(nights));
        staySubtotalLabel.setText(money(stay));
        consumptionSubtotalLabel.setText(money(consumption));
        totalLabel.setText(money(previewTotal));
    }

    @FXML private void confirmCheckout() {
        if (currentReservation == null || checkoutButton.isDisabled()) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Se generará la factura y la habitación pasará a limpieza. Total estimado: "
                        + money(previewTotal) + ". ¿Continuar?", ButtonType.CANCEL, ButtonType.OK);
        confirmation.setHeaderText("Confirmar checkout");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            FacturaHotel invoice = checkoutService.realizarCheckout(currentReservation.getIdReserva());
            totalLabel.setText(money(invoice.getTotalFactura()));
            nightsLabel.setText(Integer.toString(invoice.getDiasHospedados()));
            staySubtotalLabel.setText(money(invoice.getSubtotalHospedaje()));
            consumptionSubtotalLabel.setText(money(invoice.getSubtotalConsumos()));
            depositLabel.setText("Aplicado: " + money(invoice.getDepositoAplicado()));
            feedbackLabel.setText("Checkout completado. Factura #" + invoice.getIdFactura()
                    + " · " + invoice.getEstadoPago());
            checkoutButton.setDisable(true);
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage() == null
                    ? "No se pudo completar el checkout." : e.getMessage());
        }
    }

    private void clearSummary() {
        currentReservation = null;
        consumptions.clear();
        checkoutButton.setDisable(true);
        guestLabel.setText("—"); roomLabel.setText("—"); datesLabel.setText("—");
        rateLabel.setText("—"); depositLabel.setText("—"); nightsLabel.setText("—");
        staySubtotalLabel.setText("—"); consumptionSubtotalLabel.setText("—"); totalLabel.setText("—");
        feedbackLabel.setText("");
    }

    private String money(Double amount) { return money(amount == null ? 0 : amount.doubleValue()); }
    private String money(double amount) { return String.format(java.util.Locale.US, "Q %.2f", amount); }
}
