package nt.tuwien.ac.at.model.network;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "LTE")
public class TelephonyInfoLTE extends ITelephonyInfo {
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

    @JsonCreator
    public TelephonyInfoLTE(Integer ci, String mcc, String mnc, Integer earfcn, Integer pci, Integer tac, Integer dbm, Integer ta, Integer asu, Integer rssnr, Integer cqi, Integer rsrq, Integer rsrp, Integer bandwidth, String operatorAlphaLong, String operator, Double lat, Double lng, Float speed, String deviceId, String manufacturer, String model, Float gpsAccuracy) {
        super(lat, lng, speed, dbm, asu, operator, deviceId, manufacturer, model, gpsAccuracy);
        this.ci = ci;
        this.mcc = mcc;
        this.mnc = mnc;
        this.earfcn = earfcn;
        this.pci = pci;
        this.tac = tac;
        this.ta = ta;
        this.rssnr = rssnr;
        this.cqi = cqi;
        this.rsrq = rsrq;
        this.rsrp = rsrp;
        this.bandwidth = bandwidth;
        this.operatorAlphaLong = operatorAlphaLong;
    }
}
