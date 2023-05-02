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
@Table(name = "Wcdma")
public class TelephonyInfoWcdma extends ITelephonyInfo {
    private Integer cid;
    private String mcc;
    private String mnc;
    private Integer lac;
    private Integer psc;
    private Integer uarfcn;
    private String operatorAlphaLong;

    @JsonCreator
    public TelephonyInfoWcdma(Integer cid, Integer dbm, Integer asu, String operator, String mcc, String mnc, Integer lac, Integer psc, Integer uarfcn, String operatorAlphaLong, Double lat, Double lng, Float speed, String deviceId, String manufacturer, String model, Float gpsAccuracy) {
        super(lat, lng, speed, dbm, asu, operator, deviceId, manufacturer, model, gpsAccuracy);
        this.cid = cid;
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.psc = psc;
        this.uarfcn = uarfcn;
        this.operatorAlphaLong = operatorAlphaLong;
    }
}
