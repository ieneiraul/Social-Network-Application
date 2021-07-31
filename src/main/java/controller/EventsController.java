package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Utilizator;
import socialnetwork.service.*;
import utils.events.EventChangeEvent;
import utils.events.PrietenieChangeEvent;
import utils.observers.Observer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EventsController implements Observer<EventChangeEvent> {
    UtilizatorService service;
    CerereDePrietenieService cerereDePrietenieService;
    PrietenieService prietenieService;
    MessageService mesajService;
    EvenimenteService evenimenteService;
    ObservableList<Eveniment> model = FXCollections.observableArrayList();
    Long idUtilizatorCurent;
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
    TextField numeTextField;
    @FXML
    TextArea descriereTextArea;
    @FXML
    DatePicker dataDatePicker;
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
    Button joinEventButton;
    @FXML
    Button createButton;
    @FXML
    Button backButton;
    @FXML
    Button nextButton;
    @FXML
    Label pageNumberLabel;


    public void setServices(UtilizatorService service, CerereDePrietenieService cerereDePrietenieService,
                            PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, Long id) {
        this.service = service;
        this.prietenieService=prietenieService;
        this.cerereDePrietenieService=cerereDePrietenieService;
        this.mesajService=mesajService;
        this.evenimenteService=evenimenteService;
        this.idUtilizatorCurent=id;
        this.eventsButton.setDisable(true);
        initPaging();
        //initModel();
        initFirstEventsPage();

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
        nextButtonState=(offsetSetat+limitSetat)<evenimenteService.nrE();
        nextButton.setDisable(!nextButtonState);
    }
    public void initFirstEventsPage() {
        int total=evenimenteService.nrE();
        if (limit+ offset >= total)
        {

            //if (limit == total) limit = total-offset;
            //else limit = (total - offset) + 1;
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
        Iterable<Eveniment> eveniments = evenimenteService.getEventsByOffset(id, offset, limit);
        List<Eveniment> evenimentList = StreamSupport.stream(eveniments.spliterator(), false)
                .sorted((e1,e2) -> e1.getData().compareTo(e2.getData()))
                .collect(Collectors.toList());
        return  evenimentList;
    }



    @Override
    public void update(EventChangeEvent eventChangeEvent) {
        initPaging();
        initFirstEventsPage();
    }

    public void handleHomeButton(ActionEvent actionEvent) {
        goToHomePage();
        Stage stage = (Stage) homeButton.getScene().getWindow();
        stage.close();
    }
    public HomeController goToHomePage() {
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

            HomeController messageTaskController = loader.getController();
            messageTaskController.setServices(service,cerereDePrietenieService,
                    prietenieService, mesajService,evenimenteService, idUtilizatorCurent);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            //initUserFriends();
            return messageTaskController;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
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
            int total=evenimenteService.nrE();
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

    public void handleCreateButton(ActionEvent actionEvent) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String data = dataDatePicker.getEditor().getText();
            List<String> attr = Arrays.asList(data.split("/"));
            String data1 = "";
            if (attr.get(1).length() == 1 || attr.get(0).length()==1) {
                for (String s : attr) {
                    if (s.equals(attr.get(1))&&attr.get(1).length() == 1) {
                        data1 = data1 + "0" + s + "/";
                    }
                    else if(s.equals(attr.get(0))&&attr.get(0).length() == 1){
                        data1 = data1 + "0" + s + "/";
                    }
                    else {
                        data1 = data1 + s + "/";
                    }
                }
                data1 = data1.substring(0, 10);
            } else data1 = data;
            LocalDateTime d = LocalDate.parse(data1, formatter).atStartOfDay();
            String nume=numeTextField.getText();
            List<Long> to= new ArrayList<>();
            to.add(idUtilizatorCurent);
            String descriere= descriereTextArea.getText();
            evenimenteService.addEvent(idUtilizatorCurent,to,nume,descriere,d);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Create event","Ai creat cu succes acest eveniment!");
            descriereTextArea.clear();
            numeTextField.clear();
            initPaging();
            initFirstEventsPage();

        } catch (Exception e) {
            MessageAlert.showErrorMessage(null,"Completati toate datele pentru a crea un eveniment");
        }
    }

    public void handleJoinEventButton(ActionEvent actionEvent) {
        try {
            if(tableView.getSelectionModel().isEmpty()){
                MessageAlert.showErrorMessage(null,"Nu ati selectat");
                return;

            }
            Eveniment e = tableView.getSelectionModel().getSelectedItem();
            joinEvent(e);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Join event","te-ai inregistrat cu succes la acest eveniment!");

        }
        catch(NullPointerException e){
            MessageAlert.showErrorMessage(null,"Nu ati selectat");
            return;
        }
    }
    public void joinEvent(Eveniment e) {
        evenimenteService.joinUserAtEvent(idUtilizatorCurent,e);
    }
}
