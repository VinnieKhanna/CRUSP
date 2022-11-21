package at.ac.tuwien.nt.abe.model.network;

import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;

import androidx.room.Entity;
import androidx.room.Ignore;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class TelephonyInfoLTE extends ITelephonyInfo {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = -1793204683149622666L;

    private Integer ci;
    private String mcc;
    private String mnc;
    private Integer earfcn;
    private Integer pci;
    private Integer tac;
    private Integer ta;
    private Integer rssnr;
    private Integer cqi;
    private Integer rsrq;
    private Integer rsrp;
    private Integer bandwidth;

    private String operatorAlphaLong;

    @Ignore public TelephonyInfoLTE(CellInfoLte cellInfoLte, String deviceId, String operator) {
        super(0, ConnectionType.LTE, 0);
        CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
        CellSignalStrengthLte signalStrength = cellInfoLte.getCellSignalStrength();

        ci = wrapUnavailable(cellIdentity.getCi()); // Cell Id

        pci = wrapUnavailable(cellIdentity.getPci()); // Physical Cell Id
        tac = wrapUnavailable(cellIdentity.getTac()); // Tracking Area Code
        this.operator = operator;

        earfcn = wrapUnavailable(cellIdentity.getEarfcn()); // Absolute RF Channel Number -> translates to Band https://en.wikipedia.org/wiki/Absolute_radio-frequency_channel_number

        rsrp = wrapUnavailable(signalStrength.getRsrp());
        rsrq = wrapUnavailable(signalStrength.getRsrq());
        cqi = wrapUnavailable(signalStrength.getCqi());
        rssnr = wrapUnavailable(signalStrength.getRssnr()); // reference signal signal to noise ratio

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            operatorAlphaLong = cellIdentity.getOperatorAlphaLong() != null ? cellIdentity.getOperatorAlphaLong().toString() : null;
            bandwidth = wrapUnavailable(cellIdentity.getBandwidth());
            mcc = cellIdentity.getMccString();
            mnc = cellIdentity.getMncString();
        } else {
            operatorAlphaLong = null;
            mcc = String.valueOf(cellIdentity.getMcc()); // Mobile Country Code
            mnc = String.valueOf(cellIdentity.getMnc()); // Mobile Network Code
            bandwidth = null;
        }

        dbm = wrapUnavailable(signalStrength.getDbm());
        ta = wrapUnavailable(signalStrength.getTimingAdvance());
        asu = wrapUnavailable(signalStrength.getAsuLevel());

        this.deviceId = deviceId;
    }

    public TelephonyInfoLTE(long uid, long measurementId, Integer ci, String mcc, String mnc, Integer earfcn, Integer pci, Integer tac, Integer dbm, Integer ta,
                            Integer asu, Integer rssnr, Integer cqi, Integer rsrq, Integer rsrp, Integer bandwidth, String operatorAlphaLong, String operator, String deviceId) {
        super(uid, ConnectionType.LTE, measurementId);
        this.ci = ci;
        this.mcc = mcc;
        this.mnc = mnc;
        this.earfcn = earfcn;
        this.pci = pci;
        this.tac = tac;
        this.dbm = dbm;
        this.ta = ta;
        this.asu = asu;
        this.rssnr = rssnr;
        this.cqi = cqi;
        this.rsrq = rsrq;
        this.rsrp = rsrp;
        this.bandwidth = bandwidth;
        this.operatorAlphaLong = operatorAlphaLong;
        this.operator = operator;
        this.deviceId = deviceId;
    }

    public Integer getCi() {
        return ci;
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

    public Integer getEarfcn() {
        return earfcn;
    }

    public Integer getPci() {
        return pci;
    }

    public Integer getTac() {
        return tac;
    }

    public Integer getTa() {
        return ta;
    }

    public Integer getRssnr() {
        return rssnr;
    }

    public Integer getCqi() {
        return cqi;
    }

    public Integer getRsrq() {
        return rsrq;
    }

    public Integer getRsrp() {
        return rsrp;
    }

    public String getOperatorAlphaLong() {
        return operatorAlphaLong;
    }

    public Integer getBandwidth() { return bandwidth; }

    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

}
