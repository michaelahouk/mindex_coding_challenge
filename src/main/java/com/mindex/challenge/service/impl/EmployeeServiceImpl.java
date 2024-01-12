package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Fetching employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

//     Worth noting that this can be accomplished much more cleanly in later versions of Mongo
//     using an aggregation pipeline with a $graphLookup step
    @Override
    public ReportingStructure getReportingStructure(String id) {
        LOG.debug("Fetching reporting structure for employee with id [{}]", id);

        Set<String> directReportIds = new HashSet<>();
        Stack<String> directReportsToFetch = new Stack<>();
        // Fetch the employee from the database to look at their direct reports
        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            LOG.warn("Failed to fetch ReportingStructure - no employee found for id [{}]", id);
            return null;
        }
        // Add all initial direct reports
        directReportsToFetch.addAll(Optional.ofNullable(employee.getDirectReports()).orElse(Collections.emptyList())
                .stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList()));

        // Iterate until there are no new direct reports to look at
        while (!directReportsToFetch.isEmpty()) {
            Employee directReport = employeeRepository.findByEmployeeId(directReportsToFetch.pop());
            // Avoid re-fetching the reports for employees we've already seen
            if (!directReportIds.contains(directReport.getEmployeeId())) {
                // Mark this employee as a direct report
                directReportIds.add(directReport.getEmployeeId());
                // Add all direct reports of this employee to the stack for fetching
                directReportsToFetch.addAll(Optional.ofNullable(directReport.getDirectReports()).orElse(Collections.emptyList())
                        .stream()
                        .map(Employee::getEmployeeId)
                        .collect(Collectors.toList()));
            }
        }

        // Create reporting structure
        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(directReportIds.size());

        return reportingStructure;
    }
}
