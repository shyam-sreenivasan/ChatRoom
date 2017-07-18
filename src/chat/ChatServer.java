package chat;
// 1 - joined
// 2 - exited
// 3 - message
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class ChatServer {
	static final Map<String,String> mp = new HashMap<String,String>(); //session id and username
	static final Map<String,Integer> mp1 = new HashMap<String,Integer>(); // session id and joined/not joined status
	static final List<ChatRoom> chatRoomList = new ArrayList<ChatRoom>();
	static final String servername = "ChatRoomServer";
	static final Map<String,Session> mp2 = new TreeMap<String,Session>(); // session id and session object
	@OnOpen
	public void onOpen(Session session){
		String sessionid = session.getId();
		String queryString = session.getQueryString();
		String name = queryString.substring(queryString.indexOf('=')+1);
		System.out.println("Connection from "+session.getId()+"  ---->>>> "+name);
		mp.put(sessionid,name);
		mp1.put(sessionid, 0);
		mp2.put(sessionid, session);
		try {
			session.getBasicRemote().sendText(buildMessage(servername,1,"Your are connected to the server!!"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@OnMessage
	public void onMessage(String message,Session session){
		System.out.println("Received -- "+message);
		String id = session.getId();
		String name = mp.get(id);
		String[] msgArray = parseMessage(message);
		String msgType  = msgArray[0];
		String chatroomname = msgArray[1];
		String msg = msgArray[2];
		//System.out.println(chatroomname+" up here in OnMessage");
		//System.out.println("IntValue "+mp1.get(id).intValue());
		//System.out.println(msg+" is the message");
		if(msgType.equals("create") && (mp1.get(id).intValue() == 0)){
			String sendMessage;
				if(checkChatRoomExists(chatroomname)){
					 sendMessage = buildMessage(servername,2,"Chat room already exists");
				}else{
					ChatRoom chatRoom = new ChatRoom(chatroomname,session,mp);
					chatRoomList.add(chatRoom);
					mp1.remove(id);
					mp1.put(id, 1);
					System.out.println("Room created : "+ chatroomname);
					sendMessage = buildMessage(servername,1,"You creatted "+chatroomname+"!!!");
				}
					try {
						session.getBasicRemote().sendText(sendMessage);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
		}else if(msgType.equals("join") &&  (mp1.get(id).intValue() == 0)){
			    //chatroomname = parseName(message);
			System.out.println("Here in join");
			String sendMessage;
			if(!checkChatRoomExists(chatroomname)){
				//System.out.println("Inside if of join");
				sendMessage = buildMessage(servername,2,"Chat room does not exist");
				try {
					session.getBasicRemote().sendText(sendMessage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				addUserToChatRoom(chatroomname,session);
				mp1.remove(id);
				mp1.put(id, 1);
				//System.out.println(name+" joined"+chatroomname);
				//sendMessage = buildMessage(servername,1,name+" joined!!!");
				ChatRoom c = this.getChatRoom(chatroomname);
				//this.notifyUsers(c, session, 1, msg);
				try {
					session.getBasicRemote().sendText(buildMessage(servername,1,"You joined "+chatroomname));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}else if(msgType.contains("exit") &&  (mp1.get(id).intValue() == 1)){
			 //chatroomname = parseName(message);
			 System.out.println("In here too");
			if(exitFromChatRoom(chatroomname,session)){
				mp1.remove(id);
				mp1.put(id, 0);
				System.out.println(name+" left"+chatroomname);
				try {
					session.getBasicRemote().sendText(buildMessage(servername,1,"You left "+chatroomname+"!!!!"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ChatRoom c = getChatRoom(chatroomname);
				if(c != null){
					int n = c.getUsers().size();
					if(n == 0){
						chatRoomList.remove(c);
						//System.out.println("Removed chatroom "+c.getRoomName());
					}
				}
			}
		}else if(msgType.equals("send")){
			//System.out.println("Sending message");
			sendMessageToChatRoom(msg,chatroomname,session);
		}
	}
	
	@OnClose
	public void onClose(Session session){
		System.out.println("Session " +session.getId()+" has ended");
		String name = mp.get(session.getId());
		for(ChatRoom c : chatRoomList){
			if(c.getUsers().contains(session.getId())){
				notifyUsers(c,session,2,"");
				break;
			}
		}
		//this.notifyUsers(c, session, flag, message);
		
		mp.remove(session.getId());
		mp1.remove(session.getId());
		mp2.remove(session.getId());
	}
//	public String parseName(String query){
//		int n = query.indexOf('=');
//		String name = query.substring(n+1);
//		return name;
//	}
	
	public void addUserToChatRoom(String name,Session session){
		for(ChatRoom c : chatRoomList){
			String cname = c.getRoomName();
			if(name.equals(cname)){
				c.join(session);
				notifyUsers(c,session,1,"");
				break;
			}
		}
	}
	
	public boolean exitFromChatRoom(String cname,Session session){
		for(ChatRoom c : chatRoomList){
			if(c.getRoomName().equals(cname)){
				//System.out.println("Going to remove user");
				c.exit(session);
				notifyUsers(c,session,2,"");
				return true;
			}
		}
		return false;
	}
	
	public void sendMessageToChatRoom(String message,String chatroomname,Session session){
		//System.out.println("Inside sendmsgtochatroom");
		//System.out.println("Message :"+message+" ChatroomName: "+chatroomname);
		for(ChatRoom c: chatRoomList){
			//System.out.println("Chatroom name in loop "+c.getRoomName());
			if(c.getRoomName().equals(chatroomname)){
				//System.out.println("Condition true "+c.getRoomName());
				//System.out.println("Notifying users");
				notifyUsers(c,session,3,message);
				break;
			}
		}
	}
	public boolean checkChatRoomExists(String chatroomname){
			ChatRoom c = getChatRoom(chatroomname);
			if(c != null) return true;
		return false;
	}
	public ChatRoom getChatRoom(String roomname){
		for(ChatRoom c : chatRoomList){
			if(c.getRoomName().equals(roomname)) return c;
		}
		return null;
	}
	
	public String buildMessage(String host,int number,String msg){
		return host+":"+number+":"+msg;
	}
	public void notifyUsers(ChatRoom c,Session session,int flag,String message){
		System.out.println(mp2.size());
		List userlist = c.getUsers();
		System.out.println(mp2.values());
		String sessionid = session.getId();
		for(Session s : mp2.values()){
			//System.out.println("In the loop");
			if(flag == 1){
				//System.out.println("In flag 1: "+"notifying "+mp.get(s.getId()));
				//System.out.println("Current user: "+mp.get(sessionid)+"|| Other user : "+mp.get(s.getId()));
		// check if session present in chatroom and check if that session is not same as the current session
				if(userlist.contains(s.getId()) && !(s.getId().equals(sessionid))){
					try {
						s.getBasicRemote().sendText(buildMessage(servername,1,mp.get(session.getId())+" joined!!!"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				}else if(flag == 2){
					//System.out.println("In flag 2: "+"notifying "+mp.get(s.getId()));
					if(c.getUsers().contains(s.getId()) && !s.getId().equals(session.getId())){
						try {
							s.getBasicRemote().sendText(buildMessage(servername,1,mp.get(session.getId())+" exited!!!"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			
			}else if(flag == 3){
				//System.out.println("In flag 3: "+"notifying "+mp.get(s.getId()));
				//System.out.println("Inside notify users");
				if(c.getUsers().contains(s.getId())){
					try {
						s.getBasicRemote().sendText(buildMessage(mp.get(session.getId()),1,message));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				System.out.println("None");
			}
		}
  }
	
	public String[] parseMessage(String messgage){
		String[] msgArray  = messgage.split(":", 3);
		return msgArray;
	}
}

