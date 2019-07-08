package com.aself.student.librarycard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    ImageView image;
    EditText inputbox;
    Button button;
    TextView errorbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView)findViewById(R.id.image);
        inputbox = (EditText) findViewById(R.id.inputbox);
        button = (Button) findViewById(R.id.button);
        errorbox = (TextView) findViewById(R.id.errorbox);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onClick(View view) {
		new TalkToServerTask().execute(inputbox.getText().toString());
    }

    public class TalkToServerTask extends AsyncTask<String, Void, Bitmap> {
		private static final String SERVER_IP = "192.168.1.115";
		private static final int SERVER_PORT = 4303;

		// This method is called by execute() on a new Thread()
		// Don't mess with the UI here or Android will be angry
        @Override
        public Bitmap doInBackground(String... params) {
			// FIXME you need to actually do whatever with your errors
			InetAddress serverAddr;
			Bitmap decodedImage = null;
			try {
                serverAddr = InetAddress.getByName(SERVER_IP);
				Socket socket = new Socket(serverAddr,SERVER_PORT);
				PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// Send librarycard
				String librarycard = params[0];
				printWriter.println(librarycard);
				// FIXME I wasn't super sure what exactly was going on here, but you
				// need to return decodedImage when it is all good
				String line = null;
				while ((line = br.readLine()) != null) {
					final String strReceived = line;
					byte[] imageBytes = Base64.decode(strReceived, Base64.DEFAULT);
					decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				}
			} catch (Exception e) {
                // FIXME handle error however you want
			}
			// return decodedImage here
			return decodedImage;
		}

		// This method is called on the main/UI Thread
		// Do whatever UI interaction you need here
		@Override
		public void onPostExecute(Bitmap decodedImage){
			image.setImageBitmap(decodedImage);
		}
    }
}
