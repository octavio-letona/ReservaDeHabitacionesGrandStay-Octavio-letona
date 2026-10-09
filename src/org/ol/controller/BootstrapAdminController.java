package org.ol.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.ol.dao.UsuarioDAO;
import org.ol.dao.impl.UsuarioDAOImpl;
import org.ol.model.Usuario;
import org.ol.system.Main;
import org.ol.util.PasswordHasher;

public class BootstrapAdminController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override public void initialize(URL location, ResourceBundle resources) { messageLabel.setText(""); }

    @FXML private void createAdmin() {
        char[] password = passwordField.getText().toCharArray();
        char[] confirmation = confirmPasswordField.getText().toCharArray();
        try {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            if (username.isBlank() || email.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                messageLabel.setText("Completa todos los campos."); return;
            }
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                messageLabel.setText("Escribe un correo electrónico válido."); return;
            }
            if (password.length < 8) { messageLabel.setText("La contraseña debe tener al menos 8 caracteres."); return; }
            if (!Arrays.equals(password, confirmation)) { messageLabel.setText("Las contraseñas no coinciden."); return; }
            if (!usuarioDAO.listarTodos().isEmpty()) {
                messageLabel.setText("Ya existe una cuenta. Vuelve al login para continuar."); return;
            }
            Usuario admin = new Usuario(username, email, firstName, lastName, PasswordHasher.hash(password), "administrador");
            admin.setActivo(true);
            if (usuarioDAO.crear(admin)) Main.show("LoginView.fxml", 1100, 720);
            else messageLabel.setText("No se pudo crear la cuenta. Comprueba los datos.");
        } catch (RuntimeException | IOException e) {
            messageLabel.setText("No se pudo configurar el administrador. Revisa la base de datos.");
        } finally {
            Arrays.fill(password, '\0'); Arrays.fill(confirmation, '\0');
        }
    }

    @FXML private void back() {
        try { Main.show("LoginView.fxml", 1100, 720); }
        catch (IOException e) { throw new IllegalStateException("No se pudo abrir el login", e); }
    }
}
