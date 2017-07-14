package com.example.mattkopacz.bluetoothlinechart;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.DialogInterface;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StartActivity extends AppCompatActivity {

    // Deklaracje zmiennych

    // Informacja dla urzytkownika feedback
    TextView userInfoText;

    public String adres = null;
    private BluetoothAdapter mBluetoothAdapter;

    private List<String> mList = new ArrayList<String>();
    private List<String> mmList = new ArrayList<String>();



    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Inicjalizacja zmiennych

        userInfoText = (TextView) findViewById(R.id.textView);


        // Wywołaj funckję sprawdzającą dostępność urządzenia BT
        checkAvaliability();

        // Wywołaj funkcję sprawdzającą podłączone urządzenia BT
        checkConectedDevices();




    }

    // Wcisnij przycisk zeby wyświetlić dostępne urządzenia
    public void connectButtonClicked(View view){


        onCreateDialog().show();

    }




    // Funckja ma sprawdzić dostępność urządzeń Bluetooth

    private void checkAvaliability(){

        // Korzystam z metody getDefaultAdapter klasy BluetoothAdapter i przypisuję wartość
        // do mBluetoothAdapter

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // Jeżeli urządzenie nie ma wbudowanego adaptera bluetooth
        if(mBluetoothAdapter == null) {

            userInfoText.setText("Brak dostepnego adapatera Bluetooth....");


        } else {

            // Jeżeli urządzenie posiada adapter ale nie jest on włączony
            if (!mBluetoothAdapter.isEnabled()) {

                // Każ urzytkownikowi włączyć Bluetooth

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);

            } else {

                // Jeśli jest wszystko ok to powiedz urzytkownikowi, ze jest ok

                userInfoText.setText("Wszystko jest ok");

            }


        }


    }



    // Sprawdzam ile urządzeń jest podłączonych żeby pograc od nich info

    private void checkConectedDevices(){

        // Sprawdzam ile urządzeń mam podłączonych za pomocą Bluetooth

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // Jeżeli mam podłączone przynajmniej jedno to..

        if(pairedDevices.size() > 0) {

            // iteruj i dodaj je do listy

            for(BluetoothDevice device : pairedDevices) {

                String deviceName = device.getName();        // Pobierz Nazwę urządzenia
                String deviceAdress = device.getAddress();   // Pobierz adress MAC urządzenia

                mList.add(deviceAdress);
                mmList.add(deviceName);

            }

        }

    }


    private Dialog onCreateDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Lista urządzeń BT")
                .setSingleChoiceItems(mmList.toArray(new String[mmList.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        userInfoText.setText(mList.get(i)); // Wybieram za pomocą int element z listy

                        // Po wybraniu zmieniam okno

                        adres = mList.get(i);

                        Intent intent = new Intent(StartActivity.this, ConnectedActivity.class);

                        intent.putExtra("EXTRA_ADRESS", mList.get(i));

                        // Wyświetl wskaźnik postępu
                        progressDialog = ProgressDialog.show(StartActivity.this, "Conecting...", "Please Wait");


                        // Wyświetl nowe okno

                        startActivity(intent);

                    }
                });

        return builder.create();

    }

}
