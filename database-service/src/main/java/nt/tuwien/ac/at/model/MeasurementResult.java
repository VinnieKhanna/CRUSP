package nt.tuwien.ac.at.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class MeasurementResult {
    @Id
    @Column(name = "measurement_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;

    @NotNull
    @Column(nullable = false, precision = 19, scale  = 0)
    public BigInteger startTime;

    private int numReceivedPackets;
    private float availableBandwidth;

    @NotNull
    @Enumerated
    @Column(columnDefinition = "smallint", nullable = false)
    private CruspError errorType;

    private String errorMessage;

    @OneToMany(mappedBy = "measurementResult", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<SequenceDetails> sequenceCollection;

    @NotNull
    @JoinColumn(name = "settings_id")
    private long settingsId;

    @NotNull
    @OneToOne(targetEntity = ITelephonyInfo.class)
    @JoinColumn(name = "telephony_id")
    private ITelephonyInfo telephonyInfo;

    @NotNull
    private boolean downlink; // true if downlink, otherwise uplink
}
