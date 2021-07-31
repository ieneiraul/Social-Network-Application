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
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.domain.*;
import socialnetwork.service.*;
import utils.events.CerereDePrietenieChangeEvent;
import utils.events.MesajChangeEvent;
import utils.observers.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AfisareMesajeController implements Observer<MesajChangeEvent> {
    MessageService messageService;
    UtilizatorService utilizatorService;
    PrietenieService prietenieService;
    CerereDePrietenieService cerereDePrietenieService;
    EvenimenteService evenimenteService;
    Long id;
    Long idPrieten;
    Boolean backButtonState;
    Boolean nextButtonState;
    Long offsetSetat;
    Long limitSetat;
    Long offset;
    Long limit;
    Boolean backMessengerButtonState;
    Boolean nextMessengerButtonState;
    Long offsetMessengerSetat;
    Long limitMessengerSetat;
    Long offsetMessenger;
    Long limitMessenger;

    ObservableList<Utilizator> model = FXCollections.observableArrayList();
    ObservableList<Message> modelMessage = FXCollections.observableArrayList();
    Stage dialogStage;

    @FXML
    TableView<Utilizator> tableViewFriends;
    @FXML
    TableView<Message> tableViewMessage;
    @FXML
    TextField textFieldNume;
    @FXML
    TextArea textFieldMesaj;
    @FXML
    TableColumn<Utilizator, String> tableColumnFriendNume;
    @FXML
    TableColumn<Utilizator, String> tableColumnFriendPrenume;
    @FXML
    TableColumn<Message, String> tableColumnMessageNume;
    @FXML
    TableColumn<Message, String> tableColumnMessageMesaj;
    @FXML
    TableColumn<Message, LocalDateTime> tableColumnMesssageData;
    @FXML
    Button showMessagesButton;
    @FXML
    Text textSomething;
    @FXML
    Label pageNumberLabel;
    @FXML
    Button backButton;
    @FXML
    Button nextButton;
    @FXML
    Button nextMessengerButton;
    @FXML
    Button backMessengerButton;
    @FXML
    Label pageNumberMessengerLabel;
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


    public void setService(MessageService service, UtilizatorService utilizatorService, PrietenieService p, CerereDePrietenieService cerereDePrietenieService, EvenimenteService evenimenteService, Stage stage, Long id) {
        this.messageService = service;
        this.utilizatorService=utilizatorService;
        this.prietenieService=p;
        this.cerereDePrietenieService=cerereDePrietenieService;
        this.evenimenteService=evenimenteService;
        service.addObserver(this);
        this.id=id;
        //initModel();
        this.dialogStage=stage;
        this.messengerButton.setDisable(true);
        this.nextMessengerButton.setDisable(true);
        this.backMessengerButton.setDisable(true);
        this.showMessagesButton.setDisable(true);
        initPaging();
        initFirstFriendsPage();
        tableViewFriends.setOnMouseClicked((MouseEvent event) -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                showMessagesButton.setDisable(false);
            }
        });
    }

    @FXML
    public void initialize() {
        tableColumnFriendNume.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        tableColumnFriendPrenume.setCellValueFactory(new PropertyValueFactory<>("FirstName"));
        tableViewFriends.setItems(model);

        tableColumnMessageNume.setCellValueFactory(new PropertyValueFactory<>("UserFrom"));
        tableColumnMessageMesaj.setCellValueFactory(new PropertyValueFactory<>("Message"));
        tableColumnMesssageData.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tableViewMessage.setItems(modelMessage);
    }

    private void initModel() {
        Iterable<Message> mesaje = messageService.getMessages();
        List<Message> list  = StreamSupport.stream(mesaje.spliterator(), false)
                .filter(x->x.getFrom().getId()==id || x.getTo().contains(id))
                .collect(Collectors.toList());
      //  modelMessage.setAll(list);

        Iterable<Utilizator> utilizators=utilizatorService.getAll();
        Iterable<Prietenie> prietenii = prietenieService.getPrieteni();
        List<Prietenie> prietenieList=StreamSupport.stream(prietenii.spliterator(), false)
                .filter(x->x.getId().getRight()==id || x.getId().getLeft()==id)
                .collect(Collectors.toList());
        List<Utilizator> list2=StreamSupport.stream(utilizators.spliterator(), false)
                .filter(x->{
                    for (Prietenie p:prietenieList) {
                        if(p.getId().getLeft()==x.getId() && x.getId()!=id) return true;
                        if(p.getId().getRight()==x.getId() && x.getId()!=id) return true;
                    }
                    return false;
                } )
                .collect(Collectors.toList());
        model.setAll(list2);
    }
    public void initPaging() {
        this.offset=(long)0;
        this.limit=(long)5;
        this.offsetSetat=(long)0;
        this.limitSetat=(long)5;
        backButtonState=false;
        backButton.setDisable(true);
        nextButtonState=(offsetSetat+limitSetat)<utilizatorService.getFriend2(id).size();
        nextButton.setDisable(!nextButtonState);
    }
    public void initPagingMessenger(long idPrieten) {
        this.offsetMessenger=(long)0;
        this.limitMessenger=(long)10;
        this.offsetMessengerSetat=(long)0;
        this.limitMessengerSetat=(long)10;
        backMessengerButtonState=false;
        backMessengerButton.setDisable(true);
        nextMessengerButtonState=(offsetMessengerSetat+limitMessengerSetat)<messageService.nrEOf2Users(id, idPrieten);
        nextMessengerButton.setDisable(!nextMessengerButtonState);
    }

    public void initFirstFriendsPage() {
        int total=utilizatorService.getFriend2(id).size();
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
                creareListaByOffset(id, offset, limit)
        );
        //offset+=limitSetat;
    }

    public void initFirstMessengerPage(long idPrieten) {
        int total=messageService.nrEOf2Users(id, idPrieten);
        if (limitMessenger + offsetMessenger >= total)
        {

            //if (limit == total) limit = total-offset;
            //else limit = (total - offset) + 1;
            nextMessengerButtonState = false;
            System.out.println(limitMessenger);
        }
        System.out.println("Of:"+offsetMessenger+"  lim:"+limitMessenger);
        pageNumberMessengerLabel.setText(""+(offsetMessenger/limitMessengerSetat+1));
        modelMessage.setAll(
                creareListaMessengerByOffset(id, idPrieten, offsetMessenger, limitMessenger)
        );
        //offset+=limitSetat;
    }

    public List<Utilizator> creareListaByOffset(long id, long offset, long limit) {
        List<Utilizator> aux = new ArrayList<>();
        List<Long> l = utilizatorService.getFriend2ByOffset(id, offset, limit);
        for (Long e : l) {
            Utilizator u = utilizatorService.findOne(e);
            aux.add(u);
        }
        return aux;
    }
    public List<Message> creareListaMessengerByOffset(long id, long idPrieten, long offset, long limit) {
        List<Message> aux = new ArrayList<>();
        for (Message m:messageService.getMessagesByOffset(id,idPrieten,offset, limit)) {
            aux.add(m);
        }
        return aux.stream()
                .sorted((e1,e2) -> e1.getDate().compareTo(e2.getDate()))
                .collect(Collectors.toList());

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
            int total=utilizatorService.getFriend2(id).size();
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


    public void handleBackMessengerButton(ActionEvent actionEvent) {
        if(backMessengerButtonState)
        {
            offsetMessenger-=limitMessengerSetat;
            limitMessenger=limitMessengerSetat;
            System.out.println("intra Of:"+offsetMessenger+"  lim:"+limitMessenger);
            modelMessage.setAll(
                    creareListaMessengerByOffset(id, idPrieten, offsetMessenger, limitMessenger)
            );
            nextMessengerButtonState=true;
            nextMessengerButton.setDisable(!nextMessengerButtonState);
            backMessengerButtonState=(offsetMessenger>0);
            backMessengerButton.setDisable(!backMessengerButtonState);
            pageNumberMessengerLabel.setText(""+(offsetMessenger/limitMessengerSetat+1));
        }
    }

    public void handleNextMessengerButton(ActionEvent actionEvent) {
        if (nextMessengerButtonState)
        {
            offsetMessenger+=limitMessengerSetat;
            int total=messageService.nrEOf2Users(id,idPrieten);
            if (limitMessenger+ offsetMessenger >= total)
            {
                //if (limit == total) limit = total-offset;
                //else limit = (total - offset) + 1;
                nextMessengerButtonState = false;
                nextMessengerButton.setDisable(!nextMessengerButtonState);
            }
            pageNumberMessengerLabel.setText(""+(offsetMessenger/limitMessengerSetat+1));
            //System.out.println("intra Of:"+offset+"  lim:"+limit);
            modelMessage.setAll(
                    creareListaMessengerByOffset(id, idPrieten, offsetMessenger, limitMessenger)
            );
            //System.out.println("iese Of:"+offset+"  lim:"+limit);
            backMessengerButtonState=true;
            backMessengerButton.setDisable(!backMessengerButtonState);
        }
    }

    private void setMesaje(Utilizator u){

        System.out.println(u.toString());
        List<Message> list = new ArrayList<>();
        for(Message m: messageService.getMessages()){
            if(m.getFrom().getId()==id){
                for(Utilizator user: m.getTo()){
                    if(user.equals(u)){
                        list.add(m);
                    }
                }
            }
            else{
                if(m.getFrom().equals(u)){
                    for(Utilizator user: m.getTo()){
                        if(user.getId()==id) {
                            list.add(m);
                        }
                    }
                }
            }
        }
        list.sort(Comparator.comparing(Message::getDate));
        modelMessage.setAll(list);
    }


    @Override
    public void update(MesajChangeEvent mesajChangeEvent) {

        initPaging();
        initFirstFriendsPage();
    }

    /**
     * Cauta toti utilizatorii ce au numele dat
     * @param keyEvent
     */
    public void searchName(KeyEvent keyEvent) {
        String nume = textFieldNume.getText();
        System.out.println(nume);


        model.setAll(creareListaUsers().stream()
                .filter(x -> x.getLastName().startsWith(nume))
                .collect(Collectors.toList())
        );
    }
    /**
     * Returneaza o lista cu toti Utilizatorii
     * @return
     */
    public List<Utilizator> creareListaUsers() {

        List<Utilizator> aux = new ArrayList<>();
        utilizatorService.getAll().forEach(aux::add);
        return aux;

    }

    public List<Message> getMessagesFromFriend(Utilizator u){
        List<Message> list = new ArrayList<>();
        for(Message m: messageService.getMessages()){
            if(m.getFrom().equals(u) || m.getFrom().getId()==id)
                list.add(m);
            for(Utilizator user: m.getTo()){
                if(user.equals(u) || user.getId()==id){
                    list.add(m);
                }

            }
        }
        return  list;
    }

    public void showMessage(ActionEvent actionEvent) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.execute(()->{


        try {
            if(tableViewFriends.getSelectionModel().isEmpty()){
                MessageAlert.showErrorMessage(null,"Nu ati selectat");
                return;

            }
            Utilizator u = tableViewFriends.getSelectionModel().getSelectedItem();
            this.idPrieten=u.getId();
            //setMesaje(u);
            initPagingMessenger(u.getId());
            initFirstMessengerPage(u.getId());
            textSomething.setText(u.getFirstName()+" "+u.getLastName());


        }
        catch(NullPointerException e){
            MessageAlert.showErrorMessage(null,"Nu ati selectat");
            return;
        }
        });
        executorService.shutdown();
    }

    public void sendAMessage(ActionEvent actionEvent) {
        ExecutorService executorService=Executors.newFixedThreadPool(5);
        executorService.execute(()->{

        try{
            Utilizator u =tableViewFriends.getSelectionModel().getSelectedItem();
            String mesaj=textFieldMesaj.getText();
            List<Long> to= new ArrayList<>();
            to.add(u.getId());
            messageService.addMessage(id,to,mesaj);
            setMesaje(u);
            textSomething.setText(u.getFirstName()+" "+u.getLastName());
            textFieldMesaj.clear();
        }
        catch(NullPointerException e){
            MessageAlert.showErrorMessage(null,"Nu ati selectat");
            return;
        }
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
            messageTaskController.setServices(utilizatorService,cerereDePrietenieService,
                    prietenieService, messageService,evenimenteService, id);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return messageTaskController;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void handleMessengerButton(ActionEvent actionEvent) {
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
            friendPageController.setServices(utilizatorService,cerereDePrietenieService,
                    prietenieService, messageService,evenimenteService, id);
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
        if(id.equals(null)){
            MessageAlert.showErrorMessage(null,"Nu ati selectat un user.Treceti un id in casuta id.");
            return null;}

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/afisareCereri.fxml"));
            BorderPane root = (BorderPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Cererile de prietenie ale userului "+utilizatorService.findOne(id).getFirstName()+" "+utilizatorService.findOne(id).getLastName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            AfisareCereriController afisare = loader.getController();
            afisare.setService(utilizatorService,cerereDePrietenieService,prietenieService,messageService,evenimenteService, dialogStage, id);

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
            reportsPageController.setServices(utilizatorService,cerereDePrietenieService,
                    prietenieService, messageService,evenimenteService, id);
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
            eventsPageController.setServices(utilizatorService,cerereDePrietenieService,
                    prietenieService, messageService,evenimenteService, id);
            dialogStage.show();
            //handleSearchUser(actionEvent);
            return eventsPageController;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

}
