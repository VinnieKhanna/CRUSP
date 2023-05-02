package nt.tuwien.ac.at.controller;

import lombok.extern.slf4j.Slf4j;
import nt.tuwien.ac.at.exceptions.MeasurementNotFoundException;
import nt.tuwien.ac.at.exceptions.SettingsServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class RestExceptionController {

    @ExceptionHandler(MeasurementNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody
    Map<String, Object> handleMeasurementNotFoundException(MeasurementNotFoundException _e) {
        log.info("Measurement with id was not found");

        Map<String, Object> res = new HashMap<>();
        res.put("error", "measurement not found");

        return res;
    }

    @ExceptionHandler(SettingsServiceException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public @ResponseBody
    Map<String, Object> handleMeasurementNotFoundException(SettingsServiceException _e) {
        log.info("Settings-service could not handle request");

        Map<String, Object> res = new HashMap<>();
        res.put("error", "settings-service could not handle requerst");

        return res;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody Map<String, Object> handleValidationException(ValidationException _e) {
        log.info("Failed to validate request");

        Map<String, Object> res = new HashMap<>();
        res.put("error", "validating request failed");

        return res;
    }
}
