package nt.tuwien.ac.at.service;

import nt.tuwien.ac.at.dtos.FilterSettingsRequest;
import nt.tuwien.ac.at.model.CruspSettings;

import java.util.List;
import java.util.Optional;

public interface IPersistService {
    CruspSettings saveSettings(CruspSettings settings);
    CruspSettings saveStandardSettings(CruspSettings settings);
    Optional<CruspSettings> findSettingsById(long settingsId); //finds settings by id
    Optional<CruspSettings> findSettingsByFields(CruspSettings settings); // finds settings by fields except ID and STANDARD
    Optional<CruspSettings> findStandardSettings();

    List<CruspSettings> filterSettings(FilterSettingsRequest request);

    List<CruspSettings> readForIds(List<Long> request);
}
