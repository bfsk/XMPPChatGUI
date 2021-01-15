package controllers;

import chat.XMPPChatClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.ArrayList;

public class MainGUIController implements IController {
    XMPPChatClient client;
    public ListView roomList;

    public ObservableList<String> listOfRooms = FXCollections.observableArrayList();
    @FXML
    public void initialize(){
        roomList.setItems(listOfRooms);

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
    int x = 0;
    public void test(ActionEvent actionEvent) {
        roomList.getItems().add("test" + x);
        x++;
        System.out.println(roomList.getItems());
    }
    public void refreshRooms(ActionEvent actionEvent) {
        client.refreshRoomList();
    }

}
