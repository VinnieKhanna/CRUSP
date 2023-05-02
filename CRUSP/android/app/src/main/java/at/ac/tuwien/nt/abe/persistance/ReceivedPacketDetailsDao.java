package at.ac.tuwien.nt.abe.persistance;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import at.ac.tuwien.nt.abe.model.ReceivedPacketDetails;

@Dao
public interface ReceivedPacketDetailsDao {
    @Query("SELECT * FROM receivedpacketdetails WHERE seqId = :seqId")
    List<ReceivedPacketDetails> getAllForSequence(long seqId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReceivedPacketDetails receivedPacketDetails);

    @Delete
    void delete(ReceivedPacketDetails receivedPacketDetails);

    @Query("SELECT COUNT(*) FROM receivedpacketdetails")
    long getCount();

    @Query("DELETE FROM receivedpacketdetails WHERE seqId = :seqId")
    void deleteAllForSequence(long seqId);
}
