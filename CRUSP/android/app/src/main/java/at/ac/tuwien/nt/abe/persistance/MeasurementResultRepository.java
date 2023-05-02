package at.ac.tuwien.nt.abe.persistance;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import at.ac.tuwien.nt.abe.fragments.tasks.MeasurementSavedInLocalDBCaller;
import at.ac.tuwien.nt.abe.model.CruspError;
import at.ac.tuwien.nt.abe.model.MeasurementResult;
import at.ac.tuwien.nt.abe.model.ReceivedPacketDetails;
import at.ac.tuwien.nt.abe.model.SequenceDetails;
import at.ac.tuwien.nt.abe.viewmodels.MeasurementResultCallback;

public class MeasurementResultRepository {
    private AppDatabase appDatabase;

    private MeasurementResultDao measurementResultDao;
    private SequenceDetailsDao sequenceDetailsDao;
    private ReceivedPacketDetailsDao receivedPacketDetailsDao;

    private LiveData<List<MeasurementResult>> allMeasurementResults;

    public MeasurementResultRepository(Context context) {
        this.appDatabase = AppDatabase.getAppDatabase(context);

        this.measurementResultDao = appDatabase.measurementResultDao();
        this.sequenceDetailsDao = appDatabase.sequenceDetialsDao();
        this.receivedPacketDetailsDao = appDatabase.receivedPacketDetailsDao();
        this.allMeasurementResults = measurementResultDao.getAll();
    }


    public LiveData<List<MeasurementResult>> getAllMeasurementResults() {
        return allMeasurementResults;
    }

    public void insert(MeasurementResult measurementResult, MeasurementSavedInLocalDBCaller measurementSavedInLocalDBCaller) {
        if(measurementResult != null) {
            InsertAsyncTask task = new InsertAsyncTask(measurementResultDao, sequenceDetailsDao, receivedPacketDetailsDao, appDatabase, measurementSavedInLocalDBCaller);

            task.execute(measurementResult);
        }
    }

    public long count() {
        CountAsyncTask task = new CountAsyncTask(measurementResultDao);

        try {
            return task.execute().get();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void updatePersisted(Long measurementId) {
        if(measurementId != null) {
            UpdateAsyncTask task = new UpdateAsyncTask(measurementResultDao, appDatabase);

            task.execute(measurementId);
        }
    }

    public LiveData<MeasurementResult> readLiveResultOverview(Long resultId) {
        if(resultId == null || resultId <= 0) {
            return null;
        }

        ReadLiveMeasurementAsyncTask measurementTask = new ReadLiveMeasurementAsyncTask(measurementResultDao);

        try {
            return measurementTask.execute(resultId).get();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void readResultOverviewAsync(long resultId, MeasurementResultCallback callback) {
        ReadMeasurementAsyncTask measurementTask = new ReadMeasurementAsyncTask(measurementResultDao, callback);

        measurementTask.execute(resultId);
    }

    public MeasurementResult readResultByIdOverview(long resultId) {
        ReadMeasurementAsyncTask measurementTask = new ReadMeasurementAsyncTask(measurementResultDao, null);

        try {
            return measurementTask.execute(resultId).get();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MeasurementResult readResultByIdComplete(long resultId) {
        ReadDetailsAsyncTask task = new ReadDetailsAsyncTask(sequenceDetailsDao, receivedPacketDetailsDao);
        ReadFullMeasurementAsyncTask measurementTask = new ReadFullMeasurementAsyncTask(measurementResultDao);
        MeasurementResult result = null;

        try {
            result = measurementTask.execute(resultId).get();

            if(result != null && result.getErrorType() == CruspError.NO_ERROR) {
                List<SequenceDetails> details = task.execute(resultId).get();
                result.setSequenceCollection(details);
            }

            return result;
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void delete(MeasurementResult measurementResult) {
        DeleteAsyncTask deleteAsyncTask = new DeleteAsyncTask(measurementResultDao, sequenceDetailsDao, receivedPacketDetailsDao, appDatabase);
        deleteAsyncTask.execute(measurementResult);

    }

    private static class ReadDetailsAsyncTask extends AsyncTask<Long, Void, List<SequenceDetails>> {
        private SequenceDetailsDao sequenceDetailsDao;
        private ReceivedPacketDetailsDao receivedPacketDetailsDao;


        ReadDetailsAsyncTask(SequenceDetailsDao sequenceDetailsDao, ReceivedPacketDetailsDao receivedPacketDetailsDao) {
            this.sequenceDetailsDao = sequenceDetailsDao;
            this.receivedPacketDetailsDao = receivedPacketDetailsDao;
        }

        @Override
        protected List<SequenceDetails> doInBackground(Long... measurementIds) {
            List<SequenceDetails> details = new ArrayList<>();
            for (Long id : measurementIds) {
                 details = sequenceDetailsDao.getAllForMeasurement(id);

                for (SequenceDetails detail : details) {
                    List<ReceivedPacketDetails> packets = receivedPacketDetailsDao.getAllForSequence(detail.getUid());
                    detail.setPackets(packets);
                }
            }

            return details;
        }
    }

    private static class ReadMeasurementAsyncTask extends AsyncTask<Long, Void, MeasurementResult> {
        private MeasurementResultDao dao;
        private Optional<MeasurementResultCallback> callback;


        ReadMeasurementAsyncTask(MeasurementResultDao dao, MeasurementResultCallback callback) {
            this.dao = dao;
            this.callback = Optional.of(callback);
        }

        @Override
        protected MeasurementResult doInBackground(Long... measurementIds) {
            MeasurementResult result = null;

            for (Long id : measurementIds) {
                if(id != null) {
                    result = dao.getById(id);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(MeasurementResult measurementResult) {
            super.onPostExecute(measurementResult);

            callback.ifPresent(resultCB -> resultCB.execute(measurementResult));
        }
    }

    private static class ReadLiveMeasurementAsyncTask extends AsyncTask<Long, Void, LiveData<MeasurementResult>> {
        private MeasurementResultDao dao;

        ReadLiveMeasurementAsyncTask(MeasurementResultDao dao) {
            this.dao = dao;
        }

        @Override
        protected LiveData<MeasurementResult> doInBackground(Long... measurementIds) {
            LiveData<MeasurementResult> result = null;

            for (Long id : measurementIds) {
                if(id != null) {
                    result = dao.getByIdLive(id);
                }
            }

            return result;
        }
    }

    private static class ReadFullMeasurementAsyncTask extends AsyncTask<Long, Void, MeasurementResult> {
        private MeasurementResultDao dao;


        ReadFullMeasurementAsyncTask(MeasurementResultDao dao) {
            this.dao = dao;
        }

        @Override
        protected MeasurementResult doInBackground(Long... measurementIds) {
            MeasurementResult result = null;

            for (Long id : measurementIds) {
                if(id != null) {
                    result = dao.getById(id);
                }
            }

            return result;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<MeasurementResult, Void, Long> {
        private MeasurementResultDao measurementResultDao;
        private SequenceDetailsDao sequenceDetailsDao;
        private ReceivedPacketDetailsDao receivedPacketDetailsDao;
        private AppDatabase appDatabase;
        private MeasurementSavedInLocalDBCaller measurementSavedInLocalDBCaller;


        InsertAsyncTask(MeasurementResultDao measurementResultDao, SequenceDetailsDao sequenceDetailsDao, ReceivedPacketDetailsDao receivedPacketDetailsDao, AppDatabase appDatabase, MeasurementSavedInLocalDBCaller measurementSavedInLocalDBCaller) {
            this.measurementResultDao = measurementResultDao;
            this.sequenceDetailsDao = sequenceDetailsDao;
            this.receivedPacketDetailsDao = receivedPacketDetailsDao;
            this.appDatabase = appDatabase;
            this.measurementSavedInLocalDBCaller = measurementSavedInLocalDBCaller;
        }

        @Override
        protected Long doInBackground(MeasurementResult... measurementResults) {
            return appDatabase.runInTransaction(() -> {
                long measurementId = 0;
                for (MeasurementResult measurementResult : measurementResults) {
                    measurementId = measurementResultDao.insert(measurementResult);
                    measurementResult.setUid(measurementId);

                    if(measurementResult.getErrorType() == CruspError.NO_ERROR) {
                        for (SequenceDetails sequenceDetails : measurementResult.getSequenceCollection()) {
                            sequenceDetails.setMeasurementId(measurementId);

                            long seqId = sequenceDetailsDao.insert(sequenceDetails);
                            sequenceDetails.setUid(seqId);

                            for (ReceivedPacketDetails packet : sequenceDetails.getPackets()) {
                                packet.setSeqId(seqId);

                                long packetId = receivedPacketDetailsDao.insert(packet);
                                packet.setUid(packetId);
                            }
                        }
                    }
                }
                return measurementId;
            });
        }

        @Override
        protected void onPostExecute(Long id) {
            super.onPostExecute(id);

            measurementSavedInLocalDBCaller.call(id);
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Long, Void, Void> {
        private MeasurementResultDao measurementResultDao;
        private AppDatabase appDatabase;


        UpdateAsyncTask(MeasurementResultDao measurementResultDao, AppDatabase appDatabase) {
            this.measurementResultDao = measurementResultDao;
            this.appDatabase = appDatabase;
        }

        @Override
        protected Void doInBackground(Long... measurementIds) {
            return appDatabase.runInTransaction(() -> {
                for (Long measuremntId : measurementIds) {
                    measurementResultDao.updateToPersisted(measuremntId);
                }
                return null;
            });
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<MeasurementResult, Void, Void> {
        private MeasurementResultDao measurementResultDao;
        private SequenceDetailsDao sequenceDetailsDao;
        private ReceivedPacketDetailsDao receivedPacketDetailsDao;
        private AppDatabase appDatabase;


        DeleteAsyncTask(MeasurementResultDao measurementResultDao, SequenceDetailsDao sequenceDetailsDao, ReceivedPacketDetailsDao receivedPacketDetailsDao, AppDatabase appDatabase) {
            this.measurementResultDao = measurementResultDao;
            this.sequenceDetailsDao = sequenceDetailsDao;
            this.receivedPacketDetailsDao = receivedPacketDetailsDao;
            this.appDatabase = appDatabase;
        }

        @Override
        protected Void doInBackground(MeasurementResult... measurementResults) {
            appDatabase.runInTransaction(() -> {
                for (MeasurementResult measurementResult : measurementResults) {
                    if(measurementResult.getSequenceCollection() != null) {
                        for (SequenceDetails sequenceDetails : measurementResult.getSequenceCollection()) {
                            receivedPacketDetailsDao.deleteAllForSequence(sequenceDetails.getUid());
                        }
                        sequenceDetailsDao.deleteAllForMeasurement(measurementResult.getUid());
                    }
                    measurementResultDao.delete(measurementResult);
                }
            });
            return null;
        }
    }

    private static class CountAsyncTask extends AsyncTask<Void, Void, Long> {
        private MeasurementResultDao measurementResultDao;


        CountAsyncTask(MeasurementResultDao measurementResultDao) {
            this.measurementResultDao = measurementResultDao;
        }

        @Override
        protected Long doInBackground(Void ...empty) {
            return this.measurementResultDao.getCount();
        }
    }
}
