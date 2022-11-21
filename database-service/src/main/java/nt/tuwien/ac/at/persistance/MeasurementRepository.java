package nt.tuwien.ac.at.persistance;

import nt.tuwien.ac.at.model.MeasurementResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementRepository extends CrudRepository<MeasurementResult, Long> {
}
