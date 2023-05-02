package at.ac.tuwien.nt.abe.fragments.tasks;

import at.ac.tuwien.nt.abe.fragments.MeasurementVariant;
import at.ac.tuwien.nt.abe.fragments.interfaces.OnMeasurementSavedInLocalDB;
import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;

/**
 * Wraps the callback for OnMeasurementSavedInLocalDB and the corresponding telephony info
 */
public class MeasurementSavedInLocalDBCaller {
    private final MeasurementVariant mVariant;
    private MeasurementResult mResult;
    private ITelephonyInfo iTelephonyInfo;
    private CruspToken cruspToken;

    private OnMeasurementSavedInLocalDB callback;

    public MeasurementSavedInLocalDBCaller(MeasurementResult mResult, ITelephonyInfo iTelephonyInfo, CruspToken cruspToken, MeasurementVariant mVariant, OnMeasurementSavedInLocalDB callback) {
        this.mResult = mResult;
        this.iTelephonyInfo = iTelephonyInfo;
        this.cruspToken = cruspToken;
        this.mVariant = mVariant;
        this.callback = callback;
    }

    public void call(long measurementId) {
        mResult.setUid(measurementId);
        callback.onMeasurementSavedInLocalDB(mResult, iTelephonyInfo, cruspToken, mVariant);
    }
}
