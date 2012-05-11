package com.Adrian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class PhotoHandler implements PictureCallback {
	
	private final Context context;
	
	private boolean takingPicture = false;
	
	public PhotoHandler(Context context){
		this.context = context;
	}
	
	

	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(ShakeCameraActivity.TAG, "Stupid");

		File picture = getDir();
		Log.d(ShakeCameraActivity.TAG, "Stupid");

		if(!picture.exists() && !picture.mkdirs())
			return;
		
		Log.d(ShakeCameraActivity.TAG, "Why don't you work");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyymmddhhmmss");
		String date = dateFormat.format(new Date());
		String photoFile = "Picture_" + date + ".jpg";
		
		String fileName = picture.getPath() + File.separator + photoFile;
		
		Log.d("File", "File = " + fileName);
		
		File pictureFile = new File(fileName);
		
		FileOutputStream fos;
		try {
			Log.d(ShakeCameraActivity.TAG, "File " + pictureFile.getAbsolutePath() + " getting written");

			pictureFile.createNewFile();
			fos = new FileOutputStream(pictureFile);
			fos.write(data);
			Log.d(ShakeCameraActivity.TAG, "Output Stream done");
			fos.close();
			Toast.makeText(context, "New Image Saved: " + photoFile, Toast.LENGTH_LONG);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setTakingPicture(false);
		camera.startPreview();
	}
	
	public void setTakingPicture(boolean b){
		takingPicture = b;
		Log.d(ShakeCameraActivity.TAG, "Taking Picture: " + b);	
		}
	
	public boolean getTakingPicture(){
		Log.d(ShakeCameraActivity.TAG, "Getting Picture");	
		return takingPicture;
	}
	
	private File getDir(){
		File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "ShakeCamera");
	}
}
