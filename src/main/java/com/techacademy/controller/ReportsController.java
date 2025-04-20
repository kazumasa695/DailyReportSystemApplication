package com.techacademy.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportsService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    // 日報一覧画面
    @GetMapping
    public String list(Model model) {
        List<Report> reportsList = reportsService.getAllReports();
        model.addAttribute("reportsList", reportsList);
        model.addAttribute("listSize", reportsList.size());
        return "reports/list";
    }

    // 日報新規登録画面
    @GetMapping("/add")
    public String create(@ModelAttribute Report report, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        report.setEmployee(userDetail.getEmployee());
        return "reports/new";
    }

    // 日報保存処理
    @PostMapping("/add")
    public String save(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail,
            Model model) {

        // 入力チェック
        if (res.hasErrors()) {
            return create(report, model, userDetail);
        }

        report.setEmployee(userDetail.getEmployee());
        boolean exists = reportsService.existsByEmployeeAndReportDate(report.getEmployee(), report.getReportDate());
        if (exists) {
            res.rejectValue("reportDate", "duplicate-reportDate", "既に登録されている日付です");
            return create(report, model, userDetail);
        }


        report.setEmployee(userDetail.getEmployee());
        reportsService.saveReport(report);
        return "redirect:/reports";
    }

    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Report report = reportsService.findById(id);;
        model.addAttribute("report", report);
        return "reports/edit";
    }

    // 日報更新処理
    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id,
                         @Validated @ModelAttribute Report report,
                         BindingResult res,
                         @AuthenticationPrincipal UserDetail userDetail,
                         Model model) {

        if (res.hasErrors()) {
            model.addAttribute("report", report);
            return "reports/edit";
        }

        report.setEmployee(userDetail.getEmployee());
        ErrorKinds result = reportsService.updateReport(report);

        if (ErrorMessage.contains(result)) {
            model.addAttribute("report", report);
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return "reports/edit";
        }

        return "redirect:/reports";
    }


    // 日報詳細表示
    @GetMapping("/{id}/")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Report report = reportsService.getReport(id);
        model.addAttribute("report", report);
        model.addAttribute("employee", report.getEmployee());
        return "reports/detail";
    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        Report report = reportsService.findById(id);

        if (report == null || !report.getEmployee().getCode().equals(userDetail.getEmployee().getCode())) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.LOGINCHECK_ERROR),
                               ErrorMessage.getErrorValue(ErrorKinds.LOGINCHECK_ERROR));
            model.addAttribute("report", report);
            return detail(id, model);
        }

        ErrorKinds result = reportsService.delete(id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportsService.findById(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

}
