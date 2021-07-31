package controller;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.PrietenieService;
import socialnetwork.service.UtilizatorService;


import java.time.LocalDateTime;


public class EditPrietenieController {
    @FXML
    private TextField textFieldU1;
    @FXML
    private TextField textFieldU2;
    @FXML
    private TextField textFieldData;

    @FXML
    private DatePicker datePickerDate;

    private PrietenieService service;
    Stage dialogStage;
    Prietenie message;

    @FXML
    private void initialize() {
    }


    public void setService(PrietenieService service, Stage stage, Prietenie m) {
        this.service = service;
        this.dialogStage=stage;
        this.message=m;
        if (null != m) {
            setFields(m);
            textFieldU1.setEditable(false);
        }
    }

    @FXML
    public void handleSave(){
        Long id1= Long.valueOf(textFieldU1.getText());
        Long id2= Long.valueOf(textFieldU2.getText());
        LocalDateTime date= LocalDateTime.parse(textFieldData.getText());

        Prietenie m=new Prietenie();
        m.setDate(date);
        if (null == this.message)
            saveMessage(m);

    }

    private void updateMessage(Prietenie m)
    {

    }


    private void saveMessage(Prietenie m)
    {

        this.service.addPrietenie(m.getId().getLeft(),m.getId().getRight());

        dialogStage.close();

    }

    private void clearFields() {
        textFieldU1.setText("");
        textFieldU2.setText("");
        textFieldData.setText("");

    }
    private void setFields(Prietenie s)
    {
        textFieldU1.setText(String.valueOf(s.getId().getLeft()));
        textFieldU2.setText(String.valueOf(s.getId().getRight()));
        textFieldData.setText(String.valueOf(s.getDate()));


    }

    @FXML
    public void handleCancel(){
        dialogStage.close();
    }
}
