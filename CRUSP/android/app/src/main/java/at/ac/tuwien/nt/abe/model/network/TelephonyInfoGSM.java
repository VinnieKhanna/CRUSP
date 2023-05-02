package at.ac.tuwien.nt.abe.model.network;

import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;

import androidx.room.Entity;
import androidx.room.Ignore;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class TelephonyInfoGSM extends ITelephonyInfo {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = 3930668650102961845L;

    private Integer cid;
    private String mcc;
    private String mnc;
    private Integer lac;
    private Integer bsic;
    private Integer ta;

    private String operatorAlphaLong;
    private Integer arfcn;

    @Ignore public TelephonyInfoGSM(CellInfoGsm cellInfoGsm, String deviceId, String operator) {
        super(0, ConnectionType.GSM, 0);
        CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
        CellSignalStrengthGsm signalStrength = cellInfoGsm.getCellSignalStrength();

        this.bsic = wrapUnavailable(cellIdentity.getBsic()); // Base Station Identity Code


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.ta = wrapUnavailable(signalStrength.getTimingAdvance());
        } else {
            this.ta = null;
        }


        this.cid = cellIdentity.getCid(); // Cell Id
        this.lac = cellIdentity.getLac(); // Location Area Code

        this.operator = operator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.operatorAlphaLong = cellIdentity.getOperatorAlphaLong() != null ? cellIdentity.getOperatorAlphaLong().toString() : null;
            this.arfcn = wrapUnavailable(cellIdentity.getArfcn()); // Absolute RF Channel
            this.mcc = cellIdentity.getMccString();
            this.mnc = cellIdentity.getMncString();
        } else {
            operatorAlphaLong = null;
            this.arfcn = null;
            this.mcc = String.valueOf(cellIdentity.getMcc()); // Mobile Country Code
            this.mnc = String.valueOf(cellIdentity.getMnc()); // Mobile Network Code
        }

        this.dbm = wrapUnavailable(signalStrength.getDbm());
        this.asu = wrapUnavailable(signalStrength.getAsuLevel());
        this.deviceId = deviceId;
    }

    public TelephonyInfoGSM(long uid, long measurementId, Integer cid, String mcc, String mnc, Integer lac, Integer bsic, Integer dbm, Integer ta, Integer asu, String operatorAlphaLong, Integer arfcn, String operator, String deviceId) {
        super(uid, ConnectionType.GSM, measurementId);
        this.cid = cid;
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.bsic = bsic;
        this.dbm = dbm;
        this.ta = ta;
        this.asu = asu;
        this.operatorAlphaLong = operatorAlphaLong;
        this.arfcn = arfcn;
        this.operator = operator;
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

    public Integer getBsic() {
        return bsic;
    }

    public Integer getTa() {
        return ta;
    }

    public String getOperatorAlphaLong() {
        return operatorAlphaLong;
    }

    public Integer getArfcn() {
        return arfcn;
    }
}
