package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CompensationServiceImpl implements CompensationService {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        // Each employee can only have one compensation, so check for existing ones
        Compensation existingCompensation = compensationRepository.findByEmployee_EmployeeId(compensation.getEmployee().getEmployeeId());

        // Updates would happen in the update method were we to implement one
        if (existingCompensation != null) {
            LOG.debug("Compensation already exists for employeeId [{}]", compensation.getEmployee().getEmployeeId());
            return existingCompensation;
        }

        // Set the effective date to today if there is none
        if (compensation.getEffectiveDate() == null) {
            compensation.setEffectiveDate(LocalDate.now());
        }
        // Save new compensation
        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String employeeId) {
        LOG.debug("Fetching compensation with employeeId [{}]", employeeId);

        return compensationRepository.findByEmployee_EmployeeId(employeeId);
    }
}
