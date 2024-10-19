package com.project.safeguard.ShakeService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.project.safeguard.Contacts.ContactModel;
import com.project.safeguard.Contacts.DbHelper;
import com.project.safeguard.R;

import java.util.List;

//package com.project.safeguard.ShakeService;
//
//import android.annotation.SuppressLint;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.ActivityNotFoundException;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ServiceInfo;
//import android.hardware.Sensor;
//import android.hardware.SensorManager;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Build;
//import android.os.IBinder;
//import android.os.VibrationEffect;
//import android.os.Vibrator;
//import android.telephony.PhoneNumberUtils;
//import android.telephony.SmsManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.core.app.NotificationCompat;
//
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.CancellationToken;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.OnTokenCanceledListener;
//import com.project.safeguard.Contacts.ContactModel;
//import com.project.safeguard.Contacts.DbHelper;
//import com.project.safeguard.R;
//
//import java.util.List;
//
//public class SensorService extends Service {
//
//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;
//    private ShakeDetector mShakeDetector;
//
//    public SensorService() {
//    }
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//
//        return START_STICKY;
//    }
//    public void sendWhatsAppMessage(String phoneNumber, String message) {
//        // Phone number should include the country code without the '+' sign
//        Log.d(message, "sendWhatsAppMessage: ");
//        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message);
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(url));
//
//        try {
//            // Ensure WhatsApp is installed
//            intent.setPackage("com.whatsapp");
//            startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            // WhatsApp is not installed, show a message to the user
//            Toast.makeText(this, "WhatsApp not installed on this device", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onCreate() {
//
//        super.onCreate();
//
//        // start the foreground service
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            startMyOwnForeground();
//        else
//            startForeground(1, new Notification());
//
//        // ShakeDetector initialization
//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mShakeDetector = new ShakeDetector();
//        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
//
//            @SuppressLint("MissingPermission")
//            @Override
//            public void onShake(int count) {
//                // check if the user has shacked
//                // the phone for 3 time in a row
//                if (count == 3) {
//
//                    // vibrate the phone
//                    vibrate();
//
//                    // create FusedLocationProviderClient to get the user location
//                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
//
//                    // use the PRIORITY_BALANCED_POWER_ACCURACY
//                    // so that the service doesn't use unnecessary power via GPS
//                    // it will only use GPS at this very moment
//                    fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
//                        @Override
//                        public boolean isCancellationRequested() {
//                            return false;
//                        }
//
//                        @NonNull
//                        @Override
//                        public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
//                            return null;
//                        }
//                    }).addOnSuccessListener(new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // check if location is null
//                            // for both the cases we will
//                            // create different messages
//                            if (location != null) {
//
//                                // get the SMSManager
//                                SmsManager smsManager = SmsManager.getDefault();
//
//                                // get the list of all the contacts in Database
//                                DbHelper db = new DbHelper(SensorService.this);
//                                List<ContactModel> list = db.getAllContacts();
//
//                                // send SMS to each contact
//                                for (ContactModel c : list) {
//                                    String message = "Hey, " + c.getName() + "I am in DANGER, i need help. Please urgently reach me out. Here are my coordinates.\n " + "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
//                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
//                                }
//                            } else {
//                                String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station.";
//                                SmsManager smsManager = SmsManager.getDefault();
//                                DbHelper db = new DbHelper(SensorService.this);
//                                List<ContactModel> list = db.getAllContacts();
//                                for (ContactModel c : list) {
//                                    smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
//                                    sendWhatsAppMessage(c.getPhoneNo().toString(),"Hii Iam Arun");
//                                }
//                            }
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d("Check: ", "OnFailure");
//                            String message = "I am in DANGER, i need help. Please urgently reach me out.\n" + "GPS was turned off.Couldn't find location. Call your nearest Police Station.";
//                            SmsManager smsManager = SmsManager.getDefault();
//                            DbHelper db = new DbHelper(SensorService.this);
//                            List<ContactModel> list = db.getAllContacts();
//                            for (ContactModel c : list) {
//                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
//                            }
//                        }
//                    });
//
//                }
//            }
//        });
//
//        // register the listener
//        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
//    }
//
//    // method to vibrate the phone
//
//    public void vibrate() {
//
//        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        VibrationEffect vibEff;
//
//        // Android Q and above have some predefined vibrating patterns
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
//            vibrator.cancel();
//            vibrator.vibrate(vibEff);
//        } else {
//            vibrator.vibrate(500);
//        }
//
//
//    }
//
//    // For Build versions higher than Android Oreo, we launch
//    // a foreground service in a different way. This is due to the newly
//    // implemented strict notification rules, which require us to identify
//    // our own notification channel in order to view them correctly.
//    @RequiresApi(Build.VERSION_CODES.O)
//    private void startMyOwnForeground() {
//
//        String NOTIFICATION_CHANNEL_ID = "example.permanence";
//        String channelName = "Background Service";
//        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);
//
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        assert manager != null;
//        manager.createNotificationChannel(chan);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
//        Notification notification = notificationBuilder.setOngoing(true)
//                .setContentTitle("You are protected.")
//                .setContentText("We are there for you")
//
//                // this is important, otherwise the notification will show the way
//                // you want i.e. it will show some default notification
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//
//                .setPriority(NotificationManager.IMPORTANCE_MIN)
//                .setCategory(Notification.CATEGORY_SERVICE)
//                .build();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // For API 29 (Android 10) and above
//            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
//        } else {
//            // For below API 29
//            startForeground(2, notification);
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        // create an Intent to call the Broadcast receiver
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("restartservice");
//        broadcastIntent.setClass(this, ReactivateService.class);
//        this.sendBroadcast(broadcastIntent);
//        super.onDestroy();
//    }
//
//}
public class SensorService extends Service {
    private static final String CHANNEL_ID = "com.project.safeguard";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startMyOwnForeground();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onShake(int count) {
                // check if the user has shacked
                // the phone for 3 time in a row
                if (count == 3) {

                    // vibrate the phone
                    vibrate();

                    // create FusedLocationProviderClient to get the user location
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

                    // use the PRIORITY_BALANCED_POWER_ACCURACY
                    // so that the service doesn't use unnecessary power via GPS
                    // it will only use GPS at this very moment
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
                                DbHelper db = new DbHelper(SensorService.this);
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
                                DbHelper db = new DbHelper(SensorService.this);
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
                            DbHelper db = new DbHelper(SensorService.this);
                            List<ContactModel> list = db.getAllContacts();
                            for (ContactModel c : list) {
                                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
                            }
                        }
                    });

                }
            }
        });

        // register the listener
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }
    public void vibrate() {

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibEff;

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
            vibrator.cancel();
            vibrator.vibrate(vibEff);
        } else {
            vibrator.vibrate(500);
        }


    }
    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "Background Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Background Service Running")
                    .setContentText("This is a background service")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();

            startForeground(1, notification);
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("Background Service Running")
                    .setContentText("This is a background service")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Perform background operations here
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
