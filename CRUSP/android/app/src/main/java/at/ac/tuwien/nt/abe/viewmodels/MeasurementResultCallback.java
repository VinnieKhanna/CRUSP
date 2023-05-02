package at.ac.tuwien.nt.abe.viewmodels;

import at.ac.tuwien.nt.abe.model.MeasurementResult;

public interface MeasurementResultCallback {
    void execute(MeasurementResult result);
}
