package nt.tuwien.ac.at.service;

import lombok.extern.slf4j.Slf4j;
import nt.tuwien.ac.at.model.CruspSettings_;
import nt.tuwien.ac.at.dtos.FilterSettingsRequest;
import nt.tuwien.ac.at.exceptions.FilterValidationException;
import nt.tuwien.ac.at.dtos.filtering.StartEndFloat;
import nt.tuwien.ac.at.dtos.filtering.StartEndInteger;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.dtos.Filtered;
import nt.tuwien.ac.at.dtos.Sorted;
import nt.tuwien.ac.at.persistance.FilterSettingsRepository;
import nt.tuwien.ac.at.persistance.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static nt.tuwien.ac.at.dtos.filtering.StartEndFloat.getStartEndFloat;
import static nt.tuwien.ac.at.dtos.filtering.StartEndInteger.getStartEndInteger;

@Slf4j
@Service
public class PersistServiceImpl implements IPersistService {
    private static final String TAG = PersistServiceImpl.class.getName();
    private SettingsRepository settingsRepository;
    private FilterSettingsRepository filterRepository;

    @Autowired
    public void setSettingsRepository(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Autowired
    public void setFilterSettingsRepository(FilterSettingsRepository filterSettingsRepository) {
        this.filterRepository = filterSettingsRepository;
    }

    @Override
    public CruspSettings saveSettings(CruspSettings settings) {
        Optional<CruspSettings> foundSetting = settingsRepository.readBySetting(settings);

        return foundSetting.orElseGet(() -> settingsRepository.save(settings));
    }

    @Override
    @Transactional
    public CruspSettings saveStandardSettings(CruspSettings settings) {
        //1. change all others to not standard
        this.findStandardSettings().ifPresent((oldSettings) -> {
            oldSettings.setStandard(false);
            this.settingsRepository.save(oldSettings);
        });

        //2. check if current setting exists
        Optional<CruspSettings> settingsOptional = settingsRepository.readBySetting(settings);
        if(settingsOptional.isPresent()) {
            settings = settingsOptional.get();
        }

        //3. set standard to true and save it
        settings.setStandard(true);
        return this.settingsRepository.save(settings);
    }

    @Override
    public Optional<CruspSettings> findSettingsById(long settingsId) {
        return settingsRepository.findById(settingsId);
    }

    @Override
    public Optional<CruspSettings> findSettingsByFields(CruspSettings settings) {
        return settingsRepository.readBySetting(settings);
    }

    @Override
    public Optional<CruspSettings> findStandardSettings() {
        return settingsRepository.findByStandardIsTrue();
    }

    @Override
    public List<CruspSettings> filterSettings(FilterSettingsRequest request) {
        log.info(TAG, "filter settings");
        StartEndInteger repeats = new StartEndInteger(null, null);
        StartEndInteger volume = new StartEndInteger(null, null);
        StartEndInteger packetSize = new StartEndInteger(null, null);
        StartEndFloat rate = new StartEndFloat(null, null);
        StartEndInteger sleep = new StartEndInteger(null, null);
        StartEndInteger timeout = new StartEndInteger(null, null);

        try {
            for (Filtered filtered : request.filtered) {
                switch (filtered.getId()) {
                    case "repeats":
                        repeats = getStartEndInteger(filtered.getValue());
                        break;
                    case "volume":
                        volume = getStartEndInteger(filtered.getValue());
                        break;
                    case "packetSize":
                        packetSize = getStartEndInteger(filtered.getValue());
                        break;
                    case "rate":
                        rate = getStartEndFloat(filtered.getValue());
                        break;
                    case "sleep":
                        sleep = getStartEndInteger(filtered.getValue());
                        break;
                    case "timeout":
                        timeout = getStartEndInteger(filtered.getValue());
                        break;
                }
            }

            boolean invalidKey = request.sorted.stream()
                    .anyMatch(sorted -> !sorted.getId().matches("^[a-z|A-Z|\\d|\\.|_]+$"));

            if(invalidKey) {
                log.warn(TAG, "Invalid table-key for validation");
                return new ArrayList<>();
            }

            Map<String, Boolean> orderBy = request.sorted.stream()
                    .map(item -> {
                        switch (item.getId()) {
                            case "repeats":
                                return new Sorted(CruspSettings_.REPEATS, item.isDesc());
                            case "volume":
                                return new Sorted(CruspSettings_.VOLUME, item.isDesc());
                            case "packetSize":
                                return new Sorted(CruspSettings_.PACKET_SIZE, item.isDesc());
                            case "rate":
                                return new Sorted(CruspSettings_.RATE, item.isDesc());
                            case "sleep":
                                return new Sorted(CruspSettings_.SLEEP, item.isDesc());
                            case "timeout":
                                return new Sorted(CruspSettings_.TIMEOUT, item.isDesc());
                        }
                        return new Sorted("", false);
                    })
                    .collect(Collectors.toMap(Sorted::getId, Sorted::isDesc));

            return filterRepository.filterSettings(repeats.getStart(),
                    repeats.getEnd(),
                    volume.getStart(),
                    volume.getEnd(),
                    packetSize.getStart(),
                    packetSize.getEnd(),
                    rate.getStart(),
                    rate.getEnd(),
                    sleep.getStart(),
                    sleep.getEnd(),
                    timeout.getStart(),
                    timeout.getEnd(),
                    orderBy);
        } catch (FilterValidationException e) {
            log.warn(TAG, "Invalid value in filter");
            return new ArrayList<>(); //return empty list
        }

    }

    @Override
    public List<CruspSettings> readForIds(List<Long> request) {
        return settingsRepository.readByIds(request);
    }
}
