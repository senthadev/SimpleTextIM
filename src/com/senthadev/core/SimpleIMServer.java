package com.senthadev.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;

public class SimpleIMServer {
	private static final int PORT = 5000;
	private MessageDispatcher dispatcher;
	private final static Logger log = Logger.getLogger("SimpleIMServer");
	
	public void startServer(){
		dispatcher = new MessageDispatcher();
		try(ServerSocket server = new ServerSocket(PORT)){
			log.info("Simple Text IM server started in port:" + PORT);
			while(true){
				try{
					Socket connection = server.accept(); 
					String id = UUID.randomUUID().toString();
					Actor actor = new Actor(id, connection, dispatcher);
					dispatcher.addActor(id, actor);
					
				}catch(Exception e2){}
			}
		}catch(IOException ioe){
			System.err.println(ioe);
		}
	}
	
	public static void main(String a[]){
		SimpleIMServer s = new SimpleIMServer();
		s.startServer();
	}
}
