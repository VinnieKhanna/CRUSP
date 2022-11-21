package nt.tuwien.ac.at.model;

import lombok.Builder;
import lombok.Data;

/**
 * Same as CruspSettings from Settings-Service
 */
@Data
@Builder
public class CruspSettings {
    private long uid;
    private int repeats;
    private int volume; //in KB
    private int packetSize; // in Byte
    private float rate; //rate in MBit/sec
    private int sleep; // sleep between repeats in millis
    private int timeout; // timeout to wait for eventually lost udp packets before ending the repeat.
}
