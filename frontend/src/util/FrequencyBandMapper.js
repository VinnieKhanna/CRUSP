class Band {
    constructor(bandNumber, bandName, lowerBound, upperBound) {
        this.bandNumber = bandNumber;
        this.bandName = bandName;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    contains(channelNumber) {
        return channelNumber >= this.lowerBound && channelNumber <= this.upperBound;
    }


    getBandName() {
        return this.bandName;
    }

    getBandNumber() {
        return this.bandNumber;
    }
}

class NRBand extends Band {
    constructor(bandNumber, bandName, lowerBound, upperBound) {
        super(bandNumber, bandName, lowerBound, upperBound);
    }
}

class LTEBand extends Band {
    constructor(bandNumber, bandName, lowerBound, upperBound) {
        super(bandNumber, bandName, lowerBound, upperBound);
    }
}

class GSMBand extends Band {
    constructor(bandNumber, bandName, lowerBound, upperBound) {
        super(bandNumber, bandName, lowerBound, upperBound);
    }
}

class WCDMABand extends Band {
    constructor(bandNumber, bandName, lowerBound, upperBound) {
        super(bandNumber, bandName, lowerBound, upperBound);
    }
}

class WIFIBand extends Band {
    constructor(bandNumber, bandName, lowerBound, upperBound) {
        super(bandNumber, bandName, lowerBound, upperBound);
    }
}

/**
 * NR Info:
 * https://www.3gpp.org/ftp/Specs/latest/Rel-15/38_series/
 */
const nrBands = {};
nrBands[1] = new NRBand(1, "190 MHz", 422000, 434000);
nrBands[2] = new NRBand(2, "80 MHz", 386000, 398000);
nrBands[3] = new NRBand(3, "95 MHz", 361000, 376000);
nrBands[5] = new NRBand(5, "45 MHz", 173800, 178800);
nrBands[7] = new NRBand(7, "120 MHz", 524000, 538000);
nrBands[8] = new NRBand(8, "45 MHz", 185000, 192000);
nrBands[12] = new NRBand(12, "30 MHz", 145800, 149200);
nrBands[20] = new NRBand(20, "-41 MHz", 158200, 164200);
nrBands[25] = new NRBand(25, "80 MHz", 386000, 399000);
nrBands[28] = new NRBand(28, "55 MHz", 151600, 160600);
nrBands[34] = new NRBand(34, "-", 402000, 405000);
nrBands[38] = new NRBand(38, "-", 514000, 524000);
nrBands[39] = new NRBand(39, "-", 376000, 384000);
nrBands[40] = new NRBand(40, "-", 460000, 480000);
nrBands[41] = new NRBand(41, "-", 499200, 537999);
    //nrBands[41] = new NRBand(41, "", 499200, 537996);
nrBands[50] = new NRBand(50, "-", 286400, 303400);
nrBands[51] = new NRBand(51, "-", 285400, 286400);
nrBands[66] = new NRBand(66, "400 MHz", 422000, 440000);
nrBands[70] = new NRBand(70, "295,300 MHz", 399000, 404000);
nrBands[71] = new NRBand(71, "-46 MHz", 123400, 130400);
nrBands[74] = new NRBand(74, "48 MHz", 295000, 303600);
nrBands[75] = new NRBand(75, "-", 286400, 303400);
nrBands[76] = new NRBand(76, "-", 285400, 286400);
nrBands[77] = new NRBand(77, "-", 620000, 680000);
    //nrBands[77] = new NRBand(77, "", 620000, 680000);
nrBands[78] = new NRBand(78, "-", 620000, 653333);
    //nrBands[78] = new NRBand(78, "", 620000, 653332);
nrBands[79] = new NRBand(79, "-", 693334, 733333);
    //nrBands[79] = new NRBand(79, "", 693334, 733332));

/**
 * https://www.rtr.at/en/tk/FRQ_spectrum/LTE_Bands.pdf
 * https://www.3gpp.org/ftp/Specs/latest/Rel-15/36_series/
 */
const lteBands = {};
lteBands[1] = new LTEBand(1, "2100 MHz", 0, 599);
lteBands[2] = new LTEBand(2, "PCS A-F blocks 1,9 GHz", 600,1199);
lteBands[3] = new LTEBand(3, "1800 MHz", 1200, 1949);
lteBands[4] = new LTEBand(4, "AWS-1 1.7+2.1 GHz", 1950, 2399);
lteBands[5] = new LTEBand(5, "850MHz (was CDMA)", 2400, 2649);
lteBands[6] = new LTEBand(6, "850MHz subset (was CDMA)", 2650, 2749);
lteBands[7] = new LTEBand(7, "2600 MHz", 2750, 3449 );
lteBands[8] = new LTEBand(8, "900 MHz", 3450, 3799 );
lteBands[9] = new LTEBand(9, "DCS1800 subset", 3800, 4149);
lteBands[10] = new LTEBand(10, "Extended AWS/AWS-2/AWS-3", 4150, 4749);
lteBands[11] = new LTEBand(11, "1.5 GHz lower", 4750, 4949);
lteBands[12] = new LTEBand(12, "700 MHz lower A(BC) blocks", 5010, 5179 );
lteBands[13] = new LTEBand(13, "700 MHz upper C block", 5180, 5279);
lteBands[14] = new LTEBand(14, "700 MHz upper D block", 5280, 5379 );
lteBands[17] = new LTEBand(17, "700 MHz lower BC blocks", 5730, 5849);
lteBands[18] = new LTEBand(18, "800 MHz lower", 5850, 5999 );
lteBands[19] = new LTEBand(19, "800 MHz upper", 6000, 6149 );
lteBands[20] = new LTEBand(20, "800 MHz", 6150, 6449);
lteBands[21] = new LTEBand(21, "1.5 GHz upper", 6450, 6599 );
lteBands[22] = new LTEBand(22, "3.5 GHz", 6600, 7399);
lteBands[23] = new LTEBand(23, "2 GHz S-Band", 7500, 7699);
lteBands[24] = new LTEBand(24, "1.6 GHz L-Band", 7700, 8039);
lteBands[25] = new LTEBand(25, "PCS A-G blocks 1900", 8040, 8689);
lteBands[26] = new LTEBand(26, "ESMR+ 850 (was: iDEN)", 8690, 9039);
lteBands[27] = new LTEBand(27, "800 MHz SMR (was iDEN)", 9040, 9209);
lteBands[28] = new LTEBand(28, "700 MHz", 9210, 9659);
lteBands[29] = new LTEBand(29, "700 lower DE blocks (suppl. DL)", 9660, 9769);
lteBands[30] = new LTEBand(30, "2.3GHz WCS", 9770, 9869);
lteBands[31] = new LTEBand(31, "IMT 450 MHz", 9870, 9919);
lteBands[32] = new LTEBand(32, "1.5 GHz L-Band (suppl. DL)", 9920, 10359);
lteBands[33] = new LTEBand(33, "2 GHz TDD lower", 36000, 36199);
lteBands[34] = new LTEBand(34, "2 GHz TDD upper", 36200, 36349);
lteBands[35] = new LTEBand(35, "1,9 GHz TDD lower", 36350, 36949);
lteBands[36] = new LTEBand(36, "1.9 GHz TDD upper", 36950, 37549);
lteBands[37] = new LTEBand(37, "PCS TDD", 37550, 37749);
lteBands[38] = new LTEBand(38, "2600 MHz TDD", 37750, 38249);
lteBands[39] = new LTEBand(39, "IMT 1.9 GHz TDD (was TD-SCDMA)", 38250, 38649);
lteBands[40] = new LTEBand(40, "2300 MHz", 38650, 39649);
lteBands[41] = new LTEBand(41, "Expanded TDD 2.6 GHz", 39650, 41589);
lteBands[42] = new LTEBand(42, "3,4-3,6 GHz", 41590, 43589);
lteBands[43] = new LTEBand(43, "3.6-3,8 GHz", 43590, 45589);
lteBands[44] = new LTEBand(44, "700 MHz APT TDD", 45590, 46589);
lteBands[45] = new LTEBand(45, "1500 MHZ", 46590, 46789);
lteBands[46] = new LTEBand(46, "TD Unlicensed", 46790, 54539);
lteBands[47] = new LTEBand(47, "Vehicle to Everything (V2X) TDD", 54540, 55239);
lteBands[65] = new LTEBand(65, "Extended IMT 2100", 65536, 66435);
lteBands[66] = new LTEBand(66, "AWS-3", 66436, 67335 );
lteBands[67] = new LTEBand(67, "700 EU (Suppl. DL)", 67336, 67535 );
lteBands[68] = new LTEBand(68, "700 ME", 67536, 67835);
lteBands[69] = new LTEBand(69, "IMT-E FDD CA",67836, 68335);
lteBands[70] = new LTEBand(70, "AWS-4", 68336,68485);
lteBands[71] = new LTEBand(71, "-", 0, 100000);

const wcdmaBands = {};
wcdmaBands[1] = new WCDMABand(1, "2100 MHz", 10562, 10838);
wcdmaBands[2] = new WCDMABand(2, "1900 MHz PCS", 9662, 9938);
wcdmaBands[3] = new WCDMABand(3, "1800 MHz DCS", 1162, 1513);
wcdmaBands[4] = new WCDMABand(4, "AWS-1", 1537, 1738);
wcdmaBands[5] = new WCDMABand(5, "850 MHz", 4357,4458);
wcdmaBands[6] = new WCDMABand(6, "850 MHz Japan", 4387, 4413);
wcdmaBands[7] = new WCDMABand(7, "2600 MHz", 2237, 2563);
wcdmaBands[8] = new WCDMABand(8, "900 MHz", 2937, 3088);
wcdmaBands[9] = new WCDMABand(9, "1800 MHz Japan", 9237, 9387);
wcdmaBands[10] = new WCDMABand(10, "AWS-1+", 3112, 3388);
wcdmaBands[11] = new WCDMABand(11, "1500 MHz Lower", 3712, 3787);
wcdmaBands[12] = new WCDMABand(12, "700 MHz US a", 3842, 3903);
wcdmaBands[13] = new WCDMABand(13, "700 MHz US c", 4017, 4043);
wcdmaBands[14] = new WCDMABand(14, "700 MHz US PS", 4117, 4143);
wcdmaBands[19] = new WCDMABand(19, "800 MHz Japan", 712, 763);
wcdmaBands[20] = new WCDMABand(20, "800 MHz EU DD", 4512, 4638);
wcdmaBands[21] = new WCDMABand(21, "1500 MHz Upper", 862, 912);
wcdmaBands[22] = new WCDMABand(22, "3500 MHz", 4662, 5038);
wcdmaBands[25] = new WCDMABand(25, "1900+ MHz", 5112, 5413);
wcdmaBands[26] = new WCDMABand(26, "850+ MHz", 5762, 5913);
wcdmaBands[27] = new WCDMABand(27, "-", 0, 100000);

/**
 * https://en.wikipedia.org/wiki/Absolute_radio-frequency_channel_number
 * http://www.3gpp.org/ftp/Specs/archive/45_series/45.005/45005-e10.zip
 */
const gsmBands = {};
gsmBands[31] = new GSMBand(31, "GSM 450", 259, 293);
gsmBands[1] = new GSMBand(0, "GSM 480", 306, 340);
gsmBands[2] = new GSMBand(2, "GSM 1900", 512, 810);
gsmBands[3] = new GSMBand(3, "GSM 1800", 512, 885);
gsmBands[8] = new GSMBand(8, "GSM 900", 1, 124);
gsmBands[9] = new GSMBand(8, "GSM 900", 975, 1023);
gsmBands[6] = new GSMBand(0, "GSM 900", 955, 1023);
gsmBands[5] = new GSMBand(5, "GSM 850", 128, 251);
gsmBands[7] = new GSMBand(0, "GSM 700", 438, 511);
gsmBands[10] = new GSMBand(0, "-", 0, 100000);

const wifiBands = {};
wifiBands[0] = new WIFIBand(0, "-", 0, 100000); // Dummy input

/**
 * @param earfcn value to check
 * @return name of matching LTEBand, "-" if null or invalid
 */
export const getBandFromEarfcn = (earfcn) => {
    if(earfcn != null) {
        for (let [_key,band] of Object.entries(lteBands)) {
            if(band.contains(earfcn)) {
                return "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
            }
        }
    }
    return "-";
};

/**
 * @param uarfcn value to check
 * @return name of matching WCDMABand, "-" if null or invalid
 */
export const getBandFromUarfcn = (uarfcn) => {
    if(uarfcn != null) {
        for (let [_key,band] of Object.entries(wcdmaBands)) {
            if (band.contains(uarfcn)) {
                return "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
            }
        }
        return "-";
    }
};

/**
 * @param arfcn value to check
 * @return name of matching GSMBand, "-" if null or invalid
 */
export const getBandFromArfcn = (arfcn) => {
    if(arfcn != null) {
        for (let [_key,band] of Object.entries(gsmBands)) {
            if (band.contains(arfcn)) {
                return "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
            }
        }
    }
    return "-";
};

/**
 * @param nrarfcn value to check
 * @return name of matching NRBand, "-" if null or invalid
 */
export const getBandFromNrarfcn = (nrarfcn) => {
    if(nrarfcn != null) {
        for (let [_key,band] of Object.entries(nrBands)) {
            if (band.contains(nrarfcn)) {
                return "Band " + band.getBandNumber() + " (" + band.getBandName() + ")";
            }
        }
    }
    return "-";
};
