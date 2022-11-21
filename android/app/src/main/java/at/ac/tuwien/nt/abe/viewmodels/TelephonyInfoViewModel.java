package at.ac.tuwien.nt.abe.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;
import at.ac.tuwien.nt.abe.persistance.TelephonyInfoRepository;

public class TelephonyInfoViewModel extends AndroidViewModel {
    private TelephonyInfoRepository repository;

    public TelephonyInfoViewModel(@NonNull Application application) {
        super(application);

        repository = new TelephonyInfoRepository(application);
    }

    public void insert(ITelephonyInfo telephonyInfo) {
        repository.insert(telephonyInfo);
    }

    public void loadTelephonyInfoAsync(long resultId, TelephonyInfoCallback callback) {
        repository.readTelephonyInfoByMeasurementIdAsync(resultId, callback);
    }

    public ITelephonyInfo loadTelephonyInfo(long resultId) {
        return repository.readTelephonyInfoByMeasurementId(resultId);
    }
}
