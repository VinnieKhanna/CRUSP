package at.ac.tuwien.nt.abe.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import at.ac.tuwien.nt.abe.fragments.tasks.MeasurementSavedInLocalDBCaller;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.persistance.MeasurementResultRepository;

public class MeasurementResultsViewModel extends AndroidViewModel {
    public static final long MAX_RESULTS_IN_DB = 100;
    private MeasurementResultRepository repository;
    private LiveData<List<MeasurementResult>> allResults;

    public MeasurementResultsViewModel(@NonNull Application application) {
        super(application);
        repository = new MeasurementResultRepository(application);
        allResults = repository.getAllMeasurementResults();
    }

    public LiveData<List<MeasurementResult>> getAllResults() {
        return allResults;
    }

    public MeasurementResult getResultByIdComplete(long resultId) {
        return repository.readResultByIdComplete(resultId);
    }

    public MeasurementResult getResultByIdOverview(long resultId) {
        return repository.readResultByIdOverview(resultId);
    }

    public void insert(MeasurementResult measurementResult, MeasurementSavedInLocalDBCaller measurementSavedInLocalDBCaller) {
        repository.insert(measurementResult, measurementSavedInLocalDBCaller);
    }

    public void updateToPersisted(long resultId) {
        repository.updatePersisted(resultId);
    }

    public void delete(MeasurementResult measurementResult) {
        repository.delete(measurementResult);
    }
}
