package com.Adrian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ShakeCameraActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener{
	Camera mCamera;
	
	SurfaceView mSurfaceView;
	SurfaceHolder mSurfaceHolder;
	
	public  static final String TAG = "ShakeCamera";
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private long lastUpdate = -1;
	
	private PhotoHandler photoHandler = null;
	
	private float x, y, z;
	private float lastX, lastY, lastZ;
	
	private static final int SHAKE_THRESHOLD = 100;
	
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		
		if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
			return;
		
		photoHandler = new PhotoHandler(getApplicationContext());

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_surface);
		mSurfaceView = (SurfaceView)findViewById(R.id.surface_camera);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(mSurfaceHolder.getSurface()  == null){
			return;
		}
		
		try{
			mCamera.stopPreview();
		}catch(Exception e){
			//no preview.
		}
		
		Camera.Parameters params = mCamera.getParameters();
		params.setRotation(90);
//		params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(params);
		
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open(0);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface Destroyed");
		if(mCamera != null){
			mCamera.stopPreview();
		}

		Camera.Parameters params = mCamera.getParameters();
		params.setRotation(90);
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			long curTime = System.currentTimeMillis();
			
			if((curTime - lastUpdate) > 100){
				long diffTime = curTime - lastUpdate;
				lastUpdate = curTime;
				
				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];
				z = event.values[SensorManager.DATA_Z];
				
				float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;
				
				if(speed > SHAKE_THRESHOLD){
					if(!photoHandler.getTakingPicture()){
						photoHandler.setTakingPicture(true);
						//multithread this.
						try {
							Toast.makeText(getApplicationContext(), "Taking photo in one second", Toast.LENGTH_SHORT).show();
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						mCamera.takePicture(shutterCallback, rawCallback, photoHandler );
					}
					Log.d(TAG, "photo taken");
					Log.d(TAG, "Shake is true");
				}
				
				lastX = x;
				lastY = y;
				lastZ = z;
			}
		
		}
	}
	
	 ShutterCallback shutterCallback = new ShutterCallback() {
	        public void onShutter() {
	            Log.d(TAG, "onShutter'd");
	        }
	    };

	    // Handles data for raw picture
	    PictureCallback rawCallback = new PictureCallback() {
	        public void onPictureTaken(byte[] data, Camera camera) {
	            Log.d(TAG, "onPictureTaken - raw");
	        }
	    };
	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onDestroy() {
		mCamera.release();

		super.onDestroy();
	}
}