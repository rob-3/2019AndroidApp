package com.aself.student.librarycard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    static final String SERVER_IP = "192.168.1.115";
    static final int SERVER_PORT = 4303;

    Handler handler = new Handler();
    static Socket socket;
    PrintWriter printWriter;
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
        Thread clientThread = new Thread(new ClientThread());
        errorbox.setText("After thread instantiated");
        clientThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.shutdownInput();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                printWriter.println(inputbox.getText().toString());
            }
        });
    }

    public class ClientThread implements Runnable {
        public void run() {
            errorbox.setText("ClientThread.run() called");
            try {
                errorbox.setText("before the getByName call");
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                errorbox.setText("after getByName; before socket");
                socket = new Socket(serverAddr,SERVER_PORT);
                errorbox.setText("before the try statement");
                try {
                    errorbox.setText("printWriter about to be defined");
                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                    errorbox.setText("PrintWriter Defined");
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    try {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            final String strReceived = line;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    byte[] imageBytes = Base64.decode(strReceived, Base64.DEFAULT);
                                    Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    image.setImageBitmap(decodedImage);
                                }
                            });
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                errorbox.setText("Client Disconnected");
                            }
                        });
                    } catch (Exception e) {
                        final String error = e.getLocalizedMessage();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                errorbox.setText(error);
                            }
                        });
                    }
                } catch (Exception e) {
                    final String error = e.getLocalizedMessage();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            errorbox.setText(error);
                        }
                    });
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        errorbox.setText("Connection Closed");
                    }
                });
            } catch (Exception e) {
                final String error = e.getLocalizedMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        errorbox.setText(error);
                    }
                });
            }
        }
    }
}
