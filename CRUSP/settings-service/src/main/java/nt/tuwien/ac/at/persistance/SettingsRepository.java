package nt.tuwien.ac.at.persistance;

import nt.tuwien.ac.at.model.CruspSettings;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends CrudRepository<CruspSettings, Long> {
    /**
     * Checks all parameters except id and default
     * @param settings is a CruspSetting
     * @return found setting
     */
    default Optional<CruspSettings> readBySetting(CruspSettings settings) { //without ID
        return findByPacketSizeAndRateAndRepeatsAndSleepAndTimeoutAndVolume(settings.getPacketSize(), settings.getRate(), settings.getRepeats(), settings.getSleep(), settings.getTimeout(), settings.getVolume());
    }

    Optional<CruspSettings> findByPacketSizeAndRateAndRepeatsAndSleepAndTimeoutAndVolume(
            int packetSize, float rate, int repeats, int sleep, int timeout, int volume);

    Optional<CruspSettings> findByStandardIsTrue();

    @Query(value = "SELECT c from CruspSettings c WHERE c.uid in ?1")
    List<CruspSettings> readByIds(List<Long> ids);
}
