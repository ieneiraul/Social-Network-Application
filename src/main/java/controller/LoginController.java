package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Utilizator;
import socialnetwork.service.*;

import java.io.IOException;

public class LoginController {
    UtilizatorService service;
    CerereDePrietenieService cerereDePrietenieService;
    PrietenieService prietenieService;
    MessageService mesajService;
    EvenimenteService evenimenteService;

    @FXML
    TextField usernameTextField;
    @FXML
    PasswordField enterPasswordField;
    @FXML
    Button loginButton;
    @FXML
    Button registerButton;
    @FXML
    Button exitButton;
    @FXML
    Label tryAgainLabel;

    public void setServices(UtilizatorService service, CerereDePrietenieService cerereDePrietenieService,
                            PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService) {
        this.service = service;
        this.prietenieService=prietenieService;
        this.cerereDePrietenieService=cerereDePrietenieService;
        this.mesajService=mesajService;
        this.evenimenteService=evenimenteService;
    }

    public void exitApplication(ActionEvent actionEvent) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    public Utilizator validateLogin() {
        if(usernameTextField.getText().isEmpty() && enterPasswordField.getText().isEmpty()) {
            //System.out.println(usernameTextField.getText());
            //System.out.println(enterPasswordField.getText());
            return null;
        }
        String username=usernameTextField.getText();
        String password=enterPasswordField.getText();
        Utilizator u=service.loginUtilizator(username, password);
        return u;
    }

    public void loginUser(ActionEvent actionEvent) {
        Utilizator utilizatorValidat= validateLogin();
        if(utilizatorValidat==null) {
            tryAgainLabel.setText("Invalid Login. Please try again.");
        }
        else {
            tryAgainLabel.setText("succes.");
            //showMessenger(utilizatorValidat.getId());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
            showMenu(utilizatorValidat);
        }
    }
    public HomeController showMenu(Utilizator utilizator) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/homePage.fxml"));
            BorderPane root = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("SocialNetwork");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.show();
            HomeController messageTaskController = loader.getController();
            messageTaskController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService,evenimenteService, utilizator.getId());
            messageTaskController.initNotificari();
            //dialogStage.show();
            //handleSearchUser(actionEvent);
            return messageTaskController;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void registerUser(ActionEvent actionEvent) {
        goToRegisterPage();
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();
    }

    public RegisterController goToRegisterPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/register.fxml"));
            AnchorPane root = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Register");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            RegisterController registerController = loader.getController();
            registerController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService, evenimenteService);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return registerController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

}
