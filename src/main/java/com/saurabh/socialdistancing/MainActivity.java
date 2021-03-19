package com.saurabh.socialdistancing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;
import android.widget.ToggleButton;


import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 11;

    private ListView devicelist;
    int k,l,m;
    int rssi;
    private ToggleButton t;
    private Button scanbtn;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter;
    private ArrayAdapter<String> macadapter;
    Vibrator vibrator;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);







        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devicelist = (ListView) findViewById(R.id.deviceList);
        //scanbtn = (Button) findViewById(R.id.scanningBtn);
        t=(ToggleButton)findViewById(R.id.toggleButton);

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        macadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2);
        devicelist.setAdapter(listAdapter);


        checkBluetoothstate();
        new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(devicesFoundReciever, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReciever, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReciever, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        /*scanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    if (checkCoarseLocationPermission()) {
                        listAdapter.clear();
                        l=1;
                        bluetoothAdapter.startDiscovery();
                    }
                } else {
                    checkBluetoothstate();
                }
            }
        });*/
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t.isChecked())
                {
                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                        if (checkCoarseLocationPermission()) {
                            listAdapter.clear();
                            l=1;
                            m=9;
                            bluetoothAdapter.startDiscovery();
                            Toast.makeText(MainActivity.this, "Scanning Started", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        checkBluetoothstate();
                    }

                }
                else {
                      m=2;
                      listAdapter.clear();
                      macadapter.clear();
                }
            }
        });


        checkCoarseLocationPermission();
    }

    private boolean checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        } else return true;

    }

    private void checkBluetoothstate() {
        if (bluetoothAdapter == null)
            Toast.makeText(this, "Bluetooth is not supported in your device !", Toast.LENGTH_SHORT).show();
        else {
            if (bluetoothAdapter.isEnabled()) {
                if (bluetoothAdapter.isDiscovering()) {
                    Toast.makeText(this, "Device discovering process ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                    t.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "You need to enable bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            checkBluetoothstate();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Access coarse location allowed. You can scan bluetooth devices", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Access coarse location forbidden. You can't scan Bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private final BroadcastReceiver devicesFoundReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

if (m==9) {
    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        t.setText("\n\noff");
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
        String s7 = device.getAddress();
        String s9 = device.getName();
        listAdapter.add(device.getName() + "\n" + device.getAddress());
        String s6 = Integer.toString(rssi);
        int h = listAdapter.getPosition(s7);
        long z = listAdapter.getItemId(h);


        if (rssi > -70) {

            int l = macadapter.getPosition(s7);
            long min = macadapter.getItemId(l);
            if (l == -1) {
                macadapter.add(s7);
                Toast.makeText(MainActivity.this, s9, Toast.LENGTH_SHORT).show();
                vibrator.vibrate(1500);
                vibrator.vibrate(1500);

                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 2000);
            }
        }


        listAdapter.notifyDataSetChanged();
        macadapter.notifyDataSetChanged();
    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        t.setText("\n\noff");
        k++;
        if (k == 200) {
            macadapter.clear();
            k = 1;
        }

        listAdapter.clear();
        bluetoothAdapter.startDiscovery();
    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

        {
            t.setText("\n\noff");
        }

    }
}

        }


    };


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_why) {
            startActivity(new Intent(MainActivity.this,Whyt.class));

        } else if (id == R.id.nav_how) {
            startActivity(new Intent(MainActivity.this,Howto.class));

        } else if (id == R.id.nav_about) {
            startActivity(new Intent(MainActivity.this,Contact.class));


        } else if (id == R.id.nav_share) {
            Intent shareintent = new Intent();
            shareintent.setAction(Intent.ACTION_SEND);
            shareintent.putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id=com.saurabh.socialdistancing");
            shareintent.setType("text/plain");
            startActivity(Intent.createChooser(shareintent,"Share via"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}