package at.ac.tuwien.nt.abe.model;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class MeasurementResultTest {

    @Test
    public void sumUpUsedDataInMBShouldReturn0() {
        MeasurementResult result = new MeasurementResult(CruspError.COMMUNICATION_ERROR, "TestError", new BigInteger(String.valueOf(0)));

        double foundData = result.sumUpUsedDataInMB();

        assertEquals(0.0, foundData, 0.000000001);
    }

    @Test
    public void sumUpUsedDataInMBShouldReturnValidResult() {

        ReceivedPacketDetails packet1 = new ReceivedPacketDetails((short)1,(short)1,0,(short)1000);
        ReceivedPacketDetails packet2 = new ReceivedPacketDetails((short)1,(short)1,0,(short)1500);
        SequenceDetails details = new SequenceDetails(new ReceivedPacketDetails[]{packet1, packet2},(short)2, 10, new BigInteger(String.valueOf(System.nanoTime())));
        MeasurementResult result = new MeasurementResult(new SequenceDetails[]{details}, new BigInteger(String.valueOf(System.nanoTime())), 2, (float)10.0);

        double foundData = result.sumUpUsedDataInMB();
        assertEquals(0.0025, foundData, 0.000000001);
    }
}