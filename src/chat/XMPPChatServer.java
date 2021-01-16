package chat;

import FSM.*;
import MessageTemplate.Message;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class XMPPChatServer extends FSM implements IFSM {

    public static HashMap<String, String> users = new HashMap<>(); //username, password
    public static HashMap<String, String> addresses = new HashMap<>(); //username, address
    public static HashMap<String, String> tokens = new HashMap<>(); //token, username
    public static HashMap<String, ArrayList<String>> rooms = new HashMap<>(); //room, userlist
    public XMPPChatServer(int id) {
        super(id);
    }

    static int READY = 0;
    @Override
    public void init() {
        users.put("habib", "test");
        users.put("habib1", "test");
        rooms.put("TKM1", new ArrayList<>());
        rooms.put("TKM2", new ArrayList<>());

        setState(READY);
        addTransition(READY, new Message(Message.Types.REGISTER), "onClientRegister");
        addTransition(READY, new Message(Message.Types.LOGIN), "onClientLogin");
        addTransition(READY, new Message(Message.Types.ROOM_LIST_REQUEST), "returnRoomList");
        addTransition(READY, new Message(Message.Types.JOIN_ROOM_REQUEST), "joinRoom");
        addTransition(READY, new Message(Message.Types.LEAVE_ROOM_REQUEST), "leaveRoom");
        addTransition(READY, new Message(Message.Types.MSG_TO_SERVER), "incomingMsg");
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
            String tkn = "";
            tkn = randomString();
            response.addParam(Message.Params.TOKEN, tkn);
            tokens.put(tkn, msg.getParam(Message.Params.USERNAME));
            addresses.put(msg.getParam(Message.Params.USERNAME), msg.getFromAddress());
            System.out.println("Client " + msg.getParam(Message.Params.USERNAME) + " " + tkn +" registered!");
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
                String tkn = "";
                tkn = randomString();
                response.addParam(Message.Params.TOKEN, tkn);

                String prevTkn = "";
                for(Map.Entry<String, String> entry: tokens.entrySet())
                    if(entry.getValue().equals(msg.getParam(Message.Params.USERNAME))){
                        prevTkn = entry.getKey();
                        break;
                    }
                if(!prevTkn.equals("")){
                    tokens.remove(prevTkn);
                }
                tokens.put(tkn, msg.getParam(Message.Params.USERNAME));
                addresses.put(msg.getParam(Message.Params.USERNAME), msg.getFromAddress());
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
        if (tokens.get(token) != null){
            ArrayList<String> room_list = new ArrayList<>();
            for(Map.Entry<String, ArrayList<String>> entry: rooms.entrySet())
                room_list.add(entry.getKey());

            response.addParam(Message.Params.ROOM_LIST, room_list);
            response.addParam(Message.Params.MSG, "ok");
            addresses.put(msg.getParam(Message.Params.USERNAME), msg.getFromAddress());
        }else{
            response.addParam(Message.Params.MSG, "fail");
            System.out.println("NOT AUTHENTICATED!");
        }

        sendMessage(response);
    }
    public void joinRoom(IMessage message){
        System.out.println("Join room requested!");
        Message msg = (Message) message;
        String token = ((Message) message).getParam(Message.Params.TOKEN);
        Message response = new Message(Message.Types.JOIN_ROOM_RESPONSE);
        response.setToAddress(msg.getFromAddress());
        String targetRoom = msg.getParam(Message.Params.MSG);
        if (tokens.get(token) != null){
            ArrayList<String> userList = null;
            for(Map.Entry<String, ArrayList<String>> entry: rooms.entrySet())
                if(entry.getKey().equals(targetRoom)) userList = entry.getValue();

            if(userList == null){
                userList = new ArrayList<>();
                userList.add(tokens.get(token));
                rooms.put(targetRoom, userList);
                response.addParam(Message.Params.MSG, "ok|" + targetRoom);
            }else{
                if(userList.contains(tokens.get(token))){
                    response.addParam(Message.Params.MSG, "fail");
                }else{
                    userList.add(tokens.get(token));
                    response.addParam(Message.Params.MSG, "ok|" + targetRoom);
                }

            }
            sendMessage(response);
            System.out.println("sending response for join room! " + tokens.get(token));
            sendToAllUsersInRoom(targetRoom, "server", "User " + tokens.get(token) + " joined room!");
            addresses.put(msg.getParam(Message.Params.USERNAME), msg.getFromAddress());
        }else{
            response.addParam(Message.Params.MSG, "fail");
            sendMessage(response);
            System.out.println("NOT AUTHENTICATED!");
        }
    }
    public void leaveRoom(IMessage message){
        System.out.println("Leave room requested!");
        Message msg = (Message) message;
        String token = ((Message) message).getParam(Message.Params.TOKEN);
        Message response = new Message(Message.Types.LEAVE_ROOM_RESPONSE);
        response.setToAddress(msg.getFromAddress());
        String targetRoom = msg.getParam(Message.Params.MSG);

        boolean ok = false;
        if(token != null){
            ArrayList<String> usersInRoom = rooms.get(targetRoom);
            if(usersInRoom != null){
                if(usersInRoom.contains(tokens.get(token))){
                    usersInRoom.remove(tokens.get(token));
                    response.addParam(Message.Params.MSG, "ok|"+targetRoom);
                    sendToAllUsersInRoom(targetRoom, "server", "User " + tokens.get(token) + " left the room!");
                    ok = true;
                }
            }
        }
        if(!ok)
            response.addParam(Message.Params.MSG, "fail");

        sendMessage(response);
    }
    public void incomingMsg(IMessage message){
        Message msg = (Message) message;
        String token = ((Message) message).getParam(Message.Params.TOKEN);
        String targetRoom = msg.getParam(Message.Params.ROOM);
        String user = tokens.get(token);
        if(user != null){
            if (rooms.get(targetRoom) != null){
                if(rooms.get(targetRoom).contains(user)){
                    sendToAllUsersInRoom(targetRoom, user, msg.getParam(Message.Params.MSG));
                }
            }
        }
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
    public String randomString() {
        byte[] array = new byte[50]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }
    public void sendToAllUsersInRoom(String roomName, String from, String message){
        ArrayList<String> usersInRoom = rooms.get(roomName);
        if(usersInRoom != null){
            for(String user: usersInRoom) {
                Message msg = new Message(Message.Types.MSG_TO_CLIENT);
                msg.addParam(Message.Params.MSG, from + ": " + message);
                msg.setToAddress(addresses.get(user));
                sendMessage(msg);
            }
        }
    }
}
