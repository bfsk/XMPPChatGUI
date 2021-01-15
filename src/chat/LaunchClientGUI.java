package chat;

import controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LaunchClientGUI extends Application {
    XMPPChatClient clientFSM;
    @Override
    public void start(Stage primaryStage) throws Exception{
        clientFSM = new XMPPChatClient(-8);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        ((LoginController)loader.getController()).setClient(clientFSM.main());
        clientFSM.setController((LoginController)loader.getController());
        primaryStage.setTitle("XMPP Chat Client");
        primaryStage.setScene(new Scene(root, 300, 400));
        primaryStage.show();

    }


    public static void main(String[] args) {

        launch(args);
    }
}
