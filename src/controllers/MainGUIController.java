package controllers;

import chat.XMPPChatClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainGUIController implements IController {
    XMPPChatClient client;
    public ListView roomList;
    public ListView joinedRoomsList;
    public ListView pplList;
    public TextField message;
    public TextArea chatBox;
    public Label roomNameLabel;
    public Label userMsg;
    public TextField newRoomBox;
    private String selectedRoom = "";
    HashMap<String, ArrayList<String>> chatByRooms = new HashMap<>();
    ArrayList<String> currentChat = new ArrayList<>();
    @FXML
    public void initialize(){
        joinedRoomsList.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal)->{
            if(oldVal == null || oldVal!=null && newVal!=null && oldVal.toString() != newVal.toString()){
                roomNameLabel.setText(newVal.toString());
                if(chatByRooms.get(newVal.toString()) != null) {
                    currentChat = chatByRooms.get(newVal.toString());
                    updateChatBox();
                    client.updateUsers(newVal.toString());
                    selectedRoom = newVal.toString();
                }
            }
        });
        Platform.runLater(()->{
            String welcome = "Welcome ";
            welcome += client.getUsername() + "!";
            userMsg.setText(welcome);
        });

    }
    @FXML
    public void onEnter(ActionEvent ae){
        sendMsg(null);
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
    public void updatePeopleList(String room, ArrayList<String> list){
        Platform.runLater(()->{
            if(selectedRoom.equals(room)){
                pplList.getItems().clear();
                pplList.getItems().addAll(list);
            }
        });

    }
    @Override
    public void updateJoinedRoomList(ArrayList<String> rmlst) {
        Platform.runLater(()->{
            String selectedRoom = (String)roomList.getSelectionModel().getSelectedItem();

            ArrayList<String> toRemove = new ArrayList<>();
            for(Object joined: joinedRoomsList.getItems()){
                String rm = (String) joined;
                if(!rmlst.contains(rm)){
                    toRemove.add(rm);
                }
            }
            joinedRoomsList.getItems().removeAll(toRemove);
            for(String newRoom: rmlst){
                if(!joinedRoomsList.getItems().contains(newRoom)){
                    joinedRoomsList.getItems().add(newRoom);
                }
            }

            for(String room: rmlst){
                if(chatByRooms.get(room) == null)chatByRooms.put(room, new ArrayList<String>());
            }

            ArrayList<String> roomsToRemove = new ArrayList<>();
            for(Map.Entry<String, ArrayList<String>> entry: chatByRooms.entrySet())
                if(!rmlst.contains(entry.getKey())) roomsToRemove.add(entry.getKey());
            for(String roomToRemove: roomsToRemove)
                chatByRooms.remove(roomToRemove);


            int index = -1;
            for(int i = 0; i < roomList.getItems().size(); i++){
                if(roomList.getItems().get(i).equals(selectedRoom)){
                    index = i;
                    break;
                }
            }
            if(index >= 0 && joinedRoomsList.getItems().contains(selectedRoom)){
                joinedRoomsList.getSelectionModel().select(index);
            }else{
                if(index >= 0 &&  joinedRoomsList.getItems().size() != 0) {
                    joinedRoomsList.getSelectionModel().select(0);
                }
            }
            client.refreshRoomList();
        });
    }
    public void incomingMessage(String room, String msg){
        Platform.runLater(()->{
            //chatBox.appendText("\n" + msg);
            if(chatByRooms.get(room) != null){
                chatByRooms.get(room).add(msg);
                updateChatBox();
                if(!selectedRoom.equals(room)){

                }
            }

        });
        if(msg.indexOf("server") == 0) client.updateUsers(selectedRoom);
    }
    private void updateChatBox(){
        String txt = "";
        boolean first = true;
        for(String line: currentChat){
            if(first) {
                first = false;
            }
            else{
               txt += "\n";
            }
            txt += line;
        }
        chatBox.setText(txt);
    }
    public void sendMsg(ActionEvent actionEvent) {
        String selectedRoom = (String)joinedRoomsList.getSelectionModel().getSelectedItem();
        if(selectedRoom != null){
            client.sendMessage(selectedRoom, message.getText());
            message.setText("");
        }
    }

    public void deleteAcc(ActionEvent actionEvent) {
        client.deleteAccount();
    }
    public void refreshRooms(ActionEvent actionEvent) {
        client.refreshRoomList();
    }
    public void joinRoom(ActionEvent actionEvent) {
        String selectedRoom = (String)roomList.getSelectionModel().getSelectedItem();
        if(!newRoomBox.getText().equals("")) {
            selectedRoom = newRoomBox.getText();
            newRoomBox.setText("");
        }
        if(selectedRoom != null){
            client.joinRoom(selectedRoom);
        }
        client.refreshRoomList();
    }
    public void leaveRoom(ActionEvent actionEvent) {
        String selectedRoom = (String)joinedRoomsList.getSelectionModel().getSelectedItem();
        if(selectedRoom != null){
            client.leaveRoom(selectedRoom);
        }
    }
    @Override
    public void setElements(boolean x){}
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
    public void goToLogin(){
        Platform.runLater(() ->{
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                ((LoginController)loader.getController()).setClient(client);
                client.setController((LoginController)loader.getController());
                Stage primaryStage = (Stage) message.getScene().getWindow();
                primaryStage.setTitle("XMPP Chat Client");
                primaryStage.setScene(new Scene(root, 800, 600));
            }catch(Exception e){
            }
        });
    }
}
