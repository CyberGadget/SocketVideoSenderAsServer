package com.kahlda.socketvideosenderasserver;

import android.app.Activity;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView mEchoText;
	private Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout mLayout;
	
	private String mString;
	
	private SocketManager mSockMan;

	volatile Thread runner;
	Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		
		mSockMan = new SocketManager(this);

		if (runner == null) {
			runner = new Thread(mSockMan);
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
		mPreview = new CameraPreview(this, mCamera, mSockMan);
		mLayout.addView(mPreview);
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
	
	// This is most likely called from a different thread, so use the Handler.
	public void writeToEchoText(String str){
		mString = str;
		mHandler.post(new Runnable() {
			public void run() {
				mEchoText.setText(mString);
			}
		});
	}

	
}
