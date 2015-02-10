package com.dingohub.arcy.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import android.content.Context;

import com.dingohub.arcy.ServerSetupActivity;

public class ServerThread extends Thread {
	static InputStream input = null;
	static BufferedReader reader = null;
	static DataOutputStream output = null;
	protected Socket socket;
	private boolean init_success = false;
	private static Context appContext;
	
	public ServerThread(Socket clientSocket, Context context) {
		this.socket = clientSocket;
		appContext = context;
	}
	
	public void run(){
		try{
			input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));
			output = new DataOutputStream(socket.getOutputStream());
			init_success = true;
		} catch (IOException e){
			
		}
		
		String line;
		while(init_success){
			try{
				line = reader.readLine();
				if((line == null) || line.equalsIgnoreCase("QUIT")){
					socket.close();
					return;
				} else {
					LogServerMessage("Received:" + line
								+ "\nFrom:" + socket.getInetAddress().getHostAddress());
					
					output.writeBytes(line + "\n\r");
					output.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
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
