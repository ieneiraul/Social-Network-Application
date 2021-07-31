package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import socialnetwork.service.*;

import java.io.IOException;

public class GoToPages {
    public static HomeController goToHomePage(Class c, UtilizatorService service, CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, long idUtilizatorCurent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(c.getClass().getResource("/views/homePage.fxml"));
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

    public static AfisareMesajeController goToMessengerPage(Class c, UtilizatorService service, CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, long idUtilizatorCurent) {

        Long id = idUtilizatorCurent;

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(c.getClass().getResource("/views/MessengerView.fxml"));

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

    public static FriendPageController goToFriendsPage(Class c, UtilizatorService service, CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, long idUtilizatorCurent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(c.getClass().getResource("/views/mix2view.fxml"));
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

    public static AfisareCereriController goToFriendRequestsPage(Class c, UtilizatorService service, CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, long idUtilizatorCurent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(c.getClass().getResource("/views/afisareCereri.fxml"));
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

    public static ReportsPageController goToReportsPage(Class c, UtilizatorService service, CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, long idUtilizatorCurent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(c.getClass().getResource("/views/reportsPage.fxml"));
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

    public static EventsController goToEventsPage(Class c, UtilizatorService service, CerereDePrietenieService cerereDePrietenieService, PrietenieService prietenieService, MessageService mesajService, EvenimenteService evenimenteService, long idUtilizatorCurent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(c.getClass().getResource("/views/eventsPage.fxml"));
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
