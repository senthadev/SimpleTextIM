package com.senthadev.core.test;

import com.senthadev.core.Command;
import com.senthadev.core.CommandHandler;

public class TestJson {

	public static void main(String a[]){
		String s1 = CommandHandler.createFailResponse(404, "invalid password");
		String s2 = CommandHandler.createMessageResponse("Hello world", "alice", "public");
		String names[] = {};
		String s3 = CommandHandler.createListResponse("List action", names);
		String s4 = CommandHandler.createExitMessage();
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);
		System.out.println(s4);
		
		String s5 = "{ \"command\": \"login\", \"message\": {\"user\": \"alice\" }}";
		Command c = CommandHandler.incomingRequest(s5);
		
		System.out.println(c.getCommandType());
		System.out.println(c.getMessage());
		System.out.println(c.getTo());
		System.out.println(c.getUserName());
	}
}
