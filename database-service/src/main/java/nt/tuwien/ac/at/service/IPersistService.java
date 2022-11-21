package nt.tuwien.ac.at.service;

import nt.tuwien.ac.at.dtos.Filtered;
import nt.tuwien.ac.at.dtos.Sorted;
import nt.tuwien.ac.at.model.MeasurementResult;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;

import java.util.List;
import java.util.Optional;

public interface IPersistService {
    MeasurementResult saveMeasurement(MeasurementResult result, ITelephonyInfo telephonyInfo);
    Optional<MeasurementResult> findMeasurement(long measurementId);
    FilterMeasurementDto filterMeasurementsWithSettings(List<Filtered> filteredMeasurements,
                                                        List<Sorted> sortedMeasurements,
                                                        List<Filtered> filteredTelephonyInfo,
                                                        List<Sorted> sortedTelephonyInfo,
                                                        int page,
                                                        int pageSize,
                                                        List<Long> settingIds,
                                                        boolean settingsSorted);

    List<MeasurementResult> findDetails(List<Long> ids);
}
