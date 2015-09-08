package com.senthadev.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher {

	private ConcurrentHashMap<String, Actor> actorQueue;
	
	public MessageDispatcher(){
		actorQueue = new ConcurrentHashMap<>();
	}
	
	public void addActor(String id, Actor actor){
		actorQueue.put(id, actor);
		actor.start();
		System.out.println("add..");
	}
	
	public void removeActor(String id){
		actorQueue.remove(id);
	}
	
	public void sendAll(String id, Message mesg){
		for(Map.Entry<String, Actor> entry : actorQueue.entrySet()){
			if (!entry.getKey().equals(id)){
				entry.getValue().sendMessage(mesg);
			}
		}
	}
	
	public void dispatch(String id, String receivedText){
		System.out.println(id + ": receivedText:" + receivedText);
		sendAll(id, new Message(receivedText));
	}

}
