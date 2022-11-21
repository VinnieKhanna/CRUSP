package at.ac.tuwien.nt.abe.model.network;

import androidx.room.Entity;
import androidx.room.Ignore;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class TelephonyInfoWifi extends ITelephonyInfo {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = 8362387830331085600L;
    private int rssi;


    public TelephonyInfoWifi(long uid, long measurementId, int dbm, int asu, String operator, String deviceId, int rssi) {
        super(uid, ConnectionType.WIFI, measurementId);

        this.asu = asu;
        this.dbm = dbm;
        this.operator = operator;
        this.deviceId = deviceId;
        this.rssi = rssi;
    }

    @Ignore
    public TelephonyInfoWifi(int rssi, String operator, String deviceId) {
        super(0, ConnectionType.WIFI, 0);

        this.asu = 0;
        this.dbm = 0;
        this.operator = operator;
        this.deviceId = deviceId;
        this.rssi = rssi;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
