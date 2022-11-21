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
@Table(name = "NR")
public class TelephonyInfoNR extends ITelephonyInfo {
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

    @JsonCreator
    public TelephonyInfoNR(Long nci, String mcc, String mnc, Integer nrarfcn, Integer pci, Integer tac, Integer dbm, Integer asu, Integer csiSinr, Integer csiRsrq, Integer csiRsrp, Integer ssSinr, Integer ssRsrq, Integer ssRsrp, String operatorAlphaLong, String operator, Double lat, Double lng, Float speed, String deviceId, String manufacturer, String model, Float gpsAccuracy) {
        super(lat, lng, speed, dbm, asu, operator, deviceId, manufacturer, model, gpsAccuracy);
        this.nci = nci;
        this.mcc = mcc;
        this.mnc = mnc;
        this.nrarfcn = nrarfcn;
        this.pci = pci;
        this.tac = tac;
        this.csiSinr = csiSinr;
        this.csiRsrq = csiRsrq;
        this.csiRsrp = csiRsrp;
        this.ssSinr = ssSinr;
        this.ssRsrq = ssRsrq;
        this.ssRsrp = ssRsrp;
        this.operatorAlphaLong = operatorAlphaLong;
    }
}
