package chat;

import FSM.*;
import MessageTemplate.Message;
import controllers.IController;

import java.util.ArrayList;

public class XMPPChatClient extends FSM implements IFSM {
    public IController controller;
    private String TOKEN = "";
    private final int IDLE = 0;
    private final int READY_TO_CONNECT = 1;
    private final int CONNECTING = 2;
    private final int CONNECTED = 3;
    private final int ACCESSING = 4;
    private ArrayList<String> joinedRooms = new ArrayList<>();
    private String username = "";
    private String password = "";
    public void setController(IController controller){
        this.controller = controller;
    }
    public XMPPChatClient(int id) {
        super(id);
    }
    @Override
    public void init() {
        setState(IDLE);
        addTransition(IDLE, new Message(Message.Types.RESOLVE_DOMAIN_NAME), "resolveDomain");
        addTransition(IDLE, new Message(Message.Types.RESOLVED), "goReadyToConnect");

        addTransition(CONNECTING, new Message(Message.Types.SERVER_REACHABLE), "reachable");

        addTransition(READY_TO_CONNECT, new Message(Message.Types.REGISTER), "registerOnServer");
        addTransition(READY_TO_CONNECT, new Message(Message.Types.LOGIN), "loginOnServer");



        addTransition(ACCESSING, new Message(Message.Types.REGISTER_RESPONSE), "connectionResponse");
        addTransition(ACCESSING, new Message(Message.Types.LOGIN_RESPONSE), "connectionResponse");

        addTransition(CONNECTED, new Message(Message.Types.ROOM_LIST_REQUEST), "requestRoomList");
        addTransition(CONNECTED, new Message(Message.Types.ROOM_LIST_RESPONSE), "roomListResponse");

        addTransition(CONNECTED, new Message(Message.Types.JOIN_ROOM_REQUEST), "joinRoomRequest");
        addTransition(CONNECTED, new Message(Message.Types.JOIN_ROOM_RESPONSE), "joinRoomResponse");

        addTransition(CONNECTED, new Message(Message.Types.LEAVE_ROOM_REQUEST), "leaveRoomRequest");
        addTransition(CONNECTED, new Message(Message.Types.LEAVE_ROOM_RESPONSE), "leaveRoomResponse");

        addTransition(CONNECTED, new Message(Message.Types.MSG_TO_SERVER), "msgToServer");
        addTransition(CONNECTED, new Message(Message.Types.MSG_TO_CLIENT), "msgFromServer");

        addTransition(CONNECTED, new Message(Message.Types.USERS_LIST_REQUEST), "usersListRequest");
        addTransition(CONNECTED, new Message(Message.Types.USERS_LIST_RESPONSE), "usersListResponse");

        addTransition(CONNECTED, new Message(Message.Types.DELETE_ACCOUNT_REQUEST), "deleteAccRequest");
        addTransition(CONNECTED, new Message(Message.Types.DELETE_ACCOUNT_RESPONSE), "deleteAccResponse");
    }
    public void resolveDomain(IMessage message){
        Message msg = (Message)message;

        SERVER_IP = msg.getParam(Message.Params.IP);
        SERVER_PORT = msg.getParam(Message.Params.PORT);
        //setState(READY_TO_CONNECT);
        Message result = new Message(Message.Types.RESOLVED);
        result.addParam(Message.Params.IP, SERVER_IP);
        result.addParam(Message.Params.PORT, SERVER_PORT);
        result.setToId(0);
        sendMessage(result);
    }
    public void goReadyToConnect(IMessage message){
        Message msg = (Message) message;
        SERVER_IP = msg.getParam(Message.Params.IP);
        SERVER_PORT = msg.getParam(Message.Params.PORT);

        Message tcpMSG = new Message(5555);
        tcpMSG.addParam(Message.Params.IP, SERVER_IP);
        tcpMSG.addParam(Message.Params.PORT, SERVER_PORT);
        sendMessage(tcpMSG);
        setState(CONNECTING);
    }
    public void reachable(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.MSG).contains("ok")) {
            setState(READY_TO_CONNECT);
            controller.setElements(false);
        }else{
            controller.displayAlert(msg.getParam(Message.Params.MSG));
            setState(IDLE);
        }
    }
    public void registerOnServer(IMessage message){
        Message msg = (Message)message;
        msg.setToId(5);
        System.out.println("Registering..." + msg.getMessageId());
        sendMessage(msg);
        username = msg.getParam(Message.Params.USERNAME);
        setState(ACCESSING);
    }
    public void loginOnServer(IMessage message){
        Message msg = (Message)message;
        msg.setToId(5);
        System.out.println("Loging in...SEND");
        sendMessage(msg);
        username = msg.getParam(Message.Params.USERNAME);
        setState(ACCESSING);
    }
    public void connectionResponse(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.CONNECTION_RESPONSE).equals("fail")){
            controller.loginFail(msg.getParam(Message.Params.MSG));
            setState(READY_TO_CONNECT);
        }else{
            controller.goMain(msg.getParam(Message.Params.MSG));
            TOKEN = msg.getParam(Message.Params.TOKEN);
            setState(CONNECTED);
        }
    }
    public void requestRoomList(IMessage message){
        System.out.println("Fetching room list!");
        Message request = new Message(Message.Types.ROOM_LIST_REQUEST);
        request.setMessageId(Message.Types.ROOM_LIST_REQUEST);
        request.addParam(Message.Params.TOKEN, TOKEN);
        sendMessage(request);
    }
    public void roomListResponse(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.MSG).contains("ok")){
            controller.updateRoomList((ArrayList)msg.getParam(Message.Params.ROOM_LIST, true));
        }else{
            controller.updateRoomList(new ArrayList<String>());
        }

    }
    public void joinRoomRequest(IMessage message){
        Message msg = (Message) message;
        msg.setToId(5);
        sendMessage(msg);
    }
    public void joinRoomResponse(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.MSG).contains("ok")){
            joinedRooms.add(msg.getParam(Message.Params.ROOM));
            controller.updateJoinedRoomList(joinedRooms);
        }
    }
    public void leaveRoomRequest(IMessage message){
        Message msg = (Message) message;
        msg.setToId(5);
        sendMessage(msg);
    }
    public void leaveRoomResponse(IMessage message){
        System.out.println("Got response for leave!");
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.MSG).contains("ok")){
            joinedRooms.remove(msg.getParam(Message.Params.ROOM));
            controller.updateJoinedRoomList(joinedRooms);
        }
    }
    public void msgToServer(IMessage message){
        Message msg = (Message) message;
        msg.setToId(5);
        sendMessage(msg);
    }
    public void msgFromServer(IMessage message){
        Message msg = (Message) message;
        controller.incomingMessage(msg.getParam(Message.Params.ROOM), msg.getParam(Message.Params.MSG));
    }
    public void usersListRequest(IMessage message){
        System.out.println("Fetching users list!");
        Message req = (Message) message;
        req.setToId(5);
        sendMessage(req);
    }
    public void usersListResponse(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.MSG).contains("ok")){
            System.out.println((ArrayList)msg.getParam(Message.Params.USERS_LIST, true));
            controller.updatePeopleList(msg.getParam(Message.Params.ROOM), (ArrayList)msg.getParam(Message.Params.USERS_LIST, true));
        }
    }
    public void deleteAccRequest(IMessage message){
        System.out.println("Deleting acc...");
        Message req = (Message) message;
        req.setToId(5);
        sendMessage(req);
    }
    public void deleteAccResponse(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.MSG).contains("ok")){
            controller.goToLogin();
            setState(IDLE);
        }else{
            controller.displayAlert(msg.getParam(Message.Params.MSG));
        }
    }
    static String SERVER_PORT = "9999";
    static String SERVER_IP = "";

    public void resolve(String domainName){
        String ip = domainName.split(":")[0];
        String port = domainName.split(":")[1];
        Message msg = new Message(Message.Types.RESOLVE_DOMAIN_NAME);
        msg.addParam(Message.Params.IP, ip);
        msg.addParam(Message.Params.PORT, port);
        msg.setToId(0);
        getDispatcher().addMessage(msg);
    }
    public void refreshRoomList(){
        Message msg = new Message(Message.Types.ROOM_LIST_REQUEST);
        msg.setToId(0);
        getDispatcher().addMessage(msg);
    }
    public void register(String username, String password){
        Message tempMsg = new Message(Message.Types.REGISTER);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.USERNAME, username);
        tempMsg.addParam(Message.Params.PASSWORD, password);
        getDispatcher().addMessage(tempMsg);
    }
    public void login(String username, String password){
        Message tempMsg = new Message(Message.Types.LOGIN);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.USERNAME, username);
        tempMsg.addParam(Message.Params.PASSWORD, password);
        getDispatcher().addMessage(tempMsg);
    }
    public void joinRoom(String room){
        Message tempMsg = new Message(Message.Types.JOIN_ROOM_REQUEST);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.ROOM, room);
        tempMsg.addParam(Message.Params.TOKEN, TOKEN);
        getDispatcher().addMessage(tempMsg);
    }
    public void leaveRoom(String room){
        Message tempMsg = new Message(Message.Types.LEAVE_ROOM_REQUEST);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.ROOM, room);
        tempMsg.addParam(Message.Params.TOKEN, TOKEN);
        getDispatcher().addMessage(tempMsg);
    }
    public void sendMessage(String room, String msg){
        Message tempMsg = new Message(Message.Types.MSG_TO_SERVER);
        tempMsg.setToId(0);
        tempMsg.addParam(Message.Params.ROOM, room);
        tempMsg.addParam(Message.Params.TOKEN, TOKEN);
        tempMsg.addParam(Message.Params.MSG, msg);
        getDispatcher().addMessage(tempMsg);
    }
    public void updateUsers(String room){
        Message request = new Message(Message.Types.USERS_LIST_REQUEST);
        request.setToId(0);
        request.addParam(Message.Params.TOKEN, TOKEN);
        request.addParam(Message.Params.ROOM, room);
        getDispatcher().addMessage(request);
    }
    public void stopAll(){
        getDispatcher().stop();
    }
    public String getUsername(){
        return username;
    }
    public void deleteAccount(){
        Message msg = new Message(Message.Types.DELETE_ACCOUNT_REQUEST);
        msg.setToId(0);
        msg.addParam(Message.Params.TOKEN, TOKEN);
        getDispatcher().addMessage(msg);
    }
}
