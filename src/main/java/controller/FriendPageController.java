package controller;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;


import socialnetwork.domain.*;
import socialnetwork.domain.validators.ServiceException;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.*;
import utils.events.PrietenieChangeEvent;
import utils.events.UtilizatorChangeEvent;
import utils.observers.Observer;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendPageController implements Observer<PrietenieChangeEvent> {
    UtilizatorService service;
    CerereDePrietenieService cerereDePrietenieService;
    PrietenieService prietenieService;
    MessageService mesajService;
    EvenimenteService evenimenteService;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();
    Long idUtilizatorCurent;
    Boolean backButtonState;
    Boolean nextButtonState;
    Long offsetSetat;
    Long limitSetat;
    Long offset;
    Long limit;

    @FXML
    TableView<Utilizator> tableView;
    @FXML
    TableColumn<Utilizator, String> tableColumnNume;
    @FXML
    TableColumn<Utilizator, String> tableColumnPrenume;
    @FXML
    TableColumn<Utilizator, Long> tableColumnId;
    @FXML
    TextField TextFieldNumeU;
    @FXML
    TextField TextFieldPrenumeU;
    @FXML
    Button addFriendButton;
    @FXML
    Button deleteFriendButton;
    @FXML
    Button backButton;
    @FXML
    Button nextButton;
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


    public void setServices(UtilizatorService service, CerereDePrietenieService cerereDePrietenieService,
                            PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, Long id) {
        this.service = service;
        this.prietenieService=prietenieService;
        this.cerereDePrietenieService=cerereDePrietenieService;
        this.mesajService=mesajService;
        this.evenimenteService=evenimenteService;
        this.idUtilizatorCurent=id;
        prietenieService.addObserver(this);
        friendsButton.setDisable(true);
        addFriendButton.setDisable(true);
        deleteFriendButton.setDisable(true);
        initPaging();
        initFirstFriendsPage();
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                Long idInserat=tableView.getSelectionModel().getSelectedItem().getId();
                int ok=0;
                for (Long nr:service.getFriend2(idUtilizatorCurent)) {
                    if(nr==idInserat) ok=1;
                }
                if(ok==1){
                    addFriendButton.setDisable(true);
                    deleteFriendButton.setDisable(false);
                }
                else{
                    deleteFriendButton.setDisable(true);
                    addFriendButton.setDisable(false);
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
        nextButtonState=(offsetSetat+limitSetat)<service.getFriend2(idUtilizatorCurent).size();
        nextButton.setDisable(!nextButtonState);
    }

    @FXML
    public void initialize() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tableColumnNume.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        tableColumnPrenume.setCellValueFactory(new PropertyValueFactory<>("FirstName"));

        tableView.setItems(model);
    }

    public void initUserFriends() {
        model.setAll(
                creareListaByOffset(idUtilizatorCurent,offset,limit)
        );
    }

    private void initModel() {
        Iterable<Utilizator> users = service.getAll();
        List<Utilizator> userList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(userList);
    }

    @Override
    public void update(PrietenieChangeEvent task) {
        initModel();
    }

    public void handleDeleteMessage(ActionEvent actionEvent) {
        //sa apara mesaj daca s-a sters cu succes !!
        Utilizator m = tableView.getSelectionModel().getSelectedItem();
        if (m != null)
            service.removeUtilizator(m.getId());
        else
            MessageAlert.showErrorMessage(null, "Nu ati selectat.");
    }

    public void initFirstFriendsPage() {
        int total=service.getFriend2(idUtilizatorCurent).size();
        if (limit + offset >= total)
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
        //offset+=limitSetat;
    }


    @FXML
    public void handleUpdateMessage(ActionEvent ev) {
        Utilizator m = tableView.getSelectionModel().getSelectedItem();
        DialogNouLaAdaugare(m);

    }

    @FXML
    public void handleAddMessage(ActionEvent ev) {

        DialogNouLaAdaugare(null);


    }

    public void DialogNouLaAdaugare(Utilizator user) {
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/editMixView.fxml"));

            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Message");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            EditUtilizatorController editController = loader.getController();
            editController.setService(service, dialogStage, user);

            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Utilizator> creareListaByOffset(long id, long offset, long limit) {
        List<Utilizator> aux = new ArrayList<>();
        List<Long> l = service.getFriend2ByOffset(id, offset, limit);
        for (Long e : l) {
            Utilizator u = service.findOne(e);
            aux.add(u);
        }
        return aux;
    }


    /**
     * Creeaza o lista de utilizatori, care sunt prietenii userului dat de id-ul parametru
     * @param id ->un id de user existent
     * @return
     */
    public List<Utilizator> creareLista(long id) {

        List<Utilizator> aux = new ArrayList<>();
        List<Long> l = service.getFriend2(id);
        for (Long e : l) {
            Utilizator u = service.findOne(e);
            aux.add(u);
        }
        return aux;

    }


    /**
     * Returneaza o lista cu toti Utilizatorii
     * @return
     */
    public List<Utilizator> creareListaUsers() {

        List<Utilizator> aux = new ArrayList<>();
        service.getAll().forEach(aux::add);
        return aux;

    }

    /**
     * La cautarea unui user, se vor afisa in tableview toti prietenii lui
     * @param ev
     */
    @FXML
    public void handleSearchUser(ActionEvent ev) {

      //  System.out.println(id);
        Utilizator u = service.findOne(idUtilizatorCurent);
        if (u == null)
            MessageAlert.showErrorMessage(null, "Nu am gasit userul");
        model.setAll(
                creareLista(u.getId())
        );
    }

    /**
     * Cauta toti utilizatorii ce au numele si prenumele dat
     * @param keyEvent
     */
    public void searchName(KeyEvent keyEvent) {
        String nume = TextFieldNumeU.getText();
        String prenume = TextFieldPrenumeU.getText();
        model.setAll(creareListaUsers().stream()
                .filter(x -> x.getLastName().startsWith(nume))
                .filter(x -> x.getFirstName().startsWith(prenume))
                .collect(Collectors.toList())
        );
    }

    /**
     * Trimite o cerere de prietenie
     * @param actionEvent
     */
    public void handleAddCerere(ActionEvent actionEvent) {
        Utilizator m = tableView.getSelectionModel().getSelectedItem();
        if(idUtilizatorCurent.equals(null)){
            MessageAlert.showErrorMessage(null,"Nu ati selectat un user.");
            return;}

        Long id = idUtilizatorCurent;
        try {
            cerereDePrietenieService.addCerere(id, m.getId());
        }
        catch(ServiceException e){
            MessageAlert.showErrorMessage(null,"Ati trimis deja o cerere acestui utilizator");
        }
        catch (ValidationException e){
            MessageAlert.showErrorMessage(null,e.toString());
        }
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Add friend","ai trimis cererea de prietenie cu succes!");

    }

    /**
     * Sterge prietenul selectat de user
     * @param actionEvent
     */
    public void handleDeleteFriend(ActionEvent actionEvent) {
        Utilizator m = tableView.getSelectionModel().getSelectedItem();
        if(idUtilizatorCurent.equals(null)){
            MessageAlert.showErrorMessage(null,"Nu ati selectat un user.");
            return;}


        Long id =idUtilizatorCurent;
        Prietenie p= prietenieService.findOne(m.getId(),id);
        if(p==null){
            MessageAlert.showErrorMessage(null, "Nu exista prietenia");
            return;
        }
        try {
            prietenieService.stergePrietenie(p.getId().getLeft(), p.getId().getRight());
        }
        catch(ServiceException e){
            MessageAlert.showErrorMessage(null, e.toString());
        }
        handleSearchUser(actionEvent);
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Delete friendship","ai sters cu succes prietenia!");

    }

    public void handleBackButton(ActionEvent actionEvent) {
        if(backButtonState)
        {
            //if(!nextButtonState) offset-=limitSetat;
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
            //if(!backButtonState) offset+=limitSetat;
        }
    }

    public void handleNextButton(ActionEvent actionEvent) {
        if (nextButtonState)
        {
            offset+=limitSetat;
            int total = service.getFriend2(idUtilizatorCurent).size();
            if (limit + offset >= total)
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
            //offset+=limitSetat;
            //if (limit + offset >= total) offset-=2;
            System.out.println("iese Of:"+offset+"  lim:"+limit);
            backButtonState=true;
            backButton.setDisable(!backButtonState);

        }
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
            initUserFriends();
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
            initUserFriends();
            return afisare;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleFriendsButton(ActionEvent actionEvent) {
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

        Long id = idUtilizatorCurent;

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/afisareCereri.fxml"));

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
            initUserFriends();
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
}

