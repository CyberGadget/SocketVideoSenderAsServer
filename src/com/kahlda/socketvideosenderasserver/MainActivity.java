package com.kahlda.socketvideosenderasserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Runnable {

	private static final String EXIT_STRING = "!!EXIT";
	private static final int PORT = 8080;
	
	private int mPreviewCount;

	private ServerSocket mServer;
	private Socket mSocket;
	private BufferedReader mReader;
	private BufferedWriter mWriter;
	private TextView mEchoText;
	private Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout mLayout;

	private String mMessage;

	volatile Thread runner;
	Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPreviewCount = 0;

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifIinfo = wifiManager.getConnectionInfo();
		int address = wifIinfo.getIpAddress();
		String ipAddressStr = ((address >> 0) & 0xFF) + "."
				+ ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF)
				+ "." + ((address >> 24) & 0xFF);
		TextView ipText = (TextView) findViewById(R.id.ipText);
		ipText.setText(ipAddressStr);

		mEchoText = (TextView) findViewById(R.id.echoText);
		mLayout = (FrameLayout) findViewById(R.id.camera_preview);

		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
		Toast.makeText(this, "Thread Start", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// ÉJÉÅÉâÇäJÇ≠
		mCamera = Camera.open();
		mPreview = new CameraPreview(this, mCamera);
		mLayout.addView(mPreview);
		mCamera.setPreviewCallback( new PreviewCallback(){
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				mPreviewCount++;
				Log.d("Callback", mPreviewCount + " Preview Callbacks");
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();

		// ÉJÉÅÉâÇí‚é~Ç∑ÇÈ
		if (mCamera != null) {
			mLayout.removeView(mPreview);
			mPreview = null;
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void run() {

		try {
			mServer = new ServerSocket(PORT);

			while (true) {
				mSocket = mServer.accept();
				Log.d("SS", "Connection accepted, I think...");
				mReader = new BufferedReader(new InputStreamReader(
						mSocket.getInputStream()));
				mWriter = new BufferedWriter(new OutputStreamWriter(
						mSocket.getOutputStream()));
				Log.d("SS", "Streams established, I think...");

				while (true) {
					mMessage = mReader.readLine();
					Log.d("SS", "Message received: " + mMessage);
					if (mMessage.contentEquals(EXIT_STRING)) {
						Log.d("SS", "EXIT_STRING received. Exiting...");
						break;
					}

					mHandler.post(new Runnable() {
						public void run() {
							mEchoText.setText(mMessage);
						}
					});
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
