package nt.tuwien.ac.at.controller.request;

import lombok.Builder;
import lombok.Data;
import nt.tuwien.ac.at.model.CruspError;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.SequenceDetails;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;

@Data
@Builder
public class SaveMeasurementRequest {
    @NotNull private BigInteger startTime;
    @NotNull private boolean downlink;
    private int numReceivedPackets;
    private float availableBandwidth;
    @NotNull private CruspError errorType;
    private String errorMessage;
    private List<SequenceDetails> sequenceCollection;
    @NotNull private CruspSettings settings;
    private ITelephonyInfo telephonyInfo;
}
