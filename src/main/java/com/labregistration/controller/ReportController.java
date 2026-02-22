package com.labregistration.controller;
import com.labregistration.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('LAB_MANAGER')")
@PreAuthorize("hasAnyRole('LAB_MANAGER', 'SUPER_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    /**
     * Download session report in specified format (txt, csv, excel, pdf)
     * The pdf format routes to the professional iText table-based PDF.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<byte[]> downloadSessionReport(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "txt") String format) throws Exception {

        byte[] report;
        String filename;
        MediaType mediaType;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        switch (format.toLowerCase()) {
            case "csv":
                report = reportService.generateSessionReportCsv(sessionId);
                filename = "session_report_" + sessionId + "_" + dateStr + ".csv";
                mediaType = MediaType.parseMediaType("text/csv");
                break;
            case "excel":
            case "xlsx":
                report = reportService.generateSessionReportExcel(sessionId);
                filename = "session_report_" + sessionId + "_" + dateStr + ".xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                report = reportService.generateSessionReportPdf(sessionId);
                filename = "session_report_" + sessionId + "_" + dateStr + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "txt":
            default:
                report = reportService.generateSessionReportTxt(sessionId);
                filename = "session_report_" + sessionId + "_" + dateStr + ".txt";
                mediaType = MediaType.TEXT_PLAIN;
                break;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(report);
    }

    /**
     * Dedicated endpoint for the professional iText PDF session report.
     * GET /api/reports/session/{sessionId}/pdf
     */
    @GetMapping("/session/{sessionId}/pdf")
    public ResponseEntity<byte[]> downloadSessionReportPdf(@PathVariable Long sessionId) {
        byte[] report = reportService.generateSessionReportPdf(sessionId);
        String filename = "session_report_" + sessionId + "_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }

    /**
     * Download all registrations in specified format (csv, excel, pdf)
     */
    @GetMapping("/all")
    public ResponseEntity<byte[]> downloadAllRegistrationsReport(
            @RequestParam(defaultValue = "csv") String format) throws Exception {

        byte[] report;
        String filename;
        MediaType mediaType;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        switch (format.toLowerCase()) {
            case "excel":
            case "xlsx":
                report = reportService.generateAllRegistrationsExcel();
                filename = "all_registrations_" + dateStr + ".xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "pdf":
                report = reportService.generateAllRegistrationsPdf();
                filename = "all_registrations_" + dateStr + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "csv":
            default:
                report = reportService.generateAllRegistrationsCsv();
                filename = "all_registrations_" + dateStr + ".csv";
                mediaType = MediaType.parseMediaType("text/csv");
                break;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(report);
    }

    /**
     * Download course report in specified format
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<byte[]> downloadCourseReport(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "txt") String format) {

        byte[] report;
        String filename;
        MediaType mediaType;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        switch (format.toLowerCase()) {
            case "pdf":
                report = reportService.generateCourseReportPdf(courseId);
                filename = "course_report_" + courseId + "_" + dateStr + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "txt":
            default:
                report = reportService.generateCourseReport(courseId);
                filename = "course_report_" + courseId + "_" + dateStr + ".txt";
                mediaType = MediaType.TEXT_PLAIN;
                break;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(report);
    }

    /**
     * Download date range report
     */
    @GetMapping("/date-range")
    public ResponseEntity<byte[]> downloadDateRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {

        byte[] report;
        String filename;
        MediaType mediaType;
        String dateRangeStr = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_to_" +
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        switch (format.toLowerCase()) {
            case "pdf":
                report = reportService.generateDateRangeReportPdf(startDate, endDate);
                filename = "registrations_" + dateRangeStr + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "excel":
            case "xlsx":
                report = reportService.generateDateRangeReportExcel(startDate, endDate);
                filename = "registrations_" + dateRangeStr + ".xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
            case "csv":
            default:
                report = reportService.generateDateRangeReport(startDate, endDate);
                filename = "registrations_" + dateRangeStr + ".csv";
                mediaType = MediaType.parseMediaType("text/csv");
                break;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(report);
    }

    /**
     * Download department summary report
     */
    @GetMapping("/department/{departmentName}")
    public ResponseEntity<byte[]> downloadDepartmentReport(
            @PathVariable String departmentName,
            @RequestParam(defaultValue = "excel") String format) {

        byte[] report;
        String filename;
        MediaType mediaType;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        switch (format.toLowerCase()) {
            case "pdf":
                report = reportService.generateDepartmentReportPdf(departmentName);
                filename = "department_report_" + departmentName + "_" + dateStr + ".pdf";
                mediaType = MediaType.APPLICATION_PDF;
                break;
            case "excel":
            case "xlsx":
            default:
                report = reportService.generateDepartmentReportExcel(departmentName);
                filename = "department_report_" + departmentName + "_" + dateStr + ".xlsx";
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                break;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(report);
    }
}