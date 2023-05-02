package at.ac.tuwien.nt.abe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.ac.tuwien.nt.abe.model.CruspError;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.SequenceDetails;
import at.ac.tuwien.nt.abe.model.network.ConnectionType;
import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoCdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoGSM;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoLTE;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWcdma;
import at.ac.tuwien.nt.abe.util.BurstCalculator;
import at.ac.tuwien.nt.abe.util.FrequencyBandMapper;
import at.ac.tuwien.nt.abe.viewmodels.MeasurementResultsViewModel;
import at.ac.tuwien.nt.abe.viewmodels.TelephonyInfoViewModel;

import static at.ac.tuwien.nt.abe.util.FormatHelper.formantInteger;
import static at.ac.tuwien.nt.abe.util.FormatHelper.formatDouble;
import static at.ac.tuwien.nt.abe.util.FormatHelper.formatFloat;
import static at.ac.tuwien.nt.abe.util.FormatHelper.formatString;
import static at.ac.tuwien.nt.abe.util.FormatHelper.getDurationOfSequence;
import static at.ac.tuwien.nt.abe.util.FormatHelper.roundTwoAfterDecimal;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_ADDITIONAL_INFO_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT_ID;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_RATE_NAIVE_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_RATE_SOPHISTICATED_STRING;

public class ResultDetailsActivity extends AppCompatActivity {
    private String naiveRateString = "";
    private String sophisticatedRateString = "";
    private String additionalInfoString = "";

    private CardView card_lte;
    private TextInputEditText ti_mcc;
    private TextInputEditText ti_mnc;
    private TextInputEditText ti_earfcn;
    private TextInputEditText ti_pci;
    private TextInputEditText ti_rssnr;
    private TextInputEditText ti_cqi;
    private TextInputEditText ti_rsrq;
    private TextInputEditText ti_rsrp;
    private TextInputEditText ti_tac;
    private TextInputEditText ti_dbm;
    private TextInputEditText ti_ta;
    private TextInputEditText ti_asu;
    private TextInputEditText ti_manufacturer;
    private TextInputEditText ti_model;
    private TextInputEditText ti_mobileNetworkOperator;
    private TextInputEditText ti_lat;
    private TextInputEditText ti_lng;
    private TextInputEditText ti_accuracy;
    private TextInputEditText ti_speed;
    private TextInputEditText ti_band;
    private TextInputEditText ti_type;
    private CheckBox cb_persisted;

    private CardView card_wcdma;
    private TextInputEditText ti_wcdma_uarfcn;
    private TextInputEditText ti_wcdma_psc;
    private TextInputEditText ti_wcdma_dbm;
    private TextInputEditText ti_wcdma_lac;
    private TextInputEditText ti_wcdma_mnc;
    private TextInputEditText ti_wcdma_mcc;
    private TextInputEditText ti_wcdma_cid;
    private TextInputEditText ti_wcdma_asu;

    private CardView card_cdma;
    private TextInputEditText ti_cmda_networkId;
    private TextInputEditText ti_cmda_systemId;
    private TextInputEditText ti_cmda_dbm;
    private TextInputEditText ti_cmda_asu;
    private TextInputEditText ti_cmda_rssi;

    private CardView card_gsm;
    private TextInputEditText ti_gsm_cid;
    private TextInputEditText ti_gsm_mcc;
    private TextInputEditText ti_gsm_mnc;
    private TextInputEditText ti_gsm_lac;
    private TextInputEditText ti_gsm_bsic;
    private TextInputEditText ti_gsm_dbm;
    private TextInputEditText ti_gsm_ta;
    private TextInputEditText ti_gsm_asu;
    private TextInputEditText ti_gsm_arfcn;

    private MeasurementResultsViewModel resultsVM;
    private TelephonyInfoViewModel teleVM;
    private MeasurementResult result;
    private ITelephonyInfo telephonyInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_details);
        initToolbar();

        SavedStateViewModelFactory vmFactory = new SavedStateViewModelFactory(this.getApplication(), this);

        // get Viewmodels for results and telephony-info
        resultsVM = new ViewModelProvider(this, vmFactory).get(MeasurementResultsViewModel.class);
        teleVM = new ViewModelProvider(this, vmFactory).get(TelephonyInfoViewModel.class);

        if(savedInstanceState != null) { // read out saved instance which can modify the intent-data
            initStateFromBundle(savedInstanceState);
        } else { // get data from Intent
            long resultId = (long) Objects.requireNonNull(getIntent().getExtras()).getSerializable(KEY_MEASUREMENT_RESULT_ID);
            result = resultsVM.getResultByIdComplete(resultId);
            telephonyInfo = teleVM.loadTelephonyInfo(resultId);

            StringBuilder naiveRateBuilder = new StringBuilder();
            StringBuilder timeVolBuilder = new StringBuilder();
            StringBuilder sophisticatedRateBuilder = new StringBuilder();

            if (result.getErrorType() != CruspError.NO_ERROR) {
                naiveRateBuilder.append(result.getErrorType().toString())
                        .append(": ")
                        .append(result.getErrorMessage());
            } else {
                BurstCalculator burstCalculator = new BurstCalculator();

                if (result.getSequenceCollection() != null) {
                    List<Float> naiveRates = new ArrayList<>();
                    List<Float> sophisticateRates = new ArrayList<>();
                    int sumBursts = 0;
                    float duration = 0;
                    int sumRecvPackets = 0;
                    int sumExpectedPackets = 0;

                    for (SequenceDetails sequenceDetails : result.getSequenceCollection()) {
                        List<BurstCalculator.Burst> bursts = burstCalculator.findBursts(sequenceDetails);

                        naiveRates.add((float)Math.round(sequenceDetails.getNaiveRate()*100)/100);
                        sophisticateRates.add((float)Math.round(burstCalculator.calculateSophisticatedRate(bursts)*100)/100);
                        sumBursts += bursts.size();
                        duration += (float)(Math.round(getDurationOfSequence(sequenceDetails)*100)/100);
                        sumRecvPackets += sequenceDetails.getPackets().size();
                        sumExpectedPackets += sequenceDetails.getExpectedPackets();
                    }

                    naiveRateBuilder.append("Naive: ");
                    for (Float naiveRate : naiveRates) {
                        naiveRateBuilder.append(naiveRate);
                        naiveRateBuilder.append(' ');
                    }
                    naiveRateBuilder.append("MBit/s");


                    sophisticatedRateBuilder.append("Sophisticated: ");
                    for (Float sophisticatedRate : sophisticateRates) {
                        sophisticatedRateBuilder.append(sophisticatedRate);
                        sophisticatedRateBuilder.append(' ');
                    }
                    sophisticatedRateBuilder.append(" MBit/s");

                    timeVolBuilder.append("with ")
                            .append(result.sumUpUsedDataInMB()*1000)
                            .append( " kB in ")
                            .append(duration)
                            .append(" ms")
                            .append("\n")
                            .append(" and ")
                            .append(sumRecvPackets)
                            .append("/")
                            .append(sumExpectedPackets)
                            .append(" packets received in ")
                            .append(sumBursts)
                            .append(" bursts");
                }
            }

            naiveRateString = naiveRateBuilder.toString();
            sophisticatedRateString = sophisticatedRateBuilder.toString();
            additionalInfoString = timeVolBuilder.toString();
        }

        TextView naiveRateTextView = findViewById(R.id.result_tv_naive_rate);
        TextView sophisticatedRateTextView = findViewById(R.id.result_tv_sophisticated_rate);
        TextView timeAndVolTextView = findViewById(R.id.result_tv_time_vol);

        naiveRateTextView.setText(naiveRateString);
        sophisticatedRateTextView.setText(sophisticatedRateString);
        timeAndVolTextView.setText(additionalInfoString);

        // init burst chart button
        Button burstChartButton = findViewById(R.id.result_btn_burst_rate);
        burstChartButton.setOnClickListener(this::onBurstChartClicked);

        // init packet delta button
        Button packetDeltaButton = findViewById(R.id.result_btn_packet_delta);
        packetDeltaButton.setOnClickListener(this::onPacketDeltaClicked);

        // TextInput for Telephony initialization
        if(telephonyInfo != null) {
            initTelephonyFields(telephonyInfo.getConnectionType());
        }

        cb_persisted = findViewById(R.id.cb_persisted);
        this.cb_persisted.setChecked(result.isPersisted());

        if(result.getErrorType() == CruspError.NO_ERROR) {
            burstChartButton.setVisibility(View.VISIBLE);
            packetDeltaButton.setVisibility(View.VISIBLE);

            setTelephonyInfo(telephonyInfo);
        } else {
            burstChartButton.setVisibility(View.GONE);
            packetDeltaButton.setVisibility(View.GONE);

            card_lte.setVisibility(View.GONE);
            card_wcdma.setVisibility(View.GONE);
            card_cdma.setVisibility(View.GONE);
            card_gsm.setVisibility(View.GONE);
        }
    }

    private void initStateFromBundle(Bundle savedInstanceState) {
        long resultId =  savedInstanceState.getLong(KEY_MEASUREMENT_RESULT_ID);

        result = resultsVM.getResultByIdComplete(resultId);
        telephonyInfo = teleVM.loadTelephonyInfo(resultId);

        this.naiveRateString = savedInstanceState.getString(KEY_RATE_NAIVE_STRING);
        this.sophisticatedRateString = savedInstanceState.getString(KEY_RATE_SOPHISTICATED_STRING);
        this.additionalInfoString = savedInstanceState.getString(KEY_ADDITIONAL_INFO_STRING);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.result_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_MEASUREMENT_RESULT_ID, result.getUid());
        outState.putString(KEY_RATE_NAIVE_STRING, naiveRateString);
        outState.putString(KEY_RATE_SOPHISTICATED_STRING, sophisticatedRateString);
        outState.putString(KEY_ADDITIONAL_INFO_STRING, additionalInfoString);
    }

    public void onBurstChartClicked(View view) {
        Intent intent = new Intent(view.getContext(), BurstRateActivity.class);
        intent.putExtra(KEY_MEASUREMENT_RESULT, result);
        startActivity(intent);
    }

    private void onPacketDeltaClicked(View view) {
        Intent intent = new Intent(view.getContext(), PacketDeltaActivity.class);
        intent.putExtra(KEY_MEASUREMENT_RESULT, result);
        startActivity(intent);
    }

    private void setTelephonyInfo(ITelephonyInfo telephonyInfo) {
        ti_lat.setText(formatDouble(telephonyInfo.getLat()));
        ti_lng.setText(formatDouble(telephonyInfo.getLng()));
        ti_type.setText(telephonyInfo.getConnectionType().name().toUpperCase());
        ti_speed.setText(formatFloat((roundTwoAfterDecimal(telephonyInfo.getSpeed()))));
        ti_manufacturer.setText(formatString(telephonyInfo.getManufacturer()));
        ti_model.setText(formatString(telephonyInfo.getModel()));
        ti_accuracy.setText(formatFloat(roundTwoAfterDecimal(telephonyInfo.getGpsAccuracy())));
        ti_mobileNetworkOperator.setText(formatString(telephonyInfo.getOperator()));

        if (telephonyInfo instanceof TelephonyInfoLTE) {
            ti_mcc.setText(formatString(((TelephonyInfoLTE) telephonyInfo).getMcc()));
            ti_mnc.setText(formatString(((TelephonyInfoLTE) telephonyInfo).getMnc()));
            ti_earfcn.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getEarfcn()));
            ti_pci.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getPci()));
            ti_rssnr.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getRssnr()));
            ti_cqi.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getCqi()));
            ti_rsrq.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getRsrq()));
            ti_rsrp.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getRsrp()));
            ti_tac.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getTac()));
            ti_dbm.setText(formantInteger(telephonyInfo.getDbm()));
            ti_ta.setText(formantInteger(((TelephonyInfoLTE) telephonyInfo).getTa()));
            ti_asu.setText(formantInteger(telephonyInfo.getAsu()));
            ti_band.setText(FrequencyBandMapper.getBandFromEarfcn(((TelephonyInfoLTE) telephonyInfo).getEarfcn()));

            card_lte.setVisibility(View.VISIBLE);
            card_wcdma.setVisibility(View.GONE);
            card_cdma.setVisibility(View.GONE);
            card_gsm.setVisibility(View.GONE);
        } else if (telephonyInfo instanceof TelephonyInfoWcdma) {
            ti_wcdma_dbm.setText(formantInteger(telephonyInfo.getDbm()));
            ti_wcdma_asu.setText(formantInteger(telephonyInfo.getAsu()));
            ti_wcdma_cid.setText(formantInteger(((TelephonyInfoWcdma) telephonyInfo).getCid()));
            ti_wcdma_mcc.setText(formatString(((TelephonyInfoWcdma) telephonyInfo).getMcc()));
            ti_wcdma_mnc.setText(formatString(((TelephonyInfoWcdma) telephonyInfo).getMnc()));
            ti_wcdma_lac.setText(formantInteger(((TelephonyInfoWcdma) telephonyInfo).getLac()));
            ti_wcdma_psc.setText(formantInteger(((TelephonyInfoWcdma) telephonyInfo).getPsc()));
            ti_wcdma_uarfcn.setText(formantInteger(((TelephonyInfoWcdma) telephonyInfo).getUarfcn()));
            ti_band.setText(FrequencyBandMapper.getBandFromUarfcn(((TelephonyInfoWcdma) telephonyInfo).getUarfcn()));

            card_lte.setVisibility(View.GONE);
            card_wcdma.setVisibility(View.VISIBLE);
            card_cdma.setVisibility(View.GONE);
            card_gsm.setVisibility(View.GONE);
        } else if (telephonyInfo instanceof TelephonyInfoCdma) {
            ti_cmda_networkId.setText(formantInteger(((TelephonyInfoCdma) telephonyInfo).getNetworkId()));
            ti_cmda_systemId.setText(formantInteger(((TelephonyInfoCdma) telephonyInfo).getSystemId()));
            ti_cmda_dbm.setText(formantInteger(telephonyInfo.getDbm()));
            ti_cmda_asu.setText(formantInteger(telephonyInfo.getAsu()));
            ti_cmda_rssi.setText(formantInteger(((TelephonyInfoCdma) telephonyInfo).getRssi()));
            ti_band.setText("-");

            card_lte.setVisibility(View.GONE);
            card_wcdma.setVisibility(View.GONE);
            card_cdma.setVisibility(View.VISIBLE);
            card_gsm.setVisibility(View.GONE);
        } else if (telephonyInfo instanceof TelephonyInfoGSM) {
            ti_gsm_cid.setText(formantInteger(((TelephonyInfoGSM) telephonyInfo).getCid()));
            ti_gsm_mcc.setText(formatString(((TelephonyInfoGSM) telephonyInfo).getMcc()));
            ti_gsm_mnc.setText(formatString(((TelephonyInfoGSM) telephonyInfo).getMnc()));
            ti_gsm_lac.setText(formantInteger(((TelephonyInfoGSM) telephonyInfo).getLac()));
            ti_gsm_bsic.setText(formantInteger(((TelephonyInfoGSM) telephonyInfo).getBsic()));
            ti_gsm_dbm.setText(formantInteger(telephonyInfo.getDbm()));
            ti_gsm_ta.setText(formantInteger(((TelephonyInfoGSM) telephonyInfo).getTa()));
            ti_gsm_asu.setText(formantInteger(telephonyInfo.getAsu()));
            ti_gsm_arfcn.setText(formantInteger(((TelephonyInfoGSM) telephonyInfo).getArfcn()));
            ti_band.setText(FrequencyBandMapper.getBandFromArfcn(((TelephonyInfoGSM) telephonyInfo).getArfcn()));

            card_lte.setVisibility(View.GONE);
            card_wcdma.setVisibility(View.GONE);
            card_cdma.setVisibility(View.GONE);
            card_gsm.setVisibility(View.VISIBLE);
        } else { // TODO: Add WIFI
            card_lte.setVisibility(View.GONE);
            card_wcdma.setVisibility(View.GONE);
            card_cdma.setVisibility(View.GONE);
            card_gsm.setVisibility(View.GONE);
        }
    }

    private void initTelephonyFields(ConnectionType connectionType) {
        card_lte = findViewById(R.id.layout_include_lte);
        card_wcdma = findViewById(R.id.layout_include_wcdma);
        card_cdma = findViewById(R.id.layout_include_cdma);
        card_gsm = findViewById(R.id.layout_include_gsm);

        ti_lat = findViewById(R.id.ti_lat);
        ti_lng = findViewById(R.id.ti_lng);
        ti_speed = findViewById(R.id.ti_speed);
        ti_type = findViewById(R.id.ti_type);
        ti_accuracy = findViewById(R.id.ti_accuracy);
        ti_manufacturer = findViewById(R.id.ti_manufacturer);
        ti_model = findViewById(R.id.ti_model);
        ti_mobileNetworkOperator = findViewById(R.id.ti_mobileNetworkOperator);
        ti_band = findViewById(R.id.ti_band);

        if(connectionType == ConnectionType.LTE) {
            ti_mcc = findViewById(R.id.ti_mcc);
            ti_mnc = findViewById(R.id.ti_mnc);
            ti_earfcn = findViewById(R.id.ti_earfcn);
            ti_pci = findViewById(R.id.ti_pci);
            ti_rssnr = findViewById(R.id.ti_rssnr);
            ti_cqi = findViewById(R.id.ti_cqi);
            ti_rsrq = findViewById(R.id.ti_rsrq);
            ti_rsrp = findViewById(R.id.ti_rsrp);
            ti_tac = findViewById(R.id.ti_tac);
            ti_dbm = findViewById(R.id.ti_dbm);
            ti_ta = findViewById(R.id.ti_ta);
            ti_asu = findViewById(R.id.ti_asu);

        } else if(connectionType == ConnectionType.WCDMA) {
            ti_wcdma_dbm = findViewById(R.id.ti_wcdma_dbm);
            ti_wcdma_asu = findViewById(R.id.ti_wcdma_asu);
            ti_wcdma_cid = findViewById(R.id.ti_wcdma_cid);
            ti_wcdma_mcc = findViewById(R.id.ti_wcdma_mcc);
            ti_wcdma_mnc = findViewById(R.id.ti_wcdma_mnc);
            ti_wcdma_lac = findViewById(R.id.ti_wcdma_lac);
            ti_wcdma_psc = findViewById(R.id.ti_wcdma_psc);
            ti_wcdma_uarfcn = findViewById(R.id.ti_wcdma_uarfcn);
        } else if (connectionType == ConnectionType.CDMA) {
            ti_cmda_networkId = findViewById(R.id.ti_cdma_networkId);
            ti_cmda_systemId = findViewById(R.id.ti_cdma_systemId);
            ti_cmda_dbm = findViewById(R.id.ti_cdma_dbm);
            ti_cmda_asu = findViewById(R.id.ti_cdma_asu);
            ti_cmda_rssi = findViewById(R.id.ti_cdma_rssi);
        } else if (connectionType == ConnectionType.GSM) {
            ti_gsm_cid = findViewById(R.id.ti_gsm_cid);
            ti_gsm_mcc = findViewById(R.id.ti_gsm_mcc);
            ti_gsm_mnc = findViewById(R.id.ti_gsm_mnc);
            ti_gsm_lac = findViewById(R.id.ti_gsm_lac);
            ti_gsm_bsic = findViewById(R.id.ti_gsm_bsic);
            ti_gsm_dbm = findViewById(R.id.ti_gsm_dbm);
            ti_gsm_ta = findViewById(R.id.ti_gsm_ta);
            ti_gsm_asu = findViewById(R.id.ti_gsm_asu);
            ti_gsm_arfcn = findViewById(R.id.ti_gsm_arfcn);
        }
    }

}
