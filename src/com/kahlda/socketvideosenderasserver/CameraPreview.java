package com.kahlda.socketvideosenderasserver;

import java.io.IOException;
import java.util.List;

import com.kahlda.socketvideosenderasserver.SocketManager.SendFrameTask;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = "CP";

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private SocketManager mSockMan;
	private Camera.Size mPreviewSize;
	private int mFrameCount;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera, SocketManager sockMan) {
		super(context);
		mFrameCount = 0;
		mCamera = camera;
		mSockMan = sockMan;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
			// mCamera.setPreviewCallback( new PreviewCallback(){
			// @Override
			// public void onPreviewFrame(byte[] data, Camera camera) {
			// Log.d(CP, "Preview Callback in CameraPreview.java");
			// }
			// });
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here
		Parameters param = mCamera.getParameters();
		param.setPreviewSize(320, 240);
		//List<Integer> lislis = param.getSupportedPreviewFormats();
		//param.setPreviewFormat(ImageFormat.JPEG);
		mCamera.setParameters(param);
		//int form = param.getPreviewFormat();
		mPreviewSize = mCamera.getParameters().getPreviewSize();
		
		// start preview with new settings
		try {
			
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					// Log.d(TAG, "Preview Callback in CameraPreview.java. mFrameCount = " + mFrameCount);
					if (mFrameCount++ >= 3) {
						mFrameCount = 0;
						mSockMan.sendFrame(data, mPreviewSize.width, mPreviewSize.height);
					}
				}
			});
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

}
