package nt.tuwien.ac.at.controller;

import lombok.extern.slf4j.Slf4j;
import nt.tuwien.ac.at.controller.request.DetailsRequest;
import nt.tuwien.ac.at.controller.request.FilterMeasurementRequest;
import nt.tuwien.ac.at.controller.request.SaveMeasurementRequest;
import nt.tuwien.ac.at.controller.response.FilterMeasurementResponse;
import nt.tuwien.ac.at.controller.response.MeasurementResponse;
import nt.tuwien.ac.at.exceptions.MeasurementNotFoundException;
import nt.tuwien.ac.at.exceptions.SettingsServiceException;
import nt.tuwien.ac.at.mapper.MeasurementResultMapper;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.dtos.Filtered;
import nt.tuwien.ac.at.model.MeasurementResult;
import nt.tuwien.ac.at.dtos.Sorted;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;
import nt.tuwien.ac.at.dtos.FilterSettingsRequest;
import nt.tuwien.ac.at.service.FilterMeasurementDto;
import nt.tuwien.ac.at.service.IPersistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

import static nt.tuwien.ac.at.mapper.KeyToMetamodel.getMetamodelForKey;

@Controller
@Slf4j
@RequestMapping(path = "/v1/")
public class DatabaseController {
    private static final String TAG = DatabaseController.class.getName();
    private IPersistService measurementService;
    private SettingsServiceFeignClient proxySettingsService;

    @Autowired
    public void setMeasurementService(IPersistService measurementService) {
        this.measurementService = measurementService;
    }

    @Autowired
    public void setProxySettingsService(SettingsServiceFeignClient proxySettingsService) {
        this.proxySettingsService = proxySettingsService;
    }

    /**
     * returns all measurements without packets and sequences
     * @return List of all measurements
     */
    @PostMapping("/measurement/filter")
    @ResponseBody
    public FilterMeasurementResponse filterMeasurements(@Valid @RequestBody FilterMeasurementRequest request, BindingResult bindingResult) {
        log.info("POST /measurement/filter");

        if(bindingResult.hasErrors()) {
            throw new ValidationException();
        }

        FilterMeasurementDto results;
        Map<Long, CruspSettings> settingsMap;

        boolean invalidKeyOrValueFiltered = request.filtered.stream()
                .anyMatch(filtered -> !filtered.getId().matches("^[a-z|A-Z|\\d|\\.|@|_]+$")
                || !filtered.getValue().matches("^[a-z|A-Z|\\d|_|\\-|\\:|\\.|,|\\s]+$"));

        boolean invalidKeySorted = request.sorted.stream()
                .anyMatch(sorted -> !sorted.getId().matches("^^[a-z|A-Z|\\d|\\.|@|_]+$"));

        if(invalidKeyOrValueFiltered || invalidKeySorted) {
            log.warn(TAG, "Invalid table-key for validation");
            return FilterMeasurementResponse.builder()
                    .data(new ArrayList<>())
                    .count(0)
                    .pageSize(request.pageSize)
                    .page(request.page)
                    .build();
        }

        List<Filtered> filteredMeasurements = request.filtered.stream() // all keys without "telephonyInfo." or "settings." at the start
                .filter(filtered -> !filtered.getId().startsWith("telephonyInfo.") && !filtered.getId().startsWith("settings."))
                .map(filtered -> Filtered.builder()
                        .id(getMetamodelForKey(filtered.getId()))
                        .value(filtered.getValue())
                        .build())
                .collect(Collectors.toList());

        List<Filtered> filteredTelephonyInfo = request.filtered.stream() // all keys without "telephonyInfo." at the start
                .filter(filtered -> filtered.getId().startsWith("telephonyInfo."))
                .map(filtered -> Filtered.builder()
                        .id(getMetamodelForKey(filtered.getId()))
                        .value(filtered.getValue())
                        .build()) // cut the string "telephonyInfo."
                .collect(Collectors.toList());

        List<Filtered> filterSettings = request.filtered.stream()  // all keys with "settings." at the start
                .filter(filtered -> filtered.getId().startsWith("settings.")) //get all settings
                .map(filtered -> Filtered.builder() //filter out "settings." string at the beginning of id
                        .id(filtered.getId().substring(9)) // cut the string "settings."
                        .value(filtered.getValue())
                        .build())
                .collect(Collectors.toList());

        List<Sorted> sortedMeasurements = request.sorted.stream()  // all keys without "telephonyInfo." or "settings." at the start
                .filter(sorted -> !sorted.getId().startsWith("telephonyInfo.") && !sorted.getId().startsWith("settings."))
                .map(filtered -> new Sorted(getMetamodelForKey(filtered.getId()), filtered.isDesc()))
                .collect(Collectors.toList());

        List<Sorted> sortedTelephonyInfo = request.sorted.stream() // all keys without "telephonyInfo." at the start
                .filter(sorted -> sorted.getId().startsWith("telephonyInfo."))
                .map(filtered -> new Sorted(getMetamodelForKey(filtered.getId()), filtered.isDesc()))
                .collect(Collectors.toList());

        List<Sorted> sortedSettings = request.sorted.stream() // all keys with "settings." at the start
                .filter(sorted -> sorted.getId().startsWith("settings."))
                .map(sorted -> Sorted.builder() //filter out "settings." string at the beginning of id
                        .id(sorted.getId().substring(9))
                        .desc(sorted.isDesc())
                        .build())
                .collect(Collectors.toList());

        // 1. build request for settings-service (FilterSettingsRequest) based on the received FilterMeasurementRequest
        FilterSettingsRequest settingsRequest = FilterSettingsRequest.builder()
                .filtered(filterSettings)
                .sorted(sortedSettings)
                .build();

        // Check if a request to the settings-service is needed (not needed if filtered and sorted are empty)
        if(!settingsRequest.filtered.isEmpty() || !settingsRequest.sorted.isEmpty()) {
            // 2. send settings-request to settings-service
            List<CruspSettings> settingsList = proxySettingsService.filterSettings(settingsRequest);

            if(settingsList.isEmpty()) {
                return FilterMeasurementResponse.builder()
                        .count(0)
                        .data(new ArrayList<>())
                        .page(0) // current page -1
                        .pageSize(request.pageSize)
                        .build(); //skip further queries and return empty list
            }

            // 3. create map with settings-id and corresponding settings
            settingsMap = settingsList.stream()
                    .collect(Collectors.toMap(CruspSettings::getUid, elem -> elem));

            List<Long> settingsIds = settingsList.stream()
                    .map(CruspSettings::getUid)
                    .collect(Collectors.toList());;

            // 4. get all matching measurements
            results = measurementService.filterMeasurementsWithSettings(filteredMeasurements, sortedMeasurements, filteredTelephonyInfo, sortedTelephonyInfo, request.page, request.pageSize, settingsIds, !settingsRequest.sorted.isEmpty());
        } else { //if filter and sorted for settings are irrelevant, first query the matching measurements and then the settings for it
            // 2. get all matching measurements
            results = measurementService.filterMeasurementsWithSettings(filteredMeasurements, sortedMeasurements, filteredTelephonyInfo, sortedTelephonyInfo, request.page, request.pageSize, null, false);

            if(results.getResults().isEmpty()){ // handle empty list
                return FilterMeasurementResponse.builder()
                        .count(0)
                        .data(new ArrayList<>())
                        .page(0) // current page -1
                        .pageSize(request.pageSize)
                        .build();
            }

            // 3. send settings-request to settings-service
            Set<Long> settingsIds = results.getResults().stream()
                    .map(MeasurementResult::getSettingsId)
                    .collect(Collectors.toSet());

            List<CruspSettings> settingsList = proxySettingsService.getSettingsForIds(settingsIds);

            // 4. create map with settings-id and corresponding settings
            settingsMap = settingsList.stream()
                    .collect(Collectors.toMap(CruspSettings::getUid, elem -> elem));
        }

        // 5. add the settings to the matching results
        return FilterMeasurementResponse.builder()
                .count(results.getCount())
                .data(results.getResults().stream()
                        .map(foundResult -> MeasurementResultMapper.INSTANCE.toResponseLazy(foundResult, settingsMap.getOrDefault(foundResult.getSettingsId(), null)))
                        .collect(Collectors.toList()))
                .page(request.page)
                .pageSize(request.pageSize)
                .build();
    }

    /**
     * returns all measurements with packets and sequences for given IDs
     * @return List of measurements with given IDs
     */
    @PostMapping("/measurement/details")
    @ResponseBody
    public List<MeasurementResponse> detailsForMeasurements(@Valid @RequestBody DetailsRequest request, BindingResult bindingResult) {
        log.info("POST /measurement/details");

        if(bindingResult.hasErrors()) {
            throw new ValidationException();
        }

        List<MeasurementResult> foundResults = measurementService.findDetails(request.ids);

        Set<Long> neededIds = foundResults.stream()
                .map(MeasurementResult::getSettingsId)
                .collect(Collectors.toSet());
        List<CruspSettings> settingsList = proxySettingsService.getSettingsForIds(neededIds);

        Map<Long, CruspSettings> settingsMap = settingsList.stream()
                .collect(Collectors.toMap(CruspSettings::getUid, elem -> elem));

        return foundResults.stream()
                .map(result -> MeasurementResultMapper.INSTANCE.toResponse(result, settingsMap.getOrDefault(result.getSettingsId(), null)))
                .collect(Collectors.toList());
    }

    @GetMapping("/measurement/{id}")
    @ResponseBody
    public MeasurementResponse findMeasurement(@PathVariable("id") long measurementId) {
        log.info("GET /measurement/");

        if(measurementId == 0) {
            throw new MeasurementNotFoundException(measurementId);
        }

        Optional<MeasurementResult> result = measurementService.findMeasurement(measurementId);

        if(result.isPresent()) {
            CruspSettings settings = proxySettingsService.getSettings(result.get().getSettingsId());

            if(settings != null) {
                return MeasurementResultMapper.INSTANCE.toResponse(result.get(), settings);
            } else {
                throw new SettingsServiceException(result.get().getSettingsId());
            }
        } else {
            throw new MeasurementNotFoundException(measurementId);
        }
    }

    @PostMapping("measurement/save")
    @ResponseBody
    public long saveMeasurement(@Valid @RequestBody SaveMeasurementRequest request, BindingResult bindingResult) {
        log.info("POST /measurement/save");

        if(bindingResult.hasErrors()) {
            throw new ValidationException();
        }

        MeasurementResult result = MeasurementResultMapper.INSTANCE.toMeasurementDto(request);
        CruspSettings settings = MeasurementResultMapper.INSTANCE.toSettingsDto(request);
        ITelephonyInfo telephonyInfo = MeasurementResultMapper.INSTANCE.toTelephonyInfoDto(request);

        long settingsId = proxySettingsService.saveSettings(settings);

        if(settingsId <= 0) {
            throw new SettingsServiceException(settingsId);
        } else {
            result.setSettingsId(settingsId);
        }

        MeasurementResult ret = measurementService.saveMeasurement(result, telephonyInfo);

        return ret.getUid();
    }
}
