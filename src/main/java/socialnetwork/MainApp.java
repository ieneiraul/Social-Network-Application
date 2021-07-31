package socialnetwork;
import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.*;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.*;
import socialnetwork.service.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class MainApp  extends Application {

    Validator validator1 = new PrietenieValidator();
    final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
    final String username = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
    final String pasword = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");
    //repo-DB
    Repository<Long, Utilizator> userFileRepository3 =
            new UtilizatorRepoDB(url, username, pasword, new UtilizatorValidator());
    Repository<Long, CerereDePrietenie> cerereDBRepo =
            new CerereDePrietenieDB(url, username, pasword, new CerereDePrietenieValidator(),userFileRepository3);
    Repository<Tuple<Long, Long>, Prietenie> prietenieRepository2 = new PrietenieRepoDB(url, username, pasword, validator1);
    Repository<Long, Message> messageRepository = new MessageDB(url, username, pasword, new MessageValidator(),userFileRepository3);
    Repository<Long, Eveniment> evenimentRepository= new EvenimenteDB(url,username,pasword,new EvenimentValidator(),userFileRepository3);
    ContUtilizatorDB contUtilizatorRepository = new ContUtilizatorDB(url, username, pasword);
    StatusNotificariDB statusRepository = new StatusNotificariDB(url,username,pasword);

    //service-DB
    UtilizatorService serviceBD = new UtilizatorService(userFileRepository3, prietenieRepository2, cerereDBRepo, messageRepository, contUtilizatorRepository, statusRepository);
    CerereDePrietenieService cerereDePrietenieService = new CerereDePrietenieService(userFileRepository3, prietenieRepository2, cerereDBRepo);

    PrietenieService prietenieServiceBD = new PrietenieService(userFileRepository3, prietenieRepository2);
    MessageService messageService = new MessageService(messageRepository, userFileRepository3);
    EvenimenteService evenimenteService= new EvenimenteService(evenimentRepository,userFileRepository3);

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws IOException {
        initView(primaryStage);
        primaryStage.setWidth(700);
        primaryStage.setTitle("SocialNetwork");
        primaryStage.show();

    }


    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader messageLoader = new FXMLLoader();

        messageLoader.setLocation(getClass().getResource("/views/login.fxml"));
        BorderPane messageTaskLayout = messageLoader.load();
        primaryStage.setScene(new Scene(messageTaskLayout));

        LoginController loginController=messageLoader.getController();
        loginController.setServices(serviceBD,cerereDePrietenieService,
                prietenieServiceBD, messageService,evenimenteService);

    }
}
