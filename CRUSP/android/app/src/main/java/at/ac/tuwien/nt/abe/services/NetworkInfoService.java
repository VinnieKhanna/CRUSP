package at.ac.tuwien.nt.abe.services;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import java.util.List;

import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoCdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoGSM;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoLTE;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoNR;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWcdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWifi;

public class NetworkInfoService extends BroadcastReceiver {
    public static final String IS_MOBILE_CONN = "IS_MOBILE_CONNECTED";
    public static final String IS_WIFI_CONN = "IS_WIFI_CONNECTED";

    public static boolean isWifiConn(Context context) {
        // Get connectivity data
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

        if (networkCapabilities != null) {
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            return false;
        }
    }

    public static boolean isMobileConn(Context context) {
        // Get connectivity data
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities networkCapabilities = null;
        if (cm != null) {
            networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (networkCapabilities != null) {
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }

        return false;
    }

    public static ITelephonyInfo getTelephonyInfo(Context context) {
        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(isWifiConn(context)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int rssi = wifiInfo.getRssi();
                String ssid = wifiInfo.getSSID();

                return new TelephonyInfoWifi(rssi, ssid, deviceId);
            } else {
                return null;
            }


        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission") //permission already checked

            List<CellInfo> foundCells = tm.getAllCellInfo();
            CellInfo cell = null;
            if(foundCells != null && foundCells.size() > 0) {
                for (CellInfo foundCell : foundCells) {
                    if(foundCell.isRegistered()) {
                        cell = foundCell;
                        break;
                    }
                }
            }

            String operator = tm.getNetworkOperatorName();
            if(deviceId == null) {
                deviceId = "";
            }

            if(cell == null) {
                return null;
            } else if( cell instanceof CellInfoGsm) {
                return new TelephonyInfoGSM((CellInfoGsm)cell, deviceId, operator);
            } else if( cell instanceof CellInfoLte) {
                return new TelephonyInfoLTE((CellInfoLte)cell, deviceId, operator);
            } else if( cell instanceof CellInfoNr) {
                return new TelephonyInfoNR((CellInfoNr)cell, deviceId, operator);
            } else if( cell instanceof CellInfoCdma) {
                return new TelephonyInfoCdma((CellInfoCdma)cell, deviceId, operator);
            } else if( cell instanceof CellInfoWcdma) {
                return new TelephonyInfoWcdma((CellInfoWcdma)cell, deviceId, operator);
            }
        }

        return null;
    }


    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_MOBILE_CONN, isMobileConn(context));
        editor.putBoolean(IS_WIFI_CONN, isWifiConn(context));
        editor.apply();
    }
}

