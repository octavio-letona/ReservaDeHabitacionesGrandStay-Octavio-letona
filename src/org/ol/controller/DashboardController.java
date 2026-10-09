package org.ol.controller;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.ol.manager.RolePermissions;
import org.ol.manager.RolePermissions.Module;
import org.ol.manager.SesionContext;
import org.ol.model.Usuario;
import org.ol.system.Main;

public class DashboardController implements Initializable {
    @FXML private VBox navigation;
    @FXML private StackPane contentPane;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;

    private final Map<Module, String> titles = new LinkedHashMap<>();

    @Override public void initialize(URL location, ResourceBundle resources) {
        Usuario user = SesionContext.getInstancia().getUsuarioActual();
        if (user == null) {
            returnToLogin();
            return;
        }
        userLabel.setText(user.getFirstName() == null || user.getFirstName().isBlank()
                ? user.getUsername() : user.getFirstName() + " " + user.getLastName());
        roleLabel.setText(user.getRol());
        titles.put(Module.TIPOS_HABITACION, "Tipos de habitación");
        titles.put(Module.HABITACIONES, "Habitaciones");
        titles.put(Module.HUESPEDES, "Huéspedes");
        titles.put(Module.RESERVAS, "Reservas");
        titles.put(Module.CONSUMOS, "Consumos de servicio");
        titles.put(Module.FACTURAS, "Facturas");
        titles.put(Module.USUARIOS, "Usuarios");
        buildMenu(user.getRol());
    }

    private void buildMenu(String role) {
        for (Map.Entry<Module, String> item : titles.entrySet()) {
            if (!RolePermissions.allows(role, item.getKey())) continue;
            Button button = new Button(item.getValue());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("nav-button");
            button.setOnAction(event -> openWorkspace(item.getKey(), item.getValue()));
            navigation.getChildren().add(button);
        }
    }

    private void openWorkspace(Module module, String title) {
        Usuario user = SesionContext.getInstancia().getUsuarioActual();
        if (user == null || !RolePermissions.allows(user.getRol(), module)) {
            returnToLogin();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org/ol/view/WorkspaceView.fxml"));
            Node view = loader.load();
            WorkspaceController controller = loader.getController();
            controller.configure(module, title, "AUDITOR".equalsIgnoreCase(user.getRol().trim()));
            contentPane.getChildren().setAll(view);
        } catch (IOException | RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la sección: " + e.getMessage());
            alert.setHeaderText("Error al cargar la vista");
            alert.showAndWait();
        }
    }

    @FXML private void logout() {
        SesionContext.getInstancia().cerrarSesion();
        returnToLogin();
    }

    private void returnToLogin() {
        try { Main.show("LoginView.fxml", 1100, 720); }
        catch (IOException e) { throw new IllegalStateException("No se pudo volver al login", e); }
    }
}
