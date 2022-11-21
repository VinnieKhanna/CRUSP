package at.ac.tuwien.nt.abe.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class PermissionManager {
    private boolean coarseLocation = false;
    private boolean networkState = false; // needed to show if connected with Mobile or WIFI
    private boolean internet = false; // needed for internet access to measure bandwidth
    private boolean fineLocation = false; // needed for GPS
    private boolean wifiState = false; // needed for WIFI info
    private boolean foreGround = false; // needed for ForeGround Service

    public boolean isAnyPermissionsNotGranted(FragmentActivity activity) {
        networkState = activity.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        internet = activity.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        coarseLocation = activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        fineLocation = activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        wifiState = activity.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            foreGround = activity.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
        } else {
            foreGround = true;
        }

        return !(coarseLocation && networkState && internet && fineLocation && wifiState && foreGround);
    }

    public void requestMissingPermission(Activity activity) {
        // When the user response to the apps permission requrest, the system invokes the apps onRequestPermissionResult() method. Not implemented in our case
        if( !coarseLocation ) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }

        if( !fineLocation ) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},4);
        }

        if( !networkState ) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 2);
        }

        if( !internet ){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.INTERNET},3);
        }

        if( !wifiState ){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_WIFI_STATE},5);
        }

        if( !foreGround ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.FOREGROUND_SERVICE},6);
            }
        }
    }
}
