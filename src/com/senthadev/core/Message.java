package com.senthadev.core;

public class Message {

	public static final int MESG = 1;
	public static final int KILL = 2;
	
	private int type;
	private String from;
	private String message;
	
	public Message(String message){
		this.type = MESG;
		this.message = message;
	}
	
	private Message(int type){
		this.type = type;
	}
	
	public String getMessage(){
		return message + "\r\n";
	}
	
	public static Message createKillMessage(){
		return new Message(KILL);
	}
	
	public boolean isKill(){
		if (type == KILL)
			return true;
		return false;
	}
}
