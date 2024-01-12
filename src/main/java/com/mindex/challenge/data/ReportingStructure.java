package com.mindex.challenge.data;

/**
 * A class containing an employee and the number of unique employees
 * that are beneath them in the reporting hierarchy
 */
public class ReportingStructure {

    /**
     * The employee
     */
    private Employee employee;
    /**
     * Number of unique people who are below this employee
     * in the reporting hierarchy
     */
    private int numberOfReports;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(int numberOfReports) {
        this.numberOfReports = numberOfReports;
    }
}
