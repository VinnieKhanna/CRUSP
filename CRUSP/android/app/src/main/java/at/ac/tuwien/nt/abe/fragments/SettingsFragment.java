package at.ac.tuwien.nt.abe.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import at.ac.tuwien.nt.abe.R;
import at.ac.tuwien.nt.abe.fragments.interfaces.TextChangedListener;
import at.ac.tuwien.nt.abe.model.CruspSetting;
import at.ac.tuwien.nt.abe.viewmodels.SettingsViewModel;

import static java.util.Objects.requireNonNull;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener, TextChangedListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    static final int DEF_CYCLE = 60;
    static final int DEF_CREDIT = 1000;
    static final int DEF_UNIT_POS = 1;
    private static final int DEF_CRUSP_POS = 4;
    static final String KEY_CYCLE = "KEY_CYCLE";
    static final String KEY_CREDIT = "KEY_CREDIT";
    static final String KEY_UNIT_POS = "KEY_UNIT_POS";
    private static final String KEY_CRUSP_POS = "KEY_CRUSP_POS";

    private static final String KEY_REPEATS = "KEY_REPEATS";
    private static final String KEY_TIMEOUT = "KEY_TIMEOUT";
    private static final String KEY_VOLUME = "KEY_VOLUME";
    private static final String KEY_PACKET_SIZE = "KEY_PACKET_SIZE";
    private static final String KEY_RATE = "KEY_RATE";
    private static final String KEY_SLEEP = "KEY_SLEEP";
    private static final String KEY_STANDARD = "KEY_STANDARD";

    private int cruspSpinnerPosition = DEF_CRUSP_POS;
    private int cycle;
    private int credit;
    private int unitSpinnerPosition = DEF_UNIT_POS; // 0 == sec, 1 == min, 2 == hours
    private boolean isDefaultSettingsActivated;

    // View components
    private Spinner cruspSpinner;
    private Spinner unitSpinner;
    private TextInputEditText repeat;
    private TextInputEditText volume;
    private TextInputEditText packetSize;
    private TextInputEditText rate;
    private TextInputEditText sleep;
    private TextInputEditText timeout;
    private Button applyButton;
    private TextInputEditText inputCycle;
    private TextInputEditText inputCredit;
    private Switch switchDefaultSettings;
    private LinearLayout ll_settings;

    private SettingsViewModel mViewModel;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SavedStateViewModelFactory vmFactory = new SavedStateViewModelFactory(this.requireActivity().getApplication(), this);
        mViewModel =  new ViewModelProvider(this, vmFactory).get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        loadViews(view);

        CruspSetting cruspSetting = getCruspSettingsFromSharedPreferences(view.getContext());
        setTextFieldsWithCruspSettings(cruspSetting);

        loadRemainingSettingsFromSharedPreferences(view);
        initRemainingSettings(view);
        addTextChangedListenersToViews();

        clearFocus();
        deactivateApplyButton();

        showOrHideDefaultSettingsView(isDefaultSettingsActivated);

        addObserverToSettingsViewModel(view, cruspSetting.getPacketSize() != -1);

        return view;
    }

    private void showOrHideDefaultSettingsView(boolean visible) {
        if(visible) {
            ll_settings.setVisibility(View.GONE);
        } else {
            ll_settings.setVisibility(View.VISIBLE);
        }
    }

    private void initRemainingSettings(View view) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.unit_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
        unitSpinner.setSelection(unitSpinnerPosition);
        unitSpinner.setOnItemSelectedListener(this);

        inputCycle.setText(String.valueOf(cycle));
        inputCredit.setText(String.valueOf(credit));

        applyButton.setOnClickListener(this::onBtnApply);
        switchDefaultSettings.setChecked(isDefaultSettingsActivated);
        switchDefaultSettings.setOnCheckedChangeListener(this::onDefaultSettingsSwitchChanged);
    }

    private void addTextChangedListenersToViews() {
        // create TextWatcher and attach it to each TextInputEditText
        TextWatcher textWatcher = this.createTextWatcher(this);
        repeat.addTextChangedListener(textWatcher);
        volume.addTextChangedListener(textWatcher);
        packetSize.addTextChangedListener(textWatcher);
        rate.addTextChangedListener(textWatcher);
        sleep.addTextChangedListener(textWatcher);
        timeout.addTextChangedListener(textWatcher);
        inputCycle.addTextChangedListener(textWatcher);
        inputCredit.addTextChangedListener(textWatcher);
    }

    private void addObserverToSettingsViewModel(View view, boolean isCruspSettingsInSharedPreferences) {
        mViewModel.getAllSettings().observe(getViewLifecycleOwner(), (spinnerData) -> {
            ArrayAdapter<CruspSetting> cruspSpinnerAdapter = new ArrayAdapter<>(view.getContext(), R.layout.support_simple_spinner_dropdown_item, requireNonNull(spinnerData));
            cruspSpinner.setAdapter(cruspSpinnerAdapter);
            cruspSpinner.setSelection(cruspSpinnerPosition);

            // initialize from spinner-position if shared-preferences do not contain settings
            // usually happens the first time
            // initialization happens here because here we have access to the locally saved crusp-settings
            if(!isCruspSettingsInSharedPreferences) {
                CruspSetting setting = (CruspSetting) cruspSpinner.getItemAtPosition(cruspSpinnerPosition);
                setTextFieldsWithCruspSettings(setting);
                writeSettingsToSharedPreferences(view);
                deactivateApplyButton();
            }
        });
    }

    private void loadRemainingSettingsFromSharedPreferences(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        cruspSpinnerPosition = preferences.getInt(KEY_CRUSP_POS, DEF_CRUSP_POS);
        cycle = preferences.getInt(KEY_CYCLE, DEF_CYCLE);
        credit = preferences.getInt(KEY_CREDIT, DEF_CREDIT);
        unitSpinnerPosition = preferences.getInt(KEY_UNIT_POS, DEF_UNIT_POS);
        isDefaultSettingsActivated = preferences.getBoolean(KEY_STANDARD, false);
    }

    private void loadViews(View view) {
        ll_settings = view.findViewById(R.id.ll_settings);
        switchDefaultSettings = view.findViewById(R.id.switch_default_settings);

        cruspSpinner = view.findViewById(R.id.crusp_spinner);
        cruspSpinner.setOnItemSelectedListener(this);

        //init  textviews
        repeat = view.findViewById(R.id.editRepeats);
        volume = view.findViewById(R.id.editVolume);
        packetSize = view.findViewById(R.id.editPacketSize);
        rate = view.findViewById(R.id.editRate);
        sleep = view.findViewById(R.id.editSleep);
        timeout = view.findViewById(R.id.editMaxTimeout);

        inputCredit = view.findViewById(R.id.editCredit);
        inputCycle = view.findViewById(R.id.editCycle);
        unitSpinner = view.findViewById(R.id.spinnerUnit);

        applyButton = view.findViewById(R.id.buttonApply);

    }

    private void onDefaultSettingsSwitchChanged(View view, boolean isChecked) {
        isDefaultSettingsActivated = isChecked;
        writeStandardChangedToSharedPreferences(view);

        showOrHideDefaultSettingsView(isDefaultSettingsActivated);
    }

    static CruspSetting getCruspSettingsFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        CruspSetting cruspSetting = new CruspSetting();

        cruspSetting.setRepeats(sharedPreferences.getInt(KEY_REPEATS, -1));
        cruspSetting.setVolume(sharedPreferences.getInt(KEY_VOLUME, -1));
        cruspSetting.setPacketSize(sharedPreferences.getInt(KEY_PACKET_SIZE, -1));
        cruspSetting.setRate(sharedPreferences.getInt(KEY_RATE, -1));
        cruspSetting.setSleep(sharedPreferences.getInt(KEY_SLEEP, -1));
        cruspSetting.setTimeout(sharedPreferences.getInt(KEY_TIMEOUT, -1));
        cruspSetting.setStandard(sharedPreferences.getBoolean(KEY_STANDARD, false));

        return cruspSetting;
    }

    private void setTextFieldsWithCruspSettings(CruspSetting setting) {
        repeat.setText(String.valueOf(setting.getRepeats()));
        volume.setText(String.valueOf(setting.getVolume()));
        packetSize.setText(String.valueOf(setting.getPacketSize()));
        rate.setText(String.valueOf(setting.getRate()));
        sleep.setText(String.valueOf(setting.getSleep()));
        timeout.setText(String.valueOf(setting.getTimeout()));
    }

    private void activateApplyButton() {
        applyButton.setEnabled(true);
        applyButton.setClickable(true);
        applyButton.setBackgroundResource(R.color.Primary);
    }

    private void deactivateApplyButton() {
        applyButton.setEnabled(false);
        applyButton.setClickable(false);
        applyButton.setBackgroundResource(R.color.Disabled);
    }

    private void onBtnApply(View view) {
        writeSettingsToSharedPreferences(view);
        hideKeyboard(view);
        clearFocus();
        deactivateApplyButton();
    }

    private void writeStandardChangedToSharedPreferences(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(KEY_STANDARD, isDefaultSettingsActivated);
        editor.apply();
    }

    private void writeSettingsToSharedPreferences(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(KEY_CRUSP_POS, cruspSpinnerPosition);
        editor.putInt(KEY_UNIT_POS, unitSpinnerPosition);
        editor.putBoolean(KEY_STANDARD, isDefaultSettingsActivated);

        try {

            if(requireNonNull(inputCycle.getText()).length() > 0) {
                cycle =  Integer.parseInt(inputCycle.getText().toString().trim());
                editor.putInt(KEY_CYCLE, cycle);
            }

            if(requireNonNull(inputCredit.getText()).length() > 0) {
                credit =  Integer.parseInt(inputCredit.getText().toString().trim());
                editor.putInt(KEY_CREDIT, credit);
            }
            if(requireNonNull(repeat.getText()).length() > 0) {
                editor.putInt(KEY_REPEATS, Integer.parseInt(repeat.getText().toString().trim()));
            }
            if(requireNonNull(volume.getText()).length() > 0) {
                editor.putInt(KEY_VOLUME, Integer.parseInt(volume.getText().toString().trim()));
            }
            if(requireNonNull(packetSize.getText()).length() > 0) {
                editor.putInt(KEY_PACKET_SIZE, Integer.parseInt(packetSize.getText().toString().trim()));
            }
            if(requireNonNull(rate.getText()).length() > 0) {
                editor.putInt(KEY_RATE, Integer.parseInt(rate.getText().toString().trim()));
            }
            if(requireNonNull(sleep.getText()).length() > 0) {
                editor.putInt(KEY_SLEEP, Integer.parseInt(sleep.getText().toString().trim()));
            }
            if(requireNonNull(timeout.getText()).length() > 0) {
                editor.putInt(KEY_TIMEOUT, Integer.parseInt(timeout.getText().toString().trim()));
            }
            editor.apply();
        } catch (NumberFormatException e) {
            Log.w("PARSE", "Couldn't parse settings");
        }
    }

    private void clearFocus() {
        this.volume.clearFocus();
        this.packetSize.clearFocus();
        this.rate.clearFocus();
        this.sleep.clearFocus();
        this.timeout.clearFocus();
        this.inputCredit.clearFocus();
        this.inputCycle.clearFocus();
        this.repeat.clearFocus(); //repeat is the first focus-able view so it has to be cleared last
    }

    private void hideKeyboard(View view) {
        Log.d(TAG, "Hide Keyboard");
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.crusp_spinner:
                Log.d(TAG, "onItemSelect crusp-spinner");
                CruspSetting selected = (CruspSetting) parent.getItemAtPosition(position);

                if(selected == null) {
                    return;
                }

                if(position != this.cruspSpinnerPosition) {
                    cruspSpinnerPosition = position;

                    setTextFieldsWithCruspSettings(selected);
                    activateApplyButton();

                    clearFocus();
                    hideKeyboard(view);
                }

                break;
            case R.id.spinnerUnit:
                Log.d(TAG, "onItemSelect unit-spinner");
                if(unitSpinnerPosition != position) {
                    unitSpinnerPosition = position;
                    activateApplyButton();
                    unitSpinner.setSelection(unitSpinnerPosition);
                }
                clearFocus();
                hideKeyboard(view);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    private TextWatcher createTextWatcher(TextChangedListener listener) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(before + count == 1) { // if just one character changed -> human input, otherwise input is not from human
                    listener.onTextChanged();
                }

            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    @Override
    public void onTextChanged() {
        activateApplyButton();
    }
}

