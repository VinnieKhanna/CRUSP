package nt.tuwien.ac.at.mapper;

import nt.tuwien.ac.at.controller.request.SaveMeasurementRequest;
import nt.tuwien.ac.at.controller.response.MeasurementResponse;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.MeasurementResult;
import nt.tuwien.ac.at.model.ReceivedPacketDetails;
import nt.tuwien.ac.at.model.SequenceDetails;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface MeasurementResultMapper {
    MeasurementResultMapper INSTANCE = Mappers.getMapper(MeasurementResultMapper.class);

    default MeasurementResponse toResponse(MeasurementResult result, CruspSettings settings) {
        MeasurementResponse core = INSTANCE.toResponseCore(result);
        core.setSettings(settings);
        core.setTelephonyInfo(result.getTelephonyInfo());

        return core;
    }

    MeasurementResponse toResponseCore(MeasurementResult result);

    default MeasurementResponse toResponseLazy(MeasurementResult result, CruspSettings settings) {
        return MeasurementResponse.builder()
                .uid(result.getUid())
                .availableBandwidth(result.getAvailableBandwidth())
                .numReceivedPackets(result.getNumReceivedPackets())
                .startTime(result.getStartTime())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .sequenceCollection(null)
                .settings(settings)
                .telephonyInfo(result.getTelephonyInfo())
                .downlink(result.isDownlink())
                .build();
    }

    default CruspSettings toSettingsDto(SaveMeasurementRequest request) {
        return request.getSettings();
    }

    default ITelephonyInfo toTelephonyInfoDto(SaveMeasurementRequest request) { return request.getTelephonyInfo(); }

    default MeasurementResult toMeasurementDto(SaveMeasurementRequest request) {
        List<SequenceDetails> newDetailsList = new ArrayList<>();

        MeasurementResult mResult=  MeasurementResult.builder()
                .availableBandwidth(request.getAvailableBandwidth())
                .numReceivedPackets(request.getNumReceivedPackets())
                .startTime(request.getStartTime())
                .errorType(request.getErrorType())
                .errorMessage(request.getErrorMessage() != null && request.getErrorMessage().length() >= 256 ? request.getErrorMessage().substring(0, 256) : request.getErrorMessage()) // if the substring is >= than 256 chars, cut the remaining part
                .sequenceCollection(newDetailsList)
                .settingsId(0) // no settings-id availabe
                .downlink(request.isDownlink())
                .build();

        if(request.getSequenceCollection() != null) {
            for (SequenceDetails sequenceDetails : request.getSequenceCollection()) {
                List<ReceivedPacketDetails> newPacketList = new ArrayList<>();

                SequenceDetails newDetails = SequenceDetails.builder()
                        .expectedPackets(sequenceDetails.getExpectedPackets())
                        .measurementResult(mResult)
                        .naiveRate(sequenceDetails.getNaiveRate())
                        .seqStartTime(sequenceDetails.getSeqStartTime())
                        .packets(newPacketList)
                        .build();

                for (ReceivedPacketDetails packet : sequenceDetails.getPackets()) {
                    ReceivedPacketDetails newPacket = ReceivedPacketDetails.builder()
                            .deltaToStartTime(packet.getDeltaToStartTime())
                            .packetNr(packet.getPacketNr())
                            .recvBytes(packet.getRecvBytes())
                            .repeatNr(packet.getRepeatNr())
                            .sequence(newDetails)
                            .build();

                    newPacketList.add(newPacket);
                }

                newDetailsList.add(newDetails);
            }
        }
        return mResult;
    }
}
