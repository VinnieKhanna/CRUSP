package nt.tuwien.ac.at.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class ReceivedPacketDetails {
    @Id
    @Column(name = "packet_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;
    @NotNull private short repeatNr;
    @NotNull private short packetNr;
    @NotNull private long deltaToStartTime;
    @NotNull private short recvBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sequence_id")
    private SequenceDetails sequence;
}
