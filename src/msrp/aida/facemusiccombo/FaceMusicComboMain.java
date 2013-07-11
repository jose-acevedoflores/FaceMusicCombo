package msrp.aida.facemusiccombo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
//import android.view.View;

@SuppressLint("NewApi")
public class FaceMusicComboMain extends Activity implements TextToSpeech.OnInitListener{

	private MediaPlayer player;
	private ContentResolver cr;
	private Cursor cu;
	private TextView tvMain;
	private Camera camera;
	private CameraPreview cp;
	private TextToSpeech tts;
	private boolean ttsInit = false;
	private HashMap<String, String> map;

	private static final int CHECK_TTS_DATA_REQUEST_CODE = 1;
	public static final int VOICE_RECOGNITION_REQUEST_CODE = 3;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facemusiccombo_main);


//		PackageManager pm = getPackageManager();
//
//		try {
//			PackageInfo info =	pm.getPackageInfo("com.pandora.android",
		//PackageManager.GET_ACTIVITIES + PackageManager.GET_SERVICES + PackageManager.GET_PERMISSIONS);
//
//			for(ServiceInfo s : info.services)
//			{
//				System.out.println(s.name);
//				if(s.permission != null)
//					System.out.println(s.permission);
//			}
//
//				Intent intent = pm.getLaunchIntentForPackage("com.pandora.android");
//				//Intent intent = new Intent("com.pandora.android");
//				//intent.addFlags(Intent.);
//				this.startActivity(intent);
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


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
		if(player != null)
		{
			player.stop();
			player.release();
			player = null;
		}
		//tm = null;
		Log.d("debug", "Destroyed");
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == CHECK_TTS_DATA_REQUEST_CODE) {

			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				tts = new TextToSpeech(getApplicationContext(), this); // 1
				tts.setLanguage(Locale.US);
			} else {
				// TTS data not yet loaded, try to install it
				Intent ttsLoadIntent = new Intent();
				ttsLoadIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsLoadIntent);
			}


		}
		else if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
		{
			// Fill the list view with the strings the recognizer thought it could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);


			//	List<PackageInfo> l = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);

			//for(PackageInfo str : l )
			//	System.out.println(str.packageName);

			//			for(String str : matches)
			//			{
			//				System.out.println(str);
			//				if(str.equals("start") || str.equals("play"))
			//				{
			//					Log.d("DEBUG", "Try and launch pandora");
			//					Intent intent = pm.getLaunchIntentForPackage("com.pandora.android");
			//					this.startService(intent);
			//				}
			//			}
		}	
	}

	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS)
		{
			ttsInit = true;
			tts.setOnUtteranceProgressListener(new MyUtteranceListener());
			map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "onRespond");
			tts.speak("I'm your personal media player. Say start or stop to play music.", TextToSpeech.QUEUE_ADD, map );
		}
		else
		{
			Log.d("DEBUG", "No TTS working");
		}

	}
	//**************************************** My methods ***********************************

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

		//TTS engine part
		//map = new HashMap<String, String>();
		//Verify if TTS is available 
		//Intent checkIntent = new Intent();
		//checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		//startActivityForResult(checkIntent, CHECK_TTS_DATA_REQUEST_CODE);			


		this.enableFaceDetec();

	}

	/**
	 * 
	 */
	private void enableFaceDetec()
	{
		camera = getCameraInstance();
		camera.setFaceDetectionListener(new FaceDetect());
		camera.setDisplayOrientation(90);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		// Create our Preview view and set it as the content of our activity.
		cp = new CameraPreview(this, camera);

		preview.addView(cp);
		preview.setVisibility(View.INVISIBLE);
	}


	// ****************************************************************************************	
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
				System.out.println("id " +faces[0].id);
				System.out.println("Left Eye: " + faces[0].leftEye  + " Right Eye "+faces[0].rightEye);
//				
//				Log.d("FaceDetectTest", "face detected: "+ faces.length +
//						" Face 1 Location X: " + faces[0].rect.centerX() +
//						" || Y: " + faces[0].rect.centerY() );
//
//
//				if(  !player.isPlaying() && Math.abs(faces[0].rect.centerY() ) - Math.abs(previousY) >= 40 && 
				//System.currentTimeMillis() -  prevTime  < 1000 )
//				{
//					System.out.println("Yes - play song");
//					if(paused)
//					{
//						paused = false;
//						player.start();
//					}
//					else
//						playRandSong();
//
//
//				}
//				else if(player.isPlaying() && Math.abs(faces[0].rect.centerX() ) - Math.abs(previousX) >= 60 && 
				//System.currentTimeMillis() -  prevTime  < 1000)
//				{
//					System.out.println("No - stop song");
//					player.pause();
//					paused = true;
//				}
//				prevTime = System.currentTimeMillis();
//				previousY = faces[0].rect.centerY();
//				previousX = faces[0].rect.centerX();
			}
			else
				Log.d("FaceDetectTest", "Wu " + faces.length);
		}
	}


	/**
	 * 
	 * @author joseacevedo
	 *
	 */
	class MyUtteranceListener extends UtteranceProgressListener{

		@Override
		public void onDone(String utteranceId) {
			Log.d("DEBUG", "Utterdance Done " + utteranceId);

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);

		}

		@Override
		public void onError(String utteranceId) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStart(String utteranceId) {

			Log.d("DEBUG", "Utterance Started "+utteranceId);

		}

	}

}
