package chat;

import FSM.*;
import MessageTemplate.Message;
import controllers.IController;

import java.util.ArrayList;

public class XMPPChatClient extends FSM implements IFSM {
    public static IController controller;
    static String TOKEN = "123";
    static int IDLE = 0;
    static int READY_TO_CONNECT = 1;
    static int CONNECTING = 2;
    static int CONNECTED = 3;
    static int FETCHING = 4;
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

        addTransition(READY_TO_CONNECT, new Message(Message.Types.REGISTER), "registerOnServer");
        addTransition(READY_TO_CONNECT, new Message(Message.Types.LOGIN), "loginOnServer");

        addTransition(CONNECTING, new Message(Message.Types.REGISTER_RESPONSE), "connectionResponse");
        addTransition(CONNECTING, new Message(Message.Types.LOGIN_RESPONSE), "connectionResponse");

        addTransition(CONNECTED, new Message(Message.Types.ROOM_LIST_REQUEST), "requestRoomList");
        addTransition(CONNECTED, new Message(Message.Types.ROOM_LIST_RESPONSE), "roomListResponse");

    }
    public void resolveDomain(IMessage message){
        Message msg = (Message)message;
        System.out.println("Resolving: " + msg.getParam(Message.Params.DOMAIN));
        //resolve domain, implement this please!
        Message tcpMSG = new Message(5555);
        tcpMSG.addParam(Message.Params.IP, "127.0.0.1");
        sendMessage(tcpMSG);
        System.out.println("Resolved!");
        setState(READY_TO_CONNECT);

    }
    public void registerOnServer(IMessage message){
        Message msg = (Message)message;
        msg.setToId(5);
        System.out.println("Registering..." + msg.getMessageId());
        sendMessage(msg);
        setState(CONNECTING);
    }
    public void loginOnServer(IMessage message){
        Message msg = (Message)message;
        msg.setToId(5);
        System.out.println("Loging in...");
        sendMessage(msg);
        setState(CONNECTING);
    }

    public void connectionResponse(IMessage message){
        Message msg = (Message) message;
        if(msg.getParam(Message.Params.CONNECTION_RESPONSE).equals("fail")){
            controller.loginFail(msg.getParam(Message.Params.MSG));
            setState(READY_TO_CONNECT);
        }else{
            controller.goMain(msg.getParam(Message.Params.MSG));
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
        System.out.println("Room list:");
        controller.updateRoomList((ArrayList)msg.getParam(Message.Params.ROOM_LIST, true));
    }
    static int SERVER_PORT = 9999;
    static String SERVER_URL = "";
    static String SERVER_IP = "";
    Dispatcher dis;
    public Dispatcher getDispatcher(){
        return dis;
    }
    public XMPPChatClient main() throws Exception{
	// write your code here
        //client
        XMPPChatClient XMPPChatClientFSM = new XMPPChatClient(0);
        TcpTransportClient tcpFSM = new TcpTransportClient(5);
        tcpFSM.setServerPort(SERVER_PORT);
        tcpFSM.setReceiver(XMPPChatClientFSM);

        dis = new Dispatcher(false);
        dis.addFSM(XMPPChatClientFSM);
        dis.addFSM(tcpFSM);
        dis.start();
        Message msg = new Message(Message.Types.RESOLVE_DOMAIN_NAME);
        msg.addParam(Message.Params.DOMAIN, SERVER_URL);
        msg.setToId(0);
        dis.addMessage(msg);
        return this;
    }
    public void refreshRoomList(){
        Message msg = new Message(Message.Types.ROOM_LIST_REQUEST);
        msg.setToId(0);
        dis.addMessage(msg);
    }
}
