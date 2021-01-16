package controllers;

import chat.XMPPChatClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class MainGUIController implements IController {
    XMPPChatClient client;
    public ListView roomList;
    public ListView joinedRoomsList;
    public TextField message;
    public TextArea chatBox;
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
    public void goMain(String msg) {

    }

    @Override
    public void loginFail(String msg) {

    }

    @Override
    public void setClient(XMPPChatClient client) {
        this.client = client;
        client.refreshRoomList();
    }
    @Override
    public void updateRoomList(ArrayList<String> rmlst) {
        Platform.runLater(()->{
            roomList.getItems().clear();
            roomList.getItems().addAll(rmlst);
        });

    }
    @Override
    public void updateJoinedRoomList(ArrayList<String> rmlst) {
        System.out.println("CAC");
        Platform.runLater(()->{
            joinedRoomsList.getItems().clear();
            joinedRoomsList.getItems().addAll(rmlst);
            System.out.println("ASDAD");
        });
    }
    public void incomingMessage(String room, String msg){
        Platform.runLater(()->{
            chatBox.appendText("\n" + msg);
        });
    }
    public void sendMsg(ActionEvent actionEvent) {
        String selectedRoom = (String)joinedRoomsList.getSelectionModel().getSelectedItem();
        if(selectedRoom != null){
            client.sendMessage(selectedRoom, message.getText());
            message.setText("");
        }
    }
    public void refreshRooms(ActionEvent actionEvent) {
        client.refreshRoomList();
    }
    public void joinRoom(ActionEvent actionEvent) {
        String selectedRoom = (String)roomList.getSelectionModel().getSelectedItem();
        if(selectedRoom != null){
            client.joinRoom(selectedRoom);
        }
    }
    public void leaveRoom(ActionEvent actionEvent) {
        String selectedRoom = (String)joinedRoomsList.getSelectionModel().getSelectedItem();
        if(selectedRoom != null){
            client.leaveRoom(selectedRoom);
        }
    }


}
