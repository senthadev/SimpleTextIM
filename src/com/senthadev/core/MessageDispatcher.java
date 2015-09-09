package com.senthadev.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDispatcher {

	private ConcurrentHashMap<String, Actor> actorMap;
	private ConcurrentHashMap<String, String> nameMap;
	private final static Logger log = Logger.getLogger("MessageDispatcher");
	
	public MessageDispatcher(){
		actorMap = new ConcurrentHashMap<String, Actor>();
		nameMap = new ConcurrentHashMap<String, String>();
	}
	
	public void addActor(String id, Actor actor){
		actorMap.put(id, actor);
		actor.start();
		log.log(Level.INFO, "New actor added");
	}
	
	public void removeActor(String id){
		String userName = actorMap.get(id).getActorName();
		nameMap.remove(userName);
		actorMap.remove(id);
	}
	
	public void dispatch(String id, String receivedText){
		log.log(Level.INFO, id + ", text:" + receivedText);
		Command c = CommandHandler.incomingRequest(receivedText);
		switch(c.getCommandType()){
			case Command.LOGIN:
				processLogin(id, c);
				break;
			case Command.SEND:
				processSend(id, c);
				break;
			case Command.LIST:
				processList(id, c);
				break;
			case Command.EXIT:
				processExit(id, c);
				break;
			case Command.FAIL:
				processFail(id, c);
				break;
		}
	}
	
	private void processLogin(String id, Command c){
		try{
			if ("dummy".equals(c.getUserName())){
				throw new Exception("Username is missing");
			}
			nameMap.put(c.getUserName(), id);
			actorMap.get(id).setActorName(c.getUserName());
			log.log(Level.INFO, c.getUserName() + ", Login successful");
			sendAll(id, new Message(CommandHandler.createMessageResponse("Online !", c.getUserName(), "public")));
		}catch(Exception e){
			send(id, new Message( CommandHandler.createFailResponse(404, e.toString())));
		}
	}
	
	private void processSend(String id, Command c){
		Actor sender = actorMap.get(id);
		if ("*".equals(c.getTo())){
			sendAll(id, new Message(CommandHandler.createMessageResponse(c.getMessage(), sender.getActorName(), "public")));
		}
		else{
			String receiverId = nameMap.get(c.getTo());
			if (receiverId != null){
				send(receiverId, new Message( CommandHandler.createMessageResponse(c.getMessage(), sender.getActorName(), "private")));
			}
			else{
				send(id, new Message( CommandHandler.createFailResponse(400, "User <" +c.getTo()+ "> is offline or user doesn't exists.")));
			}
		}
	}
	
	private void processExit(String id, Command c){
		String userName = actorMap.get(id).getActorName();
		sendAll(id, new Message(CommandHandler.createMessageResponse("Offline !", userName, "public")));
	}
	
	private void processList(String id, Command c){
		String [] userNames = new String[nameMap.size() >= 1 ? nameMap.size() - 1: 0];
		int index = 0;
		for(Map.Entry<String, String> entry : nameMap.entrySet()){
			if (!entry.getValue().equals(id)){
				userNames[index++] = entry.getKey();
			}
		}
		send(id, new Message(CommandHandler.createListResponse("Online list", userNames)));
	}
	
	private void processFail(String id, Command c){
		send(id, new Message(CommandHandler.createFailResponse(400, "Failed while processing the request")));
	}
	
	private void send(String id, Message mesg){
		Actor client = actorMap.get(id);
		if (client != null)
			client.sendMessage(mesg);
	}
	
	private void sendAll(String id, Message mesg){
		for(Map.Entry<String, Actor> entry : actorMap.entrySet()){
			if (!entry.getKey().equals(id)){
				entry.getValue().sendMessage(mesg);
			}
		}
	}
	
	/*
	 * public void dispatch(String id, String receivedText){
		System.out.println(id + ": receivedText:" + receivedText);
		if ("exit".equals(receivedText)){
			System.out.println("exit received");
			//send(id, Message.createKillMessage());
			return;
		}
		sendAll(id, new Message(receivedText));
	}
	 
	public void sendAll(String id, Message mesg){
		for(Map.Entry<String, Actor> entry : actorMap.entrySet()){
			if (!entry.getKey().equals(id)){
				entry.getValue().sendMessage(mesg);
			}
		}
	}
	
	public void send(String id, Message mesg){
		actorMap.get(id).sendMessage(mesg);
	}
	
	*/
	

}
