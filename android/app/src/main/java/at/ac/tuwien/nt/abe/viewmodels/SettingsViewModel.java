package at.ac.tuwien.nt.abe.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import at.ac.tuwien.nt.abe.model.CruspSetting;
import at.ac.tuwien.nt.abe.persistance.CruspSettingsRepository;

public class SettingsViewModel extends AndroidViewModel {
    private CruspSettingsRepository cruspSettingsRepository;
    private LiveData<List<CruspSetting>> allSettings;

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        this.cruspSettingsRepository = new CruspSettingsRepository(application);
        this.allSettings = this.cruspSettingsRepository.getAllCruspSettings();
    }

    public LiveData<List<CruspSetting>> getAllSettings() {
        return allSettings;
    }

    public void insert(CruspSetting cruspSetting) {
        cruspSettingsRepository.insert(cruspSetting);
    }
}
