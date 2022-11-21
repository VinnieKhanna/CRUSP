package at.ac.tuwien.nt.abe.fragments.interfaces;

import at.ac.tuwien.nt.abe.fragments.MeasurementVariant;
import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;

public interface OnMeasurementSavedInLocalDB {
    void onMeasurementSavedInLocalDB(MeasurementResult measurementResult, ITelephonyInfo telephonyInfo, CruspToken cruspToken, MeasurementVariant mVariant);
}
