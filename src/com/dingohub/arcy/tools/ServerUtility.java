package com.dingohub.arcy.tools;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dingohub.arcy.ServerSetupActivity;

public class ServerUtility{
	static final String TAG = "ServerThreadHandler";
	static final int DEFAULT_PORT = 6665;
	static int currentPort = 0;
	static ServerSocket serverSocket;
	static String motd;
	private static Context appContext;
	public static volatile ArrayList<ServerThread> clientList = new ArrayList<ServerThread>();  
	public static volatile ArrayList<Channel> channelList = new ArrayList<Channel>();
	private static boolean acceptingConnections;
	private static String MOTD_DEFAULT = "Welcome to this IRC Server!\n"
			+ "Commands are: \n"
			+ "/join #<channel_name>\n"
			+ "/msg <nick_name> <message>\n"
			+ "/nick <new_nick_name>\n" 
			+ "/me <message_of_action>\n"
			+ "/leave \n"
			+ "/quit \n";
	
	
	
	/**
	 * Create a Runnable for executing server based operations off the 
	 * main UI thread
	 * 
	 * @param appContext The Context from the calling Activity, 
	 * 			used as a cheap way to interact with the Activity
	 * @param port The port on which we want the server to listen for 
	 * 			incoming connections
	 * @param userMOTD the server creators requested message of the day for all
	 * 			who will login to the irc server
	 * @return The configured runnable
	 */
	public static Runnable getServerRunnable(Context context, int port, String userMOTD) {
		appContext = context;
		
		if (!(port < 1 || port > 65535))
			currentPort = port;
		else if(port == 0)
			currentPort = DEFAULT_PORT;
		else
			Toast.makeText(appContext, "Port selected is not valid", Toast.LENGTH_SHORT).show();
			
		
		LogServerMessage("Server initalization success: Port" + currentPort);
		
		if(userMOTD != null)
			motd = userMOTD;
		else{
			motd = MOTD_DEFAULT;
		}
		LogServerMessage("Server MOTD: " + motd);
		
		return new Runnable(){
			@Override
			public void run() {
				initServerSocket(currentPort);
				Socket clientSocket = null;
				try{
					
					while(acceptingConnections){
						LogServerMessage("Server now listening for connections...");
						
						clientSocket = serverSocket.accept();
						
						LogServerMessage("Client connected from address:" 
									+ clientSocket.getInetAddress().getHostAddress());
						ServerThread thread = new ServerThread(clientSocket, appContext, motd);
						clientList.add(thread);
						thread.start();
						
					}
					
					LogServerMessage("Stopping Server");
				} catch (SocketException sockex){
					LogServerMessage("Stopping Server");
				} catch (IOException e){
					e.printStackTrace();
					Log.e(TAG, "ServerSocket IO Error");
				}
			}
		};

	}
	
	/**
	 * Initialize the server socket on the given port
	 * 
	 * @param port The port we will be using to listen for incoming 
	 * 			connections
	 */
	private static void initServerSocket(int port) {
		try {
			// Create the server socket
			serverSocket = new ServerSocket(port);
			acceptingConnections = true;
		} catch (IOException ioex) {
			Log.e(TAG, ioex.getLocalizedMessage(), ioex);
			LogServerMessage("Server initalization Failed");
		}
	}
	
	/**
	 * Shutdown the server socket
	 */
	public static void shutDownServer() {
		try {
			if (serverSocket != null) {
				// Close the server socket, any attempts to connect after 
				// this is called will fail
				
				serverSocket.close();
			}
		} catch (IOException ioex) {
			Log.e(TAG, ioex.getLocalizedMessage(), ioex);
		}
		
		acceptingConnections = false;
		serverSocket = null;
	}
	
	// Stops the server threads
	public void shutdownServerThreads(){
		
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
