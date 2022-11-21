package nt.tuwien.ac.at.controller;

import nt.tuwien.ac.at.model.*;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;
import nt.tuwien.ac.at.model.network.TelephonyInfoLTE;
import nt.tuwien.ac.at.model.network.TelephonyInfoNR;

import java.math.BigInteger;
import java.util.Arrays;

class TestData {
    static MeasurementResult requestMeasurement = MeasurementResult.builder()
            .availableBandwidth((float)56.287235)
            .errorType(CruspError.NO_ERROR)
            .errorMessage("test_message")
            .startTime(BigInteger.valueOf(123456789123456L))
            .numReceivedPackets(150)
            .settingsId(20) //no settings initialized
            .uid(1)
            .downlink(true)
            .build();

    static SequenceDetails requestSequenceDetails1 = SequenceDetails.builder()
            .expectedPackets((short)150)
            .naiveRate((float)56.287235)
            .seqStartTime(BigInteger.valueOf(123456789101112L))
            .uid(5)
            .measurementResult(requestMeasurement)
            .build();

    static final ReceivedPacketDetails requestPacket1 = ReceivedPacketDetails.builder()
            .deltaToStartTime(0)
            .packetNr((short)1)
            .recvBytes((short)6200)
            .repeatNr((short)1)
            .uid(10)
            .sequence(requestSequenceDetails1)
            .build();

    static final ReceivedPacketDetails requestPacket2 = ReceivedPacketDetails.builder()
            .deltaToStartTime(8614844)
            .packetNr((short)2)
            .recvBytes((short)6200)
            .repeatNr((short)1)
            .uid(11)
            .sequence(requestSequenceDetails1)
            .build();

    static SequenceDetails requestSequenceDetails2 = SequenceDetails.builder()
            .expectedPackets((short)10)
            .naiveRate((float)5.287235)
            .seqStartTime(BigInteger.valueOf(3456789123456L))
            .measurementResult(requestMeasurement)
            .build();

    static final ReceivedPacketDetails requestPacket3 = ReceivedPacketDetails.builder()
            .deltaToStartTime(0)
            .packetNr((short)1)
            .recvBytes((short)1200)
            .repeatNr((short)1)
            .uid(12)
            .sequence(requestSequenceDetails1)
            .build();

    // intelliJ can't resolve lombocks SuperBuilder annotation -> therefore it always complains about builder
    static final ITelephonyInfo requestTelephonyInfo = TelephonyInfoLTE.builder()
            .asu(6)
            .ci(1)
            .cqi(8)
            .dbm(-4)
            .earfcn(8000)
            .mcc("MCC")
            .mnc("MNC")
            .operator("3AT")
            .operatorAlphaLong("Drei AT Long")
            .pci(2)
            .rsrp(10)
            .rsrq(9)
            .rssnr(7)
            .ta(5)
            .tac(3)
            .lat(10.0)
            .lng(11.1)
            .speed((float)0.1)
            .deviceId("id0815")
            .build();

    static final ITelephonyInfo requestTelephonyInfoNr = TelephonyInfoNR.builder()
            .asu(6)
            .nci(1L)
            .dbm(-4)
            .nrarfcn(8000)
            .mcc("MCC")
            .mnc("MNC")
            .operator("3AT")
            .operatorAlphaLong("Drei AT Long")
            .pci(2)
            .csiSinr(10)
            .csiRsrp(9)
            .csiRsrq(7)
            .ssSinr(6)
            .ssRsrp(5)
            .ssRsrq(4)
            .tac(3)
            .lat(10.0)
            .lng(11.1)
            .speed((float)0.1)
            .deviceId("id0815")
            .build();

    static CruspSettings requestCruspSettings = CruspSettings.builder()
            .packetSize(6200)
            .rate(150)
            .repeats(1)
            .sleep(200)
            .timeout(100)
            .volume(58)
            .uid(20)
            .build();

    static {
        requestSequenceDetails1.setPackets(Arrays.asList(requestPacket1, requestPacket2));
        requestSequenceDetails2.setPackets(Arrays.asList(requestPacket3));
        requestMeasurement.setSequenceCollection(Arrays.asList(requestSequenceDetails1, requestSequenceDetails2));
        requestMeasurement.setTelephonyInfo(requestTelephonyInfo);
    }


    static final String requestJson = "{\"availableBandwidth\":56.287235," +
            "\"errorType\":\"NO_ERROR\"," +
            "\"errorMessage\":\"test_message\"," +
            "\"startTime\":123456789101112," +
            "\"numReceivedPackets\":150," +
            "\"sequenceCollection\":[{\"expectedPackets\":150," +
                "\"naiveRate\":56.287235," +
                "\"seqStartTime\":123456789123456," +
                "\"packets\":[{\"deltaToStartTime\":0," +
                    "\"packetNr\":1," +
                    "\"recvBytes\":6200," +
                    "\"repeatNr\":1}," +
                "{\"deltaToStartTime\":8614844," +
                    "\"packetNr\":2," +
                    "\"recvBytes\":6200," +
                    "\"repeatNr\":1}" +
                "]" +
            "}," +
            "{\"expectedPackets\":10," +
                "\"naiveRate\":5.287235," +
                "\"seqStartTime\":3456789123456," +
                "\"packets\":[{\"deltaToStartTime\":0," +
                    "\"packetNr\":1," +
                    "\"recvBytes\":1200," +
                    "\"repeatNr\":1}" +
                "]" +
            "}]," +
            "\"telephonyInfo\":{" +
                "\"@type\":\"LTE\"," +
                "\"ci\":1," +
                "\"mcc\":\"MCC\"," +
                "\"mnc\":\"MNC\"," +
                "\"earfcn\":8000," +
                "\"pci\":2," +
                "\"tac\":3," +
                "\"dbm\":-4," +
                "\"ta\":5," +
                "\"asu\":6," +
                "\"rssnr\":7," +
                "\"cqi\":8," +
                "\"rsrq\":9," +
                "\"rsrp\":10," +
                "\"operatorAlphaLong\":\"Drei AT Long\"," +
                "\"operator\":\"3AT\"," +
                "\"deviceId\":\"id0815\"," +
                "\"lat\":10.0," +
                "\"lng\":11.1," +
                "\"speed\":0.1" +
            "}," +
            "\"settings\":{" +
                "\"repeats\":1," +
                "\"volume\":58," +
                "\"packetSize\":6200," +
                "\"rate\":150," +
                "\"sleep\":200," +
                "\"timeout\":100}" +
            "}";

    static final String filterJson = "{\n" +
            "  \"page\": 0,\n" +
            "  \"pageSize\": 5,\n" +
            "  \"sorted\": [\n" +
            "    {\n" +
            "      \"id\": \"startTime\",\n" +
            "      \"desc\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"numReceivedPackets\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"availableBandwidth\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"errorType\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"errorMessage\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.repeats\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.volume\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.packetSize\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.rate\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.@type\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.dbm\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.operator\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.lat\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.lng\",\n" +
            "      \"desc\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.speed\",\n" +
            "      \"desc\": false\n" +
            "    }\n" +
            "  ],\n" +
            "  \"filtered\": [\n" +
            "    {\n" +
            "      \"id\": \"numReceivedPackets\",\n" +
            "      \"value\": \"50-1000\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"availableBandwidth\",\n" +
            "      \"value\": \"50-200\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"errorType\",\n" +
            "      \"value\": \"NO_ERROR\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.repeats\",\n" +
            "      \"value\": \"1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.volume\",\n" +
            "      \"value\": \"58\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.packetSize\",\n" +
            "      \"value\": \"6200\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"settings.rate\",\n" +
            "      \"value\": \"150\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.@type\",\n" +
            "      \"value\": \"LTE, WCDMA\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.dbm\",\n" +
            "      \"value\": \"-93--3\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.operator\",\n" +
            "      \"value\": \"3AT\"" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.asu\",\n" +
            "      \"value\": \"6\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.lat\",\n" +
            "      \"value\": \"9-49\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.lng\",\n" +
            "      \"value\": \"9-17\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"telephonyInfo.speed\",\n" +
            "      \"value\": \"0-10\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
