package edu.test.addqueryingdevices;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class LEDControl extends AppCompatActivity {

    ImageButton control;
    Button disConnect;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    boolean state = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        /*Get the Bluetooth device MAC address*/
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        setContentView(R.layout.activity_ledcontrol);


        control = findViewById(R.id.controlBtn);

        disConnect = findViewById(R.id.disConnectBtn);


        control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleLED();
            }
        });
        disConnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        new ConnectBT().execute(); //Call the class to connect


    }

    private void Disconnect()
    {
        turnOffLed();
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }

        finish(); //return to the first layout

    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());
                control.setImageResource(R.drawable.off);
                state = false;

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {

                btSocket.getOutputStream().write("1".toString().getBytes());
                control.setImageResource(R.drawable.on);
                state = true;

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void toggleLED()
    {
        if(state){
            turnOffLed();
        }
        else{
            turnOnLed();
        }

    }


    // Show msg
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            /* show a progress dialog */
            progress = ProgressDialog.show(LEDControl.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    /* get the mobile bluetooth device */
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    /* connects to the device's address and checks if it's available */
                    BluetoothDevice blService = myBluetooth.getRemoteDevice(address);
                    /* create a RFCOMM (SPP) connection */
                    btSocket = blService.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    /* start to connect */
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }
        /*check connection status*/
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }




}
