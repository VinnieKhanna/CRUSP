package nt.tuwien.ac.at.model.network;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TelephonyInfoWcdma.class, name = "WCDMA"),
        @JsonSubTypes.Type(value = TelephonyInfoCdma.class, name = "CDMA"),
        @JsonSubTypes.Type(value = TelephonyInfoGSM.class, name = "GSM"),
        @JsonSubTypes.Type(value = TelephonyInfoLTE.class, name = "LTE"),
        @JsonSubTypes.Type(value = TelephonyInfoNR.class, name = "NR"),
        @JsonSubTypes.Type(value = TelephonyInfoWifi.class, name = "WIFI") })
@Entity
@Data
@NoArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ITelephonyInfo {
    @Id
    @Column(name = "telephony_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;

    private String deviceId;
    private Double lat;
    private Double lng;
    private Float speed;
    private Integer dbm;
    private Integer asu;
    private String operator;
    private String manufacturer;
    private String model;
    private Float gpsAccuracy;

    public ITelephonyInfo(Double lat, Double lng, Float speed, Integer dbm, Integer asu, String operator, String deviceId, String manufacturer, String model, Float gpsAccuracy) {
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
        this.dbm = dbm;
        this.asu = asu;
        this.operator = operator;
        this.deviceId = deviceId;
        this.manufacturer = manufacturer;
        this.model = model;
        this.gpsAccuracy = gpsAccuracy;
    }
}
