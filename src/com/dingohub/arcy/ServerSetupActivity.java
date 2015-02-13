package com.dingohub.arcy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dingohub.arcy.tools.ServerUtility;
import com.dingohub.arcy.tools.SocketUtil;

public class ServerSetupActivity extends Activity{
	private static final String TAG = "ServerSetupActivity";
	private boolean serverRunning;
	StringBuffer log;
	TextView logText;
	private String SERVER_FILE = "serverLog.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.socket_server);
		
		logText = (TextView) findViewById(R.id.textLog);
		
		serverRunning = false;
		
	}
	
	/**
	 * Handles the onClick method of the Start button
	 */
	public void serverClick(View view) {
		String portText = ((EditText) findViewById(R.id.editPort)).
				getText().toString();
		
		/** 
		 * Ensure the user actually specified a port (we could allow this 
		 * condition and have the OS assign a port but we have chosen not 
		 * to for this example)
		 */
		if (portText == null || portText.isEmpty()) {
			Toast.makeText(this, "Please enter a valid port", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		int port = Integer.parseInt(portText);
		
		/*
		 * If the server is not currently running and/or actively 
		 * listening for incoming connections, start it
		 */
		if (!serverRunning) {
			startServer(port);
			((Button) findViewById(R.id.startButton)).setText("Stop Server");
			serverRunning = true;
			((EditText) findViewById(R.id.editPort)).setEnabled(false);
		/*
		 * The server is already running and we wish to stop it
		 */
		} else {
			stopServer();
			((Button) findViewById(R.id.startButton)).setText("Start Server");
			serverRunning = false;
			((EditText) findViewById(R.id.editPort)).setEnabled(true);
		}
	}
		
	/**
	 * Have our server start listening on the given port
	 * @param port
	 */
	private void startServer(int port) {
		String serverAddress = SocketUtil.getIPv4Address();
		
		// This is not the best way to perform this test, however, it is 
		// used just for this example to indicate that the test should be 
		// performed
		if (serverAddress == null || serverAddress.isEmpty()) {
			// Not connected to any network
			return;
		}
		
		((TextView) findViewById(R.id.ipText)).setText("Server IP: " + 
				serverAddress);
		
		// Obtain a Runnable to be used for server socket initialization 
		// and communication
		//Runnable serverRunnable = SocketUtil.getServerRunnable(this, port);
		Runnable serverRunnable = ServerUtility.getServerRunnable(this, port, null);
		
		// Execute the server runnable
		Thread serverThread = new Thread(serverRunnable);
		serverThread.start();
	}
	
	/**
	 * Shut down the server
	 */
	private void stopServer() {
		//SocketUtil.shutDownServer();
		ServerUtility.shutDownServer();
	}
	
	/**
	 * Output the given message to the log
	 * @param message
	 */
	public void LogMessage(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				log = new StringBuffer();
				
				if (!logText.getText().toString().isEmpty())
					log.append(logText.getText() + "\n");
				
				log.append(message);
				logText.setText(log.toString());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    protected void onStart(){
		super.onStart();
	}

	@Override
    protected void onRestart(){
		super.onRestart();
	}

	@Override
    protected void onResume(){
		super.onResume();
		InputStream is;
		StringBuilder text = new StringBuilder();
		try {
			is = openFileInput(SERVER_FILE);
			//if(is != null){
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader bufferedReader = new BufferedReader(isr);
				
				String line = new String();
				while(((line = bufferedReader.readLine()) != null)){
					text.append(line);
					text.append('\n');
				}
				
				bufferedReader.close();
			//}
		} catch (IOException e) {
			Log.e(TAG, "IO error occured upon retreival");
			e.getStackTrace();
		}
		
		logText.setText(text);
	}

	@Override
    protected void onPause(){
		super.onPause();
		
		try {
			FileOutputStream out = openFileOutput(SERVER_FILE, Context.MODE_PRIVATE);
				
			out.write(logText.getText().toString().getBytes());	
			out.close(); 

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
    protected void onStop(){
		super.onStop();
		

	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
}
