package com.sample.client1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ClientActivity extends Activity {

	private static final String TAG = "ClientActivity";

	private EditText mIpAddress;
	private EditText mPort;
	private EditText mSocketInput;
	private TextView mSocketOutput;
	private Button mSendButton;
	private Button mDisconnectButton;
	private Button mConnectButton;
	private Socket mSocket;
	private boolean mExit;

	private BufferedWriter mWriter;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			mSocketOutput.setText((CharSequence) msg.obj);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "in onCreate, ThreadId = " + android.os.Process.myTid());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mIpAddress = (EditText) findViewById(R.id.ip_add);
		mPort = (EditText) findViewById(R.id.port);
		mSocketInput = (EditText) findViewById(R.id.data);
		mSocketOutput = (TextView) findViewById(R.id.output);
		mSendButton = (Button) findViewById(R.id.submit);
		mDisconnectButton = (Button) findViewById(R.id.exit);
		mConnectButton = (Button) findViewById(R.id.connect);

		mSendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				String input = mSocketInput.getText().toString();
				mSocketOutput.setText("");

				sendMessage(input);
			}
		});

		mConnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSocketOutput.setText("Connected");
				mExit = false;
				String ip = mIpAddress.getText().toString();
				String port = mPort.getText().toString();

				try {
					mSocket = new Socket(ip, Integer.parseInt(port));

				} catch (IOException e) {
					Log.e(TAG, "IOException - " + e);
				}
			}
		});

		mDisconnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mSocketOutput.setText("Disconnected");
				mExit = true;
				String input = "EXIT\n";				
				sendMessage(input);
			}
		});
	}

	private void sendMessage(final String data) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Log.i(TAG, "in sendMessage, Threadid = " + android.os.Process.myTid());
				
				BufferedReader reader = null;
				String output = null;
				String input = data;
				try {
					if (mWriter == null) {
						mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
					}
					if (reader == null) {
						reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					}
					mWriter.write(input + "\n", 0, input.length() + 1);
					mWriter.flush();

					while (continueRead()) {					

						output = reader.readLine();

						Message msg = mHandler.obtainMessage();
						msg.obj = output;
						mHandler.sendMessage(msg);

					}

				} catch (IOException e) {
					Log.e(TAG, "IOException - " + e);

				} finally {

					try {
						reader.close();
					} catch (IOException e) {
						Log.e(TAG, "IOException - " + e);
					}

					try {
						mWriter.close();
					} catch (IOException e) {
						Log.e(TAG, "IOException - " + e);
					}

					try {
						mSocket.close();
					} catch (IOException e) {
						Log.e(TAG, "IOException - " + e);
					}
				}

			}
		}).start();
	}

	private boolean continueRead() {
		return !mExit;
	}
}