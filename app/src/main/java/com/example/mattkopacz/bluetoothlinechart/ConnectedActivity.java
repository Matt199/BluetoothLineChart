package com.example.mattkopacz.bluetoothlinechart;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;
import java.util.UUID;

public class ConnectedActivity extends AppCompatActivity {

    // Definicja zmiennych

    private ConnectedThread mConnectedThread;

    private String adress;

    private BluetoothAdapter btAdapter;

    private static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mmSocket;

    private boolean isBTConnected = false;

    private ProgressDialog progress;

    private TextView userInfo;

    Handler h;

    final int handlerState = 0;

    TextView btOutput;


    public StringBuilder recDataString = new StringBuilder();



    private LineGraphSeries<DataPoint> series;



    int lastXVal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        // Inicjalizuje Graf

        GraphView graph = (GraphView) findViewById(R.id.graph);

        series = new LineGraphSeries<>(new DataPoint[]{});


        graph.addSeries(series);

        series.setColor(Color.RED);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(5f);


        graph.getViewport().setBackgroundColor(Color.BLACK);

        graph.getGridLabelRenderer().setGridColor(Color.WHITE);

        graph.getViewport().setScalable(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(50);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(20);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);


        userInfo = (TextView) findViewById(R.id.textView2);

        btOutput = (TextView) findViewById(R.id.btOutput);

        Intent intent = getIntent();

        adress = intent.getStringExtra("EXTRA_ADRESS");


        new ConnectBT().execute();


        Log.d("Odczyt", "Wiadomosc");

        h = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


                // If recive any message from thread then....


                if (msg.what == handlerState) {    // If recive any message from thread then....

                    byte[] rBuff = (byte[]) msg.obj; // recived message (bytes)

                    String readMessage = new String(rBuff, 0, msg.arg1); // convert that message to string

                    recDataString.append(readMessage);

                    int endOfIndex = recDataString.indexOf("~");

                    if (endOfIndex > 0) {

                        String wiadomosc = recDataString.substring(1, endOfIndex);

                        btOutput.setText(readMessage); // Show message
                        Log.d("Odczyt", wiadomosc);

                        int wartoscInt = Integer.parseInt(wiadomosc);


                        recDataString.delete(0, recDataString.length());

                        series.appendData(new DataPoint(lastXVal, wartoscInt), true, 50);
                        lastXVal++;


                    }
                }


            }



        };


    }


    // Klasa do odczytywania wartosci


    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("Error", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Error", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            super.run();

            byte[] mmBuffer = new byte[256]; // Bytes returned from read() !!!
            int numBytes;

            // Start listening to the InputStream until an exception occurs
            while (true) {
                try {

                    // Read from InputStream

                    numBytes = mmInStream.read(mmBuffer);


                    //Send to the optained bytes to the UI activity

                    h.obtainMessage(handlerState, numBytes, -1, mmBuffer).sendToTarget();



                } catch (IOException e) {

                    Toast.makeText(getBaseContext(), "Input stream was disconnected", Toast.LENGTH_LONG).show();
                    break;
                }
            }

        }

    }





    // Próbuje za pomocą klasy AsyncTask (może zadziała...)
    private class ConnectBT extends AsyncTask<Void, Void, Void> {

        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {

            progress = ProgressDialog.show(ConnectedActivity.this, "Connecting...","Please Wait!" );
        }



        @Override
        protected Void doInBackground(Void... voids) {


            try {
                if(mmSocket == null || !isBTConnected) {

                    btAdapter = BluetoothAdapter.getDefaultAdapter();

                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(adress);

                    mmSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);

                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mmSocket.connect();

                    // Start Threating
                    mConnectedThread = new ConnectedThread(mmSocket);
                    mConnectedThread.start();
                }
            } catch (IOException e) {

                ConnectSuccess = false;
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!ConnectSuccess) {

                userInfo.setText("Niepolaczono...");
                finish();

            } else {

                isBTConnected = true;
                userInfo.setText("Polaczono");

            }

            progress.dismiss();

        }
    }
}
