package at.ac.tuwien.nt.abe.model.network;

import android.location.Location;
import android.os.Build;

import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

import static androidx.room.ForeignKey.CASCADE;

public abstract class ITelephonyInfo implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ExcludeFromJson
    private long uid;

    @Ignore
    @ExcludeFromJson
    private ConnectionType connectionType;

    @ForeignKey(entity = MeasurementResult.class,
            parentColumns = "uid",
            childColumns = "measurementId",
            onDelete = CASCADE)
    private long measurementId;

    private Double lat;
    private Double lng;
    private Float speed;
    private Float gpsAccuracy;
    Integer dbm;
    Integer asu;
    String deviceId;
    String operator;
    private String manufacturer;
    private String model;

    public ITelephonyInfo(long uid, ConnectionType connectionType, long measurementId) {
        this.uid = uid;
        this.connectionType = connectionType;
        this.measurementId = measurementId;
        this.lat = (double)0;
        this.lng = (double)0;
        this.speed = (float)0;

        this.manufacturer = Build.MANUFACTURER;
        this.model = Build.MODEL;
    }

    Integer wrapUnavailable(int value) {
        if(value == Integer.MAX_VALUE) { // Same value as unavailable
            return null;
        } else {
            return value;
        }
    }

    Long wrapUnavailable(long value) {
        if(value == Long.MAX_VALUE) { // Same value as unavailable
            return null;
        } else {
            return value;
        }
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public void setLocation(Location location) {
        if(location == null) {
            this.speed = 0.0f;
            this.lat = 0.0d;
            this.lng = 0.0d;
            this.gpsAccuracy = 0.0f;
        } else {
            this.lat = location.getLatitude();
            this.lng = location.getLongitude();
            this.speed = location.getSpeed();
            this.gpsAccuracy = location.getAccuracy();
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Integer getAsu() {
        return asu;
    }

    public void setAsu(Integer asu) {
        this.asu = asu;
    }

    public Integer getDbm() {
        return dbm;
    }

    public void setDbm(Integer dbm) {
        this.dbm = dbm;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Float getGpsAccuracy() {
        return gpsAccuracy;
    }

    public void setGpsAccuracy(Float gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }
}
