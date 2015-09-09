package com.senthadev.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Actor extends Thread{

	private String id;
	private String name;
	private Socket socket;
	private MessageDispatcher dispatcher;
	private BlockingQueue<Message> mailBox;
	private final static Logger log = Logger.getLogger("Actor");
	private BufferedWriter writer;
	private BufferedReader reader;
	private Thread listener;
	private boolean running;
	
	public Actor(String id, Socket socket, MessageDispatcher dispatcher) throws IOException{
		this.id = id;
		this.socket = socket;
		this.dispatcher = dispatcher;
		mailBox = new ArrayBlockingQueue<Message>(100, false);
		init();
	}
	
	private void init() throws IOException{
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("utf-8")));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("utf-8")));
		listener = new Thread(new Actorlistener(this));
		listener.start();
		this.running = true;
		
		log.info("Actor["+id+"] initialized from address:" + socket.getInetAddress().getHostAddress());
	}
	
	public String getActorName(){
		return this.name;
	}
	
	public void setActorName(String name){
		this.name = name;
	}
	
	public BufferedReader getReader() throws IOException{
		return reader;
	}
	
	public void sendMessage(Message mesg){
		try{
			mailBox.put(mesg);
		}catch(Exception e){
			log.log(Level.WARNING, "Failed...");
		}
	}
	
	public void dispatch(String s){
		dispatcher.dispatch(id, s);
	}
	
	public void closeListener(){
		try{
			Message m = Message.createKillMessage();
			sendMessage(m);
			reader.close();
			this.listener.join();
		}catch(Exception e){
			
		}finally{
			running = false;
		}
	}
	
	@Override
	public void run() {
		try{
			while(running){
				Message mesg = mailBox.take();
				if (mesg.isKill()){
					break;
				}
				writer.write(mesg.getMessage());
				writer.flush();
			}
		}catch(Exception e){
			log.log(Level.WARNING, e.toString());
		}finally{
			try{
				writer.close();
				if(!socket.isClosed()){
					socket.close();
				}
			}catch(Exception e){}
			//notify dispatcher to remove it self.
			log.info("Actor["+ this.id +"] ended");
			dispatcher.removeActor(id);
		}
	}
}

class Actorlistener implements Runnable{
	
	private Actor actor;
	private final static Logger log = Logger.getLogger("Actorlistener");
	
	public Actorlistener(Actor actor){
		this.actor = actor;
	}
	
	@Override
	public void run() {
		try{
			String s = null;
			while((s = actor.getReader().readLine()) != null){
				if (s.equals("exit")){
					break;
				}
				actor.dispatch(s);
			}
		}catch(Exception e){
			log.log(Level.WARNING, "READING: " + e.toString());
		}finally{
			//notify Actor to end the service.
			actor.dispatch(CommandHandler.createExitMessage());
			actor.closeListener();
		}
	}
	
}
