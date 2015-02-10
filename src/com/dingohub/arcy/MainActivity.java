package com.dingohub.arcy;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = "MainActivity";
	Button bClient;
	Button bServer;
	Button bSaveFile;
	Button bLoadFile;
	
	EditText eText;
	TextView tLoadedText;
	boolean fileCreated;
	
	private File file;
	private String DIR_PATH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bClient = (Button) findViewById(R.id.button_clientinit);
		bServer = (Button) findViewById(R.id.button_serverinit);
		bSaveFile = (Button) findViewById(R.id.button_save);
		bLoadFile = (Button) findViewById(R.id.button_load);
		
		eText = (EditText) findViewById(R.id.edittext_filesave);
		tLoadedText = (TextView) findViewById(R.id.textview_loadedtext);
		
		DIR_PATH = Environment.getExternalStorageDirectory() + "/arcy";
		bClient.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ChatClientActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
				
			}
		});
		
		bServer.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ServerSetupActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
				
			}
		});
		
		bSaveFile.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				file = new File(DIR_PATH,"test.txt");
				Log.d(TAG, file.getAbsolutePath());

				BufferedWriter out;         

				try {
					FileWriter fileWriter= new FileWriter(Environment.getExternalStorageDirectory().getPath()+"/tsxt.txt");
	
					out = new BufferedWriter(fileWriter);	
					out.write(eText.getText().toString());	
					out.close(); 

				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		
		bLoadFile.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				file = new File(DIR_PATH,"text.txt");
				StringBuilder text = new StringBuilder();
				
				try{
					BufferedReader in = new BufferedReader(new FileReader(file));
					String line = new String();
					while(((line = in.readLine()) != null)){
						text.append(line);
						text.append('\n');
					}
					
					in.close();
				} catch (IOException e) {
					Log.e(TAG, "Error Occured upon retreival");
					e.getStackTrace();
				}
				
				tLoadedText.setText(text);
				
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
}
