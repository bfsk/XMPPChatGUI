package controllers;

import FSM.FSM;
import chat.XMPPChatClient;

import java.util.ArrayList;

public interface IController {
    public IController getController();
    public void newMessageArrived();
    public void goMain(String msg);
    public void loginFail(String msg);
    public void setClient(XMPPChatClient client);
    public void updateRoomList(ArrayList<String> list);
    public void updateJoinedRoomList(ArrayList<String> list);
    public void incomingMessage(String room, String msg);
    public void updatePeopleList(String room, ArrayList<String> list);
    public void setElements(boolean x);
    public void displayAlert(String msg);
    public void goToLogin();
}
