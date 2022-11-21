package at.ac.tuwien.nt.abe.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import at.ac.tuwien.nt.abe.fragments.MeasurementType;
import at.ac.tuwien.nt.abe.fragments.MeasurementVariant;
import at.ac.tuwien.nt.abe.model.CruspSetting;
import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.services.json.JsonExclusionStrategy;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_CRUSP_SETTINGS;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_CRUSP_TOKEN;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_TYPE;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_VARIANT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_RESPONSE_CODE;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_URL_ADDRESS;

public class GetSettingsService extends IntentService{
    private static final String TAG = GetSettingsService.class.getName();
    public static final String ACTION_GET_SETTINGS = "ACTION_GET_SETTINGS";


    public GetSettingsService() {
        super("GetSettingsService");
    }

    /**
     * Starts this service to perform action getSettings with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void start(Context context, String urlAddress, CruspSetting settings, MeasurementVariant measurementVariant, MeasurementType measurementType) {
        Intent intent = new Intent(context, GetSettingsService.class);
        intent.setAction(ACTION_GET_SETTINGS);
        intent.putExtra(KEY_CRUSP_SETTINGS, settings);
        intent.putExtra(KEY_URL_ADDRESS, urlAddress);
        intent.putExtra(KEY_MEASUREMENT_VARIANT, measurementVariant);
        intent.putExtra(KEY_MEASUREMENT_TYPE, measurementType);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_SETTINGS.equals(action)) {
                final CruspSetting setting = (CruspSetting) intent.getSerializableExtra(KEY_CRUSP_SETTINGS);
                final String urlAddress = intent.getStringExtra(KEY_URL_ADDRESS);

                MeasurementVariant mVariant = (MeasurementVariant) intent.getSerializableExtra(KEY_MEASUREMENT_VARIANT);
                MeasurementType mType = (MeasurementType) intent.getSerializableExtra(KEY_MEASUREMENT_TYPE);

                handleActionGetSettings(urlAddress, setting, mVariant, mType);
            }
        }
    }

    /**
     * Handle action GetSettingsService in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetSettings(String urlAdress, CruspSetting cruspSetting, MeasurementVariant mVariant, MeasurementType mType) {

        String json = buildJson(cruspSetting);

        HttpURLConnection conn = sendRequest(urlAdress, json);
        int responseCode = 0;
        CruspToken token = null;

        if(conn == null) { //service not available
            responseCode = 404;
        } else {
            try {
                responseCode = conn.getResponseCode();
                Log.i(TAG, String.valueOf(conn.getResponseCode()));
                Log.d(TAG , conn.getResponseMessage());

                StringBuilder response = new StringBuilder();
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response.append(line);
                }

                Gson gson = new Gson();
                token = gson.fromJson(response.toString(), CruspToken.class);

            } catch (IOException| JsonSyntaxException e ) {
                Log.w(TAG, e.toString());
            }
        }

        broadCastResults(token, responseCode, mVariant, mType);
    }

    private void broadCastResults(CruspToken token, int responseCode, MeasurementVariant mVariant, MeasurementType mType) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.getBaseContext());
        Intent localIntent = new Intent(ACTION_GET_SETTINGS);
        localIntent.putExtra(KEY_CRUSP_TOKEN, token);
        localIntent.putExtra(KEY_RESPONSE_CODE, responseCode);
        localIntent.putExtra(KEY_MEASUREMENT_VARIANT, mVariant);
        localIntent.putExtra(KEY_MEASUREMENT_TYPE, mType);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    private String buildJson(CruspSetting setting) {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new JsonExclusionStrategy())
                .serializeNulls()
                .create();

        Gson gsonSettings = new GsonBuilder()
                .setExclusionStrategies(new JsonExclusionStrategy())
                .serializeNulls()
                .create();
        JsonElement settingsJson = gsonSettings.toJsonTree(setting);

        return gson.toJson(settingsJson);
    }

    /**
     * Sends request to settings-service
     * @param urlAddress is the url of the settings-service
     * @param settingsJson are the crusp-settings JSON format
     * @return settings for measurement on success, otherwise HTTP error-status
     */
    private HttpURLConnection sendRequest(String urlAddress, String settingsJson) {
        HttpURLConnection conn;
        try {
            URL url = new URL(urlAddress);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            Log.i(TAG, "Send get-settings-request with JSON: " + settingsJson);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream()); //Error is thrown if url is not reachable
            os.writeBytes(settingsJson);
            os.flush();

        } catch(Exception e) {
            Log.i(TAG, e.toString());
            conn = null;
        }

        return conn;
    }
}
