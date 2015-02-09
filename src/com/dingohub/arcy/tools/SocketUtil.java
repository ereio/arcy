package com.dingohub.arcy.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.util.Log;

import com.dingohub.arcy.ChatClientActivity;
import com.dingohub.arcy.ServerSetupActivity;

/**
 * Utility class for facilitating socket-based communication between a 
 * server and client
 */
public class SocketUtil {
	static final String TAG = "NetworkUtil";
	static ServerSocket serverSocket;
	static Socket clientSocket;
	static boolean acceptingConnections = false;
	static PrintWriter printWriter;
	private static Context appContext;
	
	/**
	 * Obtain the IPV4 address of the network interface of the device
	 * 
	 * @return The IPv4 Address of the network interface
	 */
	public static String getIPv4Address() {
		String address = "";
	
		try {
			// Obtain a collection of Network Interfaces available on the 
			// device
			Enumeration<NetworkInterface> interfaces = 
					NetworkInterface.getNetworkInterfaces();
			
			// Iterate through the interfaces
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
		
				// Get a collection of addresses associated with the 
				// current network interface
				Enumeration<InetAddress> currentAddresses = 
						current.getInetAddresses();
				
				// Iterate through the addresses
				while (currentAddresses.hasMoreElements()) {
					InetAddress inetAddress = 
							currentAddresses.nextElement();
					
					// The current address is the IPv4 address for the network interface
					if (!inetAddress.isLoopbackAddress() && 
							InetAddressUtils.isIPv4Address(
									inetAddress.getHostAddress())) {
						address = inetAddress.getHostAddress();
						break;
					}
				}
			}
		}catch (SocketException sockex) {
			Log.e(TAG, sockex.getLocalizedMessage(), sockex);
		}
		
		return address;
	}
	
	/**
	 * Obtain the IPV6 address of the network interface of the device
	 * 
	 * @return The IPv6 address of the network interface
	 */
	public static String getIPv6Address() {
		String address = "";
		
		try {
			// Obtain a collection of Network Interfaces available on the 
			// device
			Enumeration<NetworkInterface> interfaces = 
					NetworkInterface.getNetworkInterfaces();
			
			// Iterate through the interfaces
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
		
				// Get a collection of addresses associated with the 
				// current network interface
				Enumeration<InetAddress> currentAddresses = 
						current.getInetAddresses();
				
				// Iterate through the addresses
				while (currentAddresses.hasMoreElements()) {
					InetAddress inetAddress = 
							currentAddresses.nextElement();
					
					// The current address is the IPv6 address for the network interface
					if (!inetAddress.isLoopbackAddress() && 
							InetAddressUtils.isIPv6Address(
									inetAddress.getHostAddress())) {
						address = inetAddress.getHostAddress();
						break;
					}
				}
			}
		}catch (SocketException sockex) {
			Log.e(TAG, sockex.getLocalizedMessage(), sockex);
		}
		
		return address;
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
	 * Create a Runnable for executing server based operations off the 
	 * main UI thread
	 * 
	 * @param appContext The Context from the calling Activity, 
	 * 			used as a cheap way to interact with the Activity
	 * @param port The port on which we want the server to listen for 
	 * 			incoming connections
	 * @return The configured runnable
	 */
	public static Runnable getServerRunnable(final Context context, 
			final int port) {
		// We can only open ports in the range 0 - 65535
		// A value of 0 for the port will allow the OS to determine the 
		// port to be used
		// Ports from 1 - 1024 are generally used by system services
		if ((port < 0 || port > 65535))
			return null;
		
		appContext = context;
		
		return new Runnable() {
			public void run() {
				initServerSocket(port);
				
				try {
					// Loop indefinitely listening for incoming 
					// connections and messages
					while (acceptingConnections) {
						LogServerMessage("Server now listening for connections on port " + 
								port);						
						
						// Block here and wait until a client connects
						Socket clientSocket = serverSocket.accept();
			
						LogServerMessage("Client connected from address " 
								+ clientSocket.getInetAddress().getHostAddress());
						
						// Create an InputStreamReader to allow us to read 
						// the data sent via the stream as characters
						InputStreamReader streamReader = 
								new InputStreamReader(clientSocket.getInputStream());
						BufferedReader bufferedReader = 
								new BufferedReader(streamReader);
						
						// Get the OutputStream from the client socket to 
						// allow data to be sent to the connected client 
						// (echo)
						OutputStream output = clientSocket.getOutputStream();
						OutputStreamWriter outputWriter = 
								new OutputStreamWriter(output);
						
						// Attempt to read data from the client
						// This is a blocking call, execution will not 
						// continue until either data is received or the 
						// stream is closed
						String input  = bufferedReader.readLine();
						
						while (input != null) {
							LogServerMessage("Received: " + input);
							LogServerMessage("Echoing: " + input);
							
							// "Echo" the message back to the client
							outputWriter.write(input + "\n");
							// A call to flush ensures that the data is 
							// written to the target stream
							outputWriter.flush();
						
							// Attempt to read data from the client
							// This is a blocking call, execution will not 
							// continue until either data is received or the 
							// stream is closed
							input = bufferedReader.readLine();
						}
						
						LogServerMessage("Client disconnected");
					}				
					
					LogServerMessage("Stopping server");
				} catch (SocketException sockex) {
					LogServerMessage("Stopping server");
				} catch (IOException ioex) {
					Log.e(TAG, ioex.getLocalizedMessage(), ioex);
				} 
			}
		};
	}
	
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
			// Connect to the server
			clientSocket = new Socket(serverAddress, port);
		} catch (ConnectException cex) {
			Log.e("Socket Client", cex.getLocalizedMessage(), cex);
		} catch (IOException ioex) {
			Log.e("Socket Client", ioex.getLocalizedMessage(), ioex);
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
					if (clientSocket == null)
						return;
					
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
						String received = "Received: " + temp;
						LogClientMessage(received);
						temp = bufferedReader.readLine();
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
