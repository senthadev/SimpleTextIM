package com.senthadev.core;

public class Command {

	public static final int LOGIN = 1;
	public static final int SEND = 2;
	public static final int LIST = 3;
	public static final int EXIT = 4;
	public static final int FAIL = 5;
	
	private int commandType;
	private String message;
	private String to;
	private String userName;
	
	public Command(String command, String message, String to, String userName) {
		super();
		commandType = -1;
		if ("send".equals(command)){
			commandType = Command.SEND;
		}
		else if ("list".equals(command)){
			commandType = Command.LIST;
		}
		else if ("login".equals(command)){
			commandType = Command.LOGIN;
		}
		else if ("exit".equals(command)){
			commandType = Command.EXIT;
		}
		else if ("fail".equals(command)){
			commandType = Command.FAIL;
		}
		this.message = message;
		this.to = to;
		this.userName = userName;
		
		if (commandType == -1){
			commandType = Command.FAIL;
			this.message = "Unidentified command[" + command.substring(0,  10) +"] received";
		}
	}
	
	public int getCommandType() {
		return commandType;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getTo() {
		return to;
	}
	
	
}
