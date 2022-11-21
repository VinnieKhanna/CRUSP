package at.ac.tuwien.nt.abe.persistance;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import at.ac.tuwien.nt.abe.model.CruspSetting;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.ReceivedPacketDetails;
import at.ac.tuwien.nt.abe.model.SequenceDetails;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoCdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoGSM;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoLTE;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoNR;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWcdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWifi;

@Database(entities = {CruspSetting.class, MeasurementResult.class, SequenceDetails.class, ReceivedPacketDetails.class,
        TelephonyInfoLTE.class, TelephonyInfoWcdma.class, TelephonyInfoCdma.class, TelephonyInfoGSM.class, TelephonyInfoWifi.class, TelephonyInfoNR.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract CruspSettingsDao cruspSettingsDao();
    public abstract MeasurementResultDao measurementResultDao();
    public abstract SequenceDetailsDao sequenceDetialsDao();
    public abstract ReceivedPacketDetailsDao receivedPacketDetailsDao();
    public abstract TelephonyInfoDao telephonyInfoDao();

    static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class){
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "crusp-database")
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(settingsDatabaseCallback)
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS 'TelephonyInfoNR' ('dbm' INTEGER, " +
                    "'asu' INTEGER, 'deviceId' TEXT, 'operator' TEXT, 'nci' INTEGER, 'mcc' TEXT, " +
                    "'mnc' TEXT, 'nrarfcn' INTEGER, 'pci' INTEGER, 'tac' INTEGER, 'csiSinr' INTEGER, " +
                    "'csiRsrq' INTEGER, 'csiRsrp' INTEGER, 'ssSinr' INTEGER, 'ssRsrq' INTEGER, " +
                    "'ssRsrp' INTEGER, 'operatorAlphaLong' TEXT, " +
                    "'uid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "'measurementId' INTEGER NOT NULL, 'lat' REAL, 'lng' REAL, 'speed' REAL, " +
                    "'gpsAccuracy' REAL, 'manufacturer' TEXT, 'model' TEXT)");
        }
    };

    private static AppDatabase.Callback settingsDatabaseCallback =
            new AppDatabase.Callback(){
                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new CruspSettingsRepository.PopulateDbAsync(INSTANCE).execute();
                }
            };
}
