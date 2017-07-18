package chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;


public class ChatRoom {
	private String name; 
	private List<String> userList;
	private Map<String,String> idname;
	ChatRoom(String name,Session session,Map<String,String> idname){
		this.name = name;
		userList = new ArrayList<String>();
		this.idname = idname;
		userList.add(session.getId());
	}
	
	public void join(Session session){
		userList.add(session.getId());
		//notifyUsers("",idname.get(session.getId())+" joined......",session);
	}
	
	public void exit(Session session){
		userList.remove(userList.indexOf(session.getId()));
		System.out.println(idname.get(session.getId())+" exited");
	}
	
	public String getRoomName(){
		return this.name;
	}
	
	
	public List<String> getUsers(){
		return userList;
	}
}
