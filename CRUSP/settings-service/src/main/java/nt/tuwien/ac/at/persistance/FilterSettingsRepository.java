package nt.tuwien.ac.at.persistance;

import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.CruspSettings_;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class FilterSettingsRepository {
    @PersistenceContext
    private EntityManager em;

    public List<CruspSettings> filterSettings(
            Integer startRepeats,
            Integer endRepeats,
            Integer startVolume,
            Integer endVolume,
            Integer startPacketSize,
            Integer endPacketSize,
            Float startRate,
            Float endRate,
            Integer startSleep,
            Integer endSleep,
            Integer startTimeout,
            Integer endTimeout,
            Map<String, Boolean> orderBy
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CruspSettings> q = cb.createQuery(CruspSettings.class);
        Root<CruspSettings> mr = q.from(CruspSettings.class);

        List<Predicate> appliedPredicates = new ArrayList<>();

        if(startRepeats != null) {
            if(endRepeats != null) {
                appliedPredicates.add(cb.between(mr.get(CruspSettings_.REPEATS), startRepeats, endRepeats));
            } else {
                appliedPredicates.add(cb.equal(mr.get(CruspSettings_.REPEATS), startRepeats));
            }
        }

        if(startVolume != null) {
            if(endVolume != null) {
                appliedPredicates.add(cb.between(mr.get(CruspSettings_.VOLUME), startVolume, endVolume));
            } else {
                appliedPredicates.add(cb.equal(mr.get(CruspSettings_.VOLUME), startVolume));
            }
        }

        if(startPacketSize != null) {
            if(endPacketSize != null) {
                appliedPredicates.add(cb.between(mr.get(CruspSettings_.PACKET_SIZE), startPacketSize, endPacketSize));
            } else {
                appliedPredicates.add(cb.equal(mr.get(CruspSettings_.PACKET_SIZE), startPacketSize));
            }
        }

        if(startRate != null) {
            if(endRate != null) {
                appliedPredicates.add(cb.between(mr.get(CruspSettings_.RATE), startRate, endRate));
            } else {
                appliedPredicates.add(cb.equal(mr.get(CruspSettings_.RATE), startRate));
            }
        }

        if(startSleep != null) {
            if(endSleep != null) {
                appliedPredicates.add(cb.between(mr.get(CruspSettings_.SLEEP), startSleep, endSleep));
            } else {
                appliedPredicates.add(cb.equal(mr.get(CruspSettings_.SLEEP), startSleep));
            }
        }

        if(startTimeout != null) {
            if(endTimeout != null) {
                appliedPredicates.add(cb.between(mr.get(CruspSettings_.TIMEOUT), startTimeout, endTimeout));
            } else {
                appliedPredicates.add(cb.equal(mr.get(CruspSettings_.TIMEOUT), startTimeout));
            }
        }

        List<Order> orderList = new ArrayList<>();
        orderBy.forEach((key, isDesc) -> {
            if(!key.isEmpty()) {
                if(isDesc) {
                    orderList.add(cb.desc(mr.get(key)));
                } else {
                    orderList.add(cb.asc(mr.get(key)));
                }
            }

        });

        TypedQuery<CruspSettings> typedQuery = em.createQuery(q
                .select(mr)
                .where(appliedPredicates.toArray(new Predicate[]{}))
                .orderBy(orderList));

        return typedQuery.getResultList();
    }
}
