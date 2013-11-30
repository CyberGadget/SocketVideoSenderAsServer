package com.kahlda.socketvideosenderasserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.util.Log;

public class SocketManager implements Runnable {

	private static final String EXIT_STRING = "!!EXIT";
	private static final int PORT = 8080;
	private static final String TAG = "SM";

	private boolean mSendable;

	private ServerSocket mServer;
	private Socket mSocket;
	private BufferedReader mReader;
	private BufferedWriter mWriter;
	private String mMessage;
	private int mPreviewWidth, mPreviewHeight;

	private MainActivity mMainActivity;

	public SocketManager(MainActivity act) {
		mMainActivity = act;
		mSendable = false;
	}
	
	//public void setPreviewSize(int width, int height);

	public void sendFrame(byte[] data, int width, int height) {
		mPreviewWidth = width;
		mPreviewHeight = height;
		new SendFrameTask().execute(data);
	}

	public class SendFrameTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... params) {
			//Log.d(TAG, "SendFrameTask.doInBackground is executing.");
			if(mSendable){
				try {
					//Log.d(TAG, "Sending frame via mWriter.");
					mWriter.write("<<<FRAME>>>");
					mWriter.flush();
					
					YuvImage img = new YuvImage(params[0], ImageFormat.NV21, mPreviewWidth, mPreviewHeight, null);
					img.compressToJpeg(new Rect(0,0,mPreviewWidth,mPreviewHeight), 20, mSocket.getOutputStream());
					
					mWriter.write("<<<END>>>");
					mWriter.flush();
				} catch (IOException e) {
					Log.d(TAG, "Could not send via mWriter.");
				}
			}
			return null;
		}
		
//		@Override
//		protected void onPostExecute(Void result) {
//			// TODO Auto-generated method stub
//			super.onPostExecute(result);
//		}

	}

	@Override
	public void run() {

		try {
			mServer = new ServerSocket(PORT);

			while (true) {
				mSocket = mServer.accept(); // blocking
				mMainActivity.writeToEchoText("Connected from " + mSocket.getInetAddress().getHostAddress().toString());
				Log.d(TAG, "Connection accepted, I think...");
				mReader = new BufferedReader(new InputStreamReader(
						mSocket.getInputStream()));
				mWriter = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream()));
				//Log.d(TAG, "Streams established, I think...");
				mSendable = true;

				while (true) {
					mMessage = mReader.readLine(); // blocking
					//Log.d(TAG, "Message received: " + mMessage);
					if (mMessage.contentEquals(EXIT_STRING)) {
						//Log.d(TAG, "EXIT_STRING received. Exiting...");
						mMainActivity.writeToEchoText("EXIT_STRING received. Connection closing.");
						break;
					}

					mMainActivity.writeToEchoText(mMessage);

					//Log.d(TAG, "TextView Set to message.");

					mWriter.write("<<<ECHO>>>" + mMessage);
					//Log.d(TAG, "Message written to output stream.");
					mWriter.flush();
					//Log.d(TAG, "Output stream flushed.");
				}
				
				mSocket.close();
				mReader.close();
				mWriter.close();
				mSendable = false;
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
