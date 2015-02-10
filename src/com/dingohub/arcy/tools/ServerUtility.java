package com.dingohub.arcy.tools;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.dingohub.arcy.ServerSetupActivity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ServerUtility{
	static final String TAG = "ServerThreadHandler";
	static final int DEFAULT_PORT = 6665;
	static int currentPort = 0;
	static ServerSocket serverSocket;
	
	private static Context appContext;
	private static ArrayList<ServerThread> clientList;
	private static boolean acceptingConnections;
	
	
	public static Runnable getServerRunnable(Context context, int port) {
		appContext = context;
		
		if (!(port < 1 || port > 65535))
			currentPort = port;
		else if(port == 0)
			currentPort = DEFAULT_PORT;
		else
			Toast.makeText(appContext, "Port selected is not valid", Toast.LENGTH_SHORT).show();
			
		
		clientList = new ArrayList<ServerThread>();
		
		initServerSocket(currentPort);
		LogServerMessage("Server initalization success: Port" + currentPort);
		
		return new Runnable(){
			@Override
			public void run() {
				serverSocket = null;
				Socket clientSocket = null;
				
				while(acceptingConnections){
					LogServerMessage("Server now listening for connections...");
					
					try{
						clientSocket = serverSocket.accept();
					} catch (IOException e){
						
					}
					LogServerMessage("Client connected from address:" 
								+ clientSocket.getInetAddress().getHostAddress());
					ServerThread thread = new ServerThread(clientSocket, appContext);
					clientList.add(thread);
					thread.start();
					
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
