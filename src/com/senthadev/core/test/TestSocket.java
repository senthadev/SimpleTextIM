package com.senthadev.core.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TestSocket {

	public static void main(String a[]){
		System.out.println("Server Started..");
		try(ServerSocket server = new ServerSocket(5000)){
			while(true){
				try(Socket connection = server.accept() ){
					Writer out = new OutputStreamWriter(connection.getOutputStream());
					Date d = new Date();
					out.write(d.toString() + "\r\n");
					out.write("{id: \"platform\"}" + "\r\n");
					out.flush();
					
					Thread t = new Thread(new TestRun(connection));
					t.start();
					
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String s = null;
					while( (s = in.readLine()) != null){
						System.out.println(s);
						if (s.equals("exit")){
							break;
						}
						else if (s.equals("list")){
							out.write("Current members: alain, newton\r\n");
						}
						else{
							out.write(s + "\r\n");
						}
						out.flush();
					}
					connection.close();
				}catch(Exception e2){}
			}
		}catch(IOException ioe){
			System.err.println(ioe);
		}
	}
	
}

class TestRun implements Runnable{
	private Socket socket;
	
	public TestRun(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		System.out.println("thread...");
		long id = 1L;
		try{
			Writer out = new OutputStreamWriter(socket.getOutputStream());
			while(true){
				id++;
				out.write("" + id + "\r\n");
				out.flush();
				Thread.sleep(2000L);
			}
		}catch(InterruptedException ie2){
			System.err.println(ie2);
		}catch(IOException ioe){
			System.err.println(ioe);
		}
	}
	
}
