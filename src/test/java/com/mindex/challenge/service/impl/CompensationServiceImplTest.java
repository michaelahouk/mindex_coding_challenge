package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationCreateUrl;
    private String compensationReadUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationCreateUrl = "http://localhost:" + port + "/compensation";
        compensationReadUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateRead() {
        Compensation compensation = createTestCompensation(UUID.randomUUID().toString(), 1000, LocalDate.now().minusDays(2));

        // Create checks
        Compensation createdCompensation = saveCompensation(compensation);

        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(compensation, createdCompensation);


        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(compensationReadUrl, Compensation.class, createdCompensation.getEmployee().getEmployeeId()).getBody();
        assertCompensationEquivalence(createdCompensation, readCompensation);
    }

    @Test
    public void testReadWithInvalidId_ReturnsNull() {
        // Read null check
        Compensation readCompensation = restTemplate.getForEntity(compensationReadUrl, Compensation.class, "not a real id").getBody();
        assertNull(readCompensation);
    }

    @Test
    public void testCreateWithoutEmployee_ThrowsException() {
        Compensation compensation = createTestCompensation(null, 1000, LocalDate.now().minusDays(2));
        ResponseEntity<Compensation> compensationResponse = restTemplate.postForEntity(compensationCreateUrl, compensation, Compensation.class);
        assertEquals(500, compensationResponse.getStatusCodeValue());
    }

    private Compensation saveCompensation(Compensation compensation) {
        return restTemplate.postForEntity(compensationCreateUrl, compensation, Compensation.class).getBody();
    }

    private Compensation createTestCompensation(String employeeId, double salary, LocalDate date) {
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);

        Compensation compensation = new Compensation();
        compensation.setEmployee(employee);
        compensation.setSalary(salary);
        compensation.setEffectiveDate(date);

        return compensation;
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployee().getEmployeeId(), actual.getEmployee().getEmployeeId());
        assertEquals(expected.getSalary(), actual.getSalary(), 0);
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
