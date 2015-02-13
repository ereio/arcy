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
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dingohub.arcy.tools.ClientUtility;
import com.dingohub.arcy.tools.SocketUtil;

public class ChatClientActivity extends Activity{
	private String TAG = "ChatClientActivity";
	private boolean clientConnected;
	EditText messageText;

	
	
	TextView logText;
	//goes back to previous activity when  user quits
	Intent quit;
	StringBuffer log;
	
	String ipAddress;
	String portText;
	
	private String CHAT_FILE = "chatClient.txt";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_client);
		
		/* All code below in onCreate is from Leon Brown of Florida State University*/
		clientConnected = false;
		
		messageText = (EditText) findViewById(R.id.message);

		logText = (TextView) findViewById(R.id.logText);
		
		
		
		/**
		 * If the user has input a message to be sent to the server 
		 * and we are connected to the server then enable the send button
		 */
		
		Intent i = getIntent();
		Bundle bundle = i.getExtras();
		
		if(bundle!= null){
			portText =  bundle.getString("port")	;
			ipAddress = bundle.getString("IP");
			// connects to the server
			connectClick();
			
		}
		
		
		
		
		
		messageText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					ClientUtility.sendMessageToServer(messageText.getText().toString());
					messageText.setText("");
					
				}
				
				
				return false;
			}
		});
		
		
	}

	/**
	 * Handles the onClick event of the Connect button
	 */
	public void connectClick() {
		// Read the IP Address and Port supplied by the user
		
		
		// Ensure an IP address was specified
		if (ipAddress == null || ipAddress.isEmpty()) {
			Toast.makeText(this, "Please enter a valid IP address", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Ensure a port was specified
		// Does not handle the case where port is 0
		// In this case the port should be obtained from thes server 
		if (portText == null || portText.isEmpty()) {
			Toast.makeText(this, "Please enter a valid port", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		int port = Integer.parseInt(portText);
		
		// We are connecting to a server
		if (!clientConnected) {
			connectToServer(ipAddress, port);
			
			clientConnected = true;
			
		
			
		
			
			LogMessage("Connected to server @ " + ipAddress + ":" + portText);
			
		// We are already connected and would like to disconnect from 
		// the server
		} else {
			stopClient();
			
		}
	}
	
	/**
	 * Connect to the specified server
	 * 
	 * @param serverIP The IP address of the server
	 * @param port The port that the server is listening on
	 */
	private void connectToServer(String serverIP, int port) {
		String serverAddress = SocketUtil.getIPv4Address();
		
		// This is not the best way to perform this test, however, it is 
		// used just for this example to indicate that the test should be 
		// performed
		if (serverAddress == null || serverAddress.isEmpty()){
			Toast.makeText(this, "Client is not connected to a network", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Obtain a Runnable to be used for client socket initialization 
		// and communication
		Runnable clientRunnable = ClientUtility.getClientRunnable(this, 
				serverIP, port);
		
		// Execute the client runnable
		Thread clientThread = new Thread(clientRunnable);
		clientThread.start();
	}
	
	/**
	 * Output the given message to the log
	 * @param message
	 */
	public void LogMessage(final String message) {
		/**
		 * Because this can be called from a non-UI thread we need to 
		 * ensure that the UI interfacing code runs on the UI thread
		 */
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
	
	/**
	 * Shut down communication with the server
	 */


	public void stopClient() {
		runOnUiThread(new Runnable() {
		@Override
		public void run() {
				
				
				clientConnected = false;
				
				
	
				LogMessage("Disconnected from server @ " + ipAddress + ":" + portText);
				quit = new Intent(getApplicationContext(),MainActivity.class);
				startActivity(quit);
				finish();
				
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
			is = openFileInput(CHAT_FILE);
		
			if(is != null){
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader bufferedReader = new BufferedReader(isr);
				
				String line = new String();
				while(((line = bufferedReader.readLine()) != null)){
					text.append(line);
					text.append('\n');
				}
				
				bufferedReader.close();
			}
			
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
			FileOutputStream out = openFileOutput(CHAT_FILE, Context.MODE_PRIVATE);
				
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
	//	File dir = getFilesDir();
	//	File log = new File(dir, CHAT_FILE);
	//	if(log.delete())
	//		Log.i(TAG, "File deleted succesfully");
	//	else
	//		Log.e(TAG, "File not found or deleted");
	}
}
