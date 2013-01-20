package com.example.mymoviedatabase;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class EnterMovies extends Activity {
	private RatingBar ratingBar;
	EditText movie_title, movie_year;
	String url;
	TextView textView;
	ImageView img_poster;
	Bitmap poster;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_movies); 
        
        movie_title = (EditText) findViewById(R.id.movie_title);
    	movie_year = (EditText) findViewById(R.id.movie_year);
    	ratingBar = (RatingBar) findViewById(R.id.movie_rating);
    	textView = (TextView) findViewById(R.id.textView);
    	img_poster = (ImageView) findViewById(R.id.img_poster);

    }
    
    public void SearchMovies(View view) {
    	img_poster = (ImageView) findViewById(R.id.img_poster);
    	poster = null;
    	String title = "'" + movie_title.getText().toString() + "'";
    	Log.d("MMDB", "title: " + title);
    	try {
	    	SQLiteDatabase db = openOrCreateDatabase("MyMovieDB", MODE_PRIVATE, null);
	    	Cursor c = db.rawQuery("SELECT * FROM Movies WHERE Title = " + title, null);
	    	c.moveToFirst();
	    	
	    	String year = c.getString(c.getColumnIndex("Year"));
	    	Log.d("MMDB", "year: " + year);
	    	movie_year.setText(year);
	    	textView.setText("Date Watched: " + c.getString(4));
	    	
	    	int rating = c.getInt(c.getColumnIndex("Rating"));
	    	ratingBar.setRating(rating);
	    	
	    	String img = c.getString(c.getColumnIndex("Poster"));
	    	byte[] b = Base64.decode(img, Base64.DEFAULT);
	    	poster = BitmapFactory.decodeByteArray(b, 0, b.length);
	    	img_poster.setImageBitmap(poster);
	    	db.close();

    	} catch (Exception e) {
    		Log.e("ERR", "76: " + e.toString());
    	}

    }

    public void SubmitMovie(View view) {   	
    	String title = "'" + movie_title.getText().toString() + "'";
    	String year = "'" + movie_year.getText().toString() + "'";
    	int rating = (int) ratingBar.getRating();
    	
    	SQLiteDatabase db = openOrCreateDatabase("MyMovieDB", MODE_PRIVATE, null);
    	db.execSQL("CREATE TABLE IF NOT EXISTS Movies (imdbID VARCHAR(10), " +
    			"Title VARCHAR(100), Year VARCHAR(4), Rating INT(1), DateAdded date, " +
    			"Comments VARCHAR, Poster TEXT);");
        if (year != null && year != "") {
        	// if statement doesn't work because year = "''"
        	//url = "/?t=" + title.replace(" ", "%20") + "&y=" + year;
        	db.execSQL("INSERT INTO Movies (Title, Year, Rating, DateAdded) VALUES (" + title + ", " + year + ", " + rating + ", date('now'))");
        	url = "/?t=" + title.replace(" ", "%20");
        	try {
        		Log.d("MMDB", "Executing HTTP Get Request with url = " + url);
				new executeHttpGet().execute();
			} catch (Exception e) {
				Log.e("ERR", e.toString());
			}
        }
        else {
        	
        }
        
        Cursor c = db.rawQuery("SELECT * FROM Movies WHERE Title = " + title, null);
        c.moveToFirst();
        textView.setText(c.getString(c.getColumnIndex("Title")) + "\nMy Rating = " + c.getString(c.getColumnIndex("Rating")));
        Log.d("MMDB", c.getString(c.getColumnIndex("DateAdded")));
        db.close();

        movie_title.setText("");
        movie_year.setText("");
        ratingBar.setRating(3);
    }
    
    public void ClearTable(View view) {
    	SQLiteDatabase db = openOrCreateDatabase("MyMovieDB", MODE_PRIVATE, null);
    	db.execSQL("DROP TABLE IF EXISTS Movies");
    	db.close();
    	
    	Toast.makeText(this, "Database Cleared", Toast.LENGTH_SHORT).show();
    }
    
    private class executeHttpGet extends AsyncTask<Void, Void, Void> {

    	protected Void doInBackground(Void...voids) {
    		BufferedReader in = null;
        	try {
        		HttpClient client = new DefaultHttpClient();
        		HttpGet request = new HttpGet();
        		request.setURI(new URI("http://omdbapi.com" + url));
        		HttpResponse response = client.execute(request);
        		in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
        		StringBuffer sb = new StringBuffer("");
        		String line = "";
        		String NL = System.getProperty("line.separator");
        		while ((line = in.readLine()) != null) {
        			sb.append(line + NL);
        		}
        		in.close();
        		String page = sb.toString();
        		Log.d("MMDB", page);
        		JSONObject movieInfo = new JSONObject(page);
        		
        		try {	
	    			String url = movieInfo.getString("Poster");
    			
    				poster = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
    				
    				runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
		    				img_poster.setImageBitmap(poster);
						}
					});
    		    	SQLiteDatabase db = openOrCreateDatabase("MyMovieDB", MODE_PRIVATE, null);
    		    	// CHANGE TO UPDATE NOT INSERT, YOU DERP!
    		    	String year = "'" + movieInfo.getString("Year") + "'";
    		    	String title = "'" + movieInfo.getString("Title") + "'";
    		    	String imgString = Base64.encodeToString(getBitmapAsByteArray(poster), Base64.DEFAULT);
    		    	db.execSQL("UPDATE Movies SET Year = " + year + ", Poster = '" + imgString + "' WHERE Title = " + title + ";");
    	        	Log.d("MMDB", "Year and picture inserted into db. " + imgString);
    	            db.close();
    	            
    				// Try to save the bitmap to local storage
    				/*
    				FileOutputStream fos;
    		    	try {
    		    		fos = openFileOutput("poster", Context.MODE_PRIVATE);
    		    		poster.compress(Bitmap.CompressFormat.PNG, 100, fos);
    		    		fos.close();
    		    	} catch (Exception e) {
    					e.printStackTrace();
    				} 
    				*/
	    		    	
    			} catch (MalformedURLException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
        				
        		
        	} catch (Exception e) {
				Log.e("ERR", e.toString());
        	} finally {
        		if (in != null) {
        			try {
        				in.close();
        			} catch (IOException e) {
        				Log.e("ERR", e.toString());
        			}
        		}
        	}
        	
        	/*
        	grab imdbID and year JSON strings from request
        	grab poster image url
        	SQLiteDatabase db = openOrCreateDatabase("MyMovieDB", MODE_PRIVATE, null);
        	db.execSQL("INSERT INTO Movies (imdbID, Year) VALUES (aa, 22)");
        	 */
    		return null;
    	}
    }
    
    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	bitmap.compress(CompressFormat.PNG, 100, outputStream);
    	return outputStream.toByteArray();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_enter_movies, menu);
        return true;
    }
}
