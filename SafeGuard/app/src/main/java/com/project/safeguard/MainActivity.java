package com.project.safeguard;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.safeguard.Contacts.ContactModel;
import com.project.safeguard.Contacts.CustomAdapter;
import com.project.safeguard.Contacts.DbHelper;
import com.project.safeguard.ShakeService.SensorService;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button button1;
    ListView listView;
    DbHelper db;
    List<ContactModel> list;
    CustomAdapter customAdapter;
    private static final int REQUEST_CODE = 1;
    
    private static final int PICK_CONTACT = 1;
    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002;
    private SpeechRecognizer speechRecognizer;
    private FloatingActionButton chatButton;
    Intent intent;
    private boolean isFragmentDisplayed = false;
    private View grayOverlay;
    private FrameLayout fragmentContainer;
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }
    public void startButton(View view){
        view.animate().rotationBy(360).setDuration(500).start();
        speechRecognizer.startListening(intent);
    }
    private void askIgnoreOptimization() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST);
        }

    }
    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // get the contact from the PhoneBook of device
        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {

                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        @SuppressLint("Range") String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String phone = null;
                        try {
                            if (hasPhone.equalsIgnoreCase("1")) {
                                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                                phones.moveToFirst();
                                phone = phones.getString(phones.getColumnIndex("data1"));
                            }
                            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            db.addcontact(new ContactModel(0, name, phone));
                            list = db.getAllContacts();
                            customAdapter.refresh(list);
                        } catch (Exception ex) {
                        }
                    }
                }
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        button1 = findViewById(R.id.Button1);
        listView = (ListView) findViewById(R.id.ListView);
        db = new DbHelper(this);
        list = db.getAllContacts();
        customAdapter = new CustomAdapter(this, list);
        listView.setAdapter(customAdapter);
        grayOverlay = findViewById(R.id.gray_overlay);
        fragmentContainer = findViewById(R.id.fragment_container);
        FloatingActionButton fabChat = findViewById(R.id.fab_chat);
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (isFragmentDisplayed) {
                    closeFragment();
                } else {
                    displayFragment();
                }
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling of getContacts()
                if (db.count() != 5) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, PICK_CONTACT);
                } else {
                    Toast.makeText(MainActivity.this, "Can't Add more than 5 Contacts", Toast.LENGTH_SHORT).show();
                }
            }
        });
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }
            @Override
            public void onBeginningOfSpeech() {
            }
            @Override
            public void onRmsChanged(float rmsdB) {
            }
            @Override
            public void onBufferReceived(byte[] buffer) {
            }
            @Override
            public void onEndOfSpeech() {
            }
            @Override
            public void onError(int error) {
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                if (matches != null){
                    String string = matches.get(0);
                    Log.d("ChatFragment", "onResults: "+string);
                    if (string.equalsIgnoreCase("save me")||string.equals("help me")|| string.equalsIgnoreCase("kapadandi")|| string.equalsIgnoreCase("bachana")){
                        trigger();
                    }
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {
            }
            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }
    public void displayFragment() {
        ChatFragment chatFragment = new ChatFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        fragmentTransaction.add(R.id.fragment_container, chatFragment).addToBackStack(null).commit();
        grayOverlay.setVisibility(View.VISIBLE); fragmentContainer.setVisibility(View.VISIBLE);
        isFragmentDisplayed = true;
    } public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment chatFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (chatFragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentTransaction.remove(chatFragment).commit();
        }
        grayOverlay.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.GONE);
        isFragmentDisplayed = false;
    }
    @SuppressLint("MissingPermission")
    private void trigger(){
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // check if location is null
                // for both the cases we will
                // create different messages
                if (location != null) {

                    // get the SMSManager
                    SmsManager smsManager = SmsManager.getDefault();

                    // get the list of all the contacts in Database
                    DbHelper db = new DbHelper(MainActivity.this);
                    List<ContactModel> list = db.getAllContacts();

                    // send SMS to each contact
                    for (ContactModel c : list) {
                        String message = "Hey, " + c.getName() + "I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                        smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                    }
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + list.get(0).getPhoneNo()));
                } else {
                    String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station.";
                    SmsManager smsManager = SmsManager.getDefault();
                    DbHelper db = new DbHelper(MainActivity.this);
                    List<ContactModel> list = db.getAllContacts();
                    for (ContactModel c : list) {
                        smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Check: ", "OnFailure");
                String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station.";
                SmsManager smsManager = SmsManager.getDefault();
                DbHelper db = new DbHelper(MainActivity.this);
                List<ContactModel> list = db.getAllContacts();
                for (ContactModel c : list) {
                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                }
            }
        });
    }
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            }, REQUEST_CODE);
        } else {
            startBackgroundService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                startBackgroundService();
            } else {
                Toast.makeText(this, "Permissions denied, unable to start service", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startBackgroundService() {

        Intent intent = new Intent(this, SensorService.class);
        startService(intent);

    }
}
