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
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;

public class LoginController implements IController {
    public TextField usernameBox;
    public TextField domainNameBox;
    public TextField passwordBox;
    public Button regBtn;
    public Button loginBtn;
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
        setElements(true);
        domainNameBox.setText("127.0.0.1:9999");
    }

    @Override
    public void setElements(boolean state){
        Platform.runLater(()->{
            loginBtn.setDisable(state);
            regBtn.setDisable(state);
            usernameBox.setDisable(state);
            passwordBox.setDisable(state);
        });
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
    @FXML
    public void onEnter(ActionEvent ae){
        login(null);
    }
    @Override
    public void loginFail(String msg){
        displayAlert(msg);
    }
    public void login(ActionEvent actionEvent) {
        client.login(usernameBox.getText(), passwordBox.getText());
    }
    public void register(ActionEvent actionEvent) {
        client.register(usernameBox.getText(), passwordBox.getText());
    }

    public void checkServer(ActionEvent actionEvent) {
        client.resolve(domainNameBox.getText());
    }

    @Override
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
    @Override
    public void updateJoinedRoomList(ArrayList<String> l){}
    @Override
    public void incomingMessage(String room, String msg){}
    @Override
    public void updatePeopleList(String room, ArrayList<String> list){}
    @Override
    public void goToLogin(){

    }
}
