package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportsRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ReportsService(ReportsRepository reportsRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {
        this.reportsRepository = reportsRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {

            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {

            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

    // 日報IDで検索するメソッドを追加
    public Report findById(Integer id) {
        Optional<Report> option = reportsRepository.findById(id);
        return option.orElse(null);  // 見つからなければnullを返す
    }

    // 日報を保存
    @Transactional
    public void saveReport(Report report) {
        LocalDateTime now = LocalDateTime.now();

        if (report.getId() == null) {
            report.setCreatedAt(now);
        }

        report.setUpdatedAt(now);

        reportsRepository.save(report);
    }

    public List<Report> getAllReports() {
        List<Report> allReports = reportsRepository.findAll();
        List<Report> filtered = new ArrayList<>();

        for (Report r : allReports) {
            if (r.getEmployee() != null && !r.getEmployee().isDeleteFlg()) {
                filtered.add(r);
            }
        }

        return filtered;
    }
    public Report getReport(Integer id) {
        return reportsRepository.findById(id).orElse(null);
    }

    @Transactional
    public ErrorKinds updateReport(Report report) {
        Report current = findById(report.getId());

        if (current == null) {
            return ErrorKinds.BLANK_ERROR;
        }

        report.setCreatedAt(current.getCreatedAt());
        report.setUpdatedAt(LocalDateTime.now());
        report.setDeleteFlg(false);

        reportsRepository.save(report);

        return ErrorKinds.SUCCESS;
    }


    // 日報削除
    @Transactional
    public ErrorKinds delete(Integer id) {
        Report report = reportsRepository.findById(id).orElse(null);
        if (report == null) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        report.setDeleteFlg(true);
        reportsRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    public boolean existsByEmployeeAndReportDate(Employee employee, LocalDate reportDate) {
        return reportsRepository.findByEmployeeAndReportDate(employee, reportDate) != null;
    }
    // 社員に紐づく日報一覧を取得
    public List<Report> findByEmployee(Employee employee) {
        return reportsRepository.findByEmployee(employee);
    }

    public boolean existsByEmployeeAndReportDateExcludingId(Employee employee, LocalDate reportDate, Integer excludeId) {
        Report existing = reportsRepository.findByEmployeeAndReportDate(employee, reportDate);
        return existing != null && !existing.getId().equals(excludeId);
    }


}
