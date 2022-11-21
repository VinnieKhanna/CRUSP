package at.ac.tuwien.nt.abe.util;

import at.ac.tuwien.nt.abe.model.SequenceDetails;

public class FormatHelper {
    private static final int UNAVAILABLE = 2147483647;

    public static String formatDouble(Double value) {
        if(value == null || value == 0.0d) {
            return "-";
        } else {
            return String.valueOf(value);
        }
    }

    public static String formatFloat(Float value) {
        if(value == null || value == 0.0f) {
            return "-";
        } else {
            return String.valueOf(value);
        }
    }

    public static String formantInteger(Integer value) {
        if(value == null || value == UNAVAILABLE) {
            return "-";
        } else {
            return String.valueOf(value);
        }
    }

    public static Float roundTwoAfterDecimal(Float value) {
        if(value != null) {
            return (((float) Math.round(value * 100)) / 100);
        } else {
            return null;
        }
    }

    public static String formatString(String string) {
        return string == null || string.isEmpty() ? "-" : string;
    }

    public static double getDurationOfSequence(SequenceDetails sequenceDetails) {
        if(sequenceDetails.getPackets().size() <= 1) {
            return 0;
        }

        long start = sequenceDetails.getPackets().get(0).getDeltaToStartTime();
        long end = sequenceDetails.getPackets().get(sequenceDetails.getPackets().size()-1).getDeltaToStartTime();

        return(end-start) / 1000000.0;
    }
}
