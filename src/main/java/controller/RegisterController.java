package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.service.*;

import java.awt.*;
import java.io.IOException;

public class RegisterController {
    UtilizatorService service;
    CerereDePrietenieService cerereDePrietenieService;
    PrietenieService prietenieService;
    MessageService mesajService;
    EvenimenteService evenimenteService;

    @FXML
    javafx.scene.control.TextField firstnameTextField;
    @FXML
    javafx.scene.control.TextField lastnameTextField;
    @FXML
    javafx.scene.control.TextField usernameTextField;
    @FXML
    javafx.scene.control.PasswordField setPasswordField;
    @FXML
    javafx.scene.control.PasswordField setConfirmPasswordField;
    @FXML
    javafx.scene.control.Button registerButton;
    @FXML
    javafx.scene.control.Button backButton;


    public void setServices(UtilizatorService service, CerereDePrietenieService cerereDePrietenieService,
                            PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService) {
        this.service = service;
        this.prietenieService=prietenieService;
        this.cerereDePrietenieService=cerereDePrietenieService;
        this.mesajService=mesajService;
        this.evenimenteService=evenimenteService;
    }

    public void backToLoginPage(ActionEvent actionEvent) {
        goToLoginPage();
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public LoginController goToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/login.fxml"));
            BorderPane root = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            LoginController loginController = loader.getController();
            loginController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService,evenimenteService);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return loginController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void registerUtilizator(ActionEvent actionEvent) {
        String firstname=firstnameTextField.getText();
        String lastname=lastnameTextField.getText();
        String username=usernameTextField.getText();
        String password=setPasswordField.getText();
        String confirmPassword=setConfirmPasswordField.getText();
        if((firstname.isEmpty())||(lastname.isEmpty())||(username.isEmpty())||(password.isEmpty())||(confirmPassword.isEmpty())) {
            MessageAlert.showErrorMessage(null,"Toate campurile trebuie completate!");
        }
        else {
            if(service.existUsername(username)==true) {
                MessageAlert.showErrorMessage(null,"Numele de utilizator este folosit deja!");
            }
            else if(!password.equals(confirmPassword)){
                MessageAlert.showErrorMessage(null,"Parola nu este aceeasi cu cea confirmata!");
            }
            else {
                service.createUserFromRegister(firstname,lastname,username,password);
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Creare cont", "Contul a fost creat cu succes!");
                goToLoginPage();
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.close();
            }
        }
    }
}
