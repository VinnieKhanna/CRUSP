package at.ac.tuwien.nt.abe.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

import static androidx.room.ForeignKey.CASCADE;

@Entity
public class ReceivedPacketDetails implements Serializable {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = -5291984561018287981L;

    @PrimaryKey(autoGenerate = true)
    @ExcludeFromJson
    private long uid; // only used by Room
    private int repeatNr;
    private int packetNr;
    private long deltaToStartTime; //delta to sequence start time
    private int recvBytes;

    @ForeignKey(entity = SequenceDetails.class,
        parentColumns = "uid",
        childColumns = "seqId",
        onDelete = CASCADE)
    private long seqId; // only used by Room

    // used by Room
    public ReceivedPacketDetails(long uid, int repeatNr, int packetNr, long deltaToStartTime, int recvBytes, long seqId) {
        this.uid = uid;
        this.repeatNr = repeatNr;
        this.packetNr = packetNr;
        this.deltaToStartTime = deltaToStartTime;
        this.recvBytes = recvBytes;
        this.seqId = seqId;
    }

    @Ignore // used by JNI
    public ReceivedPacketDetails(int repeatNr, int packetNr, long deltaToStartTime, int recvBytes) {
        this.repeatNr = repeatNr;
        this.packetNr = packetNr;
        this.deltaToStartTime = deltaToStartTime;
        this.recvBytes = recvBytes;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getSeqId() {
        return seqId;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }

    public int getPacketNr() {
        return packetNr;
    }

    public int getRepeatNr() {
        return repeatNr;
    }

    public long getDeltaToStartTime() {
        return deltaToStartTime;
    }

    public int getRecvBytes() {
        return recvBytes;
    }
}
