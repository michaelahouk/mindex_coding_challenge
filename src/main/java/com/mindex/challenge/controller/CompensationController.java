package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    /**
     * Creates a {@link Compensation} object for a given {@link com.mindex.challenge.data.Employee}
     * @param compensation the compensation object to be created
     * @return the {@link Compensation} the newly created Compensation object (or existing one if it already exists)
     * @throws IllegalArgumentException when no employee is provided
     */
    @PostMapping("/compensation")
    public Compensation create(@RequestBody Compensation compensation) throws IllegalArgumentException {
        LOG.debug("Received compensation create request for [{}]", compensation);

        if (compensation.getEmployee() == null || compensation.getEmployee().getEmployeeId() == null) {
            throw new IllegalArgumentException("No employee provided for compensation");
        }

        return compensationService.create(compensation);
    }

    /**
     * Look up a {@link Compensation} object for a given employeeId
     * @param id an employeeId
     * @return the {@link Compensation} object for an existing employee, or null if none found
     */
    @GetMapping("/compensation/{id}")
    public Compensation read(@PathVariable String id) {
        LOG.debug("Received compensation read request for id [{}]", id);

        return compensationService.read(id);
    }
}
