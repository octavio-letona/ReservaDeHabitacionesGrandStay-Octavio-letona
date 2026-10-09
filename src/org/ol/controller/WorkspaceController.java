package org.ol.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import org.ol.dao.Crud;
import org.ol.dao.impl.ConsumoServicioDAOImpl;
import org.ol.dao.impl.FacturaHotelDAOImpl;
import org.ol.dao.impl.HabitacionDAOImpl;
import org.ol.dao.impl.HuespedDAOImpl;
import org.ol.dao.impl.ReservaDAOImpl;
import org.ol.dao.impl.TipoHabitacionDAOImpl;
import org.ol.dao.impl.UsuarioDAOImpl;
import org.ol.manager.RolePermissions.Module;
import org.ol.manager.RolePermissions;
import org.ol.manager.SesionContext;
import org.ol.model.ConsumoServicio;
import org.ol.model.FacturaHotel;
import org.ol.model.Habitacion;
import org.ol.model.Huesped;
import org.ol.model.Reserva;
import org.ol.model.TipoHabitacion;
import org.ol.model.Usuario;
import org.ol.util.PasswordHasher;

public class WorkspaceController {
    private enum InputKind { TEXT, INTEGER, DECIMAL, DATE, BOOLEAN, CHOICE, PASSWORD }
    private static final class FieldSpec {
        final String property, label;
        final InputKind kind;
        final boolean inTable, editable, required;
        final List<String> choices;
        FieldSpec(String property, String label, InputKind kind, boolean inTable, boolean editable, String... choices) {
            this(property, label, kind, inTable, editable, editable, choices);
        }
        FieldSpec(String property, String label, InputKind kind, boolean inTable, boolean editable,
                boolean required, String... choices) {
            this.property = property; this.label = label; this.kind = kind;
            this.inTable = inTable; this.editable = editable; this.required = required;
            this.choices = Arrays.asList(choices);
        }
    }

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label feedbackLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Object> table;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Module module;
    private String title;
    private Crud<?, Integer> dao;
    private Class<?> entityClass;
    private List<FieldSpec> fields;
    private final ObservableList<Object> rows = FXCollections.observableArrayList();
    private FilteredList<Object> filteredRows;

    public void configure(Module module, String title, boolean readOnly) {
        org.ol.model.Usuario currentUser = SesionContext.getInstancia().getUsuarioActual();
        if (currentUser == null || !RolePermissions.allows(currentUser.getRol(), module)) {
            throw new SecurityException("Tu rol no tiene permiso para abrir esta sección.");
        }
        this.module = module;
        this.title = title;
        configureEntity();
        titleLabel.setText(title);
        subtitleLabel.setText("Consulta, registra y mantiene la información de " + title.toLowerCase() + ".");
        createColumns();
        createButton.setDisable(readOnly);
        editButton.setDisable(readOnly);
        deleteButton.setDisable(readOnly);
        filteredRows = new FilteredList<>(rows, item -> true);
        table.setItems(filteredRows);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filter(newValue));
        editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        refresh();
    }

    private void configureEntity() {
        switch (module) {
            case TIPOS_HABITACION -> {
                dao = new TipoHabitacionDAOImpl(); entityClass = TipoHabitacion.class;
                fields = List.of(id("idTipoHabitacion", "ID"), text("nombreTipo", "Nombre", true),
                        optionalText("descripcion", "Descripción", true), integer("capacidadPersonas", "Capacidad", true),
                        decimal("tarifaNoche", "Tarifa por noche", true));
            }
            case HABITACIONES -> {
                dao = new HabitacionDAOImpl(); entityClass = Habitacion.class;
                fields = List.of(id("idHabitacion", "ID"), text("numeroHabitacion", "Número", true),
                        integer("piso", "Piso", true), choice("estadoHabitacion", "Estado", true,
                                "disponible", "ocupada", "limpieza", "mantenimiento"),
                        integer("idTipoHabitacion", "ID tipo de habitación", true));
            }
            case HUESPEDES -> {
                dao = new HuespedDAOImpl(); entityClass = Huesped.class;
                fields = List.of(id("idHuesped", "ID"), text("documentoIdentificacion", "Documento", true),
                        text("nombreHuesped", "Nombre", true), text("apellidoHuesped", "Apellido", true),
                        optionalText("telefonoHuesped", "Teléfono", true), optionalText("correoElectronico", "Correo", true),
                        optionalText("nacionalidad", "Nacionalidad", true));
            }
            case RESERVAS -> {
                dao = new ReservaDAOImpl(); entityClass = Reserva.class;
                fields = List.of(id("idReserva", "ID"), integer("idHuesped", "ID huésped", true),
                        integer("idHabitacion", "ID habitación", true), date("fechaEntrada", "Entrada", true),
                        date("fechaSalida", "Salida", true), integer("cantidadHuespedes", "Huéspedes", true),
                        decimal("tarifaAplicada", "Tarifa", true), decimal("depositoPrevio", "Depósito", true),
                        bool("depositoPagado", "Depósito pagado", true), choice("estadoReserva", "Estado", true,
                                "pendiente", "confirmada", "check_in", "check_out", "cancelada"));
            }
            case CONSUMOS -> {
                dao = new ConsumoServicioDAOImpl(); entityClass = ConsumoServicio.class;
                fields = List.of(id("idConsumo", "ID"), integer("idReserva", "ID reserva", true),
                        choice("tipoServicio", "Servicio", true, "minibar", "restaurante", "lavanderia", "spa", "otro"),
                        text("descripcion", "Descripción", true), integer("cantidad", "Cantidad", true),
                        decimal("precioUnitario", "Precio unitario", true),
                        new FieldSpec("fechaConsumo", "Fecha de consumo", InputKind.TEXT, true, false));
            }
            case FACTURAS -> {
                dao = new FacturaHotelDAOImpl(); entityClass = FacturaHotel.class;
                fields = List.of(id("idFactura", "ID"), integer("idReserva", "ID reserva", true),
                        new FieldSpec("fechaEmision", "Emisión", InputKind.TEXT, true, false),
                        integer("diasHospedados", "Días hospedados", true), decimal("subtotalHospedaje", "Hospedaje", true),
                        decimal("subtotalConsumos", "Consumos", true), decimal("depositoAplicado", "Depósito", true),
                        decimal("totalFactura", "Total", true), choice("estadoPago", "Estado de pago", true,
                                "pendiente", "pagada", "anulada"));
            }
            case USUARIOS -> {
                dao = new UsuarioDAOImpl(); entityClass = Usuario.class;
                fields = List.of(id("id", "ID"), text("username", "Usuario", true), text("email", "Correo", true),
                        text("firstName", "Nombre", true), text("lastName", "Apellido", true),
                        new FieldSpec("passwordHash", "Contraseña (vacío = conservar)", InputKind.PASSWORD, false, true),
                        choice("rol", "Rol", true, "administrador", "recepcionista", "cajero", "auditor"),
                        bool("activo", "Activo", true),
                        new FieldSpec("fechaCreacion", "Creado", InputKind.TEXT, true, false));
            }
            default -> throw new IllegalArgumentException("Sección no reconocida: " + module);
        }
    }

    private static FieldSpec id(String property, String label) {
        return new FieldSpec(property, label, InputKind.INTEGER, true, false);
    }
    private static FieldSpec text(String property, String label, boolean table) {
        return new FieldSpec(property, label, InputKind.TEXT, table, true);
    }
    private static FieldSpec optionalText(String property, String label, boolean table) {
        return new FieldSpec(property, label, InputKind.TEXT, table, true, false);
    }
    private static FieldSpec integer(String property, String label, boolean table) {
        return new FieldSpec(property, label, InputKind.INTEGER, table, true);
    }
    private static FieldSpec decimal(String property, String label, boolean table) {
        return new FieldSpec(property, label, InputKind.DECIMAL, table, true);
    }
    private static FieldSpec date(String property, String label, boolean table) {
        return new FieldSpec(property, label, InputKind.DATE, table, true);
    }
    private static FieldSpec bool(String property, String label, boolean table) {
        return new FieldSpec(property, label, InputKind.BOOLEAN, table, true);
    }
    private static FieldSpec choice(String property, String label, boolean table, String... options) {
        return new FieldSpec(property, label, InputKind.CHOICE, table, true, options);
    }

    private void createColumns() {
        table.getColumns().clear();
        for (FieldSpec field : fields) {
            if (!field.inTable) continue;
            TableColumn<Object, Object> column = new TableColumn<>(field.label);
            column.setCellValueFactory(data -> {
                try { return new javafx.beans.property.ReadOnlyObjectWrapper<>(read(data.getValue(), field.property)); }
                catch (ReflectiveOperationException e) { return new javafx.beans.property.ReadOnlyObjectWrapper<>(""); }
            });
            column.setPrefWidth(field.property.startsWith("id") ? 78 : 145);
            table.getColumns().add(column);
        }
    }

    @FXML private void reload() {
        try {
            rows.setAll(dao.listarTodos());
            feedbackLabel.setText(rows.size() + " registro(s)");
        } catch (RuntimeException e) { showError("No se pudieron cargar los datos", e); }
    }

    private void refresh() { reload(); }

    private void filter(String query) {
        if (filteredRows == null) return;
        String needle = query == null ? "" : query.trim().toLowerCase();
        Predicate<Object> predicate = row -> {
            if (needle.isEmpty()) return true;
            for (FieldSpec field : fields) {
                if (!field.inTable) continue;
                try {
                    Object value = read(row, field.property);
                    if (value != null && value.toString().toLowerCase().contains(needle)) return true;
                } catch (ReflectiveOperationException ignored) { }
            }
            return false;
        };
        filteredRows.setPredicate(predicate);
    }

    @FXML private void create() { showEditor(null); }

    @FXML private void edit() {
        Object selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) showEditor(selected);
    }

    @FXML private void delete() {
        Object selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el registro seleccionado de " + title.toLowerCase() + "?", ButtonType.CANCEL, ButtonType.OK);
        confirmation.setHeaderText("Confirmar eliminación");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            Integer id = ((Number) read(selected, idProperty())).intValue();
            if (deleteFromDao(id)) { feedbackLabel.setText("Registro eliminado."); refresh(); }
            else feedbackLabel.setText("No se encontró el registro para eliminar.");
        } catch (Exception e) { showError("No se pudo eliminar el registro", e); }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean deleteFromDao(Integer id) { return ((Crud) dao).eliminar(id); }

    private void showEditor(Object existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo registro" : "Editar registro");
        dialog.setHeaderText((existing == null ? "Crear" : "Actualizar") + " — " + title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(10); form.setPadding(new Insets(10, 18, 4, 8));
        List<Object> controls = new ArrayList<>();
        int row = 0;
        for (FieldSpec field : fields) {
            if (!field.editable) continue;
            javafx.scene.control.Control control = makeControl(field, existing);
            form.add(new Label(field.label), 0, row);
            form.add(control, 1, row++);
            controls.add(control);
        }
        Label error = new Label(); error.setWrapText(true); error.getStyleClass().add("error-message");
        Button save = new Button(existing == null ? "Crear" : "Guardar cambios");
        save.getStyleClass().add("primary-button");
        HBox actions = new HBox(10);
        if (existing == null) {
            Button clear = new Button("Limpiar campos");
            clear.getStyleClass().add("secondary-button");
            clear.setOnAction(event -> clearInputs(controls));
            actions.getChildren().add(clear);
        }
        actions.getChildren().add(save);
        VBox content = new VBox(12, form, error, actions);
        content.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(520);
        save.setOnAction(event -> {
            try {
                validate(controls, existing);
                Object entity = existing == null ? newEntity() : existing;
                for (int i = 0, controlIndex = 0; i < fields.size(); i++) {
                    FieldSpec spec = fields.get(i);
                    if (!spec.editable) continue;
                    Object control = controls.get(controlIndex++);
                    Object value = readControl(control, spec);
                    if (spec.kind == InputKind.PASSWORD) {
                        if (value == null || ((char[]) value).length == 0) continue;
                        char[] clearPassword = (char[]) value;
                        value = PasswordHasher.hash(clearPassword);
                        Arrays.fill(clearPassword, '\0');
                    }
                    write(entity, spec.property, value);
                }
                if (existing == null && module == Module.USUARIOS) write(entity, "activo", true);
                boolean saved = existing == null ? createInDao(entity) : updateInDao(entity);
                if (!saved) { error.setText("No se guardaron cambios. Revisa si el registro existe."); return; }
                dialog.close(); feedbackLabel.setText(existing == null ? "Registro creado." : "Cambios guardados."); refresh();
            } catch (IllegalArgumentException | ReflectiveOperationException ex) {
                error.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                error.setText("No se pudo guardar. Comprueba que los valores sean válidos y no estén duplicados.");
            }
        });
        dialog.showAndWait();
    }

    private void clearInputs(List<Object> controls) {
        for (Object control : controls) {
            if (control instanceof CheckBox check) check.setSelected(false);
            else if (control instanceof ChoiceBox<?> choice) choice.setValue(null);
            else if (control instanceof TextInputControl text) text.clear();
        }
    }

    private javafx.scene.control.Control makeControl(FieldSpec spec, Object existing) {
        Object current = null;
        if (existing != null) {
            try { current = read(existing, spec.property); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
        javafx.scene.control.Control control;
        switch (spec.kind) {
            case BOOLEAN -> {
                CheckBox check = new CheckBox(); check.setSelected(existing == null ? true : Boolean.TRUE.equals(current)); control = check;
            }
            case CHOICE -> {
                ChoiceBox<String> choice = new ChoiceBox<>(FXCollections.observableArrayList(spec.choices));
                if (current != null) choice.setValue(current.toString()); else if (!spec.choices.isEmpty()) choice.setValue(spec.choices.get(0));
                control = choice;
            }
            case PASSWORD -> { control = new PasswordField(); ((PasswordField) control).setPromptText("Mínimo 8 caracteres"); }
            default -> {
                TextField text = new TextField(current == null ? "" : current.toString());
                if (spec.kind == InputKind.DATE) text.setPromptText("AAAA-MM-DD");
                if (spec.kind == InputKind.INTEGER) text.setPromptText("Número entero");
                if (spec.kind == InputKind.DECIMAL) text.setPromptText("0.00");
                control = text;
            }
        }
        control.setMaxWidth(Double.MAX_VALUE);
        return control;
    }

    private void validate(List<Object> controls, Object existing) {
        for (int i = 0, index = 0; i < fields.size(); i++) {
            FieldSpec spec = fields.get(i);
            if (!spec.editable) continue;
            Object control = controls.get(index++);
            Object raw = rawControl(control);
            if (spec.kind == InputKind.PASSWORD && existing != null && ((char[]) raw).length == 0) continue;
            String textValue = raw instanceof char[] chars ? new String(chars) : raw.toString();
            if (textValue.isBlank()) {
                if (!spec.required) continue;
                throw new IllegalArgumentException("Completa el campo «" + spec.label + "».");
            }
            try {
                switch (spec.kind) {
                    case INTEGER -> Integer.parseInt(textValue.trim());
                    case DECIMAL -> {
                        double value = Double.parseDouble(textValue.trim());
                        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("El campo «" + spec.label + "» debe ser un número válido igual o mayor que cero.");
                    }
                    case DATE -> LocalDate.parse(textValue.trim());
                    case PASSWORD -> {
                        if (((char[]) raw).length < 8) throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
                    }
                    default -> { }
                }
            } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("El valor de «" + spec.label + "» no tiene un formato válido.");
            }
            if ((module == Module.USUARIOS && spec.property.equals("email")
                    || module == Module.HUESPEDES && spec.property.equals("correoElectronico"))
                    && !textValue.isBlank() && !textValue.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Escribe un correo electrónico válido.");
            }
        }
        if (module == Module.RESERVAS) {
            TextField entry = (TextField) controlFor("fechaEntrada", controls);
            TextField exit = (TextField) controlFor("fechaSalida", controls);
            if (!LocalDate.parse(exit.getText()).isAfter(LocalDate.parse(entry.getText()))) {
                throw new IllegalArgumentException("La salida debe ser posterior a la entrada.");
            }
        }
    }

    private Object controlFor(String property, List<Object> controls) {
        int index = 0;
        for (FieldSpec spec : fields) {
            if (!spec.editable) continue;
            if (spec.property.equals(property)) return controls.get(index);
            index++;
        }
        throw new IllegalArgumentException("Campo requerido no encontrado: " + property);
    }

    private Object rawControl(Object control) {
        if (control instanceof CheckBox check) return check.isSelected();
        if (control instanceof ChoiceBox<?> choice) return choice.getValue() == null ? "" : choice.getValue();
        if (control instanceof PasswordField password) return password.getText().toCharArray();
        return ((TextInputControl) control).getText().trim();
    }

    private Object readControl(Object control, FieldSpec spec) {
        Object raw = rawControl(control);
        return switch (spec.kind) {
            case BOOLEAN -> raw;
            case CHOICE, TEXT -> raw.toString().trim();
            case INTEGER -> Integer.valueOf(raw.toString().trim());
            case DECIMAL -> Double.valueOf(raw.toString().trim());
            case DATE -> Date.valueOf(LocalDate.parse(raw.toString().trim()));
            case PASSWORD -> raw;
        };
    }

    private String idProperty() {
        return fields.stream().filter(field -> !field.editable && field.property.toLowerCase().startsWith("id"))
                .map(field -> field.property).findFirst().orElse("id");
    }

    private Object newEntity() throws ReflectiveOperationException {
        Constructor<?> constructor = entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object read(Object entity, String property) throws ReflectiveOperationException {
        String capitalized = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        Method getter;
        try { getter = entityClass.getMethod("get" + capitalized); }
        catch (NoSuchMethodException e) { getter = entityClass.getMethod("is" + capitalized); }
        return getter.invoke(entity);
    }

    private void write(Object entity, String property, Object value) throws ReflectiveOperationException {
        String setter = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        for (Method method : entityClass.getMethods()) {
            if (method.getName().equals(setter) && method.getParameterCount() == 1) {
                method.invoke(entity, value);
                return;
            }
        }
        throw new NoSuchMethodException(setter);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean createInDao(Object entity) { return ((Crud) dao).crear(entity); }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean updateInDao(Object entity) { return ((Crud) dao).actualizar(entity); }

    private void showError(String header, Exception error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(error.getMessage() == null ? "Error inesperado." : error.getMessage());
        alert.showAndWait();
    }
}
