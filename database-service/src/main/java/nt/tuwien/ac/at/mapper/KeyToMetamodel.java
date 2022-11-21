package nt.tuwien.ac.at.mapper;

import nt.tuwien.ac.at.model.MeasurementResult_;
import nt.tuwien.ac.at.model.network.*;

public class KeyToMetamodel {

    /**
     *
     * @param key is a String representing the key
     * @return a String with the name of the attribute in the Metamodel
     */
    public static String getMetamodelForKey(String key) {
        switch (key) {
            case "telephonyInfo.lat": return ITelephonyInfo_.LAT;
            case "telephonyInfo.lng": return ITelephonyInfo_.LNG;
            case "telephonyInfo.speed": return ITelephonyInfo_.SPEED;
            case "telephonyInfo.@type": return "@type";
            case "telephonyInfo.operator": return ITelephonyInfo_.OPERATOR;
            case "telephonyInfo.dbm": return ITelephonyInfo_.DBM;
            case "telephonyInfo.asu": return ITelephonyInfo_.ASU;
            case "telephonyInfo.deviceId": return ITelephonyInfo_.DEVICE_ID;
            case "telephonyInfo.gpsAccuracy": return ITelephonyInfo_.GPS_ACCURACY;
            case "telephonyInfo.manufacturer": return ITelephonyInfo_.MANUFACTURER;
            case "telephonyInfo.model": return ITelephonyInfo_.MODEL;
            case "errorMessage": return MeasurementResult_.ERROR_MESSAGE;
            case "errorType": return MeasurementResult_.ERROR_TYPE;
            case "availableBandwidth": return MeasurementResult_.AVAILABLE_BANDWIDTH;
            case "numReceivedPackets": return MeasurementResult_.NUM_RECEIVED_PACKETS;
            case "startTime": return MeasurementResult_.START_TIME;
            case "uid": return MeasurementResult_.UID;
            case "downlink": return MeasurementResult_.DOWNLINK;

            default: return "";
        }
    }
}
