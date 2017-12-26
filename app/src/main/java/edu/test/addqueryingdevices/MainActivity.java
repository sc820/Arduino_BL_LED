package edu.test.addqueryingdevices;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    int ENABLE_BT_REQUEST_CODE = 1;

    BluetoothAdapter mBluetoothAdapter;
    ListView listview;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter adapter;
    Button pairedBtn, discoveryBtn;
    TextView showTextView;

    public static String EXTRA_ADDRESS = "device_address";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        listview = findViewById(R.id.BLlistview);
        pairedBtn = findViewById(R.id.showBtn);
        discoveryBtn = findViewById(R.id.discoveryBtn);
        showTextView = findViewById(R.id.showTextView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "The device doesn't support bluetooth",Toast.LENGTH_SHORT).show();
            finish();
        }

        /*if bluetooth not enabled, enable it first*/
        if(!mBluetoothAdapter.isEnabled()){
            //request user to turn bluetooth function on
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ENABLE_BT_REQUEST_CODE);
        }

        /*load paired device*/
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(myListClickListener);



        getPairedDevice();

        pairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPairedDevice();
            }
        });

        discoveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoveryNewDevices();
            }
        });

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(MainActivity.this, LEDControl.class);

            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };

    private void getPairedDevice(){
        adapter.clear();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        showTextView.setText("Bluetooth paired devices:");
        if (pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                adapter.add(deviceName + "\n" + deviceHardwareAddress);
            }
        }
        else{
            Toast.makeText(this, "Please discovery new device and paired it first",Toast.LENGTH_SHORT).show();
        }
    }

    private void discoveryNewDevices(){

        // Check if the device is already discovering
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBluetoothAdapter.isEnabled()) {
                showTextView.setText("Bluetooth discovery devices:");
                adapter.clear();
                mBluetoothAdapter.startDiscovery();
                Toast.makeText(this, "Discovering ....",Toast.LENGTH_SHORT).show();
                // Register for broadcasts when a device is discovered.
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter);
            }

        }


    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                adapter.add(device.getName() + "\n" + device.getAddress());
                adapter.notifyDataSetChanged();
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == ENABLE_BT_REQUEST_CODE){

            if(resultCode == Activity.RESULT_OK){

                Toast.makeText(MainActivity.this, "Enable BL successfully", Toast.LENGTH_SHORT).show();
            }
            else{

                Toast.makeText(MainActivity.this, "Enable BL fail", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();


        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

}
