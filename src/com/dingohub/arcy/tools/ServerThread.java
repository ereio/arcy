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
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.dingohub.arcy.ServerSetupActivity;

public class ServerThread extends Thread {
	
	private static String TAG = "ServerThread";
	static InputStreamReader inputStream = null;
	static BufferedReader reader = null;
	
	private Socket socket;
	private boolean init_success = false;
	private static Context appContext;
	
	//these are used to store the nickname and channel of each client
	public String nick = "";
	public String channel = "";
	public String address ="";
	
	public ServerThread(Socket clientSocket, Context context) {
		this.socket = clientSocket;
		appContext = context;
		Random rand = new Random();
		nick = "user" + rand.nextInt(2147483647);
		
	}
	
	public void run(){
		try{
			
			//	used to read input from client
			inputStream = new InputStreamReader( socket.getInputStream());
			reader = new BufferedReader(inputStream);
			
			// used to output to the clients socket
			OutputStreamWriter outputWriter = new OutputStreamWriter(socket.getOutputStream());
			
			// saves address
			address = socket.getInetAddress().getHostAddress();
			init_success = true;
				
			
			String input = reader.readLine();
			while(input != null && init_success){
			
					//checks if input is a command
					// pinput - Parsed Input
					String pinput[] = input.split(" ");
						
					//if input is nickname change the nickname
					if(pinput[0].equals(Commands.NICK))
						changeNickname(pinput[1], outputWriter);
					
					//if input is join,join the channel and output whoever is already in the channel
					if(pinput[0].equals(Commands.JOIN))
						subscribeToChannel(pinput);
					
					// Allows users to leave channels they've joined
					if(pinput[0].equals(Commands.LEAVE))
						unsubscribeToChannel(pinput);
					
					if(pinput[0].equals(Commands.MSG))
						messageToChannel(pinput);
						
					LogServerMessage("Received:" + input
									+ "\nFrom:" + address + " Nick: " + nick);
					
					
					
					outputWriter.write(nick + ": " + input +"\n");
					outputWriter.flush();
				
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
	
	// Unsubscribes users to the channel - Command.LEAVE
	public void unsubscribeToChannel(String[] pinput){
		Log.i(TAG, "Command.LEAVE hit");
	}
	
	// Subscribes users to a channel - Command.JOIN
	public void subscribeToChannel(String input[]){
		Log.i(TAG,"Command.JOIN hit");
		
		ArrayList<String> inChannel = new ArrayList<String>();
		channel = input[1]; 
		//sets the channel name that the person joined
		//make this an array to be able to join multiple channels
		
		//for loop will access the handler of other threads and check
		//if they are in the channel the client joined
		for(int i = 0 ; i < ServerUtility.clientList.size(); ++i)
		{
			//adds the people who are in the channel to the inchannel array
			if(channel.equals(ServerUtility.clientList.get(i).channel))
					inChannel.add(ServerUtility.clientList.get(i).nick);
				
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
		
		LogServerMessage("Are also here");
		
		
	}
	
	// Messages a specific person in the channel (Or server maybe) - Command.MSG
	public void messageToChannel(String[] pinput){
		Log.i(TAG, "Command.MSG hit");
	}
	
	// init by command Commands.NICK - changes nickname of user
	public void changeNickname(String name, OutputStreamWriter outputWriter) throws IOException{
		Log.i(TAG, "Command.NICK hit");
		
		if(name != null){
			nick = name;
			outputWriter.write("Nickname is now " + nick);
		} else {
			outputWriter.write("Invalid nickname, please choose a non-null nickname");
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
