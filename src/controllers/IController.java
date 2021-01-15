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
}