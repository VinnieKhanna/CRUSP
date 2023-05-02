package at.ac.tuwien.nt.abe.model;

import java.io.Serializable;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

public class CruspToken implements Serializable {
    @ExcludeFromJson
    private static final long serialVersionUID = -2111490449245796578L;

    private int repeats;     // number of repeats
    private int volume;      // data-volume in kilo-byte (kB)
    private float rate;      // sending rate in Megabyte per second (MB/s)
    private int sleep;       // sleep between repeats in milliseconds (ms)
    private int packetSize; // packet-size in byte
    private int timeout;     // timeout in ms between arriving packets before aborting measurement

    public CruspToken(int repeats, int volume, float rate, int sleep, int packetSize, int timeout) {
        this.repeats = repeats;
        this.volume = volume;
        this.rate = rate;
        this.sleep = sleep;
        this.packetSize = packetSize;
        this.timeout = timeout;
    }

    public int getRepeats() {
        return repeats;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
