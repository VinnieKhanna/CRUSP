package at.ac.tuwien.nt.abe.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.ac.tuwien.nt.abe.services.json.ExcludeFromJson;

@Entity
public class MeasurementResult implements Serializable {
    @Ignore
    @ExcludeFromJson
    private static final long serialVersionUID = -959729332344792915L;

    @PrimaryKey(autoGenerate = true)
    @ExcludeFromJson
    private long uid; // only used by Room

    @TypeConverters(Converters.class)
    private BigInteger startTime;

    @Ignore
    private List<SequenceDetails> sequenceCollection;
    private int numReceivedPackets;
    private float availableBandwidth;

    @TypeConverters(Converters.class) private CruspError errorType;
    private String errorMessage;

    private boolean downlink;

    private boolean persisted; // indicates if result is persisted in external DB


    // used by Room
    public MeasurementResult(long uid, BigInteger startTime, int numReceivedPackets, float availableBandwidth, CruspError errorType, String errorMessage, boolean downlink) {
        this.uid = uid;
        this.startTime = startTime;
        this.numReceivedPackets = numReceivedPackets;
        this.availableBandwidth = availableBandwidth;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.sequenceCollection = new ArrayList<>(); //empty initialization
        this.downlink = downlink;
        this.persisted = false;
    }

    @Ignore// used by JNI
    public MeasurementResult(SequenceDetails[] sequenceCollection, BigInteger startTime, int numReceivedPackets, float availableBandwidth) {
        this.sequenceCollection =  Arrays.asList(sequenceCollection); //Fixes sized list;
        this.startTime = startTime;
        this.numReceivedPackets = numReceivedPackets;
        this.availableBandwidth = availableBandwidth;
        this.errorType = CruspError.NO_ERROR;
        this.errorMessage = null;
        this.persisted = false;
        this.downlink = true;
    }

    @Ignore// used by JNI
    public MeasurementResult(CruspError errorType, String errorMessage, BigInteger startTime) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.sequenceCollection = new ArrayList<>();
        this.numReceivedPackets = 0;
        this.availableBandwidth = 0;
        this.persisted = false;
        this.downlink = true;
    }

    public double sumUpUsedDataInMB() {
        if(sequenceCollection != null) {
            return sequenceCollection.stream()
                    .flatMap(sequenceDetails -> sequenceDetails.getPackets().stream())
                    .map(packets -> (double)packets.getRecvBytes())
                    .reduce(0.0, Double::sum) / 1000000;
        } else {
            return 0.0;
        }
    }

    public long getTimeSpanInNanos() {
        if(sequenceCollection == null || sequenceCollection.size() == 0) {
            return 0;
        }

        SequenceDetails lastSequence = sequenceCollection.get(sequenceCollection.size()-1);

        int iLastPacket = lastSequence.getPackets().size()-1;
        if(iLastPacket < 0) {
            return 0;
        }
        ReceivedPacketDetails lastPacket = lastSequence.getPackets().get(iLastPacket);

        return lastSequence.getSeqStartTime()
                .add(BigInteger.valueOf(lastPacket.getDeltaToStartTime()))
                .subtract(startTime).longValue();
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

    public List<SequenceDetails> getSequenceCollection() {
        return sequenceCollection;
    }

    public void setSequenceCollection(List<SequenceDetails> sequenceCollection) {
        this.sequenceCollection = sequenceCollection;
    }

    public BigInteger getStartTime() {
        return startTime;
    }

    public CruspError getErrorType() {
        return errorType;
    }

    public int getNumReceivedPackets() {
        return numReceivedPackets;
    }

    public float getAvailableBandwidth() {
        return availableBandwidth;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public boolean isDownlink() {
        return downlink;
    }

    public void setDownlink(boolean downlink) {
        this.downlink = downlink;
    }
}
