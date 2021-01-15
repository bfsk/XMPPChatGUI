package chat;

import FSM.*;
import MessageTemplate.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class XMPPChatServer extends FSM implements IFSM {

    public static HashMap<String, String> users = new HashMap<>();

    public XMPPChatServer(int id) {
        super(id);
    }

    static int READY = 0;
    @Override
    public void init() {
        users.put("habib", "test");
        setState(READY);
        addTransition(READY, new Message(Message.Types.REGISTER), "onClientRegister");
        addTransition(READY, new Message(Message.Types.LOGIN), "onClientLogin");
        addTransition(READY, new Message(Message.Types.ROOM_LIST_REQUEST), "returnRoomList");
    }

    public void onClientRegister(IMessage message){
        Message msg = (Message) message;
        Message response;

        if(users.get(msg.getParam(Message.Params.USERNAME)) != null){
            response = new Message(Message.Types.REGISTER_RESPONSE);
            response.addParam(Message.Params.CONNECTION_RESPONSE, "fail");
            response.addParam(Message.Params.MSG, "User already exists!");
            System.out.println("Client " + msg.getParam(Message.Params.USERNAME) + " failed to register!");
        }else{
            users.put(msg.getParam(Message.Params.USERNAME), msg.getParam(Message.Params.PASSWORD));
            response = new Message(Message.Types.REGISTER_RESPONSE);
            response.addParam(Message.Params.CONNECTION_RESPONSE, "ok");
            response.addParam(Message.Params.MSG, "Registered!");
            System.out.println("Client " + msg.getParam(Message.Params.USERNAME) + " registered!");
        }
        response.setToAddress(msg.getFromAddress());
        sendMessage(response);

    }
    public void onClientLogin(IMessage message){
        Message msg = (Message) message;
        Message response;

        if(users.get(msg.getParam(Message.Params.USERNAME)) == null){
            response = new Message(Message.Types.LOGIN_RESPONSE);
            response.addParam(Message.Params.CONNECTION_RESPONSE, "fail");
            response.addParam(Message.Params.MSG, "Account doesn't exist!");
        }else{
            if(users.get(msg.getParam(Message.Params.USERNAME)).equals(msg.getParam(Message.Params.PASSWORD))){
                response = new Message(Message.Types.LOGIN_RESPONSE);
                response.addParam(Message.Params.CONNECTION_RESPONSE, "ok");
                response.addParam(Message.Params.MSG, "Welcome!");
            }else{
                response = new Message(Message.Types.LOGIN_RESPONSE);
                response.addParam(Message.Params.CONNECTION_RESPONSE, "fail");
                response.addParam(Message.Params.MSG, "Wrong password!");
            }
        }
        response.setToAddress(msg.getFromAddress());
        sendMessage(response);
        System.out.println("Client " + msg.getParam(Message.Params.USERNAME) + " logged in!");
    }
    public void returnRoomList(IMessage message){
        System.out.println("Room list requested!");
        Message msg = (Message) message;
        String token = ((Message) message).getParam(Message.Params.TOKEN);
        Message response = new Message(Message.Types.ROOM_LIST_RESPONSE);
        response.setToAddress(msg.getFromAddress());

        //check token for login
        if (token != null && true){
            ArrayList<String> room_list = new ArrayList<>();
            room_list.add("TKM1");
            room_list.add("TKM2");
            response.addParam(Message.Params.ROOM_LIST, room_list);
        }else{
            System.out.println("NOT AUTHENTICATED! METHOD NOT IMPLEMENTED!");
        }

        sendMessage(response);
    }

    static int SERVER_PORT = 9999;
    public static void main(String[] args) throws Exception{
	// write your code here
        XMPPChatServer XMPPChatServerFSM = new XMPPChatServer(0);
        TcpTransportServer tcpFSM = new TcpTransportServer(5);
        tcpFSM.setServerPort(SERVER_PORT);
        tcpFSM.setReceiver(XMPPChatServerFSM);

        Dispatcher dis = new Dispatcher(false);
        dis.addFSM(XMPPChatServerFSM);
        dis.addFSM(tcpFSM);
        dis.start();

        System.out.println("Server is running on port 9999!");
        while(true){
            Thread.sleep(1);
        }
    }
}
