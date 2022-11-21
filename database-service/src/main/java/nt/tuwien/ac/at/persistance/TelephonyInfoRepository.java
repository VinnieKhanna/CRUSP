package nt.tuwien.ac.at.persistance;

import nt.tuwien.ac.at.model.network.ITelephonyInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelephonyInfoRepository extends CrudRepository<ITelephonyInfo, Long> {
}
