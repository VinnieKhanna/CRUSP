package at.ac.tuwien.nt.abe.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Objects;

import at.ac.tuwien.nt.abe.MainActivity;
import at.ac.tuwien.nt.abe.fragments.MeasurementType;
import at.ac.tuwien.nt.abe.fragments.MeasurementVariant;
import at.ac.tuwien.nt.abe.fragments.interfaces.OnSpeedUpdate;
import at.ac.tuwien.nt.abe.fragments.tasks.SpeedAsyncTask;
import at.ac.tuwien.nt.abe.model.CruspToken;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_CRUSP_TOKEN;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_CYCLE_TIME;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_HOST;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_LOCATION;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_TYPE;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_VARIANT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_PORT;

public class ContinuousMeasurementService extends Service implements OnSpeedUpdate {
    private static final String TAG = ContinuousMeasurementService.class.getSimpleName();
    public static final String CHANNEL_ID = "ContinuousMeasurementServiceChannel";
    public static final String ACTION_START_CONTINUOUS_MEASUREMENT = "ACTION_START_CONTINUOUS_MEASUREMENT";
    private static int FOREGROUND_ID=10;
    private static ContinuousMeasurementService singleton;

    private Handler handler;
    private Location location = null;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private PowerManager.WakeLock wakeLock;

    public ContinuousMeasurementService() {
        singleton = this;
        handler = new Handler();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see Service
     */
    public static void start(Context context, MeasurementVariant mVariant, MeasurementType mType, Location location, CruspToken token, String host, int port, int cycleTimeInSec) {
        Intent serviceIntent = new Intent(context, ContinuousMeasurementService.class);
        serviceIntent.setAction(ACTION_START_CONTINUOUS_MEASUREMENT);
        serviceIntent.putExtra(KEY_MEASUREMENT_VARIANT, mVariant);
        serviceIntent.putExtra(KEY_MEASUREMENT_TYPE, mType);
        serviceIntent.putExtra(KEY_LOCATION, location);
        serviceIntent.putExtra(KEY_CRUSP_TOKEN, token);
        serviceIntent.putExtra(KEY_HOST, host);
        serviceIntent.putExtra(KEY_PORT, port);
        serviceIntent.putExtra(KEY_CYCLE_TIME, cycleTimeInSec);

        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void stopService(Context context) {
        if(singleton != null) {
            Intent serviceIntent = new Intent(context, ContinuousMeasurementService.class);
            singleton.stopService(serviceIntent);
            singleton = null;
        }
    }


    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        super.onCreate();

        initLocationUpdates(this.getApplicationContext());
        startLocationUpdates();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG+":wakelock");
            wakeLock.acquire(); // no timeout since it should run endless

        }

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CRUSP Measurement Service")
                .setContentText("Performing a mesurement...")
                //.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            fusedLocationClient = null;
        }

        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        if(wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || !Objects.equals(intent.getAction(), ACTION_START_CONTINUOUS_MEASUREMENT)) {
            Log.d(TAG, "stop service self...");
            stopSelf();
            singleton = null;
            return START_NOT_STICKY;
        }

        // read out data from intent
        MeasurementVariant mVariant = (MeasurementVariant) intent.getSerializableExtra(KEY_MEASUREMENT_VARIANT);
        MeasurementType mType = (MeasurementType) intent.getSerializableExtra(KEY_MEASUREMENT_TYPE);
        Location mLocation = intent.getParcelableExtra(KEY_LOCATION);
        CruspToken token = (CruspToken) intent.getSerializableExtra(KEY_CRUSP_TOKEN);
        String host = intent.getStringExtra(KEY_HOST);
        int port = intent.getIntExtra(KEY_PORT, 0);
        int cycleTimeInSeconds = intent.getIntExtra(KEY_CYCLE_TIME, 0);

        Runnable periodicUpdate = new ContinuousMeasurementRunnable(mVariant, mType, mLocation, token, host, port, cycleTimeInSeconds);
        handler.post(periodicUpdate);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Measurement Service Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
    }

    private void initLocationUpdates(Context context) {
        if(fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }

        ContinuousMeasurementService mService = this;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                List<Location> locations = locationResult.getLocations(); // list of locations ordered from oldest to newest

                if(!locations.isEmpty()) { // locations.size() is 1 most of the time
                    Location currentLocation = locations.get(locations.size() - 1);

                    if(currentLocation.getSpeed() == 0.0f) {  // Speed not available, calculate it manually
                        SpeedAsyncTask task = new SpeedAsyncTask(mService, locations);
                        task.execute();
                    } else {
                        updateLocation(currentLocation);
                    }
                }
            }
        };
    }

    @Override
    public void updateLocation(Location location) {
        this.location = location;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    private class ContinuousMeasurementRunnable implements Runnable {
        private MeasurementVariant mVariant;
        private MeasurementType mType;
        private CruspToken token;
        private String host ;
        private int port;
        private int cycleTimeInSec;

        private ContinuousMeasurementRunnable(MeasurementVariant mVariant, MeasurementType mType, Location mLocation, CruspToken token, String host, int port, int cycleTimeInSec) {
            this.mVariant = mVariant;
            this.mType = mType;
            this.token = token;
            this.host = host;
            this.port = port;
            this.cycleTimeInSec = cycleTimeInSec;

            if(location == null || location.getLongitude() == 0.0) {
                location = mLocation;
            }
        }

        @Override
        public void run() {
            handler.postDelayed(this, cycleTimeInSec*1000 - SystemClock.elapsedRealtime()%1000);
            Log.d(TAG, "Perform continuous measurement...");

            MeasurementService.start(getApplicationContext(), mVariant, mType, location, token, host, port);
        }
    }
}
