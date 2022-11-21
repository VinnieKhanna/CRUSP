package at.ac.tuwien.nt.abe.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.persistance.MeasurementResultRepository;

import static at.ac.tuwien.nt.abe.util.Keys.KEY_DATARATE2_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_DATARATE_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT_DOWNLINK_ID;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_MEASUREMENT_RESULT_UPLINK_ID;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_TIME2_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_TIME_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_VOLUME2_STRING;
import static at.ac.tuwien.nt.abe.util.Keys.KEY_VOLUME_STRING;

public class DisplayViewModel extends AndroidViewModel {
    private SavedStateHandle mState;
    private MeasurementResultRepository repository;

    private MutableLiveData<Long> downlinkResultId;
    private MutableLiveData<Long> uplinkResultId;

    private LiveData<MeasurementResult> downlinkResult;
    private LiveData<MeasurementResult> uplinkResult;

    private String dataRateStringDownlink = "";
    private String volumeStringDownlink = "";
    private String timeStringDownlink = "";
    private String dataRateStringUplink = "";
    private String volumeStringUplink = "";
    private String timeStringUplink = "";

    public DisplayViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.mState = savedStateHandle;

        loadData();

        repository = new MeasurementResultRepository(application);
        downlinkResult = Transformations.switchMap(downlinkResultId, ( id -> repository.readLiveResultOverview(id)));
        uplinkResult = Transformations.switchMap(uplinkResultId, ( id -> repository.readLiveResultOverview(id)));
    }

    private void loadData() {
        if(mState.contains(KEY_MEASUREMENT_RESULT_DOWNLINK_ID)) {
            downlinkResultId = mState.get(KEY_MEASUREMENT_RESULT_DOWNLINK_ID);
        } else {
            downlinkResultId = null;
        }

        if(mState.contains(KEY_MEASUREMENT_RESULT_UPLINK_ID)) {
            uplinkResultId = mState.get(KEY_MEASUREMENT_RESULT_UPLINK_ID);
        } else {
            uplinkResultId = null;
        }

        if(mState.contains(KEY_DATARATE_STRING)) {
            dataRateStringDownlink = mState.get(KEY_DATARATE_STRING);
        } else {
            dataRateStringDownlink = "";
        }

        if(mState.contains(KEY_DATARATE2_STRING)) {
            dataRateStringUplink = mState.get(KEY_DATARATE2_STRING);
        } else {
            dataRateStringUplink = "";
        }

        if(mState.contains(KEY_VOLUME_STRING)) {
            volumeStringDownlink = mState.get(KEY_VOLUME_STRING);
        } else {
            volumeStringDownlink = "";
        }

        if(mState.contains(KEY_VOLUME2_STRING)) {
            volumeStringUplink = mState.get(KEY_VOLUME2_STRING);
        } else {
            volumeStringUplink = "";
        }

        if(mState.contains(KEY_DATARATE_STRING)) {
            timeStringDownlink = mState.get(KEY_DATARATE_STRING);
        } else {
            timeStringDownlink = "";
        }

        if(mState.contains(KEY_DATARATE_STRING)) {
            timeStringUplink = mState.get(KEY_TIME2_STRING);
        } else {
            timeStringUplink = "";
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();

        if(downlinkResultId != null) {
            mState.set(KEY_MEASUREMENT_RESULT_DOWNLINK_ID, downlinkResultId);
            mState.set(KEY_DATARATE_STRING, dataRateStringDownlink);
            mState.set(KEY_VOLUME_STRING, volumeStringDownlink);
            mState.set(KEY_TIME_STRING, timeStringDownlink);
        }

        if(uplinkResultId != null) {
            mState.set(KEY_MEASUREMENT_RESULT_UPLINK_ID, uplinkResultId);
            mState.set(KEY_MEASUREMENT_RESULT_UPLINK_ID, uplinkResultId);
            mState.set(KEY_DATARATE2_STRING, dataRateStringUplink);
            mState.set(KEY_VOLUME2_STRING, volumeStringUplink);
            mState.set(KEY_TIME2_STRING, timeStringUplink);
        }
    }

    public String getDataRateStringDownlink() {
        return dataRateStringDownlink;
    }

    public void setDataRateStringDownlink(String dataRateStringDownlink) {
        this.dataRateStringDownlink = dataRateStringDownlink;
    }

    public String getVolumeStringDownlink() {
        return volumeStringDownlink;
    }

    public void setVolumeStringDownlink(String volumeStringDownlink) {
        this.volumeStringDownlink = volumeStringDownlink;
    }

    public String getTimeStringDownlink() {
        return timeStringDownlink;
    }

    public void setTimeStringDownlink(String timeStringDownlink) {
        this.timeStringDownlink = timeStringDownlink;
    }

    public String getDataRateStringUplink() {
        return dataRateStringUplink;
    }

    public void setDataRateStringUplink(String dataRateString2) {
        this.dataRateStringUplink = dataRateString2;
    }

    public String getVolumeStringUplink() {
        return volumeStringUplink;
    }

    public void setVolumeStringUplink(String volumeString2) {
        this.volumeStringUplink = volumeString2;
    }

    public String getTimeStringUplink() {
        return timeStringUplink;
    }

    public void setTimeStringUplink(String timeString2) {
        this.timeStringUplink = timeString2;
    }

    public Long getDownlinkResultId() {
        return downlinkResultId != null ? downlinkResultId.getValue() : null;
    }

    public void setDownlinkResultId(Long downlinkResultId) {
        this.downlinkResultId = new MutableLiveData<>(downlinkResultId);
    }

    public Long getUplinkResultId() {
        return uplinkResultId != null ? uplinkResultId.getValue() : null;
    }

    public void setUplinkResultId(Long uplinkResultId) {
        this.uplinkResultId = new MutableLiveData<>(uplinkResultId);
    }

    public LiveData<MeasurementResult> getDownlinkResult() {
        return downlinkResult;
    }

    public LiveData<MeasurementResult> getUplinkResult() {
        return uplinkResult;
    }
}
