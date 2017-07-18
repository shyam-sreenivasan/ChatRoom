/**
 * 
 */
// Message format to be sent  -> type of message : chatroomname : message
window.onload = this;

var webSocket;
var roomname;
var conn;
var name;


function init(){
	conn = false;
	roomname = undefined;
	name = undefined;
}


 function scroll(){
	document.getElementById("textarea").scrollTop = document.getElementById("textarea").scrollHeight +1;
}



function connect(){
	if("WebSocket" in window){
		if(webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED){
	        alert("You are already connected");
	         return;
	     }
		var text = document.getElementById("name");
		 name = text.value;
		if(name != null && name != "" && conn == false){
			var url = "ws://192.168.1.7:8080/ChatRoom1/chat";
			var fullurl = url+"?"+"name="+name;
			webSocket = new WebSocket(fullurl);
			webSocket.onopen = function(event){
				if(event.data === undefined){
					return;
				}
			};
			conn = true;
			webSocket.onmessage = function(event){
				//writeResponse(event.data);
				console.log(event.data);
				var response = event.data;
				var arr = splitResponse(response);
				msg = arr[2];
				console.log(arr);
				if(arr[1] == 1 || arr[1] == '1'){
					if(arr[0] == "ChatRoomServer") {
						if(msg.includes("connected")) alert(msg);
						else
						writeResponse(msg);
					}
					else writeResponse(arr[0]+":"+msg);
				}else if(arr[1] == 2 || arr[1] == '2'){
					alert(msg);
					//$('.nav-tabs a[href="#Home"]').tab('show');
					//$('.nav-tabs a[href="#Chat"]').removeClass('active');
					roomname = undefined;
				}
			}
			
			webSocket.onclose = function(event){
				writeResponse("Connection closed");
			}
		}else{
			alert("Enter valid name and connect");
		}
		
	}else{
		writeRespone("Browser Doesnt Support Websockets");
	}
	
}

function send(){
	if(conn == true){
		var text = document.getElementById("inputtext");
	    var value = text.value.trim();
	    if(roomname == undefined){
	    	alert("room not chosen");
	    	return;
	    }
	    if(value == null || value == ""){
	    	alert("Cant send empty message");
	    }else{
	    	text.value = "";
	        var message = buildMessage("send",roomname,value);
	        webSocket.send(message);
	        scroll();
	    }
	} else{
		alert("Make sure you are connected");
	}  
    
}

function createroom(){
	if(conn==true){
		if(roomname == undefined){
			var chatroomname = document.getElementById("createid").value;
			chatroomname = chatroomname.trim();
			if(chatroomname != null && chatroomname != ""){
				var message = buildMessage("create",chatroomname,"");
				webSocket.send(message);
				document.getElementById("createid").value = "";
				 roomname = chatroomname;
				// $('.nav-tabs a[href="#Chat"]').tab('show');
			}else{
				alert(" Create Room value cant be empty");
			}
		}else{
			alert("You are already connected to "+roomname);
		}	
	}else{
		alert("Make sure you are connected");
	}
}
function joinroom(){
	if(conn == true ){
		if(roomname == undefined){
			var chatroomname = document.getElementById("selectroom").value;
			chatroomname = chatroomname.trim();
			if(chatroomname != null && chatroomname != ""){
			var message = buildMessage("join",chatroomname,"");
			document.getElementById("selectroom").value = "";
			webSocket.send(message);
			 roomname = chatroomname;
			 //$('.nav-tabs a[href="#Chat"]').show();
		}else{
				alert(" Join room cannot be empty");
			}
	}else{
			alert("You are already connected to "+roomname);
		}
	}else{
		alert("Make sure you are connected to the server");
	}
}

function exitroom(){
	if(conn == true && roomname != undefined){
		var message = buildMessage("exit",roomname,"");
		webSocket.send(message);
		roomname="";
		roomname = undefined;
		//$('.nav-tabs a[href="#Home"]').tab('show');
		
		setTimeout(function(){
			document.getElementById("textarea").innerHTML = "";
		},3000);
	}else{
		alert("Cannot exit if you are not connected or joined");
	}
}
function closeSocket(){
	if(conn == true){
	    webSocket.close();
	    init();
	    //alert("closed");
	    setTimeout(function(){
			document.getElementById("textarea").innerHTML = "";
		},3000);
	}else{
		alert("Already closed");
	}
	
}

function toggle(button){
	if(button.disabled){
		button.disabled = false;
	}else{
		button.disabled = true;
	}
}

function writeResponse(text){
    document.getElementById("textarea").innerHTML +=  "\n"+ text;
}

function buildMessage(type,roomname,msg){
	return type+":"+roomname+":"+msg;
}
function splitResponse(response){
	arr = response.split(':');
	var msg = arr[2];
	for(i=3;i<arr.length;i++){
		msg += ":"+ arr[i];
	}
	return arr;
}

