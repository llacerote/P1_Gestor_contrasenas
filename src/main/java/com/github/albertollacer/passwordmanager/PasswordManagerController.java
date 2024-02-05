package com.github.albertollacer.passwordmanager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.beans.property.ReadOnlyStringWrapper;



import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class PasswordManagerController {
    @FXML private TableView<PasswordEntry> passwordTable;
    @FXML private TableColumn<PasswordEntry, String> serviceColumn;
    @FXML private TableColumn<PasswordEntry, String> passwordColumn;
    @FXML private TableColumn<PasswordEntry, Void> toggleColumn;
    @FXML private TextField lengthInput;
    @FXML private CheckBox numbersCheck;
    @FXML private CheckBox uppercaseCheck;
    @FXML private CheckBox lowercaseCheck;
    @FXML private CheckBox specialCharsCheck;
    @FXML private TextArea passwordDisplay;
    
    private PasswordFileManager passwordFileManager = new PasswordFileManager();
    private ObservableList<PasswordEntry> passwordList = FXCollections.observableArrayList();

    @FXML
public void initialize() {
    serviceColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
    passwordColumn.setCellValueFactory(cellData -> {
        PasswordEntry entry = cellData.getValue();
        String password = entry.getEncryptedPassword();
        if (entry.isPasswordVisible()) {
            password = passwordFileManager.decrypt(password);
        }
        return new ReadOnlyStringWrapper(password);
    });

    toggleColumn.setCellFactory(col -> new TableCell<PasswordEntry, Void>() {
        private final Button toggleButton = new Button("Mostrar");

        {
            toggleButton.setOnAction(event -> {
                PasswordEntry entry = getTableView().getItems().get(getIndex());
                entry.togglePasswordVisibility();
                getTableView().refresh(); // Refresca la tabla para actualizar la visualización de la contraseña
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
            } else {
                setGraphic(toggleButton);
            }
        }
    });

    passwordTable.setItems(passwordList);
}


    @FXML
    protected void addPasswordEntry() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Añadir Contraseña");
        dialog.setHeaderText("Introduce los detalles de la nueva contraseña:");
        ButtonType addButtonType = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serviceField = new TextField();
        serviceField.setPromptText("Servicio");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        grid.add(new Label("Servicio:"), 0, 0);
        grid.add(serviceField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(serviceField.getText(), passwordFileManager.encrypt(passwordField.getText()));
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(servicePassword -> {
            passwordList.add(new PasswordEntry(servicePassword.getKey(), servicePassword.getValue()));
        });
    }

    @FXML
    protected void editPasswordEntry() {
        PasswordEntry selectedEntry = passwordTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Editar Contraseña");
            dialog.setHeaderText("Edita los detalles de la contraseña:");
            ButtonType editButtonType = new ButtonType("Editar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField serviceField = new TextField(selectedEntry.getService());
            PasswordField passwordField = new PasswordField();
            passwordField.setText(passwordFileManager.decrypt(selectedEntry.getEncryptedPassword()));

            grid.add(new Label("Servicio:"), 0, 0);
            grid.add(serviceField, 1, 0);
            grid.add(new Label("Contraseña:"), 0, 1);
            grid.add(passwordField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == editButtonType) {
                    return new Pair<>(serviceField.getText(), passwordFileManager.encrypt(passwordField.getText()));
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();
            result.ifPresent(servicePassword -> {
                selectedEntry.setService(servicePassword.getKey());
                selectedEntry.setEncryptedPassword(servicePassword.getValue());
                passwordTable.refresh();
            });
        }
    }

    @FXML
    protected void deletePasswordEntry() {
        PasswordEntry selectedEntry = passwordTable.getSelectionModel().getSelectedItem();
        if (selectedEntry != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Estás seguro de que deseas eliminar esta entrada?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                passwordList.remove(selectedEntry);
            }
        }
    }

    @FXML
    protected void generatePassword() {
        int length = Integer.parseInt(lengthInput.getText());
        boolean useNumbers = numbersCheck.isSelected();
        boolean useLetters = lowercaseCheck.isSelected();
        boolean useUppercase = uppercaseCheck.isSelected();
        boolean useSpecialChars = specialCharsCheck.isSelected();
    
       
        PasswordGenerator generator = new PasswordGenerator();
        
        String password = generator.generatePassword(length, useNumbers, useLetters || useUppercase, useUppercase, useSpecialChars);
    
       passwordDisplay.setText(password);
    }

    @FXML
    private void savePassword() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Contraseña");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                passwordFileManager.savePassword(passwordDisplay.getText(), file.getAbsolutePath());
            } catch (GeneralSecurityException | IOException ex) {
                showAlert("Error", "No se pudo guardar la contraseña: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void loadPassword() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Contraseñas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                String password = passwordFileManager.loadPassword(file.getAbsolutePath());
                passwordDisplay.setText(password);
            } catch (GeneralSecurityException | IOException ex) {
                showAlert("Error", "No se pudo cargar la contraseña: " + ex.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
