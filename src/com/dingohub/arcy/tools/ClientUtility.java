package com.dingohub.arcy.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dingohub.arcy.ChatClientActivity;

public class ClientUtility {
	
	static final String TAG = "ClientUtility";
	static Socket clientSocket;
	static PrintWriter printWriter;
	private static Context appContext;

	
	/**
	 * Initialize the client socket on the given address and port
	 * 
	 * @param serverAddress The address of the server we wish to connect 
	 * 			to
	 * @param port The port the server is listening for incoming 
	 * 			connections on
	 */
	private static void initClientSocket(InetAddress serverAddress, 
			int port) {
		try {
		
			
			clientSocket = new Socket(serverAddress, port);	
	
				
			
		} catch (ConnectException cex) {
			
			Log.e("Socket Client", cex.getLocalizedMessage()+"LOOK HERE", cex);
		} catch (IOException ioex) {
			
			Log.e("CONNECTION", ioex.getLocalizedMessage()+"LOOK HERE", ioex);
		} 
	}
	
	/**
	 * A convenience method for writing data to the server
	 * The PrintWriter class allows to write data to its target stream 
	 * in a simple manner
	 * 
	 * @param bw The BufferedWriter associated with the target stream
	 */
	private static void initClientOutput(BufferedWriter bw) {
		printWriter = new PrintWriter(bw);
	}
	
	/**
	 * Send a message from the client to the server
	 * 
	 * @param message The message to be sent to the server
	 */
	public static void sendMessageToServer(String message) {
		if (message != null && !message.isEmpty()) {
			printWriter.println(message);
			
			// PrintWriter does not throw an exception or return an error
			// A call to checkError flushes the stream and checks if an 
			// error was encountered and recorded by setError()
			if (printWriter.checkError())
				Log.d("Socket Client", 
						"An error occurred when sending the message");
		}
	}
	
	/**
	 * Closes the clients connection to the server
	 */
	public static void shutDownClient() {
		try {
			if (clientSocket != null) {
				// Close the client's InputStream
				clientSocket.shutdownInput();
				// Close the socket
				clientSocket.close();
			}
		} catch (IOException ioex) {
			Log.e(TAG, ioex.getLocalizedMessage(), ioex);
		}
		
		clientSocket = null;
		ChatClientActivity activity = (ChatClientActivity) appContext;
		activity.stopClient();
	}
	
	/**
	 * Create a Runnable for executing client operations off the 
	 * main UI thread
	 * 
	 * @param context The context of the calling Activity. 
	 * 			Used as a cheap method for updating the Activity
	 * @param serverIP The IP address of the server
	 * @param port The port the server is listening on
	 * @return The configured runnable
	 */
	public static Runnable getClientRunnable(final Context context, 
			final String serverIP, final int port) {
		return new Runnable() {
			public void run() {
				try {
					appContext = context;
					
					// Get the String IP address (or host name) to an address 
					InetAddress server = InetAddress.getByName(serverIP);
					
					initClientSocket(server, port);
				
					// The client was unable to connect to the server, you 
					// may want to notify the Activity accordingly
					if (clientSocket == null){
						Toast.makeText(appContext, "Unable to connect to specified IRC", Toast.LENGTH_LONG).show();
						return;
					}
						
					
					// Obtain an OutputStream to write data to the server
					OutputStreamWriter outputWriter = 
							new OutputStreamWriter(clientSocket.getOutputStream());
					BufferedWriter bufferedWriter = 
							new BufferedWriter(outputWriter);
					initClientOutput(bufferedWriter);
					
					// Obtain an InputStream to read data from the server
					InputStreamReader streamReader = 
							new InputStreamReader(clientSocket.getInputStream());
					BufferedReader bufferedReader = 
							new BufferedReader(streamReader);
					
			
					// Attempt to read a line of data from the server, 
					// this is a blocking call
					String temp = bufferedReader.readLine();

					while (temp != null) {
						String received = temp;
						if(received.equals("DISCONNECT")){
							shutDownClient();
							temp = null;
						} else {
							LogClientMessage(received);
							temp = bufferedReader.readLine();
						}
					}
				} catch (UnknownHostException uhex) {
					Log.e("Socket Client", uhex.getLocalizedMessage(), uhex);
				} catch (IOException ioex) {
					Log.e("Socket Client", ioex.getLocalizedMessage(), ioex);
				}
			}
		};
	}

	/**
	 * Output the given message to the Client Activity's log
	 * 
	 * @param message The message to be written
	 */
	private static void LogClientMessage(String message) {
		ChatClientActivity activity = (ChatClientActivity) appContext;
		activity.LogMessage(message);
	}
}
