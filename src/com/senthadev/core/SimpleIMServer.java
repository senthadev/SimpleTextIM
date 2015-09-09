package com.senthadev.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;

public class SimpleIMServer {
	private MessageDispatcher dispatcher;
	private final static Logger log = Logger.getLogger("SimpleIMServer");
	
	public void startServer(int port){
		dispatcher = new MessageDispatcher();
		try(ServerSocket server = new ServerSocket(port)){
			log.info("Simple Text IM server started in port:" + port);
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
		int port = 5000;
		if (a.length >= 1){
			try{
				port = Integer.parseInt(a[0]);
			}catch(Exception e){
				port = 5000;
			}
		}
		
		SimpleIMServer s = new SimpleIMServer();
		s.startServer(port);
	}
}
