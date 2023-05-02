package at.ac.tuwien.nt.abe.persistance;

import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

import at.ac.tuwien.nt.abe.model.network.ITelephonyInfo;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoCdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoGSM;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoLTE;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoNR;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWcdma;
import at.ac.tuwien.nt.abe.model.network.TelephonyInfoWifi;
import at.ac.tuwien.nt.abe.viewmodels.TelephonyInfoCallback;

public class TelephonyInfoRepository {

    private TelephonyInfoDao dao;

    public TelephonyInfoRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getAppDatabase(context);
        this.dao = appDatabase.telephonyInfoDao();
    }

    public void insert(ITelephonyInfo telephonyInfo) {
        if(telephonyInfo != null) {
            InsertAsyncTask task = new InsertAsyncTask(dao);
            task.execute(telephonyInfo);
        }

    }

    public void readTelephonyInfoByMeasurementIdAsync(long id, TelephonyInfoCallback callback) {
        ReadTelephonyInfoByMeasurementAsyncTask task = new ReadTelephonyInfoByMeasurementAsyncTask(dao, callback);
        task.execute(id);
    }

    public ITelephonyInfo readTelephonyInfoByMeasurementId(long id) {
        ReadTelephonyInfoByMeasurementAsyncTask task = new ReadTelephonyInfoByMeasurementAsyncTask(dao, null);

        try {
            return task.execute(id).get();
        } catch (ExecutionException |InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class ReadTelephonyInfoByMeasurementAsyncTask extends AsyncTask<Long, Void, ITelephonyInfo> {
        private TelephonyInfoDao dao;
        private TelephonyInfoCallback callback;

        ReadTelephonyInfoByMeasurementAsyncTask(TelephonyInfoDao dao, TelephonyInfoCallback callback) {
            this.dao = dao;
            this.callback = callback;
        }

        @Override
        protected ITelephonyInfo doInBackground(Long... telephonyInfoIds) {
            for (Long id : telephonyInfoIds) {
                if(id != null) {
                    ITelephonyInfo telephonyInfo = dao.getLTEByMeasurementId(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getWcdmaByMeasurementId(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getNRByMeasurementId(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getCdmaByMeasurementId(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getGSMByMeasurementId(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getWifiByMeasurementId(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ITelephonyInfo telephonyInfo) {
            super.onPostExecute(telephonyInfo);

            if(callback != null) {
                callback.execute(telephonyInfo);
            }
        }
    }

    private static class ReadTelephonyInfoAsyncTask extends AsyncTask<Long, Void, ITelephonyInfo> {
        private TelephonyInfoDao dao;

        ReadTelephonyInfoAsyncTask(TelephonyInfoDao dao) {
            this.dao = dao;
        }

        @Override
        protected ITelephonyInfo doInBackground(Long... telephonyInfoIds) {
            for (Long id : telephonyInfoIds) {
                if(id != null) {
                    ITelephonyInfo telephonyInfo = dao.getLTEById(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getWcdmaById(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getNRById(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getCdmaById(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getGSMById(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }

                    telephonyInfo = dao.getWifiById(id);
                    if(telephonyInfo != null) {
                        return telephonyInfo;
                    }
                }
            }

            return null;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<ITelephonyInfo, Void, Void> {
        private TelephonyInfoDao dao;

        InsertAsyncTask(TelephonyInfoDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(ITelephonyInfo... telephonyInfos) {
            ITelephonyInfo telephonyInfo = telephonyInfos[0];
            long uid = -1L;

            if(telephonyInfo instanceof TelephonyInfoLTE) {
                uid = dao.insertLTE((TelephonyInfoLTE) telephonyInfo);
            } else if(telephonyInfo instanceof TelephonyInfoWcdma) {
                uid = dao.insertWCDMA((TelephonyInfoWcdma) telephonyInfo);
            } else if(telephonyInfo instanceof TelephonyInfoNR) {
                uid = dao.insertNR((TelephonyInfoNR) telephonyInfo);
            } else if(telephonyInfo instanceof TelephonyInfoCdma) {
                uid = dao.insertCdma((TelephonyInfoCdma) telephonyInfo);
            } else if(telephonyInfo instanceof TelephonyInfoGSM) {
                uid = dao.insertGSM((TelephonyInfoGSM) telephonyInfo);
            } else if(telephonyInfo instanceof TelephonyInfoWifi) {
                uid = dao.insertWIFI((TelephonyInfoWifi) telephonyInfo);
            }

            telephonyInfo.setUid((int)uid);

            return null;
        }
    }
}
