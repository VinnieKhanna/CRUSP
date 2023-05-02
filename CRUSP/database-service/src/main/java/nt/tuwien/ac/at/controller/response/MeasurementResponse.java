package nt.tuwien.ac.at.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nt.tuwien.ac.at.model.CruspError;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementResponse {
    private long uid;
    private BigInteger startTime;
    private int numReceivedPackets;
    private float availableBandwidth;
    private CruspError errorType;
    private String errorMessage;
    private List<SequenceDetailsResponse> sequenceCollection;
    private CruspSettings settings;
    private ITelephonyInfo telephonyInfo;
    private boolean downlink;
}
