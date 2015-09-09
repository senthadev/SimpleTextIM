package com.senthadev.core;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CommandHandler {

	public static Command incomingRequest(String jsonText){
		Command c = null;
		try{
			JsonValue v1 = Json.parse(jsonText);
			if (v1.isObject()){
				JsonObject obj = v1.asObject();
				String command = obj.getString("command", "fail");
				JsonValue sub1 = obj.get("message");
				String mesg = null;
				String userName = null;
				if (sub1.isString()){
					mesg = sub1.asString();
				}
				else if(sub1.isObject()){
					JsonObject obj2 = sub1.asObject();
					userName = obj2.getString("user", "dummy");
				}
				c = new Command(command, mesg, obj.getString("to", "*"), userName);
				obj = null;
			}
		}catch(Exception e){
			c = new Command("fail", e.toString(), null, null);
		}
		return c;
	}
	
	public static String createExitMessage(){
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"command\": \"exit\", ");
		sb.append("\"message\": \"exit\"");
		sb.append("}");
		
		return sb.toString();
	}
	
	public static String createFailResponse(int error, String reason){
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"command\": \"fail\", ");
		sb.append("\"result\": {\"error\": "+error+", \"reason\": \"" + reason+"\"}");
		sb.append("}");
		
		return sb.toString();
	}
	
	public static String createMessageResponse(String message, String userName, String messageType){
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"command\": \"message\", ");
		sb.append("\"result\": {\"text\": \""+ message +"\", \"user\": \"" + userName+"\", \"type\": \""+ messageType +"\"}");
		sb.append("}");
		
		return sb.toString();
	}
	
	public static String createListResponse(String message, String [] userNames){
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"command\": \"list\", ");
		sb.append("\"result\": {\"text\": \""+ message +"\", \"users\": [");
		for(int i=0; i<userNames.length-1; i++){
			sb.append("\""+ userNames[i] + "\",");
		}
		if (userNames.length > 0){
			sb.append("\""+ userNames[userNames.length-1] + "\"]}");
		}
		else{
			sb.append("]}");
		}
		sb.append("}");
		
		return sb.toString();
	}
}
