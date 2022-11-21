package nt.tuwien.ac.at.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import nt.tuwien.ac.at.dtos.FilterSettingsRequest;
import nt.tuwien.ac.at.exceptions.SettingsNotFoundException;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.CruspToken;
import nt.tuwien.ac.at.service.IPersistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequestMapping(path = "/v1/")
public class SettingsController {
    private IPersistService persistService;

    @Autowired
    public void setPersistService(IPersistService persistService) {
        this.persistService = persistService;
    }

    /**
     * Gets the CruspSetting for provided id
     * @param id of CruspSetting
     * @return the found CruspSetting, otherwise status 404
     */
    @GetMapping("/read/{id}")
    @ResponseBody
    public CruspSettings getSettings(@PathVariable("id") long id) {
        log.info("GET /settings/");

        Optional<CruspSettings> foundSettings = persistService.findSettingsById(id);

        if(foundSettings.isPresent()) {
            return foundSettings.get();
        } else {
            throw new SettingsNotFoundException(id);
        }
    }

    /**
     * Gets the current standard setting
     * @return the standard CruspSetting, otherwise status 404
     */
    @GetMapping("/standard")
    @ResponseBody
    public CruspSettings getStandardSettings() {
        log.debug("GET /settings/standard");

        Optional<CruspSettings> foundSettings = persistService.findStandardSettings();

        if(foundSettings.isPresent()) {
            return foundSettings.get();
        } else {
            throw new SettingsNotFoundException(null);
        }
    }

    /**
     * Saves the provides settings. If the standard-field is true, it will save the settings as a new standard
     * @param request is a Object containing the CruspSettings as well as information about standard settings
     * @param bindingResult for validation
     * @return the id of the saved setting, status 400 on a validation error
     */
    @PostMapping("/save")
    @ResponseBody
    public long saveSettings(@Valid @RequestBody CruspSettings request, BindingResult bindingResult) {
        log.info("POST /settings/save");

        if(bindingResult.hasErrors()) {
            throw new ValidationException();
        }

        CruspSettings ret;

        if(request.isStandard()) {
            ret = persistService.saveStandardSettings(request);
        } else {
            ret = persistService.saveSettings(request);
        }
        return ret.getUid();
    }

    /**
     * Verifies if the provided settings are valid and in the database
     * @param request is a Object containing the CruspSettings
     * @param bindingResult for validation
     * @return true if settings are valid, otherwise false
     */
    @PostMapping("/verify")
    @ResponseBody
    public boolean verifyToken(@Valid @RequestBody CruspToken request, BindingResult bindingResult) {
        log.info("POST /settings/verify");

        return !bindingResult.hasErrors() && persistService.findSettingsByFields(CruspSettings.from(request)).isPresent();
    }

    /**
     * This endpoint provides the client with crusp settings for its measurement.
     * If the sent CruspSettings are custom-made, validate and save them, otherwise provided the current standard settings.
     * If validation is successful, the provided settings are returned, otherwise BAD_REQUEST will be returned
     * @param request is a Object containing the CruspSettings as well as information about standard settings
     * @param bindingResult for validation
     * @return the CruspSettings to use, otherwise status 404 if settings not found or status 400 if validation fails
     */
    @PostMapping("/measurement")
    @ResponseBody
    public CruspToken getTokenForMeasurement(@Valid @RequestBody CruspSettings request, BindingResult bindingResult) {
        log.info("POST /settings/measurement");

        if(request.isStandard()) { //standard settings
            log.info("getting standard settings...");
            Optional<CruspSettings> cruspSettingsOptional = persistService.findStandardSettings();
            if(cruspSettingsOptional.isPresent()) {
                return CruspToken.from(cruspSettingsOptional.get());
            } else {
                throw new SettingsNotFoundException(null);
            }
        } else { //validate settings
            log.info("validating settings...");
            if (bindingResult.hasErrors()) { // validation
                throw new ValidationException();
            }

            Optional<CruspSettings> foundSettingsOptional = persistService.findSettingsByFields(request);

            if(foundSettingsOptional.isPresent()) { //if settings already exist in Database
                return CruspToken.from(foundSettingsOptional.get());
            } else { // save settings
                if(request.isStandard()) {
                    request.setStandard(false);
                }

                return CruspToken.from(persistService.saveSettings(request));
            }
        }
    }

    @PostMapping("/filter")
    @ResponseBody
    public List<CruspSettings> filterSettings(@Valid @RequestBody FilterSettingsRequest request, BindingResult bindingResult) {
        log.info("POST /settings/filter");

        if(bindingResult.hasErrors()) {
            throw new ValidationException();
        }

        return persistService.filterSettings(request);
    }

    @PostMapping("/read/ids")
    @ResponseBody
    public List<CruspSettings> readSettingsForIds(@Valid @RequestBody List<Long> request) {
        log.info("POST /read/ids");

        if(request.isEmpty()) {
            return new ArrayList<>(); //return empty list
        }

        return persistService.readForIds(request);
    }
}
