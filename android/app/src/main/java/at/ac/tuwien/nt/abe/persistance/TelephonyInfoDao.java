package at.ac.tuwien.nt.abe.persistance;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import at.ac.tuwien.nt.abe.model.network.TelephonyInfoCdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoGSM;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoLTE;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoNR;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWcdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWifi;

@Dao
public interface TelephonyInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGSM(TelephonyInfoGSM telephonyInfoGSM);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertLTE(TelephonyInfoLTE telephonyInfoLTE);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWCDMA(TelephonyInfoWcdma telephonyInfoWcdma);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCdma(TelephonyInfoCdma telephonyInfoCdma);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWIFI(TelephonyInfoWifi telephonyInfo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNR(TelephonyInfoNR telephonyInfo);


    @Query("SELECT * FROM telephonyinfogsm WHERE uid=:id")
    TelephonyInfoGSM getGSMById(Long id);

    @Query("SELECT * FROM telephonyinfolte WHERE uid=:id")
    TelephonyInfoLTE getLTEById(Long id);

    @Query("SELECT * FROM telephonyinfowcdma WHERE uid=:id")
    TelephonyInfoWcdma getWcdmaById(Long id);

    @Query("SELECT * FROM telephonyinfocdma WHERE uid=:id")
    TelephonyInfoCdma getCdmaById(Long id);

    @Query("SELECT * FROM telephonyinfowifi WHERE uid=:id")
    TelephonyInfoWifi getWifiById(Long id);

    @Query("SELECT * FROM telephonyinfonr WHERE uid=:id")
    TelephonyInfoNR getNRById(Long id);

    // queries by measurementId

    @Query("SELECT * FROM telephonyinfolte WHERE measurementId=:id")
    TelephonyInfoLTE getLTEByMeasurementId(Long id);

    @Query("SELECT * FROM telephonyinfowcdma WHERE measurementId=:id")
    TelephonyInfoWcdma getWcdmaByMeasurementId(Long id);

    @Query("SELECT * FROM telephonyinfocdma WHERE measurementId=:id")
    TelephonyInfoCdma getCdmaByMeasurementId(Long id);

    @Query("SELECT * FROM telephonyinfogsm WHERE measurementId=:id")
    TelephonyInfoGSM getGSMByMeasurementId(Long id);

    @Query("SELECT * FROM telephonyinfowifi WHERE measurementId=:id")
    TelephonyInfoWifi getWifiByMeasurementId(Long id);

    @Query("SELECT * FROM telephonyinfonr WHERE measurementId=:id")
    TelephonyInfoNR getNRByMeasurementId(Long id);
}
