package at.ac.tuwien.nt.abe.persistance;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import at.ac.tuwien.nt.abe.model.CruspSetting;

public class CruspSettingsRepository {
    private CruspSettingsDao cruspSettingsDao;
    private LiveData<List<CruspSetting>> allCruspSettings;

    public CruspSettingsRepository(Application application) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(application);

        this.cruspSettingsDao = appDatabase.cruspSettingsDao();
        this.allCruspSettings = cruspSettingsDao.getAll();
    }


    public LiveData<List<CruspSetting>> getAllCruspSettings() {
        return allCruspSettings;
    }

    public void insert(CruspSetting cruspSetting) {
        new InsertAsyncTask(cruspSettingsDao).execute(cruspSetting);
    }

    private static class InsertAsyncTask extends AsyncTask<CruspSetting, Void, Void> {
        private CruspSettingsDao cruspSettingsDao;

        InsertAsyncTask(CruspSettingsDao cruspSettingsDao) {
            this.cruspSettingsDao = cruspSettingsDao;
        }

        @Override
        protected Void doInBackground(CruspSetting... cruspSettings) {
            cruspSettingsDao.insertAll(cruspSettings);
            return null;
        }
    }



    protected static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private CruspSettingsDao cruspSettingsDao;

        PopulateDbAsync(AppDatabase appDatabase) {
            this.cruspSettingsDao = appDatabase.cruspSettingsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            if (cruspSettingsDao.getCount() == 0) {
                cruspSettingsDao.insertAll(new CruspSetting("CRUSP 58 ", 1, 58, 1000,150, 200,  100, false));
                cruspSettingsDao.insertAll(new CruspSetting("CRUSP 117", 1,  117, 1000, 150, 200, 100, false));
                cruspSettingsDao.insertAll(new CruspSetting("CRUSP 233", 1, 233, 1000,150, 200,  100, false));
                cruspSettingsDao.insertAll(new CruspSetting("CRUSP 465", 1, 465, 1000,150, 200,  100, false));
                cruspSettingsDao.insertAll(new CruspSetting("CRUSP 930", 1, 930, 1000,150, 200,  100, false));
                cruspSettingsDao.insertAll(new CruspSetting("CRUSP 1860", 1, 1860, 1000,300, 200,  100, false));
            }
            return null;
        }
    }
}
