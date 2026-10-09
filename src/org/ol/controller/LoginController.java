package org.ol.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.ol.dao.UsuarioDAO;
import org.ol.dao.impl.UsuarioDAOImpl;
import org.ol.manager.RolePermissions;
import org.ol.manager.SesionContext;
import org.ol.model.Usuario;
import org.ol.system.Main;
import org.ol.util.PasswordHasher;

public class LoginController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button bootstrapButton;
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override public void initialize(URL location, ResourceBundle resources) {
        messageLabel.setText("");
        try {
            boolean noUsers = usuarioDAO.listarTodos().isEmpty();
            bootstrapButton.setVisible(noUsers);
            bootstrapButton.setManaged(noUsers);
        } catch (RuntimeException e) {
            messageLabel.setText("No se pudo conectar con la base de datos.");
        }
    }

    @FXML private void login() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        char[] password = passwordField.getText() == null ? new char[0] : passwordField.getText().toCharArray();
        if (username.isEmpty() || password.length == 0) {
            messageLabel.setText("Escribe tu usuario y contraseña.");
            return;
        }
        try {
            Usuario user = usuarioDAO.buscarPorUsername(username);
            if (user == null || !user.isActivo() || !PasswordHasher.verify(password, user.getPasswordHash())) {
                messageLabel.setText("Usuario o contraseña incorrectos.");
                passwordField.clear();
                return;
            }
            if (RolePermissions.forRole(user.getRol()).isEmpty()) {
                messageLabel.setText("Tu rol no tiene permisos configurados para acceder al sistema.");
                passwordField.clear();
                return;
            }
            SesionContext.getInstancia().setUsuarioActual(user);
            Main.show("DashboardView.fxml", 1280, 800);
        } catch (RuntimeException | IOException e) {
            messageLabel.setText("No se pudo iniciar sesión. Revisa la conexión con la base de datos.");
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    @FXML private void bootstrapAdmin() {
        try { Main.show("BootstrapAdminView.fxml", 760, 820); }
        catch (IOException e) { messageLabel.setText("No se pudo abrir la configuración inicial."); }
    }
}
