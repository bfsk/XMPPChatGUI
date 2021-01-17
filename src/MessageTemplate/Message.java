package MessageTemplate;

import FSM.IMessage;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Message implements IMessage, Cloneable {
    public static class Types{
        public static int REGISTER = 0;
        public static int REGISTER_RESPONSE = -1;
        public static int LOGIN = -4;
        public static int LOGIN_RESPONSE = 1;

        public static int ROOM_LIST_REQUEST = 4;
        public static int ROOM_LIST_RESPONSE = 5;

        public static int JOIN_ROOM_REQUEST = 55;
        public static int JOIN_ROOM_RESPONSE = 56;

        public static int LEAVE_ROOM_REQUEST = 65;
        public static int LEAVE_ROOM_RESPONSE = 66;

        public static int USERS_LIST_REQUEST = 6;
        public static int USERS_LIST_RESPONSE = 7;

        public static int MSG_TO_SERVER = 8;
        public static int MSG_TO_CLIENT = 9;

        public static int DELETE_ACCOUNT_REQUEST = 200;
        public static int DELETE_ACCOUNT_RESPONSE = 201;

        public static int RESOLVE_DOMAIN_NAME = 300;
        public static int RESOLVED = 301;
        public static int SOCKET_CLOSE = 400;
    }
    public static class Params{
        public static String DOMAIN = "domain_name";
        public static String IP = "ip_addr";
        public static String NICKNAME = "nickname";
        public static String USERNAME = "username";
        public static String PASSWORD = "password";
        public static String TOKEN = "token";
        public static String ROOM_LIST = "room_list";
        public static String MSG = "message";
        public static String CONNECTION_RESPONSE = "response";
        public static String ROOM = "room";
    }
    private int messageId;
    private int toId = 5; //TCP machine ID
    private int fromId;
    private String toAddress; //ip:port
    private String fromAddress;
    private String time;
    private Map<String, Object> msg = new HashMap<>();
    public Message(){
    }
    public Message(int messageId) {
        this.messageId = messageId;
    }
    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public void setTime(String vrijeme) {
        this.time = time;
    }

    @Override
    public int getToId() {
        return toId;
    }

    @Override
    public void setToId(int toId) {
        this.toId = toId;
    }

    @Override
    public int getFromId() {
        return fromId;
    }

    @Override
    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    @Override
    public String getToAddress() {
        return toAddress;
    }

    @Override
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Override
    public String getFromAddress() {
        return fromAddress;
    }

    @Override
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
    public Map<String, Object> getMsg(){
        return msg;
    }
    public void setMsg(HashMap<String, Object> msg){
        this.msg = msg;
    }
    public void addParam(String paramName, Object value){
        this.msg.put(paramName, value);
    }
    public Object getParam(String paramName, boolean object){return this.msg.get(paramName);}
    public String getParam(String paramName){return (String)this.msg.get(paramName);}
    @Override
    public void parseTransportMessage(byte[] messageData, int length) {
        ByteArrayInputStream input = new ByteArrayInputStream(messageData);
        XMLDecoder decoder = new XMLDecoder(input);
        Message m = (Message)decoder.readObject();
        decoder.close();
        messageId = m.getMessageId();
        fromAddress = m.getFromAddress();
        msg= m.getMsg();
    }

    @Override
    public byte[] buildTransportMessage() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(output);
        encoder.writeObject(this);
        encoder.close();
        return output.toByteArray();
    }

    @Override
    public boolean equals(IMessage message) {
        return message.getMessageId() == messageId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Message clone = (Message) super.clone();
        return clone;
    }
}
