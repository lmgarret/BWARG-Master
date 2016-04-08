package com.bwarg.master.network;

import java.util.IllegalFormatException;

import eu.hgross.blaubot.core.BlaubotDevice;
import eu.hgross.blaubot.core.IBlaubotDevice;

/**
 * Created by LM on 07.04.2016.
 */
public class Message {
    private static final String FROM_ID_TAG = "_[fromID]";
    private static final String TO_ID_TAG = "_[toID]";
    private static final String DATA_TAG = "_[data]";

    public static final String ALL_ID = "all";

    private Header header;
    private String fromID;
    private String toID;
    private String data;

    public Message(Header header,String fromID, String toID, String data){
        this.header = header;
        this.fromID = fromID;
        this.toID = toID;
        this.data = data;
    }
    public Message(Header header, BlaubotDevice fromDevice, BlaubotDevice toDevice, String data){
        this(header, fromDevice.getUniqueDeviceID(), toDevice.getUniqueDeviceID(), data);
    }

    @Override
    public String toString(){
        return header+FROM_ID_TAG+fromID+TO_ID_TAG+toID+DATA_TAG+data;
    }
    public static Message parseMessage(String receivedMessage) throws IllegalArgumentException {
        if(isValidMessage(receivedMessage)){
            String headerString = receivedMessage.substring(0,3);
            Header msgheader = null;
            for(Header header : Header.values()){
                if(header.equalsName(headerString)){
                    msgheader = header;
                }
            }
            if(msgheader == null){
                throw new IllegalArgumentException("With header : "+msgheader);
            }
            int fromIDIndex = receivedMessage.indexOf("_[fromID]")+9;
            int toIDIndex = receivedMessage.indexOf("_[toID]")+7;
            int dataIndex = receivedMessage.indexOf("_[data]")+7;

            String fromID = receivedMessage.substring(fromIDIndex, toIDIndex-7);
            String toID = receivedMessage.substring(toIDIndex, dataIndex-7);
            String data = receivedMessage.substring(dataIndex);
            return new Message(msgheader,fromID,toID,data);
        }else{
            throw new IllegalArgumentException();
        }

    }
    public static boolean isValidMessage(String receivedMessage){
        return receivedMessage.contains("_[fromID]") && receivedMessage.contains("_[toID]")&&receivedMessage.contains("_[data]");
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getFromID() {
        return fromID;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public String getToID() {
        return toID;
    }

    public void setToID(String toID) {
        this.toID = toID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    protected class Builder{
        public Header header;
        public String fromID;
        public String toID;
        public String data;

        public Message build(){
            return new Message(header, fromID,toID,data);
        }
    }
    public static class Broadcast extends Message{
        public Broadcast(Header header, IBlaubotDevice fromDevice, String data){
            super(header,fromDevice.getUniqueDeviceID(),Message.ALL_ID, data);
        }
    }
}
