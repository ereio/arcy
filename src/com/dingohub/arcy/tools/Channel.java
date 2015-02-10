package com.dingohub.arcy.tools;

import java.io.File;
import java.util.ArrayList;

public class Channel {

	public File record;
	public ArrayList<String> users;
	public ArrayList<String> blackList;
	
	public Channel() {
		users = new ArrayList<String>();
	}
	
	public void addUser(String name){
		for(String i : blackList){
			if(i == name)
				return;
		}
		users.add(name);
	}
	
	public void removeUser(String name){
		users.remove(name);
	}
	
	public void addMessage(){
		
	}

}
