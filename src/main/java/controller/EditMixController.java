package controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.UtilizatorService;


import java.time.LocalDateTime;


public class EditMixController {
    @FXML
    private TextField textFieldNume;
    @FXML
    private TextField textFieldPrenume;


    private UtilizatorService service;
    Stage dialogStage;
    Utilizator message;

    @FXML
    private void initialize() {
    }


    public void setService(UtilizatorService service,  Stage stage, Utilizator m) {
        this.service = service;
        this.dialogStage=stage;
        this.message=m;
        if (null != m) {
            setFields(m);
            textFieldNume.setEditable(false);
        }
    }

    @FXML
    public void handleSave(){
        String nume=textFieldNume.getText();
        String prenume=textFieldPrenume.getText();

        Utilizator m=new Utilizator(nume,prenume);
        if (null == this.message)
            saveMessage(m);
        else
            updateMessage(m);
    }

    private void updateMessage(Utilizator m)
    {
        try {
            Utilizator r= this.service.updateUtilizator(m.getId(),m);
            if (r==null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Modificare mesaj","Mesajul a fost modificat");
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null,e.getMessage());
        }
        dialogStage.close();
    }


    private void saveMessage(Utilizator m)
    {
        try {
            Utilizator r= this.service.addUtilizator(m);
            if (r==null)
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Adaugare mesaj","Mesajul a fost adaugat");
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null,e.getMessage());
        }
        dialogStage.close();

    }

    private void clearFields() {
        textFieldNume.setText("");
        textFieldPrenume.setText("");

    }
    private void setFields(Utilizator s)
    {
        textFieldNume.setText(s.getFirstName());
        textFieldPrenume.setText(s.getLastName());

    }

    @FXML
    public void handleCancel(){
        dialogStage.close();
    }

}
