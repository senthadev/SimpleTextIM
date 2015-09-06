package com.senthadev.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimpleServer {

	public SimpleServer(int port){
		init(port);
	}
	
	private void init2(int port){
		
		try{
			AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
			AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
			
			InetSocketAddress hostAddr = new InetSocketAddress("127.0.0.1", 5000);
			server.bind(hostAddr);
			System.out.println("Server started on port:" + port);
			System.out.println(server.getLocalAddress());
			
			Future<AsynchronousSocketChannel> acceptResult = server.accept();
			System.out.println("1");
			AsynchronousSocketChannel client = acceptResult.get();
			System.out.println("2");
			//greet the client
			client.write(ByteBuffer.wrap("Hello client!!".getBytes()));
			ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
			
			int bytesRead = client.read(byteBuffer).get(20, TimeUnit.SECONDS);
			while(bytesRead != -1){
				if(byteBuffer.position() > 2){
					byteBuffer.flip();
					byte [] lineBytes = new byte[bytesRead];
					byteBuffer.get(lineBytes, 0, bytesRead);
					String line = new String(lineBytes);
					System.out.println(line);
					client.write(ByteBuffer.wrap(line.getBytes()));
					byteBuffer.clear();
							
					bytesRead = client.read(byteBuffer).get(20, TimeUnit.SECONDS);
				}
			}
			
			/*
			 * server.accept("Client connection", 
                    new CompletionHandler<AsynchronousSocketChannel, Object>() {
                public void completed(AsynchronousSocketChannel ch, Object att) {
                    System.out.println("Accepted a connection");

                    // accept the next connection
                    server.accept("Client connection", this);

			 */
					
			System.out.println("End");
			try{
				if (client.isOpen()){
					client.close();
				}
			}catch(IOException ioe1){}
			
		}catch(InterruptedException ie){
			
		}catch(ExecutionException ee){
			
		}catch(TimeoutException te){
			
		}catch(IOException ioe){
			System.err.println("Server Failed..");
			System.err.println(ioe);
		}
	}
	
	private void init(int port){
		ExecutorService pool = Executors.newFixedThreadPool(50);
		try{
			//AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
			AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
			
			InetSocketAddress hostAddr = new InetSocketAddress("127.0.0.1", 5000);
			server.bind(hostAddr);
			System.out.println("Server started on port:" + port);
			System.out.println(server.getLocalAddress());
			
			while(true){
				Future<AsynchronousSocketChannel> acceptResult = server.accept();
				System.out.println("1");
				AsynchronousSocketChannel client = acceptResult.get();
				System.out.println("2");
				Callable<Void> task = new ClientHandler(client);
				pool.submit(task);
			}
			
		}catch(InterruptedException ie){
			
		}catch(ExecutionException eee){
			
		}catch(IOException ioe){
			System.err.println("Server Failed..");
			System.err.println(ioe);
		}
	}
	
	public static void main(String a[]){
		SimpleServer ss = new SimpleServer(5000);
	}
}

class ClientHandler implements Callable<Void>{

	private AsynchronousSocketChannel client;
	
	public ClientHandler(AsynchronousSocketChannel client) {
		this.client = client;
	}
	
	@Override
	public Void call() {
	try{
		//greet the client
		client.write(ByteBuffer.wrap("Hello client!!".getBytes()));
		ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
		
		int bytesRead = client.read(byteBuffer).get(20, TimeUnit.SECONDS);
		while(bytesRead != -1){
			if(byteBuffer.position() > 2){
				byteBuffer.flip();
				byte [] lineBytes = new byte[bytesRead];
				byteBuffer.get(lineBytes, 0, bytesRead);
				String line = new String(lineBytes);
				System.out.println(line);
				client.write(ByteBuffer.wrap(line.getBytes()));
				byteBuffer.clear();
						
				bytesRead = client.read(byteBuffer).get(20, TimeUnit.SECONDS);
			}
		}
		
		System.out.println("End");
		try{
			if (client.isOpen()){
				client.close();
			}
		}catch(IOException ioe1){}
		
	}catch(InterruptedException ie){
		
	}catch(ExecutionException ee){
		
	}catch(TimeoutException te){
		
	}finally{
		try{
			if (client.isOpen()){
				client.close();
			}
		}catch(IOException ioe1){}
	}
	
	return null;
	}
	
}