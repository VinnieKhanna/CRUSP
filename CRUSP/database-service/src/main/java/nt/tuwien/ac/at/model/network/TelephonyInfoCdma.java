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
@Table(name = "Cdma")
public class TelephonyInfoCdma extends ITelephonyInfo {
    private Integer basestationId;
    private Integer bsLat;
    private Integer bsLng;
    private Integer networkId;
    private Integer systemId;
    private String operatorAlphaLong;
    private Integer rssi;

    @JsonCreator
    public TelephonyInfoCdma(Integer basestationId, Integer bsLat, Integer bsLng, Integer networkId, Integer systemId, String operatorAlphaLong, Integer dbm, Integer asu, Integer rssi, Double lat, Double lng, Float speed, String operator, String deviceId, String manufacturer, String model, Float gpsAccuracy) {
        super(lat, lng, speed, dbm, asu, operator, deviceId, manufacturer, model, gpsAccuracy);
        this.basestationId = basestationId;
        this.bsLat = bsLat;
        this.bsLng = bsLng;
        this.networkId = networkId;
        this.systemId = systemId;
        this.operatorAlphaLong = operatorAlphaLong;
        this.rssi = rssi;
    }
}
