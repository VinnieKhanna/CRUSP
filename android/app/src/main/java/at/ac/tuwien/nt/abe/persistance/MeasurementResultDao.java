package at.ac.tuwien.nt.abe.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import at.ac.tuwien.nt.abe.model.MeasurementResult;

@Dao
public interface MeasurementResultDao {
    @Query("SELECT * FROM measurementresult ORDER BY startTime DESC")
    LiveData<List<MeasurementResult>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MeasurementResult measurementResult);

    @Delete
    void delete(MeasurementResult measurementResult);

    @Query("SELECT COUNT(*) FROM measurementresult")
    long getCount();

    @Query("SELECT * FROM measurementresult WHERE uid=:id")
    MeasurementResult getById(Long id);

    @Query("SELECT * FROM measurementresult WHERE uid=:id")
    LiveData<MeasurementResult> getByIdLive(Long id);

    @Query("UPDATE measurementresult SET persisted = 1 WHERE uid=:id") //set persisted to true
    void updateToPersisted(Long id);
}
