package nt.tuwien.ac.at.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class SequenceDetails {
    @Id
    @Column(name = "sequence_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id")
    private MeasurementResult measurementResult;

    @NotNull private short expectedPackets;
    @NotNull private float naiveRate;

    @Column(nullable = false, precision = 19, scale = 0)
    @NotNull private BigInteger seqStartTime;

    @OneToMany(mappedBy = "sequence", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<ReceivedPacketDetails> packets;
}
