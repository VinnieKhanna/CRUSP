package at.ac.tuwien.nt.abe.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Objects;

import at.ac.tuwien.nt.abe.fragments.MeasurementType;
import at.ac.tuwien.nt.abe.fragments.MeasurementVariant;
import at.ac.tuwien.nt.abe.measurement.RustClient;
import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;

import static at.ac.tuwien.nt.abe.services.NetworkInfoService.getTelephonyInfo;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_CRUSP_TOKEN;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_HOST;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_LOCATION;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_TYPE;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_VARIANT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_PORT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_TELEPHONY_INFO;

/**
 * By default, the service runs in the same process as the main thread
 * A service can only be started once.
 * However, onStartCommand can be called several times
 */
public class MeasurementService extends IntentService {
    public static final String CHANNEL_ID = "MeasurementServiceChannel";
    private static final String TAG = MeasurementService.class.getSimpleName();

    private static final String PATH_EXECUTE_MEASUREMENT_UPLINK = "measurement/v1/uplink";
    private static final String PATH_EXECUTE_MEASUREMENT_DOWNLINK = "measurement/v1/downlink";

    public static final String ACTION_MEASUREMENT = "ACTION_MEASUREMENT";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public MeasurementService() {
        super(TAG);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void start(Context context, MeasurementVariant mVariant, MeasurementType mType, Location location, CruspToken token, String host, int port) {
        Intent serviceIntent = new Intent(context, MeasurementService.class);
        serviceIntent.setAction(ACTION_MEASUREMENT);
        serviceIntent.putExtra(KEY_MEASUREMENT_VARIANT, mVariant);
        serviceIntent.putExtra(KEY_MEASUREMENT_TYPE, mType);
        serviceIntent.putExtra(KEY_CRUSP_TOKEN, token);
        serviceIntent.putExtra(KEY_HOST, host);
        serviceIntent.putExtra(KEY_PORT, port);
        serviceIntent.putExtra(KEY_LOCATION, location);

        context.startService(serviceIntent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null || !Objects.equals(intent.getAction(), ACTION_MEASUREMENT)) {
            return;
        }

        // read out data from intent
        MeasurementVariant mVariant = (MeasurementVariant) intent.getSerializableExtra(KEY_MEASUREMENT_VARIANT);
        MeasurementType mType = (MeasurementType) intent.getSerializableExtra(KEY_MEASUREMENT_TYPE);
        Location location = intent.getParcelableExtra(KEY_LOCATION);
        CruspToken token = (CruspToken) intent.getSerializableExtra(KEY_CRUSP_TOKEN);
        String host = intent.getStringExtra(KEY_HOST);
        int port = intent.getIntExtra(KEY_PORT, 0);

        MeasurementResult result = executeMeasurement(mVariant, token, host, port);

        // ask for telephonyInfo after measurement because RRC connection is then open
        // otherwise TA is UNAVAILABLE, is reported when there is no active RRC connection. Refer to 3GPP 36.213 Sec 4.2.3
        ITelephonyInfo telephonyInfo = getTelephonyInfo(this.getBaseContext());
        if(telephonyInfo != null) {
            telephonyInfo.setLocation(location);
        }

        broadCastResults(result, telephonyInfo, token, mVariant, mType);

        stopSelf();
    }

    private void broadCastResults(MeasurementResult result, ITelephonyInfo telephonyInfo, CruspToken token, MeasurementVariant mVariant, MeasurementType mType) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.getBaseContext());
        Intent localIntent = new Intent(ACTION_MEASUREMENT);
        localIntent.putExtra(KEY_TELEPHONY_INFO, telephonyInfo);
        localIntent.putExtra(KEY_MEASUREMENT_RESULT, result);
        localIntent.putExtra(KEY_CRUSP_TOKEN, token);
        localIntent.putExtra(KEY_MEASUREMENT_VARIANT, mVariant);
        localIntent.putExtra(KEY_MEASUREMENT_TYPE, mType);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    private MeasurementResult executeMeasurement(MeasurementVariant mVariant, CruspToken token, String host, int port) {
        Log.d(TAG, "starting process for measurement");
        Process.setThreadPriority(-20);

        if(token == null) {
            Log.w(TAG, "token is null, abort measurement");
            return null;
        }

        RustClient client = new RustClient();

        MeasurementResult measurementResult = null;
        if(mVariant == MeasurementVariant.UPLINK || mVariant == MeasurementVariant.BOTH_UPLINK) {
            measurementResult =  client.executeUplinkMeasurement(token, host, PATH_EXECUTE_MEASUREMENT_UPLINK, String.valueOf(port));
            measurementResult.setDownlink(false);
        } else if (mVariant == MeasurementVariant.DOWNLINK || mVariant == MeasurementVariant.BOTH_DOWNLINK) {
            measurementResult = client.executeDownlinkMeasurement(token, host, PATH_EXECUTE_MEASUREMENT_DOWNLINK, String.valueOf(port));
            measurementResult.setDownlink(true);
        }

        return measurementResult;
    }
}
