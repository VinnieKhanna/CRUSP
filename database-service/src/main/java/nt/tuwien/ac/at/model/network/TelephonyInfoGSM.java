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
@Table(name = "GSM")
public class TelephonyInfoGSM extends ITelephonyInfo {
    private Integer cid;
    private String mcc;
    private String mnc;
    private Integer lac;
    private Integer bsic;
    private Integer ta;

    private String operatorAlphaLong;
    private Integer arfcn;

    @JsonCreator
    public TelephonyInfoGSM(Integer cid, String mcc, String mnc, Integer lac, Integer bsic, Integer dbm, Integer ta, Integer asu, String operatorAlphaLong, Integer arfcn, String operator, Double lat, Double lng, Float speed, String deviceId, String manufacturer, String model, Float gpsAccuracy) {
        super(lat, lng, speed, dbm, asu, operator, deviceId, manufacturer, model, gpsAccuracy);
        this.cid = cid;
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.bsic = bsic;
        this.ta = ta;
        this.operatorAlphaLong = operatorAlphaLong;
        this.arfcn = arfcn;
    }
}
