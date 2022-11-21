package at.ac.tuwien.nt.abe.model.network;

import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;

import androidx.room.Entity;
import androidx.room.Ignore;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class TelephonyInfoNR extends ITelephonyInfo {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = -1793204683149622668L;

    private Long nci;
    private String mcc;
    private String mnc;
    private Integer nrarfcn;
    private Integer pci;
    private Integer tac;
    private Integer csiSinr;
    private Integer csiRsrq;
    private Integer csiRsrp;

    private Integer ssSinr;
    private Integer ssRsrq;
    private Integer ssRsrp;

    private String operatorAlphaLong;

    @Ignore public TelephonyInfoNR(CellInfoNr cellInfoNr, String deviceId, String operator) {
        super(0, ConnectionType.NR, 0);
        CellIdentityNr cellIdentity = (CellIdentityNr) cellInfoNr.getCellIdentity();
        CellSignalStrengthNr signalStrength = (CellSignalStrengthNr) cellInfoNr.getCellSignalStrength();

        nci = wrapUnavailable(cellIdentity.getNci()); // Cell Id

        pci = wrapUnavailable(cellIdentity.getPci()); // Physical Cell Id
        tac = wrapUnavailable(cellIdentity.getTac()); // Tracking Area Code
        this.operator = operator;
        nrarfcn = wrapUnavailable(cellIdentity.getNrarfcn()); // Absolute RF Channel Number -> translates to Band https://en.wikipedia.org/wiki/Absolute_radio-frequency_channel_number

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            operatorAlphaLong = cellIdentity.getOperatorAlphaLong() != null ? cellIdentity.getOperatorAlphaLong().toString() : null;
        } else {
            operatorAlphaLong = null;
        }
        mcc = cellIdentity.getMccString();
        mnc = cellIdentity.getMncString();

        csiRsrp = wrapUnavailable(signalStrength.getCsiRsrp());
        csiRsrq = wrapUnavailable(signalStrength.getCsiRsrq());
        csiSinr = wrapUnavailable(signalStrength.getCsiSinr()); // CSI signal-to-noise and interference ratio

        ssRsrp = wrapUnavailable(signalStrength.getSsRsrp());
        ssRsrq = wrapUnavailable(signalStrength.getSsRsrq());
        ssSinr = wrapUnavailable(signalStrength.getSsSinr()); // SS signal-to-noise and interference ratio

        dbm = wrapUnavailable(signalStrength.getDbm());
        asu = wrapUnavailable(signalStrength.getAsuLevel());

        this.deviceId = deviceId;
    }

    public TelephonyInfoNR(long uid, long measurementId, Long nci, String mcc, String mnc, Integer nrarfcn, Integer pci, Integer tac, Integer dbm,
                           Integer asu, Integer csiSinr, Integer csiRsrq, Integer csiRsrp, Integer ssSinr, Integer ssRsrq, Integer ssRsrp,
                           String operatorAlphaLong, String operator, String deviceId) {
        super(uid, ConnectionType.NR, measurementId);
        this.nci = nci;
        this.mcc = mcc;
        this.mnc = mnc;
        this.nrarfcn = nrarfcn;
        this.pci = pci;
        this.tac = tac;
        this.dbm = dbm;
        this.asu = asu;
        this.csiSinr = csiSinr;
        this.csiRsrq = csiRsrq;
        this.csiRsrp = csiRsrp;
        this.ssSinr = ssSinr;
        this.ssRsrq = ssRsrq;
        this.ssRsrp = ssRsrp;
        this.operatorAlphaLong = operatorAlphaLong;
        this.operator = operator;
        this.deviceId = deviceId;
    }

    public Long getNci() {
        return nci;
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

    public Integer getNrarfcn() {
        return nrarfcn;
    }

    public Integer getPci() {
        return pci;
    }

    public Integer getTac() {
        return tac;
    }

    public Integer getCsiSinr() {
        return csiSinr;
    }

    public Integer getCsiRsrq() {
        return csiRsrq;
    }

    public Integer getCsiRsrp() {
        return csiRsrp;
    }

    public String getOperatorAlphaLong() {
        return operatorAlphaLong;
    }

    public Integer getSsRsrq() {
        return ssRsrq;
    }

    public Integer getSsSinr() {
        return ssSinr;
    }

    public Integer getSsRsrp() {
        return ssRsrp;
    }
}
