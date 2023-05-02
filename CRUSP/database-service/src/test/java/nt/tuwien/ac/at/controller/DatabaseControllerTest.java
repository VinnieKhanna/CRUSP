package nt.tuwien.ac.at.controller;

import com.netflix.discovery.converters.Auto;
import nt.tuwien.ac.at.controller.response.FilterMeasurementResponse;
import nt.tuwien.ac.at.controller.response.MeasurementResponse;
import nt.tuwien.ac.at.model.CruspSettings;
import nt.tuwien.ac.at.model.MeasurementResult;
import nt.tuwien.ac.at.model.network.ITelephonyInfo;
import nt.tuwien.ac.at.persistance.MeasurementFilterRepository;
import nt.tuwien.ac.at.persistance.MeasurementRepository;
import nt.tuwien.ac.at.persistance.TelephonyInfoRepository;
import nt.tuwien.ac.at.service.IPersistService;
import nt.tuwien.ac.at.service.PersistServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static nt.tuwien.ac.at.controller.TestData.requestCruspSettings;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DatabaseControllerTest {
    @Autowired
    private DatabaseController controller;

    @Autowired
    IPersistService persistService;

    @Mock
    IPersistService persistServiceMock;

    @Mock
    SettingsServiceFeignClient settingsService;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Before
    public void before() {
        controller.setMeasurementService(persistService);
        controller.setProxySettingsService(settingsService);
    }

    @Test
    public void saveMeasurementShouldWork() throws Exception {
        when(settingsService.saveSettings(any(CruspSettings.class))).thenReturn(1L);

        String url = "http://localhost:" + port + "/v1/measurement/save";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestJson, headers);
        ResponseEntity<Long> result = this.restTemplate.postForEntity(url, entity, Long.class);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertTrue((long)result.getBody() > 0);
    }

    @Test
    public void getMeasurementShouldReturnCorrectMeasurement() throws Exception {
        controller.setMeasurementService(persistServiceMock);
        when(settingsService.getSettings(requestCruspSettings.getUid())).thenReturn(requestCruspSettings);
        when(persistServiceMock.findMeasurement(TestData.requestMeasurement.getUid())).thenReturn(Optional.ofNullable(TestData.requestMeasurement));

        String urlRead = "http://localhost:" + port + "/v1/measurement/1";
        ResponseEntity<MeasurementResponse> responseEntity = restTemplate.getForEntity(urlRead, MeasurementResponse.class);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertNotNull(responseEntity.getBody());
        assertEquals(TestData.requestMeasurement.getUid(), responseEntity.getBody().getUid());
        assertNotNull(responseEntity.getBody().getSequenceCollection());
        assertEquals(2, responseEntity.getBody().getSequenceCollection().size());
        assertEquals(TestData.requestSequenceDetails1.getUid(), responseEntity.getBody().getSequenceCollection().get(0).getUid());
        assertEquals(TestData.requestSequenceDetails2.getUid(), responseEntity.getBody().getSequenceCollection().get(1).getUid());
        assertNotNull(responseEntity.getBody().getSettings());
        assertEquals(requestCruspSettings.getUid(), responseEntity.getBody().getSettings().getUid());
        assertNotNull(responseEntity.getBody().getTelephonyInfo());
        assertEquals(TestData.requestTelephonyInfo.getUid(), responseEntity.getBody().getTelephonyInfo().getUid());
        assertEquals(TestData.requestMeasurement.isDownlink(), responseEntity.getBody().isDownlink());
    }

    @Test
    public void filterMeasurementsShouldReturnOneResult() throws Exception {
        // 1. Save measurement into DB
        when(settingsService.saveSettings(any(CruspSettings.class))).thenReturn(requestCruspSettings.getUid());
        when(settingsService.getSettings(any(Long.class))).thenReturn(requestCruspSettings);
        String saveUrl = "http://localhost:" + port + "/v1/measurement/save";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestJson, headers);
        ResponseEntity<Long> result = this.restTemplate.postForEntity(saveUrl, entity, Long.class);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result);

        // 2. Filter for it

        List<CruspSettings> settingsList = new ArrayList<>();
        settingsList.add(requestCruspSettings);

        when(settingsService.filterSettings(any()))
                .thenReturn(settingsList);

        String filterUrl = "http://localhost:" + port + "/v1/measurement/filter";

        HttpEntity<String> filterEntity = new HttpEntity<>(TestData.filterJson, headers);
        ResponseEntity<FilterMeasurementResponse> filterResponse = this.restTemplate.postForEntity(filterUrl, filterEntity, FilterMeasurementResponse.class);

        assertNotNull(filterResponse);
        assertEquals(filterResponse.getStatusCode().value(), 200);
        assertNotNull(filterResponse.getBody());
        assertEquals(1L, filterResponse.getBody().count);
    }

    // Tests are commented out since they are old but still can be used as template for new tests
    /*@Test
    public void saveMeasurementWithSameSettingsShouldReturnOneSetting() throws Exception {
        assertNotNull(controller);

        String url = "http://localhost:" + port + "/v1/measurement/save";

        // send first measurement to save data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity1 = new HttpEntity<>(TestData.requestJson, headers);
        ResponseEntity<Long> result1 = this.restTemplate.postForEntity(url, entity1, Long.class);

        assertNotNull(result1);
        assertEquals(200, result1.getStatusCodeValue());
        assertTrue(result1.getBody() > 0L); //ID needs to be > 0 to verify that data is saved in a database

        // send second measurement to save data
        HttpEntity<String> entity2 = new HttpEntity<>(TestData.requestJson, headers);
        ResponseEntity<Long> result2 = this.restTemplate.postForEntity(url, entity1, Long.class);

        assertNotNull(result2);
        assertEquals(200, result2.getStatusCodeValue());
        assertTrue(result2.getBody() > 0L); //ID needs to be > 0 to verify that data is saved in a database

        //get results
        String urlRead1 = "http://localhost:" + port + "/v1/measurement/" + result1.getBody();
        ResponseEntity<MeasurementResponse> responseEntity1 = restTemplate.getForEntity(urlRead1, MeasurementResponse.class);

        String urlRead2 = "http://localhost:" + port + "/v1/measurement/" + result2.getBody();
        ResponseEntity<MeasurementResponse> responseEntity2 = restTemplate.getForEntity(urlRead2, MeasurementResponse.class);

        assertNotNull(responseEntity1);
        assertEquals(200, responseEntity1.getStatusCodeValue());

        assertNotNull(responseEntity2);
        assertEquals(200, responseEntity2.getStatusCodeValue());

        assertEquals(responseEntity1.getBody().getSettings().getUid(), responseEntity2.getBody().getSettings().getUid());

    }*/

    /**
     * Precondition: save measurement must work correctly
     * @throws Exception
     */
    /*@Test
    public void getMeasurementShouldReturnCorrectTelephonyInfo() throws Exception {
        assertNotNull(controller);

        String urlSave = "http://localhost:" + port + "/v1/measurement/save";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(TestData.requestJson, headers);
        ResponseEntity<Long> result = this.restTemplate.postForEntity(urlSave, entity, Long.class);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody() > 0L); //ID needs to be > 0 to verify that data is saved in a database

        String urlRead = "http://localhost:" + port + "/v1/measurement/" + result.getBody();
        ResponseEntity<MeasurementResponse> responseEntity = restTemplate.getForEntity(urlRead, MeasurementResponse.class);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(responseEntity.getBody().getUid(), result.getBody().longValue());
        assertNotNull(responseEntity.getBody().getSequenceCollection());
        assertEquals(responseEntity.getBody().getSequenceCollection().size(), 2);
        assertNotNull(responseEntity.getBody().getTelephonyInfo());
        assertTrue(responseEntity.getBody().getTelephonyInfo().getUid() > 0L);
        assertTrue(responseEntity.getBody().getTelephonyInfo() instanceof TelephonyInfoLTE);

        TelephonyInfoLTE telephonyInfoLTE = (TelephonyInfoLTE)responseEntity.getBody().getTelephonyInfo();
        assertEquals(telephonyInfoLTE.getCi(), 1);
        assertEquals(telephonyInfoLTE.getMcc(), "MCC");
        assertEquals(telephonyInfoLTE.getMnc(), "MNC");
        assertEquals(telephonyInfoLTE.getEarfcn(), 8000);
        assertEquals(telephonyInfoLTE.getPci(), 2);
        assertEquals(telephonyInfoLTE.getTac(), 3);
        assertEquals(telephonyInfoLTE.getDbm(), 4);
        assertEquals(telephonyInfoLTE.getTa(), 5);
        assertEquals(telephonyInfoLTE.getAsu(), 6);
        assertEquals(telephonyInfoLTE.getRssnr(), 7);
        assertEquals(telephonyInfoLTE.getCqi(), 8);
        assertEquals(telephonyInfoLTE.getRsrq(), 9);
        assertEquals(telephonyInfoLTE.getRsrp(), 10);
        assertEquals(telephonyInfoLTE.getOperatorAlphaLong(), "Drei AT Long");
        assertEquals(telephonyInfoLTE.getMobileNetworkOperator(), "3AT");
    }*/

}