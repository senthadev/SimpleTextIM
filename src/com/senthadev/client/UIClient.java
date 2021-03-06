package com.senthadev.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


public class UIClient extends JFrame{

	private String serverIP = "127.0.0.1";
	private int port = 5000;
	private String loginName;
	private String password;
	
	private JTextArea messages;
	private JScrollPane pane1;
	private JTextField sendText;
	
	private BlockingQueue<String> mailBox;
	
	public UIClient(String serverIP, int port, String loginName, String password){
		super("Text IM Client");
		
		this.serverIP = serverIP;
		this.port = port;
		this.loginName = loginName;
		this.password = password;
		mailBox = new ArrayBlockingQueue<String>(100, false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
        
		messages = new JTextArea();
		messages.setColumns(40);
		messages.setLineWrap(true);
		messages.setRows(20);
		messages.setWrapStyleWord(true);
		pane1 = new JScrollPane(messages);
		getContentPane().add(pane1, BorderLayout.CENTER);
		
		sendText = new JTextField(40);
		sendText.addActionListener(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String line = sendText.getText();
				if (line == null || line.trim().length() <= 0){
					return;
				}
				line = line.trim();
				try{
					if (line.startsWith("list")){
						mailBox.put(endcodeMesg("list", line, "*")+ "\r\n");
						displayResponse("[me]" + line);
					}
					else if(line.startsWith("send")){
						String m = line.substring(4);
						mailBox.put(endcodeMesg("send", m.trim(), "*")+ "\r\n");
						displayResponse("[me]" + m.trim());
					}
					else if(line.startsWith("private:")){
						int start = line.indexOf("private:");
						int end = line.indexOf(' ');
						String userName = null;
						if (end > 0){
							userName = line.substring(start+8, end);
							String m = line.substring(end);
							mailBox.put(endcodeMesg("send", m.trim(), userName)+ "\r\n");
							displayResponse("[me]" + m.trim());
						}
						else{
							displayResponse("Invalid private message!!, [private:alice hello world]");
						}
					}
					else if(line.startsWith("exit")){
						mailBox.put("exit"+ "\r\n");
						displayResponse("[me]" + "exit");
					}
					else{
						displayResponse("Command missing!!, what should I do with:[" + line + "]");
					}
					
				}catch(Exception e1){
					displayResponse(e1.toString());
				}
				//messages.append(line + "\r\n");
				sendText.setText("");
			}
		});
		getContentPane().add(sendText, BorderLayout.SOUTH);
		setTitle(loginName);
		setSize(200, 100);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		pack();
		setVisible(true);
		
		startSocket();
	}
	
	public String endcodeMesg(String command, String mesg, String name){
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"command\": \"" + command + "\", ");
		sb.append("\"message\": \""+ mesg +"\", ");
		sb.append("\"to\": \""+ name +"\"}");
		
		return sb.toString();
	}
	
	public void startSocket(){
		displayResponse("Connecting to :" + serverIP +":"+ port);
		Socket socket = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try{
			socket = new Socket(serverIP, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("utf-8")));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("utf-8")));
			
			ChatWriter cw = new ChatWriter(this, writer, mailBox);
			cw.start();
			
			ChatReader cr = new ChatReader(this, reader);
			cr.start();
			
			displayResponse("Connected");
			//Send the login message
			StringBuilder sb = new StringBuilder();
			sb.append("{ \"command\": \"login\", ");
			sb.append("\"message\": {\"user\": \"");
			sb.append(loginName).append("\" }");
			sb.append("}");
			
			mailBox.put(sb.toString() + "\r\n");
			
		}catch(Exception e){
			displayResponse(e.toString());
		}
		finally{
		}
	}
	
	public void displayResponse(String line){
		messages.append(line + "\r\n");
	}
	
	public static void main(String args[]){
		//java UIClient ip loginname password
		if (args.length >= 4){
			final String serverIP = args[0];
			final int port = Integer.parseInt(args[1]);
			final String loginName = args[2];
			final String password = args[3];
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new UIClient(serverIP, port, loginName, password);
				}
			});
		}
		else{
			System.out.println("java com.senthadev.client.UIClient 127.0.0.1 5000 alice alice_secrect");
		}
	}
}

class ChatReader extends Thread{
	
	private UIClient display;
	private BufferedReader reader;
	
	public ChatReader(UIClient display, BufferedReader reader){
		this.display = display;
		this.reader = reader;
	}
	
	@Override
	public void run() {
		try{
			String s = null;
			while( (s = reader.readLine()) != null){
				JsonValue v1 = Json.parse(s);
				if (v1.isObject()){
					JsonObject obj1 = v1.asObject();
					String comm = obj1.getString("command", "dummy");
					JsonValue re = obj1.get("result"); 
					if ("message".equals(comm)){
						JsonObject obj2 = re.asObject();
						String userName = obj2.getString("user", "undefined");
						String text = obj2.getString("text", "undefined");
						String type = obj2.getString("type", "public");
						if ("public".equals(type))
							display.displayResponse("["+userName+"] " + text);
						else
							display.displayResponse("["+userName+":private] " + text);
					}
					else if ("list".equals(comm)){
						JsonObject obj2 = re.asObject();
						String text = obj2.getString("text", "undefined");
						JsonValue names = obj2.get("users");
						JsonArray namesArray = names.asArray();
						display.displayResponse(text + ":" + namesArray.toString());
					}
					else if ("fail".equals(comm)){
						JsonObject obj2 = re.asObject();
						String reason = obj2.getString("reason", "undefined");
						display.displayResponse("Failed: " + reason);
					}
					else{
						display.displayResponse(s);
					}
				}
				else{
					display.displayResponse(s);
				}
			}
		}catch(Exception e){
			display.displayResponse(e.toString());
		}
	}
	
}

class ChatWriter extends Thread{
	
	private UIClient display;
	private BufferedWriter writer;
	private BlockingQueue<String> mailBox;
	
	public ChatWriter(UIClient display, BufferedWriter writer, BlockingQueue<String> mailBox){
		this.display = display;
		this.writer = writer;
		this.mailBox = mailBox;
	}
	
	@Override
	public void run() {
		try{
			while(true){
				String mesg = mailBox.take();
				writer.write(mesg);
				writer.flush();
			}
		}catch(Exception e){
			display.displayResponse(e.toString());
		}finally{
			display.displayResponse("Restart the client for a new session");	
		}
	}
	
}
