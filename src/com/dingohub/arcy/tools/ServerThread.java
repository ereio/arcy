package com.dingohub.arcy.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.dingohub.arcy.ServerSetupActivity;

public class ServerThread extends Thread {
	
	private static String TAG = "ServerThread";
	public volatile InputStreamReader inputStream = null;
	public volatile BufferedReader reader = null;
	
	private Socket socket;
	private boolean init_success = false;
	private static Context appContext;
	
	//these are used to store the nickname and channel of each client
	public String nick = "";
	public String channel = null;
	public String address ="";
	public String motd = "";
	
	public volatile OutputStreamWriter outputWriter = null;
	boolean CommandUsed = false;
	
	public ServerThread(Socket clientSocket, Context context, String motd) {
		this.socket = clientSocket;
		appContext = context;
		Random rand = new Random();
		nick = "user" + rand.nextInt(2147483647);
		this.motd = motd;
		
	}
	
	public void run(){
		try{
			
			//	used to read input from client
			inputStream = new InputStreamReader( socket.getInputStream());
			reader = new BufferedReader(inputStream);
			
			// used to output to the clients socket
			outputWriter = new OutputStreamWriter(socket.getOutputStream());
			
			// saves address
			address = socket.getInetAddress().getHostAddress();
			init_success = true;
			
			// writes the message of the day to the user then flushes and enters loop
			outputWriter.write(motd);
			outputWriter.flush();
			
			String input = reader.readLine();
			while(input != null && init_success){
			
					// Logs input to server terminal
					LogServerMessage("Received:" + input
						+ "\nFrom:" + address + " Nick: " + nick);
					
					//checks if input is a command
					// pinput - Parsed Input
					String pinput[] = input.split(" ");
						
					//if input is nickname change the nickname
					if(pinput[0].equals(Commands.NICK))
						changeNickname(pinput[1], outputWriter);
					
					//if input is join,join the channel and output whoever is already in the channel
					if(pinput[0].equals(Commands.JOIN))
						subscribeToChannel(pinput[1], outputWriter);
					
					// Allows users to leave channels they've joined
					if(pinput[0].equals(Commands.LEAVE))
						unsubscribeToChannel(outputWriter);
					
					// Private messaging command
					if(pinput[0].equals(Commands.MSG))
						privateMessage(pinput, outputWriter);
					
					if(pinput[0].equals(Commands.LIST))
						listChannelUsers(outputWriter);
					
					if(pinput[0].equals(Commands.QUIT))
						quitConnection();
					
					if(pinput[0].equals(Commands.ME))
						userAction(pinput);
					// Test if command is used, if not it will write
					// if it is used it won't because commands write to output
					if(!CommandUsed){
						postInChannel(input, outputWriter);
					}
					
					// flips the command used and blocks at read line
					CommandUsed = false;
					input = reader.readLine();
			}
		} catch (IOException e){
			e.printStackTrace();
			return;
		}
	}

	/*
	 * FOLLOWING COMMANDS REQUEST OPERATIONS BACK TO SERVER THREAD 
	 */
	
	public void userAction(String[] pinput) throws IOException{
		CommandUsed = true;
		
		StringBuilder temp = new StringBuilder();
		for(int i = 1; i < pinput.length; ++i)
			temp.append(pinput[i] + " ");
		
		if(channel != null){
			ArrayList<ServerThread> userToMessage = new ArrayList<ServerThread>();
			
			// Finds the channel object in case more people added the channel
			// or state of channel changes
			// might be able to just keep a handle using array index but don't
			// know how safe that is
			// probably should use a map and not array
			for(int i = 0; i < ServerUtility.channelList.size(); ++i)
				if(channel.equals(ServerUtility.channelList.get(i).name))
					userToMessage = ServerUtility.channelList.get(i).userThreads;
			
			// Use the thread handles to send everyone messages from their threads
			for(ServerThread i: userToMessage){
				i.outputWriter.write(nick + " " + temp +"\n");
				i.outputWriter.flush();
			}
		} else {
			
			outputWriter.write(nick + " " + temp +"\n");
			outputWriter.flush();
		}
	}
	
	public void quitConnection(){
		try {
			unsubscribeToChannel(outputWriter);
			outputWriter.write("Disconnecting from Server...\n");
			outputWriter.flush();
			outputWriter.write("DISCONNECT");
			outputWriter.flush();
			outputWriter.close();
			inputStream.close();
			socket.close();
			init_success = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void listChannelUsers(OutputStreamWriter outputWriter) throws IOException{
		CommandUsed = true;
		
		if(channel != null){
			StringBuilder listingUsers = new StringBuilder();
			ArrayList<String> userArray = new ArrayList<String>();
			
			for(int i = 0; i < ServerUtility.channelList.size(); ++i)
				if(channel.equals(ServerUtility.channelList.get(i).name))
					userArray = ServerUtility.channelList.get(i).users;
			
			for(String i : userArray)
				listingUsers.append(i + " ");
			
			outputWriter.write("Users in " + channel + " are " + listingUsers.toString() + "\n");
			outputWriter.flush();
		} else {
			outputWriter.write("You're in the Lobby\n" + "Users online:" + Integer.toString(ServerUtility.clientList.size()) + "\n");
			outputWriter.flush();
		}
	}	
	
	// Default when no command is used
	public void postInChannel(String input, OutputStreamWriter outputWriter) throws IOException{
		if(channel != null){
			ArrayList<ServerThread> userToMessage = new ArrayList<ServerThread>();
			
			// Finds the channel object in case more people added the channel
			// or state of channel changes
			// might be able to just keep a handle using array index but don't
			// know how safe that is
			// probably should use a map and not array
			for(int i = 0; i < ServerUtility.channelList.size(); ++i)
				if(channel.equals(ServerUtility.channelList.get(i).name))
					userToMessage = ServerUtility.channelList.get(i).userThreads;
			
			// Use the thread handles to send everyone messages from their threads
			for(ServerThread i: userToMessage){
				i.outputWriter.write(nick + ": " + input +"\n");
				i.outputWriter.flush();
			}
		} else {
			outputWriter.write(nick + ": " + input +"\n");
			outputWriter.flush();
		}
	}
	
	// Unsubscribes users to the channel - Command.LEAVE
	public void unsubscribeToChannel(OutputStreamWriter outputWriter) throws IOException{
		Log.i(TAG, "Command.LEAVE hit");
		CommandUsed = true;
		
		if(channel != null){
			for(int i = 0; i < ServerUtility.channelList.size(); ++i)
				if(channel.equals(ServerUtility.channelList.get(i).name)){
					ServerUtility.channelList.get(i).removeUser(this, nick);
				}
			
			outputWriter.write("You've left the channel " + channel + "\n");
			outputWriter.flush();
			channel = null;
		} else {
			outputWriter.write("You're in the Lobby\n" + "Users online:" + Integer.toString(ServerUtility.clientList.size()) + "\n");
			outputWriter.flush();
		}
	}
	
	// Subscribes users to a channel - Command.JOIN
	public void subscribeToChannel(String reqChannel, OutputStreamWriter outputWriter) throws IOException{
		Log.i(TAG,"Command.JOIN hit");
		
		ArrayList<String> channelUsers = new ArrayList<String>();
		boolean channelFound = false;
		CommandUsed = true;
		
		// NEED ERROR CHECKING
		
		channel = reqChannel;
		
		// Channels are added through the Server Utility Channel Array
		// When a user joins a channel, it searchs to add the user
		// if it's a real channel, then the user is add and the channel Users nicknames
		// are added to the array for printing
		for(int i = 0; i < ServerUtility.channelList.size(); ++i){
			if(channel.equals(ServerUtility.channelList.get(i).name)){
				ServerUtility.channelList.get(i).addUser(this, nick);
				channelUsers = ServerUtility.channelList.get(i).users;
				channelFound = true;
			}
		}
		
		// if it's NOT a real channel, then the channel is add and the users is added
		// as the first user in the channel
		if(!channelFound){
			ServerUtility.channelList.add(new Channel(channel));
			ServerUtility.channelList.get(ServerUtility.channelList.size()-1).addUser(this, nick);
			channelUsers.add(nick);
		}

		
		//logs the list of everyone in the channel
		outputWriter.write("You've joined: " + channel + "\n");
	
		
		for(String i : channelUsers)
			outputWriter.append(i + " ");
		
		outputWriter.write("\n" + "Are also here\n");
		outputWriter.flush();
		
		
	}
	
	// Messages a specific person in the channel (Or server maybe) - Command.MSG
	public void privateMessage(String[] pinput, OutputStreamWriter outputWriter) throws IOException{
		Log.i(TAG, "Command.MSG hit");
		CommandUsed = true;
		
		StringBuilder temp = new StringBuilder();
		for(int i = 2; i < pinput.length; ++i)
			temp.append(pinput[i] + " ");
			
		for(int i = 0 ; i < ServerUtility.clientList.size(); ++i){
			if(pinput[1].equals(ServerUtility.clientList.get(i).nick)){
				ServerUtility.clientList.get(i).outputWriter.write(nick + " says " + temp + "\n");
				ServerUtility.clientList.get(i).outputWriter.flush();
			}
		}
		
		outputWriter.write(nick + " says " + temp + "\n");
		outputWriter.flush();
	}
	
	// init by command Commands.NICK - changes nickname of user
	public void changeNickname(String name, OutputStreamWriter outputWriter) throws IOException{
		Log.i(TAG, "Command.NICK hit");
		CommandUsed = true;

		if(name != null){
			swapChannelName(name);
			nick = name;
			outputWriter.write("Your nickname is now " + nick + "\n");
		} else {
			outputWriter.write("Invalid nickname, please choose a non-null nickname\n");
		}
		
		outputWriter.flush();
	}
	
	private void swapChannelName(String name){
		if(channel != null){
			for(int i = 0; i < ServerUtility.channelList.size(); ++i)
				if(channel.equals(ServerUtility.channelList.get(i).name)){
					ServerUtility.channelList.get(i).removeUser(this, nick);
					ServerUtility.channelList.get(i).addUser(this, name);		
			}
		}
	}
	
	//gets the nickname of the person
	public String getNickname(String address){
		return nick;
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
