package com.dingohub.arcy;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dingohub.arcy.tools.ClientUtility;
import com.dingohub.arcy.tools.SocketUtil;

public class ChatClientActivity extends Activity{
	private boolean clientConnected;
	EditText messageText;
	Button sendButton;
	TextView logText;
	
	StringBuffer log;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_client);
		
		/* All code below in onCreate is from Leon Brown of Florida State University*/
		clientConnected = false;
		
		messageText = (EditText) findViewById(R.id.message);
		sendButton = (Button) findViewById(R.id.sendButton);
		logText = (TextView) findViewById(R.id.logText);
		
		/**
		 * If the user has input a message to be sent to the server 
		 * and we are connected to the server then enable the send button
		 */
		
		
		
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ClientUtility.sendMessageToServer(messageText.getText().toString());
				messageText.setText("");
				
			}
		});
		
		messageText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (messageText.getText().toString().isEmpty()) {
					sendButton.setEnabled(false);
				} else if (clientConnected) {
					sendButton.setEnabled(true);
				} else {
					sendButton.setEnabled(false);
				}
			}
			
			public void beforeTextChanged(CharSequence s, int start, 
					int count, int after) {}
			
			public void onTextChanged(CharSequence s, int start, 
					int before, int count) {}
		});
	}

	/**
	 * Handles the onClick event of the Connect button
	 */
	public void connectClick(View view) {
		// Read the IP Address and Port supplied by the user
		String ipAddress = ((EditText) findViewById(R.id.editIP)).
				getText().toString();
		String portText = ((EditText) findViewById(R.id.editPort)).
				getText().toString();	
		
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
			((Button) findViewById(R.id.connectionButton)).setText("Disconnect");
			clientConnected = true;
			
			((EditText) findViewById(R.id.editIP)).setEnabled(false);
			((EditText) findViewById(R.id.editPort)).setEnabled(false);
			
			if (!messageText.getText().toString().isEmpty())
				sendButton.setEnabled(true);
			
			LogMessage("Connected to server @ " + ipAddress + ":" + portText);
			
		// We are already connected and would like to disconnect from 
		// the server
		} else {
			stopClient();
			((Button) findViewById(R.id.connectionButton)).setText("Connect");
			sendButton.setEnabled(false);
			clientConnected = false;
			
			((EditText) findViewById(R.id.editIP)).setEnabled(true);
			((EditText) findViewById(R.id.editPort)).setEnabled(true);
	
			LogMessage("Disconnected from server @ " + ipAddress + ":" + portText);
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
	private void stopClient() {
		ClientUtility.shutDownClient();
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
	}

	@Override
    protected void onPause(){
		super.onPause();
	}

	@Override
    protected void onStop(){
		super.onStop();
	}
}
