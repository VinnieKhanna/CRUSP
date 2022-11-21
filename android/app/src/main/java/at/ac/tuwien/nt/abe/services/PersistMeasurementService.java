package at.ac.tuwien.nt.abe.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;
import at.ac.tuwien.nt.abe.services.json.JsonExclusionStrategy;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_PERSISTED_SUCCESSFULL;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_RESULT_ID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PersistMeasurementService extends IntentService {
    public static final String ACTION_PERSIST_MEASUREMENT = "ACTION_PERSIST_MEASUREMENT";

    private static final String PARAM_MEASUREMENT_RESULT = "at.ac.tuwien.nt.abe.param.measurement.result";
    private static final String PARAM_TELEPHONY_INFO = "at.ac.tuwien.nt.abe.param.telephony.info";
    private static final String PARAM_CRUSP_TOKEN = "at.ac.tuwien.nt.abe.param.crusp.token";
    private static final String PARAM_URL_ADDRESS = "at.ac.tuwien.nt.abe.param.url.address";

    public PersistMeasurementService() {
        super("PersistMeasurementService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startService(Context context,
                                    String urlAddress,
                                    MeasurementResult measurementResult,
                                    ITelephonyInfo telephonyInfo,
                                    CruspToken token) {
        Intent intent = new Intent(context, PersistMeasurementService.class);
        intent.setAction(ACTION_PERSIST_MEASUREMENT);
        intent.putExtra(PARAM_URL_ADDRESS, urlAddress);
        intent.putExtra(PARAM_MEASUREMENT_RESULT, measurementResult);
        intent.putExtra(PARAM_TELEPHONY_INFO, telephonyInfo);
        intent.putExtra(PARAM_CRUSP_TOKEN, token);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PERSIST_MEASUREMENT.equals(action)) {
                final MeasurementResult measurementResult = (MeasurementResult) intent.getSerializableExtra(PARAM_MEASUREMENT_RESULT);
                final ITelephonyInfo telephonyInfo = (ITelephonyInfo) intent.getSerializableExtra(PARAM_TELEPHONY_INFO);
                final CruspToken cruspToken = (CruspToken) intent.getSerializableExtra(PARAM_CRUSP_TOKEN);
                final String urlAddress = intent.getStringExtra(PARAM_URL_ADDRESS);

                handleActionPersistMeasurement(urlAddress, measurementResult, telephonyInfo, cruspToken);
            }
        }
    }

    /**
     * Handle action PersistMeasurementService in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPersistMeasurement(String urlAdress,
                                                MeasurementResult measurementResult,
                                                ITelephonyInfo telephonyInfo,
                                                CruspToken token) {

        String json = buildJson(measurementResult, telephonyInfo, token);

        boolean successfull = sendRequest(urlAdress, json);

        broadCastResults(successfull, measurementResult.getUid());
    }

    private void broadCastResults(boolean successfull, long uid) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.getBaseContext());
        Intent localIntent = new Intent(ACTION_PERSIST_MEASUREMENT);
        localIntent.putExtra(KEY_PERSISTED_SUCCESSFULL, successfull);
        localIntent.putExtra(KEY_RESULT_ID, uid);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    private String buildJson(MeasurementResult measurementResult, ITelephonyInfo telephonyInfo, CruspToken token) {
        Gson gson = new GsonBuilder() // Basic json
                .setExclusionStrategies(new JsonExclusionStrategy())
                .serializeNulls()
                .create();

        JsonElement gsonTree = gson.toJsonTree(measurementResult); //measurement result
        JsonElement telephonyInfoJson = gson.toJsonTree(telephonyInfo); //telephony info
        telephonyInfoJson.getAsJsonObject().addProperty("@type", telephonyInfo.getConnectionType().name());
        gsonTree.getAsJsonObject().add("telephonyInfo", telephonyInfoJson);

        JsonElement tokenJson = gson.toJsonTree(token); // settings token
        gsonTree.getAsJsonObject().add("settings", tokenJson);

        return gson.toJson(gsonTree);
    }

    /**
     * Sends request to database-service
     * @param urlAddress is the url of the database-service
     * @param measurementJson is the to persist measurement-result in JSON format
     * @return true if persist was successful, false otherwise
     */
    private boolean sendRequest(String urlAddress, String measurementJson) {
        boolean successful = false;
        int[] waitTimes = new int[] {1000, 3000, 10000}; // wait times in ms

        for(int i = 0; i < waitTimes.length+1 && !successful; i++) {
            DataOutputStream os = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlAddress);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                Log.i("JSON", measurementJson);
                os = new DataOutputStream(conn.getOutputStream()); //Error is thrown if url is not reachable
                os.writeBytes(measurementJson);
                os.flush();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG" , conn.getResponseMessage());

                if(conn.getResponseCode() == 200) {
                    successful = true;
                }
            } catch(Exception e) {
                Log.i("POST-FAILED", e.toString());
            } finally {
                if(!successful && i < waitTimes.length) { //otherwise index out of bounds
                    try {
                        if(os != null) {
                            os.close();
                        }

                        if(conn != null) {
                            conn.disconnect();
                        }

                        Thread.sleep(waitTimes[i]);
                    } catch (InterruptedException | IOException e) {
                        Log.i("DATABASE-SERVER", e.toString());
                    }
                }
            }
        }

        return successful;
    }
}
