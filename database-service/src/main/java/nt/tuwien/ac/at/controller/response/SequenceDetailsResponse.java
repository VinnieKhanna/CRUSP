package nt.tuwien.ac.at.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SequenceDetailsResponse {
    private long uid;
    private short expectedPackets;
    private float naiveRate;
    private BigInteger seqStartTime;
    private List<ReceivedPacketDetailsResponse> packets;
}
