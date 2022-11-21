package nt.tuwien.ac.at.persistance;

import lombok.extern.slf4j.Slf4j;
import nt.tuwien.ac.at.model.CruspError;
import nt.tuwien.ac.at.model.MeasurementResult;
import nt.tuwien.ac.at.model.MeasurementResult_;
import nt.tuwien.ac.at.dtos.Sorted;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;
import nt.tuwien.ac.at.model.network.ITelephonyInfo_;
import nt.tuwien.ac.at.model.network.TelephonyInfoLTE_;
import nt.tuwien.ac.at.dtos.filtering.StartEndDouble;
import nt.tuwien.ac.at.dtos.filtering.StartEndFloat;
import nt.tuwien.ac.at.dtos.filtering.StartEndInteger;
import nt.tuwien.ac.at.dtos.filtering.StartEndStringForBigInteger;
import nt.tuwien.ac.at.service.FilterMeasurementDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Repository
public class MeasurementFilterRepository {
    private static final String TAG = MeasurementFilterRepository.class.getName();

    @PersistenceContext
    private EntityManager em;

    public FilterMeasurementDto filterMeasurements(
            Integer uid,
            StartEndStringForBigInteger startTime,
            StartEndInteger numReceivedPackets,
            StartEndFloat availableBandwidth,
            Set<CruspError> errorType,
            String errorMessage,

            Set<Class<? extends ITelephonyInfo>> types,
            StartEndInteger dbm,
            Set<String> operators,

            StartEndDouble lat,
            StartEndDouble lng,
            StartEndFloat speed,
            StartEndInteger asu,
            String deviceId,
            Boolean downlink,
            String manufacturer,
            String model,

            List<Long> settingsIds,
            boolean sortSettings,

            List<Sorted> sortedMeasurements,
            List<Sorted> sortedTelephonyInfo,
            int page,
            int pageSize
    ) {
        // check if sorted contains type
        SortType sortType = SortType.NONE;
        Iterator<Sorted> i = sortedTelephonyInfo.iterator();
        while (i.hasNext()) {
            Sorted sorted = i.next();
            if(sorted.getId().equals("@type")) {
                sortType = sorted.isDesc() ? SortType.DESC : SortType.ASC;
                i.remove();
                break;
            }
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<MeasurementResult> q = cb.createQuery(MeasurementResult.class);
        Root<MeasurementResult> mr = q.from(MeasurementResult.class);
        Join<MeasurementResult, ITelephonyInfo> tiJ = mr.join(MeasurementResult_.telephonyInfo, JoinType.LEFT);

        List<Predicate> appliedPredicates = new ArrayList<>();

        if(uid != null) {
            appliedPredicates.add(cb.equal(mr.get(MeasurementResult_.UID), uid));
        }

        if(downlink != null) {
            appliedPredicates.add(cb.equal(mr.get(MeasurementResult_.DOWNLINK), downlink));
        }

        if(types != null) {
            appliedPredicates.add(tiJ.type().in(types));
        }

        if(startTime.getStart() != null) {
            if(startTime.getEnd() != null) {
                appliedPredicates.add(cb.between(mr.get(MeasurementResult_.START_TIME), new BigInteger(startTime.getStart()), new BigInteger(startTime.getEnd())));
            }
        }

        if(numReceivedPackets.getStart() != null) {
            if(numReceivedPackets.getEnd() != null) {
                appliedPredicates.add(cb.between(mr.get(MeasurementResult_.NUM_RECEIVED_PACKETS), numReceivedPackets.getStart(), numReceivedPackets.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(mr.get(MeasurementResult_.NUM_RECEIVED_PACKETS), numReceivedPackets.getStart()));
            }
        }

        if(availableBandwidth.getStart() != null) {
            if(availableBandwidth.getEnd() != null) {
                appliedPredicates.add(cb.between(mr.get(MeasurementResult_.AVAILABLE_BANDWIDTH), availableBandwidth.getStart(), availableBandwidth.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(mr.get(MeasurementResult_.AVAILABLE_BANDWIDTH), startTime.getStart()));
            }
        }

        if(errorType != null) {
            appliedPredicates.add(mr.get(MeasurementResult_.ERROR_TYPE).in(errorType));
        }

        if(errorMessage != null) {
            appliedPredicates.add(cb.like(mr.get(MeasurementResult_.ERROR_MESSAGE), errorMessage));
        }

        if(settingsIds != null) {
            appliedPredicates.add(mr.get(MeasurementResult_.SETTINGS_ID).in(settingsIds));
        }

        if(dbm.getStart() != null) {
            if(dbm.getEnd() != null) {
                appliedPredicates.add(cb.between(tiJ.get(TelephonyInfoLTE_.DBM), dbm.getStart(), dbm.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(tiJ.get(TelephonyInfoLTE_.DBM), dbm.getStart()));
            }
        }

        if(lat.getStart() != null) {
            if(lat.getEnd() != null) {
                appliedPredicates.add(cb.between(tiJ.get(ITelephonyInfo_.LAT), lat.getStart(), lat.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.LAT), lat.getStart()));
            }
        }

        if(lng.getStart() != null) {
            if(lng.getEnd() != null) {
                appliedPredicates.add(cb.between(tiJ.get(ITelephonyInfo_.LNG), lng.getStart(), lng.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.LNG), lng.getStart()));
            }
        }

        if(speed.getStart() != null) {
            if(speed.getEnd() != null) {
                appliedPredicates.add(cb.between(tiJ.get(ITelephonyInfo_.SPEED), speed.getStart(), speed.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.SPEED), speed.getStart()));
            }
        }

        if(asu.getStart() != null) {
            if(asu.getEnd() != null) {
                appliedPredicates.add(cb.between(tiJ.get(ITelephonyInfo_.ASU), asu.getStart(), asu.getEnd()));
            } else {
                appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.ASU), asu.getStart()));
            }
        }

        if(operators != null) {
            appliedPredicates.add(tiJ.get(ITelephonyInfo_.OPERATOR).in(operators));
        }

        if(deviceId != null) {
            appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.DEVICE_ID), deviceId));
        }

        if(manufacturer != null) {
            appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.MANUFACTURER), manufacturer));
        }

        if(model != null) {
            appliedPredicates.add(cb.equal(tiJ.get(ITelephonyInfo_.MODEL), model));
        }

        List<Order> orderList = new ArrayList<>();
        sortedMeasurements.forEach((sorted) -> orderList.add(sorted.isDesc()? cb.desc(mr.get(sorted.getId())) : cb.asc(mr.get(sorted.getId()))));
        sortedTelephonyInfo.forEach((sorted) -> orderList.add(sorted.isDesc()? cb.desc(tiJ.get(sorted.getId())) : cb.asc(tiJ.get(sorted.getId()))));

        TypedQuery<MeasurementResult> selectQuery = em.createQuery(q
                .select(mr)
                .where(appliedPredicates.toArray(new Predicate[]{}))
                .orderBy(orderList));

        selectQuery.setFirstResult(page * pageSize);
        selectQuery.setMaxResults(pageSize);

        CriteriaQuery<Long> c = cb.createQuery(Long.class);
        Root<MeasurementResult> mrC = c.from(MeasurementResult.class);
        mrC.join(MeasurementResult_.telephonyInfo, JoinType.LEFT);

        long count = em.createQuery(c
                .select(cb.count(mrC))
                .where(appliedPredicates.toArray(new Predicate[]{})))
                .getSingleResult();

        List<MeasurementResult> measurementResults = selectQuery.getResultList();

        if(!sortType.equals(SortType.NONE)) { //sort measurements by type
            if(sortType.equals(SortType.DESC)) {
                measurementResults.sort((measurementResult, t1) -> {
                    if (measurementResult.getTelephonyInfo() == null) { return -1; }
                    if (t1.getTelephonyInfo() == null) { return 1; }
                    return measurementResult.getTelephonyInfo().getClass().toString().compareTo(
                            t1.getTelephonyInfo().getClass().toString());
                });
            } else {
                measurementResults.sort((measurementResult, t1) -> {
                    if (measurementResult.getTelephonyInfo() == null) { return 1; }
                    if (t1.getTelephonyInfo() == null) { return -1; }
                    return measurementResult.getTelephonyInfo().getClass().toString().compareTo(
                            t1.getTelephonyInfo().getClass().toString());
                });
            }
        }

        if(sortSettings) { //sort by order of given setting IDs
            measurementResults.sort(Comparator.comparingInt(m -> settingsIds.indexOf(m.getSettingsId())));
        }

        return new FilterMeasurementDto(measurementResults, count);
    }

    private enum SortType {
        ASC,
        DESC,
        NONE
    }
}
