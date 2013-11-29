package com.kahlda.socketvideosenderasserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class SocketManager implements Runnable{
	
	private static final String EXIT_STRING = "!!EXIT";
	private static final int PORT = 8080;
	
	private ServerSocket mServer;
	private Socket mSocket;
	private BufferedReader mReader;
	private BufferedWriter mWriter;
	private String mMessage;
	
	private MainActivity mMainActivity;
	
	public SocketManager(MainActivity act){
		mMainActivity = act;
	}
	
	@Override
	public void run() {

		try {
			mServer = new ServerSocket(PORT);

			while (true) {
				mSocket = mServer.accept();
				mMainActivity.writeToEchoText("Connected from " + mSocket.getInetAddress().getHostAddress().toString());
				Log.d("SS", "Connection accepted, I think...");
				mReader = new BufferedReader(new InputStreamReader(
						mSocket.getInputStream()));
				mWriter = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream()));
				Log.d("SS", "Streams established, I think...");

				while (true) {
					mMessage = mReader.readLine();
					//mReader.
					Log.d("SS", "Message received: " + mMessage);
					if (mMessage.contentEquals(EXIT_STRING)) {
						Log.d("SS", "EXIT_STRING received. Exiting...");
						mMainActivity.writeToEchoText("EXIT_STRING received. Connection closing.");
						break;
					}

					mMainActivity.writeToEchoText(mMessage);
					
					Log.d("SS", "TextView Set to message.");

					mWriter.write(mMessage);
					Log.d("SS", "Message written to output stream.");
					mWriter.flush();
					Log.d("SS", "Output stream flushed.");
				}
				mSocket.close();
				mReader.close();
				mWriter.close();

			}
		} catch (IOException e) {
			try {
				if (mWriter != null) {
					mWriter.close();
				}

				if (mReader != null) {
					mReader.close();
				}

				if (mSocket != null) {
					mSocket.close();
				}

				if (mServer != null) {
					mServer.close();
				}
			} catch (IOException ex) {
				Log.e(ex.getClass().getName(), ex.getMessage());
			}

			Log.e(e.getClass().getName(), e.getMessage());
		}

	}
	
}
