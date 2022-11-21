package nt.tuwien.ac.at.service;

import lombok.extern.slf4j.Slf4j;
import nt.tuwien.ac.at.dtos.Filtered;
import nt.tuwien.ac.at.dtos.Sorted;
import nt.tuwien.ac.at.exceptions.FilterValidationException;
import nt.tuwien.ac.at.model.*;
import nt.tuwien.ac.at.model.network.*;
import nt.tuwien.ac.at.persistance.MeasurementFilterRepository;
import nt.tuwien.ac.at.persistance.MeasurementRepository;
import nt.tuwien.ac.at.persistance.TelephonyInfoRepository;
import nt.tuwien.ac.at.dtos.filtering.StartEndDouble;
import nt.tuwien.ac.at.dtos.filtering.StartEndFloat;
import nt.tuwien.ac.at.dtos.filtering.StartEndInteger;
import nt.tuwien.ac.at.dtos.filtering.StartEndStringForBigInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static nt.tuwien.ac.at.dtos.filtering.StartEndDouble.getStartEndDouble;
import static nt.tuwien.ac.at.dtos.filtering.StartEndFloat.getStartEndFloat;
import static nt.tuwien.ac.at.dtos.filtering.StartEndInteger.getStartEndInteger;
import static nt.tuwien.ac.at.dtos.filtering.StartEndStringForBigInteger.getStartEndString;


@Slf4j
@Service
public class PersistServiceImpl implements IPersistService {
    private static final String TAG = PersistServiceImpl.class.getName();
    private MeasurementRepository measurementRepository;
    private TelephonyInfoRepository telephonyInfoRepository;
    private MeasurementFilterRepository filterRepository;

    @Autowired
    public void setMeasurementRepository(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Autowired
    public void setTelephonyInfoRepository(TelephonyInfoRepository telephonyInfoRepository) {
        this.telephonyInfoRepository = telephonyInfoRepository;
    }

    @Autowired
    public void setMeasurementFilterRepository(MeasurementFilterRepository measurementFilterRepository) {
        this.filterRepository = measurementFilterRepository;
    }

    @Override
    @Transactional
    public MeasurementResult saveMeasurement(MeasurementResult result, ITelephonyInfo telephonyInfo) {
        ITelephonyInfo savedTelephonyInfo = telephonyInfoRepository.save(telephonyInfo);
        result.setTelephonyInfo(savedTelephonyInfo);

        return measurementRepository.save(result);
    }

    @Override
    public Optional<MeasurementResult> findMeasurement(long measurementId) {
        return measurementRepository.findById(measurementId);
    }

    @Override
    public FilterMeasurementDto filterMeasurementsWithSettings(
            List<Filtered> filteredMeasurements,
            List<Sorted> sortedMeasurements,
            List<Filtered> filteredTelephonyInfo,
            List<Sorted> sortedTelephonyInfo,
            int page,
            int pageSize,
            List<Long> settingsIds,
            boolean settingsSorted) {
        log.info(TAG, "filter measurements with settings");

        StartEndInteger received = new StartEndInteger(null, null);
        StartEndFloat availableBandwidth = new StartEndFloat(null, null);
        StartEndStringForBigInteger startTime = new StartEndStringForBigInteger(null, null);
        Set<CruspError> errorType = null;
        String errorMessage = null;
        Set<Class<? extends ITelephonyInfo>> types = null;
        StartEndInteger dbm = new StartEndInteger(null, null);
        Set<String> operators = null;
        StartEndDouble lat = new StartEndDouble(null, null);
        StartEndDouble lng = new StartEndDouble(null, null);
        StartEndFloat speed = new StartEndFloat(null, null);
        StartEndInteger asu = new StartEndInteger(null, null);
        String deviceId = null;
        Boolean downlink = null;
        Integer uid = null;
        String manufacturer = null;
        String model = null;

        try {
            for (Filtered filtered : filteredMeasurements) {
                String key = filtered.getId();
                String value = filtered.getValue();

                switch (key) {
                    case MeasurementResult_.START_TIME:
                        startTime = getStartEndString(value);
                        break;
                    case MeasurementResult_.NUM_RECEIVED_PACKETS:
                        received = getStartEndInteger(value);
                        break;
                    case MeasurementResult_.AVAILABLE_BANDWIDTH:
                        availableBandwidth = getStartEndFloat(value);
                        break;
                    case MeasurementResult_.ERROR_TYPE:
                        errorType = Arrays.stream(value.split(","))
                                .map(String::trim)
                                .map(CruspError::valueOf)
                                .collect(Collectors.toSet());
                        break;
                    case MeasurementResult_.ERROR_MESSAGE:
                        errorMessage = value;
                        break;
                    case MeasurementResult_.UID:
                        uid = Integer.parseInt(value);
                        break;
                    case MeasurementResult_.DOWNLINK:
                        downlink = Boolean.parseBoolean(value);
                }
            }

            for(Filtered filtered: filteredTelephonyInfo) {
                String key = filtered.getId();
                String value = filtered.getValue();

                switch (key) {
                    case "@type":
                       types = Arrays.stream(value.split(","))
                               .map(String::trim)
                               .map((type) -> {
                                   switch (type) {
                                       case "LTE": return TelephonyInfoLTE.class;
                                       case "WCDMA": return TelephonyInfoWcdma.class;
                                       case "CDMA": return TelephonyInfoCdma.class;
                                       case "GSM": return TelephonyInfoGSM.class;
                                       case "WIFI": return TelephonyInfoWifi.class;
                                       case "NR": return TelephonyInfoNR.class;
                                       default: return ITelephonyInfo.class;
                                   }})
                               .collect(Collectors.toSet());
                        break;
                    case ITelephonyInfo_.DBM:
                        dbm = getStartEndInteger(value);
                        break;
                    case ITelephonyInfo_.OPERATOR:
                        operators = Arrays.stream(value.split(","))
                                .map(String::trim)
                                .collect(Collectors.toSet());
                        break;
                    case ITelephonyInfo_.LAT:
                        lat = getStartEndDouble(value);
                        break;
                    case ITelephonyInfo_.LNG:
                        lng = getStartEndDouble(value);
                        break;
                    case ITelephonyInfo_.SPEED:
                        speed = getStartEndFloat(value);
                        break;
                    case ITelephonyInfo_.ASU:
                        asu = getStartEndInteger(value);
                        break;
                    case ITelephonyInfo_.DEVICE_ID:
                        deviceId = value;
                        break;
                    case ITelephonyInfo_.MANUFACTURER:
                        manufacturer = value;
                        break;
                    case ITelephonyInfo_.MODEL:
                        model = model;
                        break;
                }
            }
        } catch (FilterValidationException | NumberFormatException e) {
            log.warn(TAG, "Invalid value in filter");
            return new FilterMeasurementDto(new ArrayList<>(), 0L); //return empty list
        }

        return filterRepository.filterMeasurements(
                uid,
                startTime,
                received,
                availableBandwidth,
                errorType,
                errorMessage,
                types,
                dbm,
                operators,
                lat,
                lng,
                speed,
                asu,
                deviceId,
                downlink,
                manufacturer,
                model,
                settingsIds,
                settingsSorted,
                sortedMeasurements,
                sortedTelephonyInfo,
                page,
                pageSize
        );
    }

    @Override
    public List<MeasurementResult> findDetails(List<Long> ids) {
        Iterable<MeasurementResult> iterable = measurementRepository.findAllById(ids);

        List resultList = new ArrayList<>();
        iterable.forEach(resultList::add);

        return resultList;
    }
}
