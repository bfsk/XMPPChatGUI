package controllers;

import FSM.Dispatcher;
import MessageTemplate.Message;
import chat.XMPPChatClient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;

public class LoginController implements IController {
    public TextField usernameBox;
    public TextField nicknameBox;
    public TextField passwordBox;

    XMPPChatClient client;
    Dispatcher dis;
    @Override
    public void setClient(XMPPChatClient client){
        this.client = client;
        client.setController(this);
        dis = client.getDispatcher();
    }

    @Override
    public void updateRoomList(ArrayList<String> list) {

    }

    @FXML
    public void initialize(){

    }
    @Override
    public IController getController() {
        return this;
    }

    @Override
    public void newMessageArrived() {

    }
    @Override
    public void goMain(String msg){
        displayAlert(msg);
        Platform.runLater(() ->{
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainGUI.fxml"));
                Parent root = loader.load();
                ((MainGUIController)loader.getController()).setClient(client);
                client.setController((MainGUIController)loader.getController());
                Stage primaryStage = (Stage) usernameBox.getScene().getWindow();
                primaryStage.setTitle("XMPP Chat Client");
                primaryStage.setScene(new Scene(root, 800, 600));
            }catch(Exception e){
            }
        });
    }
    @Override
    public void loginFail(String msg){
        displayAlert(msg);
    }
    public void login(ActionEvent actionEvent) {
        Message tempMsg = new Message(Message.Types.LOGIN);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.USERNAME, usernameBox.getText());
        tempMsg.addParam(Message.Params.PASSWORD, passwordBox.getText());
        dis.addMessage(tempMsg);

    }
    public void register(ActionEvent actionEvent) {
        Message tempMsg = new Message(Message.Types.REGISTER);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.USERNAME, usernameBox.getText());
        tempMsg.addParam(Message.Params.PASSWORD, passwordBox.getText());
        dis.addMessage(tempMsg);
    }

    public void displayAlert(String msg){
        Platform.runLater(() ->{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("XMPP Chat Client");
            alert.setHeaderText("");
            alert.setContentText(msg);
            alert.showAndWait().ifPresent(rs -> {
            });
        });


    }
}
