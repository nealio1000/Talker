package com.example.aaron.talker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    public final String TCLIENT = "Talker Client";  //for Log.X
    public final String TSERVER = "Talker SERVER";  //for Log.X
    public final int REQUEST_ENABLE_BT = 3313;  //our own code used with Intents
    //public final String APP_NAME = "com.example.aaron.talker";
    private final String MY_UUID_STRING = "12ce62cb-60a1-4edf-9e3a-ca889faccd6c"; //from www.uuidgenerator.net
    private UUID MY_UUID;
    private final String SERVICE_NAME = "Talker";
    private BluetoothAdapter mBluetoothAdapter; //holds the Bluetooth Adapter
    private ArrayList<BluetoothDevice> mBTdeviceList;    //holds found Bluetooth devices
    private TextView mTextarea;                 //for writing messages to screen
    private AcceptThread server;                //server object

    private final BroadcastReceiver mReceiver =        //when activated, scans for Bluetooth devices
            new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    handleBTDevice(intent);

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MY_UUID = UUID.fromString(MY_UUID_STRING);
        mTextarea = (TextView)findViewById(R.id.textView);
        mTextarea.append("My UUID: " + MY_UUID + "\n");
        mBTdeviceList = new ArrayList<>();
        setUpButtons();
    }

    private void setUpButtons() {
        final int NSECONDS = 255;
        Button scan_button;
        Button connect_button;
        scan_button = (Button) findViewById(R.id.scan_button);
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mBluetoothAdapter != null) {
                    getPairedDevices();
                    setUpBroadcastReceiver();
                }
                else {
                    // Device does not support Bluetooth
                    Toast.makeText(getBaseContext(),
                            "No Bluetooth on this device", Toast.LENGTH_LONG).show();
                }
            }
        });
        connect_button = (Button) findViewById(R.id.connect_button);
        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null) {
                    Log.i(TSERVER, "Connect Button setting up server");
                    mTextarea.append("Connect Button: setting up server\n");
                    //make server discoverable for NSECONDS
                    Intent discoverableIntent = new
                            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, NSECONDS);
                    startActivity(discoverableIntent);
                    //create server thread
                    server = new AcceptThread();
                    Log.i(TSERVER, "Connect Button spawning server thread");
                    mTextarea.append("Connect Button: spawning server thread " + server + "\n");
                    if (server != null) {   //start server thread
                        server.start();     //calls AcceptThread's run() method
                    }
                } else {
                    // Device does not support Bluetooth
                    Toast.makeText(getBaseContext(),
                            "No Bluetooth on this device", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothAdapter   = BluetoothAdapter.getDefaultAdapter();
        Log.i("Bluetooth Test", "onResume()");
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getBaseContext(),
                    "No Bluetooth on this device", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Log.i("Bluetooth Test", "enabling Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Log.i("Bluetooth Test", "End of onResume()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Bluetooth Test", "onActivityResult(): requestCode = " + requestCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.i("Bluetooth Test", "  --    Bluetooth is enabled");
                //getPairedDevices(); //find already known paired devices
                //setUpBroadcastReceiver();
            }
        }
    }

    private void getPairedDevices() {//find already known paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.i(TCLIENT, "--------------------------\ngetPairedDevices() - Known Paired Devices");
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.i(TCLIENT, device.getName() + "\n" + device);
                mTextarea.append("" + device.getName() + "\n" + device + "\n");
            }
        }
        Log.i(TCLIENT,"getPairedDevices() - End of Known Paired Devices\n-------------------------");
    }

    private void setUpBroadcastReceiver() { //scan for Bluetooth devices in the area
        // Create a BroadcastReceiver for ACTION_FOUND

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    //called by Broadcast Reciever callback when a new BlueTooth device is found
    private void handleBTDevice(Intent intent) {
//        Log.i(TCLIENT, "onReceive() -- starting   <<<<--------------------");
//        String action = intent.getAction();
//        // When discovery finds a device
//        if  (BluetoothDevice.ACTION_FOUND.equals(action)) {
//            // Get the BluetoothDevice object from the Intent
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            String deviceName = device.getName();
//            Log.i(TCLIENT, deviceName + "\n" + device);
//            mTextarea.append(deviceName + ",  " + device + "\n");
//            if (deviceName.length() > 3) { //for now, looking for MSU prefix
//                String prefix = deviceName.substring(0, 3);
//                mTextarea.append("Prefix = " + prefix + "\n    ");
//                if (prefix.equals("MSU")) {//This is the server
//                    mBluetoothAdapter.cancelDiscovery();
//                    ConnectThread client = new ConnectThread(device);
//                    client.start();
//                }
//            }
//        }

        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            mBTdeviceList.add(device);
            Log.d("FOUND DEVICE", device.getName());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothAdapter.cancelDiscovery();    //stop looking for Bluetooth devices
    }

    public void echoMsg(String msg) {
        mTextarea.append(msg);
    }

    ///////////////////////////////////// Client Thread to talk to Server here //////////////////////

    private class ConnectThread extends Thread { //from android developer
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(TCLIENT,"IOException when creating RFcommSocket\n" + e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception after 12 seconds (or so)
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.i(TCLIENT,"Connect IOException when trying socket connection\n" + connectException);
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.i(TCLIENT,"Close IOException when trying socket connection\n" + closeException);
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);      //talk to server
        }

        //manage the connection over the passed-in socket
        private void manageConnectedSocket(BluetoothSocket socket) {
            OutputStream out;
            String theMessage = "ABC";
            byte [] msg =  theMessage.getBytes();
            try {
                out = socket.getOutputStream();
                Log.i(TCLIENT,"Sending the message: [" + theMessage + "]");
                out.write(msg);
            } catch (IOException ioe) {
                Log.e(TCLIENT, "IOException when opening outputStream\n" + ioe);
                return;
            }
            //cancel();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ioe) {
                Log.e(TCLIENT, "IOException when closing outputStream\n" + ioe);
            }
        }
    }


    /////////////////////////////////////  ServerSocket stuff here ///////////////////////////

    private class AcceptThread extends Thread {  //from android developer
        private  BluetoothServerSocket mmServerSocket = null;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is supposed to be final
            BluetoothServerSocket tmp;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID);
            } catch (IOException e) {return; }
            Log.i(TSERVER, "AcceptThread registered the server\n");
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                Log.i(TSERVER,"Server Looking for a Connection");
                try {
                    socket = mmServerSocket.accept();  //blocks until connection made or exception
                } catch (IOException e) {
                    Log.i(TSERVER, "socket accept through an exception\n" + e);
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    break;
                }
            }
        }

        //manage the Server's end of the conversation on the passed-in socket
        public void manageConnectedSocket(BluetoothSocket socket) {
            Log.i(TSERVER, "\nManaging the Socket\n");
            InputStream in;
            final int nBytes;
            byte [] msg = new byte[255]; //arbitrary size
            try {
                in = socket.getInputStream();
                nBytes = in.read(msg);
                Log.i(TSERVER, "\nServer Received " + nBytes + "\n");
            } catch (IOException ioe) {
                Log.e(TSERVER, "IOException when opening inputStream\n" + ioe);
                return;
            }
            try {
                final String msgString = new String(msg, "UTF-8"); //convert byte array to string
                Log.i(TSERVER, "\nServer Received " + nBytes + "Bytes:  [" + msgString + "]\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        echoMsg("\nReceived " + nBytes + ":  [" + msgString + "]\n");
                    }
                });
            } catch (UnsupportedEncodingException uee) {
                Log.e(TSERVER, "UnsupportedEncodingException when converting bytes to String\n" + uee);
            } finally {
                cancel();
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TSERVER, "IOException when canceling serverSocket\n" + ioe);
            }
        }
    }
}

