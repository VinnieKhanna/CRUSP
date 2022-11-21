package at.ac.tuwien.nt.abe.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

import static androidx.room.ForeignKey.CASCADE;

@Entity
public class SequenceDetails implements Serializable {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = -996838455357381753L;

    @PrimaryKey(autoGenerate = true)
    @ExcludeFromJson
    private long uid; // only used by Room

    @ForeignKey(entity = MeasurementResult.class,
            parentColumns = "uid",
            childColumns = "measurementId",
            onDelete = CASCADE)
    private long measurementId; // only used by Room
    private int expectedPackets;
    private float naiveRate;

    @TypeConverters(Converters.class)
    private BigInteger seqStartTime;

    @Ignore private List<ReceivedPacketDetails> packets;

    // used by Room
    public SequenceDetails(long uid, long measurementId, int expectedPackets, float naiveRate, BigInteger seqStartTime) {
        this.uid = uid;
        this.measurementId = measurementId;
        this.expectedPackets = expectedPackets;
        this.naiveRate = naiveRate;
        this.seqStartTime = seqStartTime;
    }

    @Ignore // used by JNI
    public SequenceDetails(ReceivedPacketDetails[] packets, int expectedPackets, float naiveRate, BigInteger seqStartTime) {
        this.packets = Arrays.asList(packets); //fixed sized List
        this.expectedPackets = expectedPackets;
        this.naiveRate = naiveRate;
        this.seqStartTime = seqStartTime;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }

    public List<ReceivedPacketDetails> getPackets() {
        return packets;
    }

    public int getExpectedPackets() {
        return expectedPackets;
    }

    public float getNaiveRate() {
        return naiveRate;
    }

    public BigInteger getSeqStartTime() {
        return seqStartTime;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setPackets(List<ReceivedPacketDetails> packets) {
        this.packets = packets;
    }
}
