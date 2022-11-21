package at.ac.tuwien.nt.abe.util;

import java.util.HashMap;

public class FrequencyBandMapper {

    /**
     * @param earfcn value to check
     * @return name of matching LTEBand, "-" if null or invalid
     */
    public static String getBandFromEarfcn(Integer earfcn){
        if(earfcn != null) {
            for (LTEBand band: lteBands.values()) {
                if(band.contains(earfcn)) {
                    return  "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
                }
            }
        }
        return "-";
    }

    /**
     * @param uarfcn value to check
     * @return name of matching WCDMABand, "-" if null or invalid
     */
    public static String getBandFromUarfcn(Integer uarfcn){
        if(uarfcn != null) {
            for (WCDMABand band : wcdmaBands.values()) {
                if (band.contains(uarfcn)) {
                    return  "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
                }
            }
        }
        return "-";
    }

    /**
     * @param arfcn value to check
     * @return name of matching GSMBand, "-" if null or invalid
     */
    public static String getBandFromArfcn(Integer arfcn){
        if(arfcn != null) {
            for (GSMBand band : gsmBands.values()) {
                if (band.contains(arfcn)) {
                    return "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
                }
            }
        }
        return "-";
    }

    /**
     * NR Info:
     * https://www.3gpp.org/ftp/Specs/latest/Rel-15/38_series/
     */
    /**
     * @param nrarfcn value to check
     * @return name of matching NRBand, "-" if null or invalid
     */
    public static String getBandFromNrarfcn(Integer nrarfcn){
        if(nrarfcn != null) {
            for (NRBand band : nrBands.values()) {
                if (band.contains(nrarfcn)) {
                    return "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
                }
            }
        }
        return "-";
    }

    private static HashMap<Integer, NRBand> nrBands = new HashMap<Integer, NRBand>() {{
        put(1, new NRBand(1, "190 MHz", 422000, 434000));
        put(2, new NRBand(2, "80 MHz", 386000, 398000));
        put(3, new NRBand(3, "95 MHz", 361000, 376000));
        put(5, new NRBand(5, "45 MHz", 173800, 178800));
        put(7, new NRBand(7, "120 MHz", 524000, 538000));
        put(8, new NRBand(8, "45 MHz", 185000, 192000));
        put(12, new NRBand(12, "30 MHz", 145800, 149200));
        put(20, new NRBand(20, "-41 MHz", 158200, 164200));
        put(25, new NRBand(25, "80 MHz", 386000, 399000));
        put(28, new NRBand(28, "55 MHz", 151600, 160600));
        put(34, new NRBand(34, "-", 402000, 405000));
        put(38, new NRBand(38, "-", 514000, 524000));
        put(39, new NRBand(39, "-", 376000, 384000));
        put(40, new NRBand(40, "-", 460000, 480000));
        put(41, new NRBand(41, "-", 499200, 537999));
        //put(41, new NRBand(41, "", 499200, 537996));
        put(50, new NRBand(50, "-", 286400, 303400));
        put(51, new NRBand(51, "-", 285400, 286400));
        put(66, new NRBand(66, "400 MHz", 422000, 440000));
        put(70, new NRBand(70, "295,300 MHz", 399000, 404000));
        put(71, new NRBand(71, "-46 MHz", 123400, 130400));
        put(74, new NRBand(74, "48 MHz", 295000, 303600));
        put(75, new NRBand(75, "-", 286400, 303400));
        put(76, new NRBand(76, "-", 285400, 286400));
        put(77, new NRBand(77, "-", 620000, 680000));
        //put(77, new NRBand(77, "", 620000, 680000));
        put(78, new NRBand(78, "-", 620000, 653333));
        //put(78, new NRBand(78, "", 620000, 653332));
        put(79, new NRBand(79, "-", 693334, 733333));
        //put(79, new NRBand(79, "", 693334, 733332));
    }};

    /**
     * https://www.rtr.at/en/tk/FRQ_spectrum/LTE_Bands.pdf
     * https://www.3gpp.org/ftp/Specs/latest/Rel-15/36_series/
    */
    private static HashMap<Integer, LTEBand> lteBands = new HashMap<Integer, LTEBand>() {{
        put(1, new LTEBand(1, "2100 MHz", 0, 599));
        put(2, new LTEBand(2, "PCS A-F blocks 1,9 GHz", 600,1199));
        put(3, new LTEBand(3, "1800 MHz", 1200, 1949));
        put(4, new LTEBand(4, "AWS-1 1.7+2.1 GHz", 1950, 2399));
        put(5, new LTEBand(5, "850MHz (was CDMA)", 2400, 2649));
        put(6, new LTEBand(6, "850MHz subset (was CDMA)", 2650, 2749));
        put(7, new LTEBand(7, "2600 MHz", 2750, 3449 ));
        put(8, new LTEBand(8, "900 MHz", 3450, 3799 ));
        put(9, new LTEBand(9, "DCS1800 subset", 3800, 4149));
        put(10, new LTEBand(10, "Extended AWS/AWS-2/AWS-3", 4150, 4749));
        put(11, new LTEBand(11, "1.5 GHz lower", 4750, 4949));
        put(12, new LTEBand(12, "700 MHz lower A(BC) blocks", 5010, 5179 ));
        put(13, new LTEBand(13, "700 MHz upper C block", 5180, 5279));
        put(14, new LTEBand(14, "700 MHz upper D block", 5280, 5379 ));
        put(17, new LTEBand(17, "700 MHz lower BC blocks", 5730, 5849));
        put(18, new LTEBand(18, "800 MHz lower", 5850, 5999 ));
        put(19, new LTEBand(19, "800 MHz upper", 6000, 6149 ));
        put(20, new LTEBand(20, "800 MHz", 6150, 6449));
        put(21, new LTEBand(21, "1.5 GHz upper", 6450, 6599 ));
        put(22, new LTEBand(22, "3.5 GHz", 6600, 7399));
        put(23, new LTEBand(23, "2 GHz S-Band", 7500, 7699));
        put(24, new LTEBand(24, "1.6 GHz L-Band", 7700, 8039));
        put(25, new LTEBand(25, "PCS A-G blocks 1900", 8040, 8689));
        put(26, new LTEBand(26, "ESMR+ 850 (was: iDEN)", 8690, 9039));
        put(27, new LTEBand(27, "800 MHz SMR (was iDEN)", 9040, 9209));
        put(28, new LTEBand(28, "700 MHz", 9210, 9659));
        put(29, new LTEBand(29, "700 lower DE blocks (suppl. DL)", 9660, 9769));
        put(30, new LTEBand(30, "2.3GHz WCS", 9770, 9869));
        put(31, new LTEBand(31, "IMT 450 MHz", 9870, 9919));
        put(32, new LTEBand(32, "1.5 GHz L-Band (suppl. DL)", 9920, 10359));
        put(33, new LTEBand(33, "2 GHz TDD lower", 36000, 36199));
        put(34, new LTEBand(34, "2 GHz TDD upper", 36200, 36349));
        put(35, new LTEBand(35, "1,9 GHz TDD lower", 36350, 36949));
        put(36, new LTEBand(36, "1.9 GHz TDD upper", 36950, 37549));
        put(37, new LTEBand(37, "PCS TDD", 37550, 37749));
        put(38, new LTEBand(38, "2600 MHz TDD", 37750, 38249));
        put(39, new LTEBand(39, "IMT 1.9 GHz TDD (was TD-SCDMA)", 38250, 38649));
        put(40, new LTEBand(40, "2300 MHz", 38650, 39649));
        put(41, new LTEBand(41, "Expanded TDD 2.6 GHz", 39650, 41589));
        put(42, new LTEBand(42, "3,4-3,6 GHz", 41590, 43589));
        put(43, new LTEBand(43, "3.6-3,8 GHz", 43590, 45589));
        put(44, new LTEBand(44, "700 MHz APT TDD", 45590, 46589));
        put(45, new LTEBand(45, "1500 MHZ", 46590, 46789));
        put(46, new LTEBand(46, "TD Unlicensed", 46790, 54539));
        put(47, new LTEBand(47, "Vehicle to Everything (V2X) TDD", 54540, 55239));
        put(65, new LTEBand(65, "Extended IMT 2100", 65536, 66435));
        put(66, new LTEBand(66, "AWS-3", 66436, 67335 ));
        put(67, new LTEBand(67, "700 EU (Suppl. DL)", 67336, 67535 ));
        put(68, new LTEBand(68, "700 ME", 67536, 67835));
        put(69, new LTEBand(69, "IMT-E FDD CA",67836, 68335));
        put(70, new LTEBand(70, "AWS-4", 68336,68485));
        put(71, new LTEBand(71, "-", 0, 100000));
    }};

    private static HashMap<Integer, WCDMABand> wcdmaBands = new HashMap<Integer, WCDMABand>() {{
        put(1, new WCDMABand(1, "2100 MHz", 10562, 10838));
        put(2, new WCDMABand(2, "1900 MHz PCS", 9662, 9938));
        put(3, new WCDMABand(3, "1800 MHz DCS", 1162, 1513));
        put(4, new WCDMABand(4, "AWS-1", 1537, 1738));
        put(5, new WCDMABand(5, "850 MHz", 4357,4458));
        put(6, new WCDMABand(6, "850 MHz Japan", 4387, 4413));
        put(7, new WCDMABand(7, "2600 MHz", 2237, 2563));
        put(8, new WCDMABand(8, "900 MHz", 2937, 3088));
        put(9, new WCDMABand(9, "1800 MHz Japan", 9237, 9387));
        put(10, new WCDMABand(10, "AWS-1+", 3112, 3388));
        put(11, new WCDMABand(11, "1500 MHz Lower", 3712, 3787));
        put(12, new WCDMABand(12, "700 MHz US a", 3842, 3903));
        put(13, new WCDMABand(13, "700 MHz US c", 4017, 4043));
        put(14, new WCDMABand(14, "700 MHz US PS", 4117, 4143));
        put(19, new WCDMABand(19, "800 MHz Japan", 712, 763));
        put(20, new WCDMABand(20, "800 MHz EU DD", 4512, 4638));
        put(21, new WCDMABand(21, "1500 MHz Upper", 862, 912));
        put(22, new WCDMABand(22, "3500 MHz", 4662, 5038));
        put(25, new WCDMABand(25, "1900+ MHz", 5112, 5413));
        put(26, new WCDMABand(26, "850+ MHz", 5762, 5913));
        put(27, new WCDMABand(27, "-", 0, 100000));
    }};


    /**
     * https://en.wikipedia.org/wiki/Absolute_radio-frequency_channel_number
     * http://www.3gpp.org/ftp/Specs/archive/45_series/45.005/45005-e10.zip
     */
    private static HashMap<Integer, GSMBand> gsmBands = new HashMap<Integer, GSMBand>() {{
        put(31, new GSMBand(31, "GSM 450", 259, 293));
        put(1, new GSMBand(0, "GSM 480", 306, 340));
        put(4, new GSMBand(0, "GSM 700", 438, 511));
        put(5, new GSMBand(5, "GSM 850", 128, 251));
        put(8, new GSMBand(8, "GSM 900", 1, 124));
        put(9, new GSMBand(8, "GSM 900", 975, 1023));
        put(6, new GSMBand(0, "GSM 900", 955, 1023));
        put(3, new GSMBand(3, "GSM 1800", 512, 885));
        put(2, new GSMBand(2, "GSM 1900", 512, 810));
        put(10, new GSMBand(0, "-", 0, 100000));
    }};

    private static HashMap<Integer, WIFIBand> wifiBands = new HashMap<Integer, WIFIBand>() {{
        put(0, new WIFIBand(0, "-", 0, 100000)); // Dummy input
    }};


    static abstract class Band {
        private int bandNumber;
        private String bandName;
        private float lowerBound; // channel
        private float upperBound; // channel

        Band(int bandNumber, String bandName, int lowerBound, int upperBound) {
            this.bandNumber = bandNumber;
            this.bandName = bandName;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        boolean contains(int channelNumber) {
            return channelNumber >= lowerBound && channelNumber <= upperBound;
        }


        String getBandName() {
            return bandName;
        }

        int getBandNumber() {
            return bandNumber;
        }
    }

    public static class LTEBand extends Band {
        LTEBand(int bandNumber, String bandName, int lowerBound, int upperBound) {
            super(bandNumber, bandName, lowerBound, upperBound);
        }
    }

    public static class NRBand extends Band {
        NRBand(int bandNumber, String bandName, int lowerBound, int upperBound) {
            super(bandNumber, bandName, lowerBound, upperBound);
        }
    }

    public static class GSMBand extends Band {
        GSMBand(int bandNumber, String bandName, int lowerBound, int upperBound) {
            super(bandNumber, bandName, lowerBound, upperBound);
        }
    }

    public static class WCDMABand extends Band {
        WCDMABand(int bandNumber, String bandName, int lowerBound, int upperBound) {
            super(bandNumber, bandName, lowerBound, upperBound);
        }
    }

    public static class WIFIBand extends Band {

        WIFIBand(int bandNumber, String bandName, int lowerBound, int upperBound) {
            super(bandNumber, bandName, lowerBound, upperBound);
        }
    }
}
