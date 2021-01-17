package chat;

import FSM.Dispatcher;
import FSM.TcpTransportClient;
import MessageTemplate.Message;
import controllers.LoginController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LaunchClientGUI extends Application {
    XMPPChatClient clientFSM;
    //XMPPChatClient clientFSM;
    Dispatcher disp;
    @Override
    public void start(Stage primaryStage) throws Exception{


        clientFSM = new XMPPChatClient(0);

        TcpTransportClient tcpFSM = new TcpTransportClient(5);
        tcpFSM.setServerPort(9999);
        tcpFSM.setReceiver(clientFSM);

        disp = new Dispatcher(false);
        disp.addFSM(clientFSM);
        disp.addFSM(tcpFSM);
        disp.start();

        Message msg = new Message(Message.Types.RESOLVE_DOMAIN_NAME);
        msg.addParam(Message.Params.DOMAIN, "127.0.0.1");
        msg.setToId(0);
        disp.addMessage(msg);



        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        ((LoginController)loader.getController()).setClient(clientFSM);
        clientFSM.setController((LoginController)loader.getController());
        primaryStage.setTitle("XMPP Chat Client");
        primaryStage.setScene(new Scene(root, 300, 400));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                clientFSM.stopAll();
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {

        launch(args);
    }
}
