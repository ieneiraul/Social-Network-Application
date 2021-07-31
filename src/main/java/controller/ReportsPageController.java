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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.CreatePdf;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Utilizator;
import socialnetwork.service.*;
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

public class ReportsPageController implements Observer<PrietenieChangeEvent> {
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
    DatePicker DataPickerData;
    @FXML
    DatePicker  DataPickerDataFinal;
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
        this.reportsButton.setDisable(true);
        initPaging();
        //initModel();
        initFirstUsersPage();

    }

    @FXML
    public void initialize() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tableColumnNume.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        tableColumnPrenume.setCellValueFactory(new PropertyValueFactory<>("FirstName"));

        tableView.setItems(model);
    }

    public void initPaging() {
        this.offset=(long)0;
        this.limit=(long)5;
        this.offsetSetat=(long)0;
        this.limitSetat=(long)5;
        backButtonState=false;
        backButton.setDisable(true);
        nextButtonState=(offsetSetat+limitSetat)<service.nrE();
        nextButton.setDisable(!nextButtonState);
    }

    public void initUserFriends() {
        model.setAll(
                creareLista(idUtilizatorCurent)
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
        initPaging();
        initFirstUsersPage();
    }


    public void initFirstUsersPage() {
        int total=service.nrE();
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



    public List<Utilizator> creareListaByOffset(long id, long offset, long limit) {
        List<Utilizator> aux = new ArrayList<>();
        Iterable<Utilizator> users = service.getUsersByOffset(id, offset, limit);
        List<Utilizator> userList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        return  userList;
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
     * Activitatile unui utilizator dintr-o perioada calendaristica,
     * referitor la prietenii noi
     * creati si mesajele primite in acea perioada
     */
    public void Raport1(){
        try {
            Utilizator m = service.findOne(idUtilizatorCurent);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String data = DataPickerData.getEditor().getText();
            if(data.equals("")){
                MessageAlert.showErrorMessage(null,"Selectati data de inceput!");
                return;
            }
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
            String datafinal = DataPickerDataFinal.getEditor().getText();
            if(datafinal.equals("")){
                MessageAlert.showErrorMessage(null,"Selectati data de final!");
                return;
            }
            List<String> attr1 = Arrays.asList(datafinal.split("/"));
            String data2 = "";
            if (attr1.get(1).length() == 1 || attr1.get(0).length()==1)  {
                for (String s : attr1) {
                    if (s.equals(attr1.get(1)) &&attr1.get(1).length() == 1 ) {
                        data2 = data2 + "0" + s + "/";
                    }
                    else if(s.equals(attr1.get(0))&&attr1.get(0).length() == 1) {
                        data2 = data2 + "0" + s + "/";
                    }
                    else {
                        data2 = data2 + s + "/";
                    }
                }
                data2 = data2.substring(0, 10);
            } else data2 = datafinal;
            LocalDateTime d2 = LocalDate.parse(data2, formatter).atStartOfDay();
            List<String> rez = service.getMessagesAndFriendships(m.getId(), d, d2);
            for (String s : rez) {
                System.out.println(s);
            }
            CreatePdf createPdf = new CreatePdf();
            createPdf.creare(rez, "C:\\Users\\alini\\Desktop\\pdfCerinta1.pdf", "Raport activitati pentru "+m.getFirstName()+" "+m.getLastName());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Generare raport","Raportul a fost generat cu succes!");
        }
        catch (NullPointerException e){
            //MessageAlert.showErrorMessage(null,"Selectati un user din tabel");
        }


    }

    /**
     * Raport2 pentru pdf
     * Arata toate mesajele primite de la un anumit prieten ale userului cautat
     */
    public void Raport2(){
        try {
            Long id = idUtilizatorCurent;
            System.out.println(id);
            Utilizator u = service.findOne(id);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            model.setAll(
                    creareLista(u.getId())
            );
            Utilizator prieten = tableView.getSelectionModel().getSelectedItem();

            if (u == null)
                MessageAlert.showErrorMessage(null, "Nu am gasit userul");
            else if (prieten == null)
                MessageAlert.showErrorMessage(null, "Nu ati selectat niciun prieten");
            else if (DataPickerData.getEditor().getText().equals("") ||
                    DataPickerDataFinal.getEditor().getText().equals("")) {
                MessageAlert.showErrorMessage(null, "Una dintre date este vida!");
                return;
            }

            String data = DataPickerData.getEditor().getText();
            List<String> attr = Arrays.asList(data.split("/"));
            String data1 = "";
            if (attr.get(1).length() == 1 || attr.get(0).length() == 1) {
                for (String s : attr) {
                    if (s.equals(attr.get(1)) && attr.get(1).length() == 1) {
                        data1 = data1 + "0" + s + "/";
                    } else if (s.equals(attr.get(0)) && attr.get(0).length() == 1) {
                        data1 = data1 + "0" + s + "/";
                    } else {
                        data1 = data1 + s + "/";
                    }
                }
                data1 = data1.substring(0, 10);
            } else data1 = data;

            LocalDateTime d = LocalDate.parse(data1, formatter).atStartOfDay();
            String datafinal = DataPickerDataFinal.getEditor().getText();
            List<String> attr1 = Arrays.asList(datafinal.split("/"));
            String data2 = "";
            if (attr1.get(1).length() == 1 || attr1.get(0).length() == 1) {
                for (String s : attr1) {
                    if (s.equals(attr1.get(1)) && attr1.get(1).length() == 1) {
                        data2 = data2 + "0" + s + "/";
                    } else if (s.equals(attr1.get(0)) && attr1.get(0).length() == 1) {
                        data2 = data2 + "0" + s + "/";
                    } else {
                        data2 = data2 + s + "/";
                    }
                }
                data2 = data2.substring(0, 10);
            } else data2 = datafinal;
            LocalDateTime d2 = LocalDate.parse(data2, formatter).atStartOfDay();
            List<String> rez = service.getMesajeDelaPrieten(u, prieten, d, d2);
            //for (String s : rez) System.out.println(s);
            CreatePdf createPdf = new CreatePdf();
            createPdf.creare(rez, "C:\\Users\\alini\\Desktop\\pdfCerinta2.pdf", "Raport mesaje primite pentru " + u.getFirstName()+" " + u.getLastName());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Generare raport", "Raportul a fost generat cu succes!");
        } catch (Exception e) {
           // MessageAlert.showErrorMessage(null,"Selectati un user din tabel");
        }

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
            int total=service.nrE();
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


