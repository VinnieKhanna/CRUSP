package nt.tuwien.ac.at.controller;

import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.dtos.FilterSettingsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;

@FeignClient(name = "settings-service")
public interface SettingsServiceFeignClient {
    @RequestMapping("/v1/save")
    long saveSettings(CruspSettings request);

    @RequestMapping("/v1/read/{id}")
    CruspSettings getSettings(@PathVariable("id") long id);

    @RequestMapping("/v1/filter")
    List<CruspSettings> filterSettings(FilterSettingsRequest request);

    @RequestMapping("/v1/read/ids")
    List<CruspSettings> getSettingsForIds(Set<Long> settingsIds);
}
