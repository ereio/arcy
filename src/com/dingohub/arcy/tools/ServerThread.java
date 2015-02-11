package com.dingohub.arcy.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;

import com.dingohub.arcy.ServerSetupActivity;

public class ServerThread extends Thread {
	
	static InputStreamReader input = null;
	static BufferedReader reader = null;
	
	protected Socket socket;
	private boolean init_success = false;
	private static Context appContext;
	
	//these are used to store the nickname and channel of each client
	String Nick = "";
	String channel = "";
	String address ="";
	
	public ServerThread(Socket clientSocket, Context context) {
		this.socket = clientSocket;
		appContext = context;
	}
	
	public void run(){
		try{
			
			//used to read input from client
			input = new InputStreamReader( socket.getInputStream());
			reader = new BufferedReader(input);
			
			init_success = true;
		
		OutputStream output = socket.getOutputStream();
			OutputStreamWriter outputWriter = 
					new OutputStreamWriter(output);
			
			
			String line = reader.readLine();
			while(line != null){
				try{
					
					//checks if input is a command
						String name[] = line.split(" ");
						address = socket.getInetAddress().getHostAddress();
						//if input is nickname change the nickname
					if(name[0].equals(Commands.NICK))
					{
						//changes the nickname
					changeNickname(name[1], address);
					
					}
					//if input is join,join the channel and output whoever is already in the channel
					if(name[0].equals(Commands.JOIN))
					{
						subscribeToChannel(name);
						
					}
					
					LogServerMessage("Received:" + line
							+ "\nFrom:" + getNickname(address));
					
					
					
					outputWriter.write("hello"+"/n");
						output.flush();
				
						line = reader.readLine();
						
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		} catch (IOException e){
			
		}
		
		
	
	}

	/*
	 * FOLLOWING COMMANDS REQUEST OPERATIONS BACK TO SERVER THREAD 
	 */
	
	public void unsubscribeToChannel(){
		
	}
	
	public void subscribeToChannel(String name[]){
		
		
		ArrayList<String> inChannel = new ArrayList<String>();
		channel = name[1]; 
		//sets the channel name that the person joined
		//make this an array to be able to join multiple channels
		
		//for loop will access the handler of other threads and check
		//if they are in the channel the client joined
		for(int i = 0 ; i < ServerUtility.clientList.size(); ++ i)
		{
			//adds the people who are in the channel to the inchannel array
			if(channel.equals(ServerUtility.clientList.get(i).channel))
			{
				if(ServerUtility.clientList.get(i).Nick.equals( ""))
				inChannel.add(ServerUtility.clientList.get(i).address);
				else
					inChannel.add(ServerUtility.clientList.get(i).Nick);
					
				
				
				
			}
		}
		
		//logs the list of everyone in the channel
		LogServerMessage("Joined: " + channel );
		for(int i = 0 ; i < inChannel.size(); ++i)
		LogServerMessage( inChannel.get(i));
		
		//!!! important!!!!!!!
		//instead of logging to the Server it should send a message back to the client telling them
		//who is in the server but however the outputWriter.write(String) function is not sending anything to 
		//the client its not "echoing anything back" so the client buffer.readline is blocking and not receiving anything
		//couldn't figure it out , however i think the same reasoning could be used to send private messages and outputing
		//messages to all that are in the same channel.
		
		LogServerMessage( "Are also here");
		
		
	}
	
	public void messageToChannel(){
		
	}
	
	//changes the nickname
	public void changeNickname(String name,String address){
	
			Nick = name;
	}
	
	//gets the nickname of the person
	public String getNickname(String address)
	{
		if(Nick.equals(""))
		return address;
		else
			return Nick;
	}
	/**
	 * Output the given message to the Server Activity's log
	 * 
	 * @param message The message to be written
	 */
	private static void LogServerMessage(String message) {
		ServerSetupActivity activity = (ServerSetupActivity) appContext;
		activity.LogMessage(message);
	}
}
