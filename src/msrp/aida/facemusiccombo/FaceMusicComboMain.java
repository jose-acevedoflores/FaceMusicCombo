package msrp.aida.facemusiccombo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.util.Log;
import android.view.Menu;
//import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class FaceMusicComboMain extends Activity {

	private MediaPlayer player;
	private ContentResolver cr;
	private Cursor cu;
	private TextView tvMain;
	private Camera camera;
	private CameraPreview cp;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facemusiccombo_main);

		this.initFields();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	@Override
	protected void onPause() {
		if (camera != null){
			camera.release();        // release the camera for other applications
			camera = null;
		}        
		if(player != null && player.isPlaying())
			player.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if(player != null)
			player.start();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		player.stop();
		player.release();
		player = null;
		//tm = null;
		Log.d("debug", "Destroyed");
		super.onDestroy();
	}



	/**
	 * 
	 */
	private void playRandSong()
	{
		cu = cr.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				null, null, null, null);

		if(cu == null)
		{
			System.out.println("Error in media query");
		}
		else if(!cu.moveToFirst())
		{
			System.out.println("No music on phone");
		}
		else
		{
			ArrayList<Long> ids = new ArrayList<Long>(); 
			ArrayList<String> title = new ArrayList<String>();
			int titleColumn = cu.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = cu.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int isMusicCol = cu.getColumnIndex(android.provider.MediaStore.Audio.Media.IS_MUSIC);
			do {
				long thisId = cu.getLong(idColumn);
				String thisTitle = cu.getString(titleColumn);
				String isMusic = cu.getString(isMusicCol);
				if(isMusic.equals("1"))
				{
					ids.add(thisId);
					title.add(thisTitle);
				}

			} while (cu.moveToNext());

			int rand = new Random().nextInt(ids.size());
			long thisId = ids.get(rand);
			String songTitle = title.get(rand);

			Uri songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);


			player.setAudioStreamType(AudioManager.STREAM_MUSIC);

			try {
				tvMain.setText(songTitle);
				player.setDataSource(getApplicationContext(), songUri);
				player.prepare();
				player.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	/**
	 * 
	 */
	private void initFields()
	{
		tvMain = (TextView) findViewById(R.id.tvMain);
		player = new MediaPlayer();
		cr = this.getContentResolver();
		cu = null;

		camera = getCameraInstance();
		camera.setFaceDetectionListener(new FaceDetect());
		camera.setDisplayOrientation(90);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		// Create our Preview view and set it as the content of our activity.
		cp = new CameraPreview(this, camera);

		preview.addView(cp);
		//preview.setVisibility(View.INVISIBLE);
	}

	/**
	 * 
	 * @author joseacevedo
	 *
	 */
	class FaceDetect implements FaceDetectionListener{

		private int previousY=0; 
		private int previousX = 0;
		private long prevTime;
		private boolean paused=false;

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			if(faces.length >= 1)
			{	
				Log.d("FaceDetectTest", "face detected: "+ faces.length +
						" Face 1 Location X: " + faces[0].rect.centerX() +
						" || Y: " + faces[0].rect.centerY() );


				if(  !player.isPlaying() && Math.abs(faces[0].rect.centerY() ) - Math.abs(previousY) >= 40 && System.currentTimeMillis() -  prevTime  < 1000 )
				{
					System.out.println("Yes - play song");
					if(paused)
					{
						paused = false;
						player.start();
					}
					else
						playRandSong();
						
					
				}
				else if(player.isPlaying() && Math.abs(faces[0].rect.centerX() ) - Math.abs(previousX) >= 60 && System.currentTimeMillis() -  prevTime  < 1000)
				{
					System.out.println("No - stop song");
					player.pause();
					paused = true;
				}
				prevTime = System.currentTimeMillis();
				previousY = faces[0].rect.centerY();
				previousX = faces[0].rect.centerX();
			}
			else
				Log.d("FaceDetectTest", "Wu " + faces.length);
		}
	}

}
