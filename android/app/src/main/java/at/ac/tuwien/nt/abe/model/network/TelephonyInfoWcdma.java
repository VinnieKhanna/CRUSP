package at.ac.tuwien.nt.abe.model.network;

import android.os.Build;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthWcdma;

import androidx.room.Entity;
import androidx.room.Ignore;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class TelephonyInfoWcdma extends ITelephonyInfo {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = 8909076315521567397L;

    private Integer cid;
    private String mcc;
    private String mnc;
    private Integer lac;
    private Integer psc;
    private Integer uarfcn;
    private String operatorAlphaLong;

    @Ignore public TelephonyInfoWcdma(CellInfoWcdma cellInfoWcdma, String deviceId, String operator) {
        super(0, ConnectionType.WCDMA, 0);
        CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
        CellSignalStrengthWcdma signalStrength = cellInfoWcdma.getCellSignalStrength();

        uarfcn = wrapUnavailable(cellIdentity.getUarfcn()); //  UMTS Absolute RF Channel Number

        cid = wrapUnavailable(cellIdentity.getCid()); // Cell Id
        lac = wrapUnavailable(cellIdentity.getLac()); // Location Area Code
        psc = wrapUnavailable(cellIdentity.getPsc()); // Primary Scrambling Code
        this.operator = operator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            operatorAlphaLong = cellIdentity.getOperatorAlphaLong() != null ? cellIdentity.getOperatorAlphaLong().toString() : null;
            mnc = cellIdentity.getMccString();
            mcc = cellIdentity.getMncString();
        } else {
            operatorAlphaLong = null;
            mnc = String.valueOf(cellIdentity.getMnc()); // Mobile Network Code
            mcc = String.valueOf(cellIdentity.getMcc()); // Mobile Country Code
        }

        dbm = wrapUnavailable(signalStrength.getDbm());
        asu = wrapUnavailable(signalStrength.getAsuLevel());
        this.deviceId = deviceId;
    }

    public TelephonyInfoWcdma(long uid, long measurementId, Integer cid, Integer dbm, Integer asu, String operator, String mcc, String mnc, Integer lac, Integer psc, Integer uarfcn, String operatorAlphaLong, String deviceId) {
        super(uid, ConnectionType.WCDMA, measurementId);
        this.cid = cid;
        this.dbm = dbm;
        this.asu = asu;
        this.operator = operator;
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.psc = psc;
        this.uarfcn = uarfcn;
        this.operatorAlphaLong = operatorAlphaLong;
        this.deviceId = deviceId;
    }

    public Integer getCid() {
        return cid;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public Integer getLac() {
        return lac;
    }

    public Integer getPsc() {
        return psc;
    }

    public Integer getUarfcn() {
        return uarfcn;
    }

    public String getOperatorAlphaLong() {
        return operatorAlphaLong;
    }

}
