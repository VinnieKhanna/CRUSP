package at.ac.tuwien.nt.abe.fragments.tasks;

import android.location.Location;
import android.os.AsyncTask;

import java.util.List;

import at.ac.tuwien.nt.abe.fragments.interfaces.OnSpeedUpdate;

public class SpeedAsyncTask extends AsyncTask<Void, Void, Location> {
    private OnSpeedUpdate callback;
    private static Location currentLocation;
    private List<Location> locations;
    private static double omegaLat = 0;
    private static double omegaLong = 0;

    public SpeedAsyncTask(OnSpeedUpdate callback, List<Location> locations) {
        this.callback = callback;
        this.locations = locations;
    }

    @Override
    protected Location doInBackground(Void... empty) {
        /*
         * Speed measurement by Richard :)
         * The higher beta, the more weight has the new result
         */
        float beta = 0.1f;
        float tau = 10; // 10 sec, 3 times tau
        Location mLastLocation = currentLocation; // set last location to old location

        if(!locations.isEmpty()) { // locations.size() is 1 most of the time
            currentLocation = locations.get(locations.size()-1);

            if(mLastLocation != null) {
                double timeInSec = (currentLocation.getTime() - mLastLocation.getTime())/1000;
                if(timeInSec == 0.0) { return mLastLocation; }

                double alpha = (1-beta)*(1-Math.exp(-timeInSec/tau)) + beta;

                omegaLat = (currentLocation.getLatitude() - mLastLocation.getLatitude())/timeInSec * alpha + (1-alpha)*omegaLat;
                omegaLong = (currentLocation.getLongitude() - mLastLocation.getLongitude())/timeInSec * alpha + (1-alpha)*omegaLong;

                double vLat = omegaLat/180*Math.PI * 6371 * 1000;
                double vLong = omegaLong/180*Math.PI * 6371 * 1000;

                float speed = (float) Math.sqrt(Math.pow(vLat, 2) + Math.pow(vLong, 2));

                currentLocation.setSpeed(speed);
            }
        }

        return currentLocation;
    }

    @Override
    protected void onPostExecute(Location location) {
        super.onPostExecute(location);
        callback.updateLocation(location);
    }
}
