package at.ac.tuwien.nt.abe.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class CruspSetting implements Serializable {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = -2111490449245796578L;

    @PrimaryKey(autoGenerate = true)
    @ExcludeFromJson
    private int uid;

    @ExcludeFromJson
    private String name;

    private int repeats;
    private int volume;
    private int packetSize;
    private int rate;
    private int sleep;
    private int timeout;
    private boolean standard;

    @Ignore
    public CruspSetting() {
    }

    public CruspSetting(int uid, String name, int repeats, int volume, int packetSize, int rate, int sleep, int timeout, boolean standard) {
        this.uid = uid;
        this.name = name;
        this.repeats = repeats;
        this.volume = volume;
        this.packetSize = packetSize;
        this.rate = rate;
        this.sleep = sleep;
        this.timeout = timeout;
        this.standard = standard;
    }

    @Ignore public CruspSetting(String name, int repeats, int volume, int packetSize, int rate, int sleep, int timeout, boolean standard) {
        this.name = name;
        this.repeats = repeats;
        this.volume = volume;
        this.packetSize = packetSize;
        this.rate = rate;
        this.sleep = sleep;
        this.timeout = timeout;
        this.standard = standard;
    }

    public CruspSetting deepCopy() {
        return new CruspSetting(this.name,
                this.repeats,
                this.volume,
                this.packetSize,
                this.rate,
                this.sleep,
                this.timeout,
                this.standard);
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public int getRepeats() {
        return repeats;
    }

    public int getVolume() {
        return volume;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getRate() {
        return rate;
    }

    public int getSleep() {
        return sleep;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isStandard() {
        return standard;
    }

    public void setStandard(boolean standard) {
        this.standard = standard;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
