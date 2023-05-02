package nt.tuwien.ac.at.model.network;

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
@Table(name = "WIFI")
public class TelephonyInfoWifi extends ITelephonyInfo {
    public TelephonyInfoWifi(Double lat, Double lng, Float speed, String operator, String deviceId, Integer dbm, Integer asu, String manufacturer, String model, Float gpsAccuracy) {
        super(lat, lng, speed, dbm, asu, operator, deviceId, manufacturer, model, gpsAccuracy);
    }
}
