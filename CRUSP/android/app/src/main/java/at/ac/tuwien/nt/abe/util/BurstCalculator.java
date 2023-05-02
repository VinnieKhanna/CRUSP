package at.ac.tuwien.nt.abe.util;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.nt.abe.model.ReceivedPacketDetails;
import at.ac.tuwien.nt.abe.model.SequenceDetails;

public class BurstCalculator {
    private final static long BURST_DELTA_MIN = 500000; // 500 millis

    /**
     * calculate rate from CRUSP paper of Raida et al.
     * get last packets of bursts and divide data volume by time
     */
    public double calculateSophisticatedRate(List<Burst> bursts) {
        int numOfBursts = bursts.size();
        if(numOfBursts > 2) {

            long totalBits = 0;
            for (Burst burst : bursts.subList(1, bursts.size()-1)) { // skip last burst because it is not included in CRUSP formula
                totalBits += burst.getDataVolumeInBits();
            }

            long totalTime = bursts.get(numOfBursts - 2).getDeltaOfLastPacketToSequenceStart(); // skip last burst because it is not included in CRUSP formula
            long durationsDifference = totalTime - bursts.get(0).getDeltaOfLastPacketToSequenceStart();

            if(durationsDifference == 0.0) {
                return 0;
            }

            // calculation: MBits / sophisticatedDuration in seconds
            // calculation: (bits / 1.000.000)  / (time deltaToNextPacket between last and first time stamp in usec / 1.000.000.000)
            return (double)totalBits * 1000 / (double)durationsDifference;
        } else {
            return 0.0;
        }
    }

    /**
     * This list contains all bursts including the first and last one
     */
    public List<Burst> findBursts(SequenceDetails sequenceDetails) {
        // 1. get deltas between packets
        List<ExtendedReceivedPacketDetails> deltasList = getDeltas(sequenceDetails);

        // 2. to merge the bursts, we select only those packets which have a time-gap >= DELTA_min on the right side
        // save indizes where time-gap >= DELTAmin on the right side, DELTAmin should be around 3 ms
        List<Burst> burstCollection = new ArrayList<>();

        Burst burst = new Burst(0);
        for (ExtendedReceivedPacketDetails details : deltasList) {
            burst.add(details);

            if(details.getDeltaToNextPacket() >= BURST_DELTA_MIN) { // if true, then details is the last packet of the burst
                burstCollection.add(burst);
                burst = new Burst(details.getDeltaToNextPacket()); // the delta between the two bursts is the delta from the current packet (=details) to the next one
            }
        }

        //the last packet has a delta of 0 => therefore add it to the collection
        if(!burst.packets.isEmpty()) {
            burstCollection.add(burst);
        }

        return burstCollection;
    }

    /**
     * calculates the delta/space between two arrived packets.
     * The resulting List of ExtendedReceivedPacketDetails contains a List of all received
     * UDP Packets including the space/delta to the next packet
     */
    private List<ExtendedReceivedPacketDetails> getDeltas(SequenceDetails sequenceDetails) {
        List<ExtendedReceivedPacketDetails> extendedReceivedPacketDetails = new ArrayList<>();

        for (int i = 0; i < sequenceDetails.getPackets().size(); i++) {
            ReceivedPacketDetails packet = sequenceDetails.getPackets().get(i);

            if(i < sequenceDetails.getPackets().size()-1) {
                long delta = sequenceDetails.getPackets().get(i+1).getDeltaToStartTime()- packet.getDeltaToStartTime();
                extendedReceivedPacketDetails.add(new ExtendedReceivedPacketDetails(delta, packet));
            } else {
                extendedReceivedPacketDetails.add(new ExtendedReceivedPacketDetails(0, packet));
            }
        }

        return extendedReceivedPacketDetails;
    }

    /**
     * Additionally saves the deltaToNextPacket to the next packet in nanoseconds
     * If there is no next packet, the deltaToNextPacket is 0
     */
    private class ExtendedReceivedPacketDetails extends ReceivedPacketDetails {
        private static final long serialVersionUID = -836630833986479154L;
        private long deltaToNextPacket;
        ExtendedReceivedPacketDetails(long deltaToNext, ReceivedPacketDetails details) {
            super(details.getRepeatNr(), details.getPacketNr(), details.getDeltaToStartTime(), details.getRecvBytes());
            this.deltaToNextPacket = deltaToNext;
        }

        long getDeltaToNextPacket() {
            return deltaToNextPacket;
        }
    }

    /**
     * Collects the received packets into bursts
     */
    public class Burst {
        private List<ExtendedReceivedPacketDetails> packets;
        private long deltaToPreviousBurst; //delta to the previous packet in previous burst in nanos
        private long dataVolumeInBits; // data Volume of this burst
        private long sophisticatedDuration; // duration from last packet of last burst to last packet of this burst
        private long deltaToSequenceStartFromFirstPacket; // of first packet in burst

        Burst(long deltaToPreviousBurst) {
            this.packets = new ArrayList<>();
            this.deltaToPreviousBurst = deltaToPreviousBurst;
            deltaToSequenceStartFromFirstPacket = 0;
            dataVolumeInBits = 0;
            sophisticatedDuration = 0;
        }

        public double getRate() {
            // calculation: MBits / sophisticatedDuration in seconds
            // calculation: (bits / 1.000.000)  / (time delta between last and first time stamp in usec / 1.000.000.000)
            if(sophisticatedDuration <= 0) {
                return 0.0;
            } else {
                return (double) dataVolumeInBits * 1000.0 / sophisticatedDuration;
            }

        }

        void add(ExtendedReceivedPacketDetails packet) {
            if(packets.isEmpty()) { // if it is the first packet of the burst
                deltaToSequenceStartFromFirstPacket = packet.getDeltaToStartTime();
            }

            this.packets.add(packet);
            dataVolumeInBits += packet.getRecvBytes() * 8;
            sophisticatedDuration = packet.getDeltaToStartTime() - deltaToSequenceStartFromFirstPacket + deltaToPreviousBurst;
        }

        long getDataVolumeInBits() {
            return dataVolumeInBits;
        }

        long getDeltaOfLastPacketToSequenceStart() {
            return packets.get(packets.size()-1).getDeltaToStartTime();
        }

        public int packetCount() {
            return this.packets.size();
        }
    }
}
