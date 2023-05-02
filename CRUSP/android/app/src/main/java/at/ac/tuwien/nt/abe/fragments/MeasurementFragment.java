package at.ac.tuwien.nt.abe.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;

import at.ac.tuwien.nt.abe.R;
import at.ac.tuwien.nt.abe.ResultDetailsActivity;
import at.ac.tuwien.nt.abe.fragments.interfaces.OnSpeedUpdate;
import at.ac.tuwien.nt.abe.fragments.tasks.MeasurementSavedInLocalDBCaller;
import at.ac.tuwien.nt.abe.fragments.tasks.SpeedAsyncTask;
import at.ac.tuwien.nt.abe.fragments.tasks.UpdateContinuousMeasurementChartAsyncTask;
import at.ac.tuwien.nt.abe.model.CruspError;
import at.ac.tuwien.nt.abe.model.CruspSetting;
import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;
import at.ac.tuwien.nt.abe.services.ContinuousMeasurementService;
import at.ac.tuwien.nt.abe.services.GetSettingsService;
import at.ac.tuwien.nt.abe.services.MeasurementService;
import at.ac.tuwien.nt.abe.services.NetworkInfoService;
import at.ac.tuwien.nt.abe.services.PersistMeasurementService;
import at.ac.tuwien.nt.abe.util.PermissionManager;
import at.ac.tuwien.nt.abe.viewmodels.DisplayViewModel;
import at.ac.tuwien.nt.abe.viewmodels.MeasurementResultsViewModel;
import at.ac.tuwien.nt.abe.viewmodels.TelephonyInfoViewModel;

import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.DEF_CREDIT;
import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.DEF_CYCLE;
import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.DEF_UNIT_POS;
import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.KEY_CREDIT;
import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.KEY_CYCLE;
import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.KEY_UNIT_POS;
import static at.ac.tuwien.nt.abe.fragments.SettingsFragment.getCruspSettingsFromSharedPreferences;
import static at.ac.tuwien.nt.abe.services.NetworkInfoService.IS_MOBILE_CONN;
import static at.ac.tuwien.nt.abe.services.NetworkInfoService.IS_WIFI_CONN;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_CONT_MEASUREMENT_ACTIVATED;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_CRUSP_TOKEN;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MAX_DATA;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT_ID;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_TYPE;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_VARIANT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_PERSISTED_SUCCESSFULL;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_RESPONSE_CODE;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_RESULT_ID;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_TELEPHONY_INFO;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_USED_DATA;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_USED_DATA_DATE;

public class MeasurementFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        OnSpeedUpdate{
    private static final String TAG = MeasurementFragment.class.getSimpleName();

    private static final String HOST = "hossman.nt.tuwien.ac.at";
    private static final int PORT = 8090;
    private static final String PATH_SAVE_MEASUREMENT = "database/v1/measurement/save";
    private static final String PATH_GET_SETTINGS = "settings/v1/measurement";

    private MeasurementType measurementType = MeasurementType.SINGLE;
    private MeasurementVariant measurementVariant = MeasurementVariant.DOWNLINK;
    private int credit;
    private int cycle;
    private int unitPos; // 0 == sec, 1 == min, 2 == hours
    private float usedData;

    private MeasurementResultsViewModel resultsVM;
    private TelephonyInfoViewModel teleVM;
    private DisplayViewModel displayVM;

    private LinearLayout layoutBottomSheet;
    private Button detailsButton;
    private Button detailsButton2;
    private Button goBtn;
    private Button connectionTypeButton;
    private RadioButton radio_downlink;
    private RadioButton radio_uplink;
    private RadioButton radio_both;
    private ProgressBar pb_measurement;
    private ProgressBar pb_dataVolume;
    private LinearLayout ll_measurment1;
    private LinearLayout ll_measurment2;
    private TextView tv_bandwidth;
    private TextView tv_vol;
    private TextView tv_time;
    private TextView tv_bandwidth_uplink;
    private TextView tv_vol_uplink;
    private TextView tv_time_uplink;
    private TextView tv_totalDataVolume;
    private TextView tv_cont_result;
    private TextView tv_cont_next_measurement;
    private Switch cont_measurement_switch;

    private PermissionManager permissionManager;

    private Location location = null;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Observer<List<MeasurementResult>> measurementChartObserver;
    private Observer<List<MeasurementResult>> measurementDeleteObserver;
    //private Observer<MeasurementResult> mrUplink;
    //private Observer<MeasurementResult> mrDownlink;
    private SharedPreferences sharedPreferences;

    private BroadcastReceiver broadcastReceiver;

    public MeasurementFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SavedStateViewModelFactory vmFactory = new SavedStateViewModelFactory(this.requireActivity().getApplication(), this);
        permissionManager = new PermissionManager();

        // get Viewmodels for results and telephony-info
        resultsVM = new ViewModelProvider(this, vmFactory).get(MeasurementResultsViewModel.class);
        teleVM = new ViewModelProvider(this, vmFactory).get(TelephonyInfoViewModel.class);
        displayVM = new ViewModelProvider(this, vmFactory).get(DisplayViewModel.class);

        if(savedInstanceState != null) {
            measurementType = MeasurementType.valueOf(savedInstanceState.getString(KEY_CONT_MEASUREMENT_ACTIVATED));
            measurementVariant = MeasurementVariant.valueOf(savedInstanceState.getString(KEY_MEASUREMENT_VARIANT));
        } else {
            measurementType = MeasurementType.SINGLE;
            measurementVariant = MeasurementVariant.DOWNLINK;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_measurement, container, false);
        loadViews(view);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        initLocationUpdates(view.getContext());
        initAutomaticMeasurementDeletion();

        initConnectionType(view);
        initViewsForCurrentResult();
        initButtonListeners();
        initRadioButtons();

        loadContinuousMeasurementSettingsFromSharedPreferences();
        initBottomSheet(view);
        updateContinuousMeasurementUI();

        return view;
    }

    private void loadContinuousMeasurementSettingsFromSharedPreferences() {
        cycle = sharedPreferences.getInt(KEY_CYCLE, DEF_CYCLE);
        credit = sharedPreferences.getInt(KEY_CREDIT, DEF_CREDIT);
        unitPos = sharedPreferences.getInt(KEY_UNIT_POS, DEF_UNIT_POS);
    }

    private void initDownloadDataLimit() {
        usedData = sharedPreferences.getFloat(KEY_USED_DATA, 0);
    }

    private void initConnectionType(View view) {
        // init connection type
        boolean isWifiConn = NetworkInfoService.isWifiConn(view.getContext().getApplicationContext());
        boolean isMobileConn = NetworkInfoService.isMobileConn(view.getContext().getApplicationContext());

        connectionTypeButton.setText(isWifiConn ? "WiFi" : isMobileConn ? "Mobile" : "No connection");
    }

    private void initViewsForCurrentResult() {
        pb_measurement.setVisibility(View.GONE);

        updateDownlinkStrings();
        updateUplinkStrings();

        if(displayVM.getDownlinkResultId() == null) {
            ll_measurment1.setVisibility(View.GONE);
        } else {
            ll_measurment1.setVisibility(View.VISIBLE);
        }

        if(displayVM.getUplinkResultId() == null) {
            ll_measurment2.setVisibility(View.GONE);
        } else {
            ll_measurment2.setVisibility(View.VISIBLE);
        }

    }

    private void initButtonListeners() {
        goBtn.setOnClickListener(this::onBtnGo);
        detailsButton.setOnClickListener(this::onBtnDetailsDownlink);
        detailsButton2.setOnClickListener(this::onBtnDetailsUplink);
    }

    private void initRadioButtons() {
        switch(measurementVariant) {
            case UPLINK:
                radio_uplink.setChecked(true);
                break;
            case BOTH_DOWNLINK:
            case BOTH_UPLINK:
                radio_both.setChecked(true);
                break;
            default: // also on Downlink
                radio_downlink.setChecked(true);
                break;
        }

        radio_downlink.setOnClickListener(this::onRadioBtn);
        radio_uplink.setOnClickListener(this::onRadioBtn);
        radio_both.setOnClickListener(this::onRadioBtn);
    }

    private void loadViews(View view) {
        pb_measurement = view.findViewById(R.id.progressBar);

        ll_measurment1 = view.findViewById(R.id.measurement_card1);
        ll_measurment2 = view.findViewById(R.id.measurement_card2);
        tv_bandwidth = view.findViewById(R.id.tv_bandwidth);
        tv_vol = view.findViewById(R.id.tv_vol);
        tv_time = view.findViewById(R.id.tv_time);
        tv_bandwidth_uplink = view.findViewById(R.id.tv_bandwidth2);
        tv_vol_uplink = view.findViewById(R.id.tv_vol2);
        tv_time_uplink = view.findViewById(R.id.tv_time2);

        goBtn = view.findViewById(R.id.btn_go);
        detailsButton = view.findViewById(R.id.btn_details_measurement);
        detailsButton2 = view.findViewById(R.id.btn_details_measurement2);
        connectionTypeButton = view.findViewById(R.id.btn_connection_type);

        //Bottom Sheet
        layoutBottomSheet = view.findViewById(R.id.continuous_measurement_bottomsheet);
        pb_dataVolume = view.findViewById(R.id.pb_data_volume);
        tv_totalDataVolume = view.findViewById(R.id.tv_totalDataVolume);
        cont_measurement_switch = view.findViewById(R.id.switch_continuous_measurement);
        tv_cont_result = view.findViewById(R.id.tv_result_last_measurement);
        tv_cont_next_measurement = view.findViewById(R.id.tv_next_measurement);
        radio_downlink = view.findViewById(R.id.radio_downlink);
        radio_uplink = view.findViewById(R.id.radio_uplink);
        radio_both = view.findViewById(R.id.radio_both);
    }

    private void initAutomaticMeasurementDeletion() {
        this.measurementDeleteObserver = measurementResults -> {
            if(measurementResults.size() > MeasurementResultsViewModel.MAX_RESULTS_IN_DB) {
                resultsVM.delete(measurementResults.get(measurementResults.size()-1));
            }
        };
        resultsVM.getAllResults().observe(getViewLifecycleOwner(), measurementDeleteObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        resultsVM.getAllResults().removeObserver(this.measurementChartObserver);
        resultsVM.getAllResults().removeObserver(this.measurementDeleteObserver);
        //displayVM.getDownlinkResult().removeObserver();
        //displayVM.getUplinkResult().removeObserver();
    }

    @Override
    public void onResume() {
        super.onResume();

        registerBroadcastReceiver();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        startLocationUpdates();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MeasurementService.ACTION_MEASUREMENT);
        intentFilter.addAction(PersistMeasurementService.ACTION_PERSIST_MEASUREMENT);
        intentFilter.addAction(GetSettingsService.ACTION_GET_SETTINGS);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onBroadCastReceived(context, intent);
            }
        };


        LocalBroadcastManager.getInstance(this.requireContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this.requireContext()).unregisterReceiver(broadcastReceiver);

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void initBottomSheet(View view) {
        initDownloadDataLimit();
        initContMeasurementChart(view);

        cont_measurement_switch.setChecked(measurementType != MeasurementType.SINGLE);
        cont_measurement_switch.setOnCheckedChangeListener(this::onContinuousMeasurementChanged);
        credit = sharedPreferences.getInt(KEY_CREDIT, DEF_CREDIT);

        //Progress Bar initialization
        pb_dataVolume.setMax(credit);
        pb_dataVolume.setMin(0);

        initBottomSheetBehaviour();
    }

    private void initBottomSheetBehaviour() {
        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) { //do nothing
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    updateContinuousMeasurementUI();
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) { //do nothing
            }
        });

        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_MAX_DATA, credit);
        outState.putString(KEY_CONT_MEASUREMENT_ACTIVATED, measurementType.name());
        outState.putString(KEY_MEASUREMENT_VARIANT, measurementVariant.name());
    }

    private void cancelContinuousMeasurement() {
        if(measurementType == MeasurementType.CONTINUOUS) {
            Log.d(TAG, "Cancel continuous measurement...");
            ContinuousMeasurementService.stopService(this.getContext());
        }

        measurementType = MeasurementType.SINGLE;

        String nextMeasurementText = getString(R.string.next_measurement) + "-";
        tv_cont_next_measurement.setText(nextMeasurementText);

        if(cont_measurement_switch.isChecked()) { //set switch to false
            cont_measurement_switch.setChecked(false);
        }
    }

    private void onContinuousMeasurementChanged(View view, boolean isChecked) {
        if(permissionManager.isAnyPermissionsNotGranted(this.requireActivity())){
            cont_measurement_switch.setChecked(false);
            permissionManager.requestMissingPermission(this.requireActivity());
            return; //return without execution since not all permissions are granted
        }

        //activated; avoid triggering if just the UI-Element is checked/unchecked
        if (isChecked && measurementType == MeasurementType.SINGLE) {
            Log.d(TAG, "start foreground service");

            CruspSetting cruspSettings = getCruspSettingsFromSharedPreferences(view.getContext());
            measurementType = MeasurementType.CONTINUOUS;

            String url = "http://" + HOST + ":" + PORT + "/" + PATH_GET_SETTINGS;
            GetSettingsService.start(this.getContext(), url, cruspSettings, measurementVariant, measurementType);
        } else if(!isChecked && measurementType != MeasurementType.SINGLE) { // deactivated
            cancelContinuousMeasurement();
        }
    }

    private int getCycleTimeInSec(int cycle, int unitPos) {
        return cycle * (unitPos == 0 ? 1 : (unitPos == 1 ? 60 : 60*60));
    }

    private void initLocationUpdates(Context context) {
        if(fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }

        MeasurementFragment mFragment = this;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                List<Location> locations = locationResult.getLocations(); // list of locations ordered from oldest to newest

                if(!locations.isEmpty()) { // locations.size() is 1 most of the time
                    Location currentLocation = locations.get(locations.size() - 1);

                    if(currentLocation.getSpeed() == 0.0f) {  // Speed not available, calculate it manually
                        SpeedAsyncTask task = new SpeedAsyncTask(mFragment, locations);
                        task.execute();
                    } else {
                        mFragment.updateLocation(currentLocation);
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
    }

    @Override
    public void updateLocation(Location location) {
        this.location = location;
    }

    /**
     * This method should get executed after the measurement is persisted in the local DB.
     *
     * It persists the telephonyInfo in the local DB
     * and starts the PersistMeasurementService to persist the measurement in the database-service
     * @param mResult is the Measurement-Result incl. uid of the local persisted measurement
     * @param telephonyInfo is the locally unpersisted telephony info for the measurement
     * @param cruspToken is the cruspToken used for the measurement
     */
    private void onMeasurementPersistedLocal(MeasurementResult mResult, ITelephonyInfo telephonyInfo, CruspToken cruspToken, MeasurementVariant mVariant) {
        if(mVariant == MeasurementVariant.BOTH_DOWNLINK || mVariant == MeasurementVariant.DOWNLINK) {
            displayVM.setDownlinkResultId(mResult.getUid());
        } else {
            displayVM.setUplinkResultId(mResult.getUid());
        }

        // Persist TelephonyInfo local
        if (telephonyInfo != null) {
            telephonyInfo.setMeasurementId(mResult.getUid());

            teleVM.insert(telephonyInfo);
        }

        // Start the Persist Service which persist the result in database-service
        String url = "http://" + HOST + ":" + PORT + "/" + PATH_SAVE_MEASUREMENT;
        PersistMeasurementService.startService(getContext(), url, mResult, telephonyInfo, cruspToken);
    }

    /**
     * Updates the UI after a single measurement
     * @param result of a single measurement
     */
    private void updateUIWithDownlinkResult(MeasurementResult result, MeasurementVariant variant) {
        if(result.getErrorType() == CruspError.NO_ERROR) {
            displayVM.setDataRateStringDownlink(((float) Math.round(result.getAvailableBandwidth() * 100)) / 100 + " Mbps");
            long kb = Math.round(result.sumUpUsedDataInMB()*1000);
            displayVM.setVolumeStringDownlink("with " + kb + " kB");
            long ms = Math.round(((double)result.getTimeSpanInNanos())/1000000);
            displayVM.setTimeStringDownlink("in " + ms + " ms");
        } else {
            displayVM.setDataRateStringDownlink(result.getErrorType().toString().replace('_', '\n'));
            displayVM.setVolumeStringDownlink("");
            displayVM.setTimeStringDownlink("");
        }

        updateDownlinkStrings();
        updateUsedData(result);

        if(variant.equals(MeasurementVariant.BOTH_DOWNLINK) || variant.equals(MeasurementVariant.BOTH_UPLINK)) {
            ll_measurment2.setVisibility(View.VISIBLE);
        } else {
            ll_measurment2.setVisibility(View.GONE);
            setUplinkStringToEmpty();
            updateUplinkStrings();
        }

        goBtn.setEnabled(true);
        goBtn.setVisibility(View.VISIBLE);
        ll_measurment1.setVisibility(View.VISIBLE);
        ll_measurment2.setVisibility(View.GONE);
        pb_measurement.setVisibility(View.GONE);
    }

    private void updateUIWithUplinkResult(MeasurementResult result, MeasurementVariant variant) {
        if(result.getErrorType() == CruspError.NO_ERROR) {
            displayVM.setDataRateStringUplink(((float) Math.round(result.getAvailableBandwidth() * 100)) / 100 + " Mbps");
            long kb = Math.round(result.sumUpUsedDataInMB()*1000);
            displayVM.setVolumeStringUplink("with " + kb + " kB");
            long ms = Math.round(((double)result.getTimeSpanInNanos())/1000000);
            displayVM.setTimeStringUplink("in " + ms + " ms");
        } else {
            displayVM.setDataRateStringUplink(result.getErrorType().toString().replace('_', '\n'));
            displayVM.setVolumeStringUplink("");
            displayVM.setTimeStringUplink("");
        }

        updateUplinkStrings();
        updateUsedData(result);

        if(variant.equals(MeasurementVariant.BOTH_DOWNLINK) || variant.equals(MeasurementVariant.BOTH_UPLINK)) {
            ll_measurment1.setVisibility(View.VISIBLE);
        } else {
            ll_measurment1.setVisibility(View.GONE);
            setDownlinkStringToEmpty();
            updateDownlinkStrings();
        }

        goBtn.setEnabled(true);
        goBtn.setVisibility(View.VISIBLE);
        ll_measurment2.setVisibility(View.VISIBLE);
        pb_measurement.setVisibility(View.GONE);
    }

    private void setDownlinkStringToEmpty() {
        displayVM.setDataRateStringDownlink("");
        displayVM.setVolumeStringDownlink("");
        displayVM.setTimeStringDownlink("");
    }

    private void updateDownlinkStrings() {
        tv_bandwidth.setText(displayVM.getDataRateStringDownlink());
        tv_vol.setText(displayVM.getVolumeStringDownlink());
        tv_time.setText(displayVM.getTimeStringDownlink());
    }

    /**
     * empties the uplink texfields in UI and sets the strings connected with uplink to 0;
     */
    private void setUplinkStringToEmpty() {
        displayVM.setDataRateStringUplink("");
        displayVM.setVolumeStringUplink("");
        displayVM.setTimeStringUplink("");
    }

    private void updateUplinkStrings() {
        tv_bandwidth_uplink.setText(displayVM.getDataRateStringUplink());
        tv_vol_uplink.setText(displayVM.getVolumeStringUplink());
        tv_time_uplink.setText(displayVM.getTimeStringUplink());
    }

    private void updateUIOnFailedTokenRequest(int status) {
        tv_bandwidth.setText("");
        ll_measurment1.setVisibility(View.VISIBLE);
        ll_measurment2.setVisibility(View.GONE);
        goBtn.setEnabled(true);
        goBtn.setVisibility(View.VISIBLE);
        pb_measurement.setVisibility(View.GONE);

        if(status == 400) {
            requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Validation of settings failed", Toast.LENGTH_SHORT).show());
        } else if (status == 404) {
            requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "No default settings are available", Toast.LENGTH_SHORT).show());
        } else {
            requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error when getting settings", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateUsedData(MeasurementResult newResult) {
        new Thread(() -> {
            long usedDataDate = sharedPreferences.getLong(KEY_USED_DATA_DATE, 0);

            LocalDateTime firstOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            firstOfMonth.toEpochSecond(ZoneOffset.of("+01:00"));

            LocalDateTime lastDate = LocalDateTime.ofEpochSecond(usedDataDate, 0, ZoneOffset.of("+01:00"));

            if(firstOfMonth.isAfter(lastDate)) {
                usedData = 0;
            }

            double usedDataNewResult = newResult.sumUpUsedDataInMB();
            usedData += usedDataNewResult;

            writeUsedDataDateIntoSharedPreferences();

            requireActivity().runOnUiThread(() -> {
                String text = Math.round(usedData) + "/" + credit + getString(R.string.space_MB) + getString(R.string.this_month);
                tv_totalDataVolume.setText(text);
                pb_dataVolume.setProgress(Math.round(usedData));
            });
        }).start();
    }

    private void writeUsedDataDateIntoSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        long usedDataDate;
        usedDataDate = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+01:00"));

        editor.putLong(KEY_USED_DATA_DATE, usedDataDate);
        editor.putFloat(KEY_USED_DATA, usedData);
        editor.apply();
    }

    /**
     * Updates the Bottom Sheet in the UI after a continuous measurement
     * @param result a continuous measurement
     */
    private void updateBottomSheetWithResult(MeasurementResult result) {
        String resultString;

        if(result.getErrorType() == CruspError.NO_ERROR) {
            resultString = "Last measurement: " + ((float) Math.round(result.getAvailableBandwidth() * 1000)) / 1000 + " Mbps";
        } else {
            resultString = "Last measurement: " + result.getErrorType().toString().replace('_', '\n');
        }

        String nextMeasurementText = getString(R.string.next_measurement);

        if(measurementType == MeasurementType.CONTINUOUS) {
            LocalTime nextMeasurementTime = LocalTime.now().plusSeconds(getCycleTimeInSec(cycle, unitPos));
            nextMeasurementText += nextMeasurementTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
        } else {
            nextMeasurementText += "-";
            resultString = "Last measurement: -"; //overwride resultstring if continuous measurement is not active anymore
        }

        tv_cont_result.setText(resultString);
        tv_cont_next_measurement.setText(nextMeasurementText);
    }

    private void onBtnDetailsDownlink(View view) {
        Intent intent = new Intent(view.getContext(), ResultDetailsActivity.class);
        intent.putExtra(KEY_MEASUREMENT_RESULT_ID, displayVM.getDownlinkResultId());
        view.getContext().startActivity(intent);
    }

    private void onBtnDetailsUplink(View view) {
        Intent intent = new Intent(view.getContext(), ResultDetailsActivity.class);
        intent.putExtra(KEY_MEASUREMENT_RESULT_ID, displayVM.getUplinkResultId());
        view.getContext().startActivity(intent);
    }

    /**
     * Starts measurement process
     * Executed when Go-Button is pressed
     * After getting the settings, {@link #onBroadCastReceived(Context, Intent)} is executed
     * @param view element
     */
    private void onBtnGo(View view) {
        if(permissionManager.isAnyPermissionsNotGranted(this.requireActivity())){
            permissionManager.requestMissingPermission(this.requireActivity());
            return; //return without execution since not all permissions are granted
        }

        // Make UI-elements visible/invisible
        goBtn.setVisibility(View.GONE);
        pb_measurement.setVisibility(View.VISIBLE);
        ll_measurment1.setVisibility(View.GONE);
        ll_measurment2.setVisibility(View.GONE);

        CruspSetting settings = getCruspSettingsFromSharedPreferences(view.getContext());

        String settingsUrl = "http://" + HOST + ":" + PORT + "/" + PATH_GET_SETTINGS;
        GetSettingsService.start(getContext(), settingsUrl, settings, measurementVariant, measurementType);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(IS_MOBILE_CONN) || key.equals(IS_WIFI_CONN)) {
            connectionTypeButton.setText(sharedPreferences.getBoolean(IS_MOBILE_CONN, false) ? "Mobile" : sharedPreferences.getBoolean(IS_WIFI_CONN, false) ? "WiFi" : "No connection");
        }

        if(key.equals(KEY_CREDIT)) {
            credit = sharedPreferences.getInt(KEY_CREDIT, DEF_CREDIT);
        }

        if(key.equals(KEY_CYCLE)) {
            cycle = sharedPreferences.getInt(KEY_CYCLE, DEF_CYCLE);
        }

        if(key.equals(KEY_UNIT_POS)) {
            unitPos = sharedPreferences.getInt(KEY_UNIT_POS, DEF_UNIT_POS);
        }
    }

    /**
     * Updates the Bottom Sheet UI
     */
    private void updateContinuousMeasurementUI() {
        pb_dataVolume.setMax(credit);
        String text = Math.round(usedData) + "/" + credit + getString(R.string.space_MB) + getString(R.string.this_month);
        tv_totalDataVolume.setText(text);
    }

    private void onBroadCastReceived(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), GetSettingsService.ACTION_GET_SETTINGS)) {
            onGetSettingsServiceFinished(intent);

        } else if(Objects.equals(intent.getAction(), MeasurementService.ACTION_MEASUREMENT)) {
            onMeasurementFinished(intent);

        } else if(Objects.equals(intent.getAction(), PersistMeasurementService.ACTION_PERSIST_MEASUREMENT)) {
            onMeasurementPersistedInServer(intent);
        }
    }

    /**
     * Receives the token from the settings-service after executing {@link #onBtnGo(View)}
     * and starts the MeasurementService or ContinuousMeasurementService
     * Automatically called when GetSettingsService is finished
     */
    private void onGetSettingsServiceFinished(Intent intent) {
        Log.d(TAG, "Message received from GetSettingsService");
        CruspToken token = (CruspToken) intent.getSerializableExtra(KEY_CRUSP_TOKEN);
        int status = intent.getIntExtra(KEY_RESPONSE_CODE, 0);
        MeasurementType mType = (MeasurementType) intent.getSerializableExtra(KEY_MEASUREMENT_TYPE);
        MeasurementVariant mVariant = (MeasurementVariant) intent.getSerializableExtra(KEY_MEASUREMENT_VARIANT);

        if(status != 200) {
            Log.w(TAG, "GetSettingsService returned status " + status);
            this.updateUIOnFailedTokenRequest(status);
            cancelContinuousMeasurement();
        } else if(mType == MeasurementType.SINGLE) {
            MeasurementService.start(this.getContext(), mVariant, mType, this.location, token, HOST, PORT);
        } else if(mType == MeasurementType.CONTINUOUS) {
            int cycleTimeInSec = getCycleTimeInSec(cycle, unitPos);

            ContinuousMeasurementService.start(this.getContext(), mVariant, mType, this.location, token, HOST, PORT, cycleTimeInSec);
        }
    }

    private void onMeasurementFinished(Intent intent) {
        MeasurementResult mResult = (MeasurementResult) intent.getSerializableExtra(KEY_MEASUREMENT_RESULT);
        ITelephonyInfo telephonyInfo = (ITelephonyInfo) intent.getSerializableExtra(KEY_TELEPHONY_INFO);
        CruspToken token = (CruspToken) intent.getSerializableExtra(KEY_CRUSP_TOKEN);
        MeasurementType mType = (MeasurementType) intent.getSerializableExtra(KEY_MEASUREMENT_TYPE);
        MeasurementVariant mVariant = (MeasurementVariant) intent.getSerializableExtra(KEY_MEASUREMENT_VARIANT);

        // 1. Persist locally, afterwards it is persisted in the database-service
        persistMeasurementDataInLocalDB(mResult, telephonyInfo, token, mVariant);

        if(mVariant == MeasurementVariant.BOTH_DOWNLINK) { // this means that the uplink part still has to be performed
            updateVMsDownlink(mResult, telephonyInfo);
            updateVMsUplink(null, null);

            updateUIWithDownlinkResult(mResult, mVariant);

            Location location = this.location;
            MeasurementService.start(this.getContext(), MeasurementVariant.BOTH_UPLINK, mType, location, token, HOST, PORT);

        } else if(mVariant == MeasurementVariant.BOTH_UPLINK) {
            updateVMsUplink(mResult, telephonyInfo);

            updateUIWithUplinkResult(mResult, mVariant);

        } else if(mVariant == MeasurementVariant.DOWNLINK) {
            updateVMsDownlink(mResult, telephonyInfo);
            updateVMsUplink(null, null);

            updateUIWithDownlinkResult(mResult, mVariant);
        } else if(mVariant == MeasurementVariant.UPLINK) {
            updateVMsDownlink(null, null);
            updateVMsUplink(mResult, telephonyInfo);

            updateUIWithUplinkResult(mResult, mVariant);
        }

        if(mType == MeasurementType.CONTINUOUS) {
            updateBottomSheetWithResult(mResult);
        }
    }

    private void onMeasurementPersistedInServer(Intent intent) {
        Log.d(TAG, "Result received from PersistMeasurementService");
        boolean successful = intent.getBooleanExtra(KEY_PERSISTED_SUCCESSFULL, false);
        long resultId = intent.getLongExtra(KEY_RESULT_ID, 0); // is the uid of the MeasurementResult in the local Database

        if(!successful) {
            requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Saving measurement on server failed", Toast.LENGTH_SHORT).show());
        } else {
            resultsVM.updateToPersisted(resultId);
        }
    }

    private void updateVMsDownlink(MeasurementResult measurementResult, ITelephonyInfo telephonyInfo) {
        displayVM.setDownlinkResultId(measurementResult != null ? measurementResult.getUid() : null);
    }

    private void updateVMsUplink(MeasurementResult measurementResult, ITelephonyInfo telephonyInfo) {
        displayVM.setUplinkResultId(measurementResult != null ? measurementResult.getUid() : null);
    }

    private void persistMeasurementDataInLocalDB(MeasurementResult mResult, ITelephonyInfo telephonyInfo, CruspToken token, MeasurementVariant mVariant) {
        resultsVM.insert(mResult, new MeasurementSavedInLocalDBCaller(mResult, telephonyInfo, token, mVariant, this::onMeasurementPersistedLocal));
    }

    private void onRadioBtn(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radio_downlink:
                if (checked)
                    measurementVariant = MeasurementVariant.DOWNLINK;
                    break;
            case R.id.radio_uplink:
                if (checked)
                    measurementVariant = MeasurementVariant.UPLINK;
                    break;
            case R.id.radio_both:
                if (checked)
                    measurementVariant = MeasurementVariant.BOTH_DOWNLINK;
                    break;
        }
    }

    private void initContMeasurementChart(View view) {
        MeasurementFragment fragment = this;
        this.measurementChartObserver = measurementResults -> {
            int color = view.getContext().getColor(R.color.Primary);

            UpdateContinuousMeasurementChartAsyncTask task = new UpdateContinuousMeasurementChartAsyncTask(fragment, measurementResults, measurementType, color);
            task.execute();
        };
        this.resultsVM.getAllResults().observe(getViewLifecycleOwner(), this.measurementChartObserver );
    }
}
