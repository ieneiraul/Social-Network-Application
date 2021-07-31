package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Eveniment;
import socialnetwork.service.*;
import utils.events.EventChangeEvent;
import utils.observers.Observer;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HomeController  implements Observer<EventChangeEvent> {
    UtilizatorService service;
    CerereDePrietenieService cerereDePrietenieService;
    PrietenieService prietenieService;
    MessageService mesajService;
    EvenimenteService evenimenteService;
    Long idUtilizatorCurent;
    ObservableList<Eveniment> model = FXCollections.observableArrayList();
    Boolean backButtonState;
    Boolean nextButtonState;
    Long offsetSetat;
    Long limitSetat;
    Long offset;
    Long limit;


    @FXML
    TableView<Eveniment> tableView;
    @FXML
    TableColumn<Eveniment, String> tableColumnCreat;
    @FXML
    TableColumn<Eveniment, String> tableColumnNume;
    @FXML
    TableColumn<Eveniment, String> tableColumnDescriere;
    @FXML
    TableColumn<Eveniment, LocalDateTime> tableColumnData;
    @FXML
    TableColumn<Eveniment, Long> tableColumnId;
    @FXML
    Button backButton;
    @FXML
    Button nextButton;
    @FXML
    Label pageNumberLabel;
    @FXML
    Button deleteButton;
    @FXML
    Button homeButton;
    @FXML
    Button messengerButton;
    @FXML
    Button friendsButton;
    @FXML
    Button friendRequestsButton;
    @FXML
    Button reportsButton;
    @FXML
    Button eventsButton;
    @FXML
    Button exitButton;
    @FXML
    Label helloLabel;
    @FXML
    CheckBox enableNotificationsCheckBox;



    public void setServices(UtilizatorService service, CerereDePrietenieService cerereDePrietenieService,
                            PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, Long id) {
        this.service = service;
        this.prietenieService=prietenieService;
        this.cerereDePrietenieService=cerereDePrietenieService;
        this.mesajService=mesajService;
        this.evenimenteService=evenimenteService;
        this.idUtilizatorCurent=id;
        this.homeButton.setDisable(true);
        if(service.getStatusNotificari(idUtilizatorCurent)==true){
            enableNotificationsCheckBox.setSelected(true);
        }
        else enableNotificationsCheckBox.setSelected(false);
        this.helloLabel.setText("Hello "+service.findOne(idUtilizatorCurent).getFirstName()+"!");
        initPaging();
        initFirstEventsPage();
        //initNotificari();
    }
    @FXML
    public void initialize() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnCreat.setCellValueFactory(new PropertyValueFactory<>("utilizatorCreator"));
        tableColumnNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        tableColumnDescriere.setCellValueFactory(new PropertyValueFactory<>("descriere"));
        tableColumnData.setCellValueFactory(new PropertyValueFactory<>("data"));
        tableView.setItems(model);
    }

    public void initPaging() {
        this.offset=(long)0;
        this.limit=(long)5;
        this.offsetSetat=(long)0;
        this.limitSetat=(long)5;
        backButtonState=false;
        backButton.setDisable(true);
        nextButtonState=(offsetSetat+limitSetat)<evenimenteService.nrEOfUser(idUtilizatorCurent);
        nextButton.setDisable(!nextButtonState);
    }
    public void initFirstEventsPage() {
        int total=evenimenteService.nrEOfUser(idUtilizatorCurent);
        if (limit+ offset >= total)
        {
            nextButtonState = false;
            System.out.println(limit);
        }
        System.out.println("Of:"+offset+"  lim:"+limit);
        pageNumberLabel.setText(""+(offset/limitSetat+1));
        model.setAll(
                creareListaByOffset(idUtilizatorCurent, offset, limit)
        );
    }
    public List<Eveniment> creareListaByOffset(long id, long offset, long limit) {
        List<Eveniment> aux = new ArrayList<>();
        Iterable<Eveniment> eveniments = evenimenteService.getUsersEventsByOffset(id, offset, limit);
        List<Eveniment> evenimentList = StreamSupport.stream(eveniments.spliterator(), false)
                .collect(Collectors.toList());
        return  evenimentList;
    }

    public void initNotificari() {
        if(service.getStatusNotificari(idUtilizatorCurent)==true) {
            List<Eveniment> aux = new ArrayList<>();
            Iterable<Eveniment> eveniments = evenimenteService.getEventsOfAnUser(idUtilizatorCurent);
            String rezultat = "";
            for (Eveniment e : eveniments) {
                LocalDateTime d = LocalDateTime.now();
                if (e.getData().isBefore(d)) {
                } else if (e.getData().isBefore(d.plusDays(1)))
                    rezultat = rezultat + "Evenimentul " + e.getNume() + " va incepe maine!\n";
                else if (e.getData().isBefore(d.plusDays(7)))
                    rezultat = rezultat + "Evenimentul " + e.getNume() + " va incepe in aceasta saptamana!\n";
            }
            if (!rezultat.isEmpty())
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Participation event", rezultat);
        }
    }

    public void handleHomeButton(ActionEvent actionEvent) {

    }

    public void handleMessengerButton(ActionEvent actionEvent) {
        goToMessengerPage();
        Stage stage = (Stage) messengerButton.getScene().getWindow();
        stage.close();
    }
    public AfisareMesajeController goToMessengerPage() {
        if(idUtilizatorCurent.equals(null)){
            MessageAlert.showErrorMessage(null,"Nu ati selectat un user.Treceti un id in casuta id.");
            return null;}

        Long id = idUtilizatorCurent;

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/MessengerView.fxml"));

            BorderPane root = (BorderPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Messenger");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            AfisareMesajeController afisare = loader.getController();
            afisare.setService(mesajService, service, prietenieService,cerereDePrietenieService,evenimenteService, dialogStage, id);

            dialogStage.show();
            //handleSearchUser(actionEvent);
            return afisare;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleFriendsButton(ActionEvent actionEvent) {
        goToFriendsPage();
        Stage stage = (Stage) friendsButton.getScene().getWindow();
        stage.close();
    }

    public FriendPageController goToFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/mix2view.fxml"));
            BorderPane root = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Register");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            FriendPageController friendPageController = loader.getController();
            friendPageController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService,evenimenteService, idUtilizatorCurent);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return friendPageController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleFriendRequestsButton(ActionEvent actionEvent) {
        goToFriendRequestsPage();
        Stage stage = (Stage) friendRequestsButton.getScene().getWindow();
        stage.close();
    }
    public AfisareCereriController goToFriendRequestsPage() {
        if(idUtilizatorCurent.equals(null)){
            MessageAlert.showErrorMessage(null,"Nu ati selectat un user.Treceti un id in casuta id.");
            return null;}

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/afisareCereri.fxml"));
            Long id= idUtilizatorCurent;
            BorderPane root = (BorderPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Cererile de prietenie ale userului "+service.findOne(id).getFirstName()+" "+service.findOne(id).getLastName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            AfisareCereriController afisare = loader.getController();
            afisare.setService(service,cerereDePrietenieService,prietenieService,mesajService,evenimenteService, dialogStage, id);

            dialogStage.show();
            //handleSearchUser(actionEvent);
            return afisare;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleReportsButton(ActionEvent actionEvent) {
        goToReportsPage();
        Stage stage = (Stage) reportsButton.getScene().getWindow();
        stage.close();
    }
    public ReportsPageController goToReportsPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/reportsPage.fxml"));
            BorderPane root = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("SocialNetwork");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            ReportsPageController reportsPageController = loader.getController();
            reportsPageController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService,evenimenteService, idUtilizatorCurent);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return reportsPageController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleEventsButton(ActionEvent actionEvent) {
        goToEventsPage();
        Stage stage = (Stage) reportsButton.getScene().getWindow();
        stage.close();
    }

    public EventsController goToEventsPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/eventsPage.fxml"));
            BorderPane root = loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("SocialNetwork");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            EventsController eventsPageController = loader.getController();
            eventsPageController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService,evenimenteService, idUtilizatorCurent);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return eventsPageController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleExitButton(ActionEvent actionEvent) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    public void handleBackButton(ActionEvent actionEvent) {
        if(backButtonState)
        {
            offset-=limitSetat;
            limit=limitSetat;
            System.out.println("intra Of:"+offset+"  lim:"+limit);
            model.setAll(
                    creareListaByOffset(idUtilizatorCurent, offset, limit)
            );
            nextButtonState=true;
            nextButton.setDisable(!nextButtonState);
            backButtonState=(offset>0);
            backButton.setDisable(!backButtonState);
            pageNumberLabel.setText(""+(offset/limitSetat+1));
        }
    }

    public void handleNextButton(ActionEvent actionEvent) {
        if (nextButtonState)
        {
            offset+=limitSetat;
            int total=evenimenteService.nrEOfUser(idUtilizatorCurent);
            if (limit+ offset >= total)
            {
                //if (limit == total) limit = total-offset;
                //else limit = (total - offset) + 1;
                nextButtonState = false;
                nextButton.setDisable(!nextButtonState);
            }
            pageNumberLabel.setText(""+(offset/limitSetat+1));
            System.out.println("intra Of:"+offset+"  lim:"+limit);
            model.setAll(
                    creareListaByOffset(idUtilizatorCurent, offset, limit)
            );
            System.out.println("iese Of:"+offset+"  lim:"+limit);
            backButtonState=true;
            backButton.setDisable(!backButtonState);
        }
    }

    public void handleDeleteButton(ActionEvent actionEvent) {
        try {
            if(tableView.getSelectionModel().isEmpty()){
                MessageAlert.showErrorMessage(null,"Nu ati selectat un user");
                return;

            }
            Eveniment e = tableView.getSelectionModel().getSelectedItem();
            evenimenteService.deleteUserFromEvent(idUtilizatorCurent,e.getId());
            initPaging();
            initFirstEventsPage();
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Delete participation event","Ai sters cu succes participarea la eveniment!");

        }
        catch(NullPointerException e){
            MessageAlert.showErrorMessage(null,"Nu ati selectat");
            return;
        }
    }

    @Override
    public void update(EventChangeEvent eventChangeEvent) {
        initPaging();
        initFirstEventsPage();
    }


    public void handleEnableNotificationsCheckBox(ActionEvent actionEvent) {
        boolean statusNou = enableNotificationsCheckBox.isSelected();
        if(statusNou==true) {
            service.setStatusNotificari(idUtilizatorCurent, (long) 1);
        }
        else {
            service.setStatusNotificari(idUtilizatorCurent, (long) 0);
        }
    }
}
