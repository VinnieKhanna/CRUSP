package at.ac.tuwien.nt.abe.model.network;

import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellSignalStrengthCdma;

import androidx.room.Entity;
import androidx.room.Ignore;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class TelephonyInfoCdma extends ITelephonyInfo {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = 4001618884340789412L;

    private Integer basestationId;
    private Integer bsLat;
    private Integer bsLng;
    private Integer networkId;
    private Integer systemId;
    private String operatorAlphaLong;

    private int rssi;

    @Ignore public TelephonyInfoCdma(CellInfoCdma cellInfoCdma, String deviceId, String operator) {
        super(0, ConnectionType.CDMA, 0);
        CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
        CellSignalStrengthCdma signalStrength = cellInfoCdma.getCellSignalStrength();

        basestationId = wrapUnavailable(cellIdentity.getBasestationId()); // Base Station Id
        bsLat = wrapUnavailable(cellIdentity.getLatitude()); // Base Station Latitude
        bsLng = wrapUnavailable(cellIdentity.getLongitude()); // Base Station Longitude
        networkId = wrapUnavailable(cellIdentity.getNetworkId()); // Network Id
        systemId = wrapUnavailable(cellIdentity.getSystemId()); // System Id

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            operatorAlphaLong = cellIdentity.getOperatorAlphaLong() != null ? cellIdentity.getOperatorAlphaLong().toString() : null;
        } else {
            operatorAlphaLong = null;
        }

        dbm = wrapUnavailable(signalStrength.getDbm());
        asu = wrapUnavailable(signalStrength.getAsuLevel());
        this.operator = operator;
        this.deviceId = deviceId;
        rssi = wrapUnavailable(signalStrength.getCdmaDbm()); // cdma rssi
    }

    public TelephonyInfoCdma(long uid, long measurementId, Integer basestationId, Integer bsLat, Integer bsLng, Integer networkId, Integer systemId, String operatorAlphaLong, Integer dbm, Integer asu, Integer rssi, String deviceId) {
        super(uid, ConnectionType.CDMA, measurementId);
        this.basestationId = basestationId;
        this.bsLat = bsLat;
        this.bsLng = bsLng;
        this.networkId = networkId;
        this.systemId = systemId;
        this.operatorAlphaLong = operatorAlphaLong;
        this.dbm = dbm;
        this.asu = asu;
        this.operator = operatorAlphaLong;
        this.rssi = rssi;
        this.deviceId = deviceId;
    }

    public Integer getBasestationId() {
        return basestationId;
    }

    public Integer getBsLat() {
        return bsLat;
    }

    public Integer getBsLng() {
        return bsLng;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public Integer getSystemId() {
        return systemId;
    }

    public String getOperatorAlphaLong() {
        return operatorAlphaLong;
    }

    public int getRssi() {
        return rssi;
    }
}
