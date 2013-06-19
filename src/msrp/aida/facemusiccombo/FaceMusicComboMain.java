package msrp.aida.facemusiccombo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class FaceMusicComboMain extends Activity {

	private MediaPlayer player;
	private ContentResolver cr;
	private Cursor cu;
	private TextView tvMain;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facemusiccombo_main);
		
		this.initFields();
		
		this.playRandSong();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	@Override
	protected void onPause() {
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
	
	/**
	 * 
	 */
	private void initFields()
	{
		tvMain = (TextView) findViewById(R.id.tvMain);
		player = new MediaPlayer();
		cr = this.getContentResolver();
		cu = null;
	}
	
	
}
