package at.ac.tuwien.nt.abe.persistance;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import at.ac.tuwien.nt.abe.model.SequenceDetails;

@Dao
public interface SequenceDetailsDao {
    @Query("SELECT * FROM sequencedetails WHERE measurementId = :measurementId")
    List<SequenceDetails> getAllForMeasurement(long measurementId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SequenceDetails sequenceDetails);

    @Delete
    void delete(SequenceDetails sequenceDetails);

    @Query("SELECT COUNT(*) FROM sequencedetails")
    long getCount();

    @Query("DELETE FROM sequencedetails WHERE measurementId = :measurementId")
    void deleteAllForMeasurement(long measurementId);
}
