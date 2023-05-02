package nt.tuwien.ac.at.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceivedPacketDetailsResponse {
    private long uid;
    private short repeatNr;
    private short packetNr;
    private long deltaToStartTime;
    private short recvBytes;
}
