package at.ac.tuwien.nt.abe.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import at.ac.tuwien.nt.abe.model.CruspSetting;

@Dao
public interface CruspSettingsDao {
    @Query("SELECT * FROM cruspsetting")
    LiveData<List<CruspSetting>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CruspSetting... cruspSettings);

    @Delete
    void delete(CruspSetting cruspSetting);

    @Query("SELECT COUNT(*) FROM cruspsetting")
    long getCount();
}
