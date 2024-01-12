package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = "http://localhost:" + port + "/employee/reporting/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = createTestEmployee("John", "Doe", Collections.emptyList());

        // Create checks
        Employee createdEmployee = saveEmployee(testEmployee);

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testReportingStructure_NoDirectReports() {
        // Null safe check
        Employee employeeWithNullReports = createTestEmployee("Bobby", "Employee", null);
        employeeWithNullReports = saveEmployee(employeeWithNullReports);
        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, employeeWithNullReports.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertEquals(0, reportingStructure.getNumberOfReports());
        assertEmployeeEquivalence(employeeWithNullReports, reportingStructure.getEmployee());

        // Empty list check
        Employee employeeWithEmptyReports = createTestEmployee("Bobby", "Employee", Collections.emptyList());
        employeeWithEmptyReports = saveEmployee(employeeWithEmptyReports);
        reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, employeeWithEmptyReports.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertEquals(0, reportingStructure.getNumberOfReports());
        assertEmployeeEquivalence(employeeWithEmptyReports, reportingStructure.getEmployee());
    }

    @Test
    public void testReportingStructure_RepeatedDirectReports() {
        // Ensure that reports are not counted twice
        Employee report1 = createTestEmployee("Report", "1", null);
        report1 = saveEmployee(report1);
        Employee report2 = createTestEmployee("Report", "1", Arrays.asList(report1));
        report2 = saveEmployee(report2);
        Employee report3 = createTestEmployee("Report", "1", Arrays.asList(report1));
        report3 = saveEmployee(report3);

        Employee testEmployee = createTestEmployee("Bobby", "Employee", Arrays.asList(report1, report2, report3));
        testEmployee = saveEmployee(testEmployee);
        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, testEmployee.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertEquals(3, reportingStructure.getNumberOfReports());
        assertEmployeeEquivalence(testEmployee, reportingStructure.getEmployee());
    }

    @Test
    public void testReportingStructure_NestedReports() {
        // Ensure that the nested reports are checked
        Employee report1 = createTestEmployee("Report", "1", null);
        report1 = saveEmployee(report1);
        Employee report2 = createTestEmployee("Report", "1", Arrays.asList(report1));
        report2 = saveEmployee(report2);
        Employee report3 = createTestEmployee("Report", "1", null);
        report3 = saveEmployee(report3);

        Employee testEmployee = createTestEmployee("Bobby", "Employee", Arrays.asList(report2, report3));
        testEmployee = saveEmployee(testEmployee);
        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, testEmployee.getEmployeeId()).getBody();

        assertNotNull(reportingStructure);
        assertEquals(3, reportingStructure.getNumberOfReports());
        assertEmployeeEquivalence(testEmployee, reportingStructure.getEmployee());
    }

    @Test
    public void testReportingStructure_InvalidEmployeeId_ReturnsNull() {
        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "invalid id").getBody();
        assertNull(reportingStructure);
    }

    private Employee saveEmployee(Employee employee) {
        return restTemplate.postForEntity(employeeUrl, employee, Employee.class).getBody();
    }

    private Employee createTestEmployee(String firstName, String lastName, List<Employee> directReports) {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName(firstName);
        testEmployee.setLastName(lastName);
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setDirectReports(directReports);
        return testEmployee;
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
