package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.CerereDePrietenie;
import socialnetwork.domain.Utilizator;
import socialnetwork.service.*;
import utils.events.CerereDePrietenieChangeEvent;
import utils.events.PrietenieChangeEvent;
import utils.events.UtilizatorChangeEvent;
import utils.observers.Observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AfisareCereriController implements Observer<CerereDePrietenieChangeEvent> {
    UtilizatorService service;
    CerereDePrietenieService cerereDePrietenieService;
    PrietenieService prietenieService;
    MessageService mesajService;
    EvenimenteService evenimenteService;
    ObservableList<CerereDePrietenie> model = FXCollections.observableArrayList();
    Long id;
    Boolean backButtonState;
    Boolean nextButtonState;
    Long offsetSetat;
    Long limitSetat;
    Long offset;
    Long limit;

    Stage dialogStage;
    @FXML
    TableView<CerereDePrietenie> tableView;
    @FXML
    TableColumn<CerereDePrietenie,Long> tableColumnId;
    @FXML
    TableColumn<CerereDePrietenie,Utilizator> tableColumnFrom;
    @FXML
    TableColumn<CerereDePrietenie,Utilizator> tableColumnTo;
    @FXML
    TableColumn<CerereDePrietenie,String> tableColumnStatus;
    @FXML
    TableColumn<CerereDePrietenie,String> tableColumnData;
    @FXML
    Button approveButton;
    @FXML
    Button rejectButton;
    @FXML
    Button deleteRequestButton;
    @FXML
    Button nextButton;
    @FXML
    Button backButton;
    @FXML
    Label pageNumberLabel;
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




    public void setService(UtilizatorService service,CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService messageService, EvenimenteService evenimenteService,Stage stage, Long id) {
        this.service=service;
        this.cerereDePrietenieService = cerereDePrietenieService;
        this.prietenieService=prietenieService;
        this.mesajService=messageService;
        this.evenimenteService=evenimenteService;
        cerereDePrietenieService.addObserver(this);
        this.id=id;
        initModel();
        this.dialogStage=stage;
        friendRequestsButton.setDisable(true);
        initPaging();
        initFirstRequestsPage();
        approveButton.setDisable(true);
        deleteRequestButton.setDisable(true);
        rejectButton.setDisable(true);
    }

    @FXML
    public void initialize() {
        // TODO
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tableColumnFrom.setCellValueFactory(new PropertyValueFactory<>("NameTrimite"));
        tableColumnTo.setCellValueFactory(new PropertyValueFactory<>("NamePrimeste"));
        tableColumnStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
        tableColumnData.setCellValueFactory(new PropertyValueFactory<>("Data"));
        tableView.setItems(model);

    }

    private void initModel() {
        Iterable<CerereDePrietenie> cereri = cerereDePrietenieService.getCereri();

        List<CerereDePrietenie> list  = StreamSupport.stream(cereri.spliterator(), false)
                .filter(x->x.getTrimite().getId()==id || x.getPrimeste().getId()==id)
                .collect(Collectors.toList());
        model.setAll(list);
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                if(tableView.getSelectionModel().getSelectedItem().getStatus().equals("approved") || tableView.getSelectionModel().getSelectedItem().getStatus().equals("rejected")){
                    approveButton.setDisable(true);
                    deleteRequestButton.setDisable(true);
                    rejectButton.setDisable(true);
                }
                System.out.println(tableView.getSelectionModel().getSelectedItem().getTrimite().getId()==id);
                if(tableView.getSelectionModel().getSelectedItem().getTrimite().getId()==id){
                    if(tableView.getSelectionModel().getSelectedItem().getStatus().equals("pending")){
                        approveButton.setDisable(true);
                        deleteRequestButton.setDisable(false);
                        rejectButton.setDisable(true);
                    }
                }
                else{
                    if(tableView.getSelectionModel().getSelectedItem().getStatus().equals("pending")){
                        approveButton.setDisable(false);
                        deleteRequestButton.setDisable(true);
                        rejectButton.setDisable(false);
                    }
                }
            }
        });
    }

    public void initPaging() {
        this.offset=(long)0;
        this.limit=(long)5;
        this.offsetSetat=(long)0;
        this.limitSetat=(long)5;
        backButtonState=false;
        backButton.setDisable(true);
        nextButtonState=(offsetSetat+limitSetat)<cerereDePrietenieService.nrEOfUser(id);
        nextButton.setDisable(!nextButtonState);
    }

    public void initFirstRequestsPage() {
        int total=cerereDePrietenieService.nrEOfUser(id);
        if (limit+ offset >= total)
        {

            //if (limit == total) limit = total-offset;
            //else limit = (total - offset) + 1;
            nextButtonState = false;
        }
        System.out.println("Of:"+offset+"  lim:"+limit);
        pageNumberLabel.setText(""+(offset/limitSetat+1));
        model.setAll(creareListaByOffset(id,offset,limit));
    }



    public List<CerereDePrietenie> creareListaByOffset(long id, long offset, long limit) {
        List<CerereDePrietenie> aux = new ArrayList<>();
        System.out.println(1);
        Iterable<CerereDePrietenie> requests = cerereDePrietenieService.getRequestsFromOffset(id, offset, limit);
        List<CerereDePrietenie> requestList = StreamSupport.stream(requests.spliterator(), false)
                .collect(Collectors.toList());
        return  requestList;
    }


    public void stergeCererea(){
        try {
            CerereDePrietenie pr = tableView.getSelectionModel().getSelectedItem();
            if (pr == null) {
                MessageAlert.showErrorMessage(null, "Nu ati selectat.");
                return;
            }
            System.out.println(pr);
            if(pr.getTrimite().getId()!=id)
            {
                MessageAlert.showErrorMessage(null,"Cererea nu poate fi stearsa.");
                return;
            }
            if(!pr.getStatus().equals("pending")){
                MessageAlert.showErrorMessage(null,"Cererea nu poate fi stearsa.");
                return;
            }

            cerereDePrietenieService.stergeCerere(pr.getId());
        }
        catch(NullPointerException e){
            MessageAlert.showErrorMessage(null,"Nu ati selectat.");
        }
    }

    public void handleBackButton(ActionEvent actionEvent) {
        if(backButtonState)
        {
            offset-=limitSetat;
            limit=limitSetat;
            System.out.println("intra Of:"+offset+"  lim:"+limit);
            model.setAll(
                    creareListaByOffset(id, offset, limit)
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
            int total=cerereDePrietenieService.nrEOfUser(id);
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
                    creareListaByOffset(id, offset, limit)
            );
            System.out.println("iese Of:"+offset+"  lim:"+limit);
            backButtonState=true;
            backButton.setDisable(!backButtonState);
        }
    }


    @Override
    public void update(CerereDePrietenieChangeEvent cerereDePrietenieChangeEvent) {

        initPaging();
        initFirstRequestsPage();
    }

    public void rejectRequest(){
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(()-> {
            try {
                CerereDePrietenie pr = tableView.getSelectionModel().getSelectedItem();
                if (pr == null) {
                    MessageAlert.showErrorMessage(null, "Nu ati selectat.");
                    return;
                }
                System.out.println(pr);
                if (!pr.getStatus().equals("pending")) {
                    MessageAlert.showErrorMessage(null, "Cererea nu poate fi aprobata/respinsa.");
                    return;
                }
                if (pr.getPrimeste().getId().equals(id)) {

                    cerereDePrietenieService.modificaCerere(pr.getId(), "rejected");

                }
                else {
                    MessageAlert.showErrorMessage(null, "Nu puteti accepta/refuza cererea de prietenie pe care ati trimis-o.");
                    return;
                }


            } catch (NullPointerException e) {
                MessageAlert.showErrorMessage(null,"Nu ati selectat.");


            }
            initModel();
        });

        executorService.shutdown();


    }

    public void approveRequest(ActionEvent actionEvent) {

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.execute(()-> {
            try {
                CerereDePrietenie pr = tableView.getSelectionModel().getSelectedItem();
                if (pr == null) {
                    MessageAlert.showErrorMessage(null, "Nu ati selectat.");
                    return;
                }
                System.out.println(pr);
                if (!pr.getStatus().equals("pending")) {
                    MessageAlert.showErrorMessage(null, "Cererea nu poate fi aprobata/respinsa.");
                    return;
                }
                if (pr.getPrimeste().getId().equals(id)) {

                    cerereDePrietenieService.modificaCerere(pr.getId(), "approved");

                }
                else {
                    MessageAlert.showErrorMessage(null, "Nu puteti accepta/refuza cererea de prietenie pe care ati trimis-o.");
                    return;
                }
                //choiceBox.getSelectionModel().clearSelection();

            } catch (NullPointerException e) {
                //MessageAlert.showErrorMessage(null,"Nu ati selectat.");
                //choiceBox.getSelectionModel().clearSelection();

            }
            initModel();
        });

        executorService.shutdown();
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
                    prietenieService, mesajService, evenimenteService, id);
            dialogStage.show();
            //handleSearchUser(actionEvent);
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
        if(id.equals(null)){
            MessageAlert.showErrorMessage(null,"Nu ati selectat un user.Treceti un id in casuta id.");
            return null;}

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
            afisare.setService(mesajService, service, prietenieService,cerereDePrietenieService, evenimenteService, dialogStage, id);

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
                    prietenieService, mesajService, evenimenteService, id);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return friendPageController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleFriendRequestsButton(ActionEvent actionEvent) { }


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
                    prietenieService, mesajService, evenimenteService, id);
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
                    prietenieService, mesajService,evenimenteService, id);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return eventsPageController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

}
