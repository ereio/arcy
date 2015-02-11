package com.dingohub.arcy.tools;

import java.util.ArrayList;

public class Channel {

	public String name;
	public ArrayList<String> users = new ArrayList<String>();
	public ArrayList<String> blackList = new ArrayList<String>();
	public ArrayList<ServerThread> userThreads = new ArrayList<ServerThread>();
	
	public Channel() {
		name = "DefaultChannelName";
	}
	
	public Channel(String name){
		this.name = name;
	}
	
	public boolean addUser(ServerThread thread, String name){
		for(String i : blackList){
			if(i == name)
				return false;
		}
		users.add(name);
		userThreads.add(thread);
		return true;
	}
	
	public void removeUser(ServerThread thread, String name){
		users.remove(name);
		userThreads.remove(thread);
	}
	
}
