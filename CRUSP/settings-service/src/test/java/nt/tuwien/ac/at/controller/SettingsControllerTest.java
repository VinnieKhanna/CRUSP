package nt.tuwien.ac.at.controller;

import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.CruspToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SettingsControllerTest {
    @Autowired
    private SettingsController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    /**
     * Preconditions: No standard-setting is persisted
     * Therefore must run as first test or as a single test
     */
    /*@Test
    public void getSettingsForMeasurementShouldNotFindAnyStandardSettings() throws Exception {
        String urlRead = "http://localhost:" + port + "/settings/measurement";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestJsonStandard2, headers); //make request with standard-field set to true
        ResponseEntity<CruspSettings> result = this.restTemplate.postForEntity(urlRead, entity, CruspSettings.class);

        assertNotNull(result);
        assertEquals(404, result.getStatusCodeValue()); // not found error
    }*/

    @Test
    public void saveSettingsShouldWork() throws Exception {
        assertNotNull(controller);

        saveSettings(TestData.requestJsonNotStandard);
    }

    private long saveSettings(String requestJson) throws Exception{
        String urlSave = "http://localhost:" + port + "/v1/save";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<Long> result = this.restTemplate.postForEntity(urlSave, entity, Long.class);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() > 0L); //ID needs to be > 0 to verify that data is saved in a database

        return result.getBody();
    }

    /**
     * Precondition: save settings must work correctly
     */
    @Test
    public void getSettingsShouldReturnCorrectSetting() throws Exception {
        assertNotNull(controller);

        long savedId = saveSettings(TestData.requestJsonNotStandard);

        CruspSettings foundSettings = readSettingsById(savedId);

        assertEquals(foundSettings.getUid(), savedId);
        assertEquals(foundSettings.getPacketSize(), 6200);
        assertEquals(foundSettings.getRepeats(), 2);
        assertEquals(foundSettings.getVolume(), 58);
        assertEquals(foundSettings.getRate(), 150, 0.0000001);
        assertEquals(foundSettings.getSleep(), 200);
        assertEquals(foundSettings.getTimeout(), 100);
        assertFalse(foundSettings.isStandard());
    }

    @Test
    public void saveMeasurementWithSameSettingsShouldReturnOneSetting() throws Exception {
        assertNotNull(controller);

        long savedId1 = saveSettings(TestData.requestJsonStandard1);
        long savedId2 = saveSettings(TestData.requestJsonStandard1);

        assertEquals(savedId1, savedId2);
    }

    /**
     * Precondition: readSettings must work correctly
     */
    @Test
    public void saveStandardSettingsShouldWork() throws Exception {
        assertNotNull(controller);

        long id = saveSettings(TestData.requestJsonStandard1);
        CruspSettings foundSettings = readSettingsById(id);

        assertTrue(foundSettings.isStandard());
    }

    /*
    read settings should work
     */
    @Test
    public void addNewStandardSettingsShouldWork() throws Exception {
        assertNotNull(controller);

        long id1 = saveSettings(TestData.requestJsonStandard1);
        long id2 = saveSettings(TestData.requestJsonStandard2);

        CruspSettings foundSetting1 = readSettingsById(id1);
        CruspSettings foundSetting2 = readSettingsById(id2);

        assertFalse(foundSetting1.isStandard());
        assertTrue(foundSetting2.isStandard());
    }

    /**
     * Precondition: save standard settings must work correctly
     */
    @Test
    public void getStandardSettingsShouldReturnCorrectSetting() throws Exception {
        assertNotNull(controller);

        long savedId = saveSettings(TestData.requestJsonStandard1);

        CruspSettings foundSettings = readStandardSettings();

        assertEquals(foundSettings.getUid(), savedId);
        assertEquals(foundSettings.getPacketSize(), 6200);
        assertEquals(foundSettings.getRepeats(), 1);
        assertEquals(foundSettings.getVolume(), 58);
        assertEquals(foundSettings.getRate(), 150, 0.0000001);
        assertEquals(foundSettings.getSleep(), 200);
        assertEquals(foundSettings.getTimeout(), 100);
        assertTrue(foundSettings.isStandard());
    }

    @Test
    public void getTokenForMeasurementShouldReturnStandardSettings() throws Exception {
        saveSettings(TestData.requestJsonStandard1); // set this object as standard CruspSetting

        String urlRead = "http://localhost:" + port + "/v1/measurement";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestJsonStandard2, headers); //make request with standard-field set to true
        ResponseEntity<CruspToken> result = this.restTemplate.postForEntity(urlRead, entity, CruspToken.class);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getRepeats());
        assertEquals(58, result.getBody().getVolume());
        assertEquals(6200, result.getBody().getPacketSize());
        assertEquals(150, result.getBody().getRate(), 0.0000001);
        assertEquals(200, result.getBody().getSleep());
        assertEquals(100, result.getBody().getTimeout());
    }

    /**
     * The standard field is interpreted as false on null by default
     */
    @Test
    public void getTokenForMeasurementShouldGetStandardWithMissingStandardValue() throws Exception {
        saveSettings(TestData.requestJsonStandard1); // set this object as standard CruspSetting

        String urlRead = "http://localhost:" + port + "/v1/measurement";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //make request with standard-field set to null, otherwise the same as requestJsonStandard1
        HttpEntity<String> entity = new HttpEntity<>(TestData.requestStandardIsNull, headers);
        ResponseEntity<CruspToken> result = this.restTemplate.postForEntity(urlRead, entity, CruspToken.class);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getRepeats());
        assertEquals(58, result.getBody().getVolume());
        assertEquals(6200, result.getBody().getPacketSize());
        assertEquals(150, result.getBody().getRate(), 0.0000001);
        assertEquals(200, result.getBody().getSleep());
        assertEquals(100, result.getBody().getTimeout());
    }

    @Test
    public void getTokenForMeasurementShouldFailOnValidationWithWrongPacketSize() throws Exception {
        saveSettings(TestData.requestJsonStandard1); // set this object as standard CruspSetting

        String urlRead = "http://localhost:" + port + "/v1/measurement";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestPacketSizeIs0, headers); //make request with packet-size = 0
        ResponseEntity<CruspToken> result = this.restTemplate.postForEntity(urlRead, entity, CruspToken.class);

        assertNotNull(result);
        assertEquals(400, result.getStatusCodeValue()); // Bad Request
    }

    @Test
    public void filterSettingsShouldReturnOneSetting() throws Exception {
        saveSettings(TestData.requestJsonStandard1);
        saveSettings(TestData.requestJsonStandard2);

        String urlFilter = "http://localhost:" + port + "/v1/filter";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestFilter, headers); //make request with packet-size = 0
        ResponseEntity<Object> result = this.restTemplate.postForEntity(urlFilter, entity, Object.class);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, (((List<CruspSettings>)result.getBody()).size()));

    }

    @Test
    public void filterSettingsShouldReturnTwoSetting() throws Exception {
        saveSettings(TestData.requestJsonStandard1);
        saveSettings(TestData.requestJsonStandard2);

        String urlFilter = "http://localhost:" + port + "/v1/filter";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestFilter2, headers); //make request with packet-size = 0
        ResponseEntity<Object> result = this.restTemplate.postForEntity(urlFilter, entity, Object.class);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(2, (((List<CruspSettings>)result.getBody()).size()));

    }

    private CruspSettings readStandardSettings() throws Exception {
        String urlRead = "http://localhost:" + port + "/v1/standard" ;
        ResponseEntity<CruspSettings> responseEntity = restTemplate.getForEntity(urlRead, CruspSettings.class);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());

        return responseEntity.getBody();
    }

    private CruspSettings readSettingsById(long id) throws Exception {
        String urlRead = "http://localhost:" + port + "/v1/read/" + id;
        ResponseEntity<CruspSettings> responseEntity = restTemplate.getForEntity(urlRead, CruspSettings.class);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());

        return responseEntity.getBody();
    }
}