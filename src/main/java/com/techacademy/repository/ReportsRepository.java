package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;



@Repository
public interface ReportsRepository extends JpaRepository<Report, Integer> {
    List<Report> findByEmployee(Employee employee);
    Report findByEmployeeAndReportDate(Employee employee, LocalDate reportDate);
}
