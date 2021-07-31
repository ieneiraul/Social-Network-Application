package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Utilizator;
import socialnetwork.service.UtilizatorService;
import utils.events.UtilizatorChangeEvent;
import utils.observers.Observer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UtilizatorController implements Observer<UtilizatorChangeEvent> {
    UtilizatorService service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();


    @FXML
    TableView<Utilizator> tableView;
    @FXML
    TableColumn<Utilizator,String> tableColumnNume;
    @FXML
    TableColumn<Utilizator,String> tableColumnPrenume;


    public void setMessageTaskService(UtilizatorService messageTaskService) {
        service = messageTaskService;
        service.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        // TODO
        tableColumnNume.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        tableColumnPrenume.setCellValueFactory(new PropertyValueFactory<>("FirstName"));

        tableView.setItems(model);
    }

    private void initModel() {
        // TODO
        Iterable<Utilizator> messages = service.getAll();
        List<Utilizator> messageTaskList = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(messageTaskList);
    }

    @Override
    public void update(UtilizatorChangeEvent messageTaskChangeEvent) {
        initModel();
    }

    public void handleDeleteMessage(ActionEvent actionEvent) {
        //sa apara mesaj daca s-a sters cu succes !!
        Utilizator m= tableView.getSelectionModel().getSelectedItem();
        if(m!=null)
            service.removeUtilizator(m.getId());
        else
            MessageAlert.showErrorMessage(null,"Nu ati selectat.");
    }


    @FXML
    public void handleUpdateMessage(ActionEvent ev) {
        Utilizator m= tableView.getSelectionModel().getSelectedItem();
        showMessageTaskEditDialog(m);

    }

    @FXML
    public void handleAddMessage(ActionEvent ev) {

        showMessageTaskEditDialog(null);


    }

    public void showMessageTaskEditDialog(Utilizator messageTask) {
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/editUtilizatorView.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Message");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            EditUtilizatorController editMessageViewController = loader.getController();
            editMessageViewController.setService(service, dialogStage, messageTask);

            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
