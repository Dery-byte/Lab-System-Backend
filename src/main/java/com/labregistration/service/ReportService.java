package com.labregistration.service;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.labregistration.exception.ResourceNotFoundException;
import com.labregistration.model.*;
import com.labregistration.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final LabSessionRepository labSessionRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;

    // ── Formatters ────────────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_DISPLAY     = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT      = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMAT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATETIME_DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ── PDF colour palette ────────────────────────────────────────────────────
    private static final DeviceRgb PDF_NAVY       = new DeviceRgb(0x1E, 0x3A, 0x5F);
    private static final DeviceRgb PDF_BLUE       = new DeviceRgb(0x2E, 0x6D, 0xA4);
    private static final DeviceRgb PDF_COL_HDR    = new DeviceRgb(0x3B, 0x82, 0xC4);
    private static final DeviceRgb PDF_ALT_ROW    = new DeviceRgb(0xEE, 0xF4, 0xFB);
    private static final DeviceRgb PDF_BORDER     = new DeviceRgb(0xB0, 0xC4, 0xD8);
    private static final DeviceRgb PDF_WHITE      = new DeviceRgb(0xFF, 0xFF, 0xFF);
    private static final DeviceRgb PDF_TEXT       = new DeviceRgb(0x1A, 0x1A, 0x2E);
    private static final DeviceRgb PDF_LIGHT_BLUE = new DeviceRgb(0xAD, 0xD8, 0xE6);
    private static final DeviceRgb PDF_WAITLIST   = new DeviceRgb(0x7B, 0x3F, 0x00);
    private static final DeviceRgb PDF_FULL       = new DeviceRgb(0xC0, 0x39, 0x2B);
    private static final DeviceRgb PDF_AVAILABLE  = new DeviceRgb(0x27, 0xAE, 0x60);
    private static final DeviceRgb PDF_EMPTY      = new DeviceRgb(0x95, 0xA5, 0xA6);
    private static final DeviceRgb PDF_INFO_BG    = new DeviceRgb(0xF0, 0xF4, 0xF8);
    private static final DeviceRgb PDF_ORANGE     = new DeviceRgb(0xE6, 0x7E, 0x22);

    // =========================================================================
    // TXT REPORT
    // =========================================================================

    public byte[] generateSessionReportTxt(Long sessionId) {
        LabSession session = labSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", sessionId));

        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        writer.println("================================================================================");
        writer.println("                    LAB SESSION REGISTRATION REPORT");
        writer.println("================================================================================");
        writer.println();
        writer.println("  Session    : " + session.getName());
        writer.println("  Course     : " + session.getCourse().getCourseCode() + " - " + session.getCourse().getCourseName());
        writer.println("  Level      : " + session.getCourse().getLevel().getDisplayName());
        writer.println("  Department : " + session.getCourse().getDepartment().getName().toString());
        writer.println("  Lab Room   : " + session.getLabRoom());
        writer.println("  Date Range : " + session.getStartDate().format(DATE_FORMAT) + " to " + session.getEndDate().format(DATE_FORMAT));
        writer.println("  Days       : " + String.join(", ", session.getSessionDaysSet()));
//        writer.println("  Status     : " + session.getStatus());
        writer.println("  Generated  : " + LocalDateTime.now().format(DATETIME_FORMAT));
        writer.println();

        int totalRegistered = 0;

        for (TimeSlot slot : slots) {
            List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId())
                    .stream()
                    .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                            || r.getStatus() == RegistrationStatus.PENDING)
                    .collect(Collectors.toList());

            String fill = registrations.isEmpty()
                    ? "EMPTY"
                    : registrations.size() >= slot.getMaxStudents() ? "FULL" : "AVAILABLE";

            writer.println("--------------------------------------------------------------------------------");
            writer.printf("  GROUP #%d  |  %s (%s)  |  %s - %s  |  [%s]  |  %d/%d students%n",
                    slot.getGroupNumber(),
                    slot.getSessionDate().format(DATE_FORMAT),
                    slot.getSessionDate().getDayOfWeek(),
                    slot.getStartTime().format(TIME_FORMAT),
                    slot.getEndTime().format(TIME_FORMAT),
                    fill,
                    registrations.size(),
                    slot.getMaxStudents());
            writer.println("--------------------------------------------------------------------------------");

            if (registrations.isEmpty()) {
                writer.println("  No students registered for this Group.");
            } else {
                writer.printf("  %-4s  %-12s  %-25s  %-15s  %-32s  %-22s  %-10s%n",
                        "#", "Student ID", "Full Name", "Username", "Email", "Program", "Status");
                writer.printf("  %-4s  %-12s  %-25s  %-15s  %-32s  %-22s  %-10s%n",
                        "----", "------------", "-------------------------", "---------------",
                        "--------------------------------", "----------------------", "----------");

                int count = 1;
                for (Registration reg : registrations) {
                    User s = reg.getStudent();
                    writer.printf("  %-4d  %-12s  %-25s  %-15s  %-32s  %-22s  %-10s%n",
                            count++,
                            nullSafe(s.getStudentId()),
                            truncate(s.getFullName(), 25),
                            truncate(s.getUsername(), 15),
                            truncate(s.getEmail(), 32),
                            truncate(s.getProgramName(), 22),
//                            nullSafe(s.getPhone()),
                            reg.getStatus());
                }
                totalRegistered += registrations.size();
            }
            writer.println();
        }

        // Waitlist
        List<Registration> waitlisted = registrationRepository
                .findByLabSessionIdAndStatusOrderByWaitlistPositionAsc(sessionId, RegistrationStatus.WAITLISTED);

        if (!waitlisted.isEmpty()) {
            writer.println("================================================================================");
            writer.println("  WAITLISTED STUDENTS");
            writer.println("================================================================================");
            writer.printf("  %-5s  %-12s  %-25s  %-15s  %-32s  %-22s  %-13s%n",
                    "Pos", "Student ID", "Full Name", "Username", "Email", "Program", "Registered");
            writer.printf("  %-5s  %-12s  %-25s  %-15s  %-32s  %-22s  %-20s%n",
                    "-----", "------------", "-------------------------", "---------------",
                    "--------------------------------", "----------------------", "--------------------");

            for (Registration reg : waitlisted) {
                User s = reg.getStudent();
                writer.printf("  #%-4d  %-12s  %-25s  %-15s  %-32s  %-22s  %-20s%n",
                        reg.getWaitlistPosition(),
                        nullSafe(s.getStudentId()),
                        truncate(s.getFullName(), 25),
                        truncate(s.getUsername(), 15),
                        truncate(s.getEmail(), 32),
                        truncate(s.getProgramName(), 22),
                        reg.getRegisteredAt().format(DATETIME_FORMAT));
            }
            writer.println();
        }

        writer.println("================================================================================");
        writer.println("  SUMMARY");
        writer.println("  Total Groups     : " + slots.size());
        writer.println("  Total Registered: " + totalRegistered);
        writer.println("  Total Waitlisted: " + waitlisted.size());
        writer.println("  Total Capacity  : " + session.getTotalCapacity());
        writer.println("================================================================================");

        writer.flush();
        return baos.toByteArray();
    }

    // =========================================================================
    // CSV REPORTS
    // =========================================================================

    public byte[] generateSessionReportCsv(Long sessionId) {
        LabSession session = labSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", sessionId));

        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        writer.println("Group #,Date,Day,Start Time,End Time,Capacity,Student ID,Full Name,Username,Email,Program,Status,Registered At");

        for (TimeSlot slot : slots) {
            List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId());
            for (Registration reg : registrations) {
                User s = reg.getStudent();
                writer.printf("%d,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s%n",
                        slot.getGroupNumber(),
                        slot.getSessionDate().format(DATE_FORMAT),
                        slot.getSessionDate().getDayOfWeek(),
                        slot.getStartTime().format(TIME_FORMAT),
                        slot.getEndTime().format(TIME_FORMAT),
                        slot.getMaxStudents(),
                        escapeCsv(s.getStudentId()),
                        escapeCsv(s.getFullName()),
                        escapeCsv(s.getUsername()),
                        escapeCsv(s.getEmail()),
                        escapeCsv(s.getProgramName()),
//                        escapeCsv(s.getPhoneNumber()),
                        reg.getStatus(),
                        reg.getRegisteredAt().format(DATETIME_FORMAT));
            }
        }

        writer.flush();
        return baos.toByteArray();
    }

    public byte[] generateAllRegistrationsCsv() {
        List<LabSession> sessions = labSessionRepository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        writer.println("Course Code,Course Name,Level,Department,Session,Group #,Date,Day,Start,End,Student ID,Full Name,Username,Email,Program,Status,Registered At");

        for (LabSession session : sessions) {
            List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId());
            for (TimeSlot slot : slots) {
                for (Registration reg : registrationRepository.findByTimeSlotId(slot.getId())) {
                    User s = reg.getStudent();
                    writer.printf("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                            escapeCsv(session.getCourse().getCourseCode()),
                            escapeCsv(session.getCourse().getCourseName()),
                            session.getCourse().getLevel().getDisplayName(),
                            escapeCsv(session.getCourse().getDepartmentName()),
                            escapeCsv(session.getName()),
                            slot.getGroupNumber(),
                            slot.getSessionDate().format(DATE_FORMAT),
                            slot.getSessionDate().getDayOfWeek(),
                            slot.getStartTime().format(TIME_FORMAT),
                            slot.getEndTime().format(TIME_FORMAT),
                            escapeCsv(s.getStudentId()),
                            escapeCsv(s.getFullName()),
                            escapeCsv(s.getUsername()),
                            escapeCsv(s.getEmail()),
                            escapeCsv(s.getProgramName()),
//                            escapeCsv(s.getPhone()),
                            reg.getStatus(),
                            reg.getRegisteredAt().format(DATETIME_FORMAT));
                }
            }
        }

        writer.flush();
        return baos.toByteArray();
    }

    public byte[] generateDateRangeReport(LocalDate startDate, LocalDate endDate) {
        List<TimeSlot> slots = timeSlotRepository
                .findBySessionDateBetweenOrderBySessionDateAsc(startDate, endDate);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        writer.println("Date,Day,Start,End,Course,Session,Group #,Student ID,Full Name,Username,Email,Program,Status");

        for (TimeSlot slot : slots) {
            LabSession session = slot.getLabSession();
            List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId())
                    .stream()
                    .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                            || r.getStatus() == RegistrationStatus.PENDING)
                    .collect(Collectors.toList());

            if (registrations.isEmpty()) {
                writer.printf("%s,%s,%s,%s,%s,%s,%d,,,,,,EMPTY%n",
                        slot.getSessionDate().format(DATE_FORMAT),
                        slot.getSessionDate().getDayOfWeek(),
                        slot.getStartTime().format(TIME_FORMAT),
                        slot.getEndTime().format(TIME_FORMAT),
                        escapeCsv(session.getCourse().getCourseCode()),
                        escapeCsv(session.getName()),
                        slot.getGroupNumber());
            } else {
                for (Registration reg : registrations) {
                    User s = reg.getStudent();
                    writer.printf("%s,%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s%n",
                            slot.getSessionDate().format(DATE_FORMAT),
                            slot.getSessionDate().getDayOfWeek(),
                            slot.getStartTime().format(TIME_FORMAT),
                            slot.getEndTime().format(TIME_FORMAT),
                            escapeCsv(session.getCourse().getCourseCode()),
                            escapeCsv(session.getName()),
                            slot.getGroupNumber(),
                            escapeCsv(s.getStudentId()),
                            escapeCsv(s.getFullName()),
                            escapeCsv(s.getUsername()),
                            escapeCsv(s.getEmail()),
                            escapeCsv(s.getProgramName()),
//                            escapeCsv(s.getPhone()),
                            reg.getStatus());
                }
            }
        }

        writer.flush();
        return baos.toByteArray();
    }

    public byte[] generateCourseReport(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        List<LabSession> sessions = labSessionRepository.findByCourseId(courseId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        writer.println("================================================================================");
        writer.println("                     COURSE LAB REGISTRATION REPORT");
        writer.println("================================================================================");
        writer.println();
        writer.println("  Course     : " + course.getCourseCode() + " - " + course.getCourseName());
        writer.println("  Level      : " + course.getLevel().getDisplayName());
        writer.println("  Department : " + course.getDepartment());
        writer.println("  Semester   : " + course.getSemester());
        writer.println("  Generated  : " + LocalDateTime.now().format(DATETIME_FORMAT));
        writer.println();

        for (LabSession session : sessions) {
            writer.println("================================================================================");
            writer.println("  SESSION: " + session.getName() + "  [" + session.getStatus() + "]");
            writer.println("  " + session.getStartDate().format(DATE_FORMAT) + " to "
                    + session.getEndDate().format(DATE_FORMAT)
                    + "  |  Days: " + String.join(", ", session.getSessionDaysSet()));
            writer.println("================================================================================");

            List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId());
            for (TimeSlot slot : slots) {
                List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId())
                        .stream()
                        .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                || r.getStatus() == RegistrationStatus.PENDING)
                        .collect(Collectors.toList());

                writer.printf("%n  Group #%d  |  %s (%s)  |  %s-%s  |  %d/%d%n",
                        slot.getGroupNumber(),
                        slot.getSessionDate().format(DATE_FORMAT),
                        slot.getSessionDate().getDayOfWeek(),
                        slot.getStartTime().format(TIME_FORMAT),
                        slot.getEndTime().format(TIME_FORMAT),
                        registrations.size(),
                        slot.getMaxStudents());

                if (registrations.isEmpty()) {
                    writer.println("    No students registered.");
                } else {
                    writer.printf("    %-4s  %-12s  %-25s  %-15s  %-32s  %-22s%n",
                            "#", "Student ID", "Full Name", "Username", "Email", "Program");
                    writer.printf("    %-4s  %-12s  %-25s  %-15s  %-32s  %-22s%n",
                            "----", "------------", "-------------------------", "---------------",
                            "--------------------------------", "----------------------");
                    int i = 1;
                    for (Registration reg : registrations) {
                        User s = reg.getStudent();
                        writer.printf("    %-4d  %-12s  %-25s  %-15s  %-32s  %-22s%n",
                                i++,
                                nullSafe(s.getStudentId()),
                                truncate(s.getFullName(), 25),
                                truncate(s.getUsername(), 15),
                                truncate(s.getEmail(), 32),
                                truncate(s.getProgramName(), 22));
                    }
                }
            }
            writer.println();
        }

        writer.flush();
        return baos.toByteArray();
    }

    // =========================================================================
    // EXCEL REPORTS
    // =========================================================================

    public byte[] generateSessionReportExcel(Long sessionId) throws Exception {
        LabSession session = labSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", sessionId));

        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle    = createHeaderStyle(workbook);
            CellStyle boldStyle      = createBoldStyle(workbook);
            CellStyle slotTitleStyle = createSlotTitleStyle(workbook);
            CellStyle colStyle       = createColumnHeaderStyle(workbook);
            CellStyle dataStyle      = createDataStyle(workbook);
            CellStyle altDataStyle   = createAltDataStyle(workbook);

            // ── Sheet 1: Summary ──────────────────────────────────────────
            Sheet summarySheet = workbook.createSheet("Summary");
            int rowNum = 0;

            org.apache.poi.ss.usermodel.Cell titleCell = summarySheet.createRow(rowNum++).createCell(0);
            titleCell.setCellValue("LAB SESSION REGISTRATION REPORT");
            titleCell.setCellStyle(boldStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            rowNum++;
            addSummaryRow(summarySheet, rowNum++, "Session:",    session.getName(), workbook);
            addSummaryRow(summarySheet, rowNum++, "Course:",     session.getCourse().getCourseCode() + " - " + session.getCourse().getCourseName(), workbook);
            addSummaryRow(summarySheet, rowNum++, "Level:",      session.getCourse().getLevel().getDisplayName(), workbook);
            addSummaryRow(summarySheet, rowNum++, "Department:", session.getCourse().getDepartmentName(), workbook);
            addSummaryRow(summarySheet, rowNum++, "Lab Room:",   session.getLabRoom(), workbook);
            addSummaryRow(summarySheet, rowNum++, "Date Range:", session.getStartDate().format(DATE_FORMAT) + " to " + session.getEndDate().format(DATE_FORMAT), workbook);
            addSummaryRow(summarySheet, rowNum++, "Days:",       String.join(", ", session.getSessionDaysSet()), workbook);
            addSummaryRow(summarySheet, rowNum++, "Status:",     session.getStatus().name(), workbook);
            addSummaryRow(summarySheet, rowNum++, "Generated:",  LocalDateTime.now().format(DATETIME_FORMAT), workbook);

            rowNum += 2;
            Row overviewHdr = summarySheet.createRow(rowNum++);
            String[] ovCols = {"Group #", "Date", "Day", "Start", "End", "Registered", "Capacity", "Fill %", "Status"};
            for (int i = 0; i < ovCols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = overviewHdr.createCell(i);
                c.setCellValue(ovCols[i]);
                c.setCellStyle(headerStyle);
            }

            int grandTotal = 0;
            for (TimeSlot slot : slots) {
                int registered = slot.getCurrentCount();
                grandTotal += registered;
                double pct = slot.getMaxStudents() > 0
                        ? Math.round(registered * 100.0 / slot.getMaxStudents()) : 0;
                CellStyle rowStyle = getStatusStyle(workbook, registered, slot.getMaxStudents());

                Row row = summarySheet.createRow(rowNum++);
                createStyledCell(row, 0, "Slot #" + slot.getGroupNumber(), rowStyle);
                createStyledCell(row, 1, slot.getSessionDate().format(DATE_FORMAT), rowStyle);
                createStyledCell(row, 2, slot.getSessionDate().getDayOfWeek().toString(), rowStyle);
                createStyledCell(row, 3, slot.getStartTime().format(TIME_FORMAT), rowStyle);
                createStyledCell(row, 4, slot.getEndTime().format(TIME_FORMAT), rowStyle);
                createStyledCell(row, 5, String.valueOf(registered), rowStyle);
                createStyledCell(row, 6, String.valueOf(slot.getMaxStudents()), rowStyle);
                createStyledCell(row, 7, pct + "%", rowStyle);
                createStyledCell(row, 8, registered >= slot.getMaxStudents() ? "FULL"
                        : registered == 0 ? "EMPTY" : "AVAILABLE", rowStyle);
            }
            // Totals
            rowNum++;
            Row totRow = summarySheet.createRow(rowNum);
            createStyledCell(totRow, 0, "TOTAL", boldStyle);
            createStyledCell(totRow, 5, String.valueOf(grandTotal), boldStyle);
            createStyledCell(totRow, 6, String.valueOf(session.getTotalCapacity()), boldStyle);

            for (int i = 0; i < ovCols.length; i++) summarySheet.autoSizeColumn(i);

            // ── Sheet 2: Roster by Slot ───────────────────────────────────
            Sheet rosterSheet = workbook.createSheet("Roster by Group");
            rowNum = 0;

            for (TimeSlot slot : slots) {
                List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId())
                        .stream()
                        .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                || r.getStatus() == RegistrationStatus.PENDING)
                        .collect(Collectors.toList());

                Row slotRow = rosterSheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell slotCell = slotRow.createCell(0);
                slotCell.setCellValue(String.format("GROUP #%d   |   %s (%s)   |   %s - %s   |   %d / %d students   [%s]",
                        slot.getGroupNumber(),
                        slot.getSessionDate().format(DATE_FORMAT),
                        slot.getSessionDate().getDayOfWeek(),
                        slot.getStartTime().format(TIME_FORMAT),
                        slot.getEndTime().format(TIME_FORMAT),
                        registrations.size(),
                        slot.getMaxStudents(),
                        registrations.isEmpty() ? "EMPTY"
                                : registrations.size() >= slot.getMaxStudents() ? "FULL" : "AVAILABLE"));
                slotCell.setCellStyle(slotTitleStyle);
                rosterSheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

                if (registrations.isEmpty()) {
                    rosterSheet.createRow(rowNum++).createCell(1)
                            .setCellValue("No students registered for this slot.");
                } else {
                    Row colRow = rosterSheet.createRow(rowNum++);
                    String[] cols = {"#", "Student ID", "Full Name", "Username", "Email", "Program", "Phone", "Status", "Registered At"};
                    for (int i = 0; i < cols.length; i++) {
                        org.apache.poi.ss.usermodel.Cell c = colRow.createCell(i);
                        c.setCellValue(cols[i]);
                        c.setCellStyle(colStyle);
                    }

                    int count = 1;
                    for (Registration reg : registrations) {
                        User s = reg.getStudent();
                        CellStyle style = (count % 2 == 0) ? altDataStyle : dataStyle;
                        Row row = rosterSheet.createRow(rowNum++);
                        createStyledCell(row, 0, String.valueOf(count++), style);
                        createStyledCell(row, 1, nullSafe(s.getStudentId()), style);
                        createStyledCell(row, 2, nullSafe(s.getFullName()), style);
                        createStyledCell(row, 3, nullSafe(s.getUsername()), style);
                        createStyledCell(row, 4, nullSafe(s.getEmail()), style);
                        createStyledCell(row, 5, nullSafe(s.getProgramName()), style);
//                        createStyledCell(row, 6, nullSafe(s.getPhone()), style);
                        createStyledCell(row, 7, reg.getStatus().name(), style);
                        createStyledCell(row, 8, reg.getRegisteredAt().format(DATETIME_FORMAT), style);
                    }
                }
                rowNum++; // blank gap between slots
            }

            for (int i = 0; i < 9; i++) rosterSheet.autoSizeColumn(i);

            // ── Sheet 3: Waitlist ─────────────────────────────────────────
            List<Registration> waitlisted = registrationRepository
                    .findByLabSessionIdAndStatusOrderByWaitlistPositionAsc(sessionId, RegistrationStatus.WAITLISTED);

            if (!waitlisted.isEmpty()) {
                Sheet waitSheet = workbook.createSheet("Waitlist");
                rowNum = 0;

                Row wHdr = waitSheet.createRow(rowNum++);
                String[] wCols = {"Position", "Student ID", "Full Name", "Username", "Email", "Program", "Phone", "Registered At"};
                for (int i = 0; i < wCols.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = wHdr.createCell(i);
                    c.setCellValue(wCols[i]);
                    c.setCellStyle(headerStyle);
                }

                int wCount = 1;
                for (Registration reg : waitlisted) {
                    User s = reg.getStudent();
                    CellStyle style = (wCount % 2 == 0) ? altDataStyle : dataStyle;
                    Row row = waitSheet.createRow(rowNum++);
                    createStyledCell(row, 0, "#" + reg.getWaitlistPosition(), style);
                    createStyledCell(row, 1, nullSafe(s.getStudentId()), style);
                    createStyledCell(row, 2, nullSafe(s.getFullName()), style);
                    createStyledCell(row, 3, nullSafe(s.getUsername()), style);
                    createStyledCell(row, 4, nullSafe(s.getEmail()), style);
                    createStyledCell(row, 5, nullSafe(s.getProgramName()), style);
//                    createStyledCell(row, 6, nullSafe(s.getPhone()), style);
                    createStyledCell(row, 7, reg.getRegisteredAt().format(DATETIME_FORMAT), style);
                    wCount++;
                }

                for (int i = 0; i < wCols.length; i++) waitSheet.autoSizeColumn(i);
            }

            // ── Sheet 4: Flat all-registrations ───────────────────────────
            Sheet flatSheet = workbook.createSheet("All Registrations");
            rowNum = 0;

            Row flatHdr = flatSheet.createRow(rowNum++);
            String[] flatCols = {"Group #", "Date", "Day", "Start", "End",
                    "Student ID", "Full Name", "Username", "Email",
                    "Program", "Phone", "Status", "Registered At"};
            for (int i = 0; i < flatCols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = flatHdr.createCell(i);
                c.setCellValue(flatCols[i]);
                c.setCellStyle(headerStyle);
            }

            int fc = 1;
            for (TimeSlot slot : slots) {
                for (Registration reg : registrationRepository.findByTimeSlotId(slot.getId())) {
                    User s = reg.getStudent();
                    CellStyle style = (fc % 2 == 0) ? altDataStyle : dataStyle;
                    Row row = flatSheet.createRow(rowNum++);
                    createStyledCell(row, 0,  "Group #" + slot.getGroupNumber(), style);
                    createStyledCell(row, 1,  slot.getSessionDate().format(DATE_FORMAT), style);
                    createStyledCell(row, 2,  slot.getSessionDate().getDayOfWeek().toString(), style);
                    createStyledCell(row, 3,  slot.getStartTime().format(TIME_FORMAT), style);
                    createStyledCell(row, 4,  slot.getEndTime().format(TIME_FORMAT), style);
                    createStyledCell(row, 5,  nullSafe(s.getStudentId()), style);
                    createStyledCell(row, 6,  nullSafe(s.getFullName()), style);
                    createStyledCell(row, 7,  nullSafe(s.getUsername()), style);
                    createStyledCell(row, 8,  nullSafe(s.getEmail()), style);
                    createStyledCell(row, 9,  nullSafe(s.getProgramName()), style);
//                    createStyledCell(row, 10, nullSafe(s.getPhone()), style);
                    createStyledCell(row, 11, reg.getStatus().name(), style);
                    createStyledCell(row, 12, reg.getRegisteredAt().format(DATETIME_FORMAT), style);
                    fc++;
                }
            }
            for (int i = 0; i < flatCols.length; i++) flatSheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generateAllRegistrationsExcel() throws Exception {
        List<LabSession> sessions = labSessionRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle dataStyle    = createDataStyle(workbook);
            CellStyle altDataStyle = createAltDataStyle(workbook);

            Sheet sheet = workbook.createSheet("All Registrations");
            int rowNum = 0;

            Row hdr = sheet.createRow(rowNum++);
            String[] cols = {"Course Code", "Course Name", "Level", "Dept", "Session",
                    "Group #", "Date", "Day", "Start", "End",
                    "Student ID", "Full Name", "Username", "Email",
                    "Program", "Phone", "Status", "Registered At"};
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = hdr.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int count = 1;
            for (LabSession session : sessions) {
                for (TimeSlot slot : timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId())) {
                    for (Registration reg : registrationRepository.findByTimeSlotId(slot.getId())) {
                        User s = reg.getStudent();
                        CellStyle style = (count % 2 == 0) ? altDataStyle : dataStyle;
                        Row row = sheet.createRow(rowNum++);
                        createStyledCell(row, 0,  session.getCourse().getCourseCode(), style);
                        createStyledCell(row, 1,  session.getCourse().getCourseName(), style);
                        createStyledCell(row, 2,  session.getCourse().getLevel().getDisplayName(), style);
                        createStyledCell(row, 3,  session.getCourse().getDepartmentName(), style);
                        createStyledCell(row, 4,  session.getName(), style);
                        createStyledCell(row, 5,  "Slot #" + slot.getGroupNumber(), style);
                        createStyledCell(row, 6,  slot.getSessionDate().format(DATE_FORMAT), style);
                        createStyledCell(row, 7,  slot.getSessionDate().getDayOfWeek().toString(), style);
                        createStyledCell(row, 8,  slot.getStartTime().format(TIME_FORMAT), style);
                        createStyledCell(row, 9,  slot.getEndTime().format(TIME_FORMAT), style);
                        createStyledCell(row, 10, nullSafe(s.getStudentId()), style);
                        createStyledCell(row, 11, nullSafe(s.getFullName()), style);
                        createStyledCell(row, 12, nullSafe(s.getUsername()), style);
                        createStyledCell(row, 13, nullSafe(s.getEmail()), style);
                        createStyledCell(row, 14, nullSafe(s.getProgramName()), style);
//                        createStyledCell(row, 15, nullSafe(s.getPhone()), style);
                        createStyledCell(row, 16, reg.getStatus().name(), style);
                        createStyledCell(row, 17, reg.getRegisteredAt().format(DATETIME_FORMAT), style);
                        count++;
                    }
                }
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generateDateRangeReportExcel(LocalDate startDate, LocalDate endDate) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle boldStyle    = createBoldStyle(workbook);
            CellStyle slotStyle    = createSlotTitleStyle(workbook);
            CellStyle colStyle     = createColumnHeaderStyle(workbook);
            CellStyle dataStyle    = createDataStyle(workbook);
            CellStyle altDataStyle = createAltDataStyle(workbook);

            Sheet sheet = workbook.createSheet("Date Range Report");
            int rowNum = 0;

            org.apache.poi.ss.usermodel.Cell titleCell = sheet.createRow(rowNum++).createCell(0);
            titleCell.setCellValue("Registrations: " + startDate.format(DATE_FORMAT) + " to " + endDate.format(DATE_FORMAT));
            titleCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
            rowNum++;

            List<TimeSlot> slots = timeSlotRepository
                    .findBySessionDateBetweenOrderBySessionDateAsc(startDate, endDate);

            for (TimeSlot slot : slots) {
                LabSession session = slot.getLabSession();
                List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId())
                        .stream()
                        .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                || r.getStatus() == RegistrationStatus.PENDING)
                        .collect(Collectors.toList());

                Row slotRow = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell slotCell = slotRow.createCell(0);
                slotCell.setCellValue(String.format("%s | %s | Group #%d | %s-%s | %d/%d students",
                        session.getCourse().getCourseCode(), session.getName(),
                        slot.getGroupNumber(),
                        slot.getStartTime().format(TIME_FORMAT), slot.getEndTime().format(TIME_FORMAT),
                        registrations.size(), slot.getMaxStudents()));
                slotCell.setCellStyle(slotStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

                if (!registrations.isEmpty()) {
                    Row colRow = sheet.createRow(rowNum++);
                    String[] cols = {"#", "Student ID", "Full Name", "Username", "Email", "Program", "Phone", "Status"};
                    for (int i = 0; i < cols.length; i++) {
                        org.apache.poi.ss.usermodel.Cell c = colRow.createCell(i);
                        c.setCellValue(cols[i]);
                        c.setCellStyle(colStyle);
                    }
                    int count = 1;
                    for (Registration reg : registrations) {
                        User s = reg.getStudent();
                        CellStyle style = (count % 2 == 0) ? altDataStyle : dataStyle;
                        Row row = sheet.createRow(rowNum++);
                        createStyledCell(row, 0, String.valueOf(count++), style);
                        createStyledCell(row, 1, nullSafe(s.getStudentId()), style);
                        createStyledCell(row, 2, nullSafe(s.getFullName()), style);
                        createStyledCell(row, 3, nullSafe(s.getUsername()), style);
                        createStyledCell(row, 4, nullSafe(s.getEmail()), style);
                        createStyledCell(row, 5, nullSafe(s.getProgramName()), style);
//                        createStyledCell(row, 6, nullSafe(s.getPhone()), style);
                        createStyledCell(row, 7, reg.getStatus().name(), style);
                    }
                }
                rowNum++;
            }

            for (int i = 0; i < 9; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating date range Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    public byte[] generateDepartmentReportExcel(String departmentName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle  = createHeaderStyle(workbook);
            CellStyle boldStyle    = createBoldStyle(workbook);
            CellStyle slotStyle    = createSlotTitleStyle(workbook);
            CellStyle colStyle     = createColumnHeaderStyle(workbook);
            CellStyle dataStyle    = createDataStyle(workbook);
            CellStyle altDataStyle = createAltDataStyle(workbook);

            Sheet sheet = workbook.createSheet("Department Report");
            int rowNum = 0;

            org.apache.poi.ss.usermodel.Cell titleCell = sheet.createRow(rowNum++).createCell(0);
            titleCell.setCellValue("Department Report: " + departmentName
                    + "   |   Generated: " + LocalDateTime.now().format(DATETIME_FORMAT));
            titleCell.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            rowNum++;

            List<Course> courses = courseRepository.findByDepartmentName(departmentName);
            for (Course course : courses) {
                Row courseRow = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell courseCell = courseRow.createCell(0);
                courseCell.setCellValue("Course: " + course.getCourseCode()
                        + " - " + course.getCourseName()
                        + "   |   " + course.getLevel().getDisplayName());
                courseCell.setCellStyle(boldStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 9));

                for (LabSession session : labSessionRepository.findByCourseId(course.getId())) {
                    for (TimeSlot slot : timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId())) {
                        List<Registration> registrations = registrationRepository.findByTimeSlotId(slot.getId())
                                .stream()
                                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                        || r.getStatus() == RegistrationStatus.PENDING)
                                .collect(Collectors.toList());

                        Row slotRow = sheet.createRow(rowNum++);
                        org.apache.poi.ss.usermodel.Cell slotCell = slotRow.createCell(0);
                        slotCell.setCellValue(String.format("%s | Group #%d | %s (%s) | %s-%s | %d/%d",
                                session.getName(), slot.getGroupNumber(),
                                slot.getSessionDate().format(DATE_FORMAT),
                                slot.getSessionDate().getDayOfWeek(),
                                slot.getStartTime().format(TIME_FORMAT),
                                slot.getEndTime().format(TIME_FORMAT),
                                registrations.size(), slot.getMaxStudents()));
                        slotCell.setCellStyle(slotStyle);
                        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 9));

                        if (registrations.isEmpty()) {
                            sheet.createRow(rowNum++).createCell(1).setCellValue("No students registered.");
                        } else {
                            Row colRow = sheet.createRow(rowNum++);
                            String[] cols = {"#", "Student ID", "Full Name", "Username", "Email", "Program", "Phone", "Status", "Registered At"};
                            for (int i = 0; i < cols.length; i++) {
                                org.apache.poi.ss.usermodel.Cell c = colRow.createCell(i);
                                c.setCellValue(cols[i]);
                                c.setCellStyle(colStyle);
                            }
                            int count = 1;
                            for (Registration reg : registrations) {
                                User s = reg.getStudent();
                                CellStyle style = (count % 2 == 0) ? altDataStyle : dataStyle;
                                Row row = sheet.createRow(rowNum++);
                                createStyledCell(row, 0, String.valueOf(count++), style);
                                createStyledCell(row, 1, nullSafe(s.getStudentId()), style);
                                createStyledCell(row, 2, nullSafe(s.getFullName()), style);
                                createStyledCell(row, 3, nullSafe(s.getUsername()), style);
                                createStyledCell(row, 4, nullSafe(s.getEmail()), style);
                                createStyledCell(row, 5, nullSafe(s.getProgramName()), style);
//                                createStyledCell(row, 6, nullSafe(s.getPhone()), style);
                                createStyledCell(row, 7, reg.getStatus().name(), style);
                                createStyledCell(row, 8, reg.getRegisteredAt().format(DATETIME_FORMAT), style);
                            }
                        }
                        rowNum++;
                    }
                }
                rowNum++;
            }

            for (int i = 0; i < 10; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating department Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }






        /**
     * Professional iText table-based PDF — landscape A4.
     * Sections: header bar → session info card → groups overview →
     *           per-group rosters → waitlist → summary footer.
     */
    public byte[] generateSessionReportPdf(Long sessionId) {
        LabSession session = labSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab Session", "id", sessionId));

        List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(sessionId);
        List<Registration> waitlisted = registrationRepository
                .findByLabSessionIdAndStatusOrderByWaitlistPositionAsc(sessionId, RegistrationStatus.WAITLISTED);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            // 1. Header bar
            doc.add(pdfBuildReportHeader(session));
            doc.add(pdfSpacer(8));

            // 2. Session info card
            doc.add(pdfBuildSessionInfoCard(session));
            doc.add(pdfSpacer(12));

            // 3. Groups overview
            doc.add(pdfSectionTitle("Groups Overview", PDF_NAVY));
            doc.add(pdfSpacer(4));
            doc.add(pdfBuildGroupsOverviewTable(slots));
            doc.add(pdfSpacer(16));

            // 4. Per-group rosters
            doc.add(pdfSectionTitle("Student Rosters by Group", PDF_NAVY));
            doc.add(pdfSpacer(4));

            int totalRegistered = 0;
            for (TimeSlot slot : slots) {
                List<Registration> regs = registrationRepository.findByTimeSlotId(slot.getId())
                        .stream()
                        .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                || r.getStatus() == RegistrationStatus.PENDING)
                        .collect(Collectors.toList());
                totalRegistered += regs.size();

                doc.add(pdfBuildGroupBanner(slot, regs.size()));
                doc.add(pdfSpacer(3));
                if (regs.isEmpty()) {
                    doc.add(new Paragraph("No students registered for this group.")
                            .setFontColor(PDF_EMPTY).setItalic().setFontSize(8).setPaddingLeft(10));
                } else {
                    doc.add(pdfBuildRosterTable(regs));
                }
                doc.add(pdfSpacer(10));
            }

            // 5. Waitlist
            if (!waitlisted.isEmpty()) {
                doc.add(pdfSectionTitle("Waitlisted Students", PDF_WAITLIST));
                doc.add(pdfSpacer(4));
                doc.add(pdfBuildWaitlistTable(waitlisted));
                doc.add(pdfSpacer(16));
            }

            // 6. Summary footer
            doc.add(pdfBuildSummaryFooter(slots.size(), totalRegistered, waitlisted.size(), session.getTotalCapacity()));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating professional PDF for session {}: {}", sessionId, e.getMessage(), e);
            return convertTxtToPdf(new String(generateSessionReportTxt(sessionId)), "Lab Session Registration Report");
        }
    }

    public byte[] generateAllRegistrationsPdf() {
        List<LabSession> sessions = labSessionRepository.findAll();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            // Header bar
            Table header = pdfFullWidthTable(2);
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .add(new Paragraph("ALL REGISTRATIONS REPORT")
                            .setFontColor(PDF_WHITE).setBold().setFontSize(16))
                    .add(new Paragraph("Complete export across all sessions and departments")
                            .setFontColor(PDF_LIGHT_BLUE).setFontSize(10)));
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Generated").setFontColor(PDF_LIGHT_BLUE).setFontSize(8))
                    .add(new Paragraph(LocalDateTime.now().format(DATETIME_DISPLAY))
                            .setFontColor(PDF_WHITE).setFontSize(9)));
            doc.add(header);
            doc.add(pdfSpacer(14));

            int grandTotal = 0;

            for (LabSession session : sessions) {
                List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId());
                if (slots.isEmpty()) continue;

                // Session banner — dark blue
                Table sessionBanner = pdfFullWidthTable(1);
                sessionBanner.addCell(new Cell().setBackgroundColor(PDF_NAVY).setBorder(null)
                        .setPaddingTop(7).setPaddingBottom(7).setPaddingLeft(12).setPaddingRight(12)
                        .add(new Paragraph(
                                session.getCourse().getCourseCode() + "  –  " + session.getCourse().getCourseName()
                                        + "   |   " + session.getName()
                                        + "   |   " + session.getStartDate().format(DATE_DISPLAY)
                                        + "  →  " + session.getEndDate().format(DATE_DISPLAY))
                                .setFontColor(PDF_WHITE).setBold().setFontSize(9)));
                doc.add(sessionBanner);
                doc.add(pdfSpacer(4));

                for (TimeSlot slot : slots) {
                    List<Registration> regs = registrationRepository.findByTimeSlotId(slot.getId())
                            .stream()
                            .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                    || r.getStatus() == RegistrationStatus.PENDING)
                            .collect(Collectors.toList());
                    grandTotal += regs.size();

                    doc.add(pdfBuildGroupBanner(slot, regs.size()));
                    doc.add(pdfSpacer(3));
                    if (regs.isEmpty()) {
                        doc.add(new Paragraph("No students registered for this group.")
                                .setFontColor(PDF_EMPTY).setItalic().setFontSize(8).setPaddingLeft(10));
                    } else {
                        doc.add(pdfBuildRosterTable(regs));
                    }
                    doc.add(pdfSpacer(8));
                }
                doc.add(pdfSpacer(8));
            }

            // Summary footer
            doc.add(pdfBuildSummaryFooter(sessions.size(), grandTotal, 0, 0));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating all-registrations PDF: {}", e.getMessage(), e);
            return convertTxtToPdf(new String(generateAllRegistrationsCsv()), "All Registrations Report");
        }
    }

    public byte[] generateCourseReportPdf(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        List<LabSession> sessions = labSessionRepository.findByCourseId(courseId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            // Header bar
            Table header = pdfFullWidthTable(2);
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .add(new Paragraph("COURSE LAB REGISTRATION REPORT")
                            .setFontColor(PDF_WHITE).setBold().setFontSize(16))
                    .add(new Paragraph(course.getCourseCode() + "  –  " + course.getCourseName())
                            .setFontColor(PDF_LIGHT_BLUE).setFontSize(11)));
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Generated").setFontColor(PDF_LIGHT_BLUE).setFontSize(8))
                    .add(new Paragraph(LocalDateTime.now().format(DATETIME_DISPLAY))
                            .setFontColor(PDF_WHITE).setFontSize(9)));
            doc.add(header);
            doc.add(pdfSpacer(8));

            // Course info card
            Table info = pdfFullWidthTable(4);
            info.setBorder(new SolidBorder(PDF_BORDER, 1));
            pdfAddInfoCell(info, "Course Code",  course.getCourseCode());
            pdfAddInfoCell(info, "Course Name",  course.getCourseName());
            pdfAddInfoCell(info, "Level",        course.getLevel().getDisplayName());
            pdfAddInfoCell(info, "Department",   course.getDepartment().toString());
            doc.add(info);
            doc.add(pdfSpacer(14));

            int totalRegistered = 0;

            for (LabSession session : sessions) {
                List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId());

                // Session banner
                Table sessionBanner = pdfFullWidthTable(1);
                sessionBanner.addCell(new Cell().setBackgroundColor(PDF_NAVY).setBorder(null)
                        .setPaddingTop(7).setPaddingBottom(7).setPaddingLeft(12).setPaddingRight(12)
                        .add(new Paragraph(
                                "Session: " + session.getName()
                                        + "   |   " + session.getStartDate().format(DATE_DISPLAY)
                                        + "  →  " + session.getEndDate().format(DATE_DISPLAY)
                                        + "   |   Days: " + String.join(", ", session.getSessionDaysSet())
                                        + "   |   Status: " + session.getStatus())
                                .setFontColor(PDF_WHITE).setBold().setFontSize(9)));
                doc.add(sessionBanner);
                doc.add(pdfSpacer(4));

                // Groups overview for this session
                doc.add(pdfSectionTitle("Groups Overview", PDF_COL_HDR));
                doc.add(pdfSpacer(3));
                doc.add(pdfBuildGroupsOverviewTable(slots));
                doc.add(pdfSpacer(10));

                // Per-group rosters
                doc.add(pdfSectionTitle("Student Rosters", PDF_NAVY));
                doc.add(pdfSpacer(4));

                for (TimeSlot slot : slots) {
                    List<Registration> regs = registrationRepository.findByTimeSlotId(slot.getId())
                            .stream()
                            .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                    || r.getStatus() == RegistrationStatus.PENDING)
                            .collect(Collectors.toList());
                    totalRegistered += regs.size();

                    doc.add(pdfBuildGroupBanner(slot, regs.size()));
                    doc.add(pdfSpacer(3));
                    if (regs.isEmpty()) {
                        doc.add(new Paragraph("No students registered for this group.")
                                .setFontColor(PDF_EMPTY).setItalic().setFontSize(8).setPaddingLeft(10));
                    } else {
                        doc.add(pdfBuildRosterTable(regs));
                    }
                    doc.add(pdfSpacer(8));
                }

                // Waitlist for this session
                List<Registration> waitlisted = registrationRepository
                        .findByLabSessionIdAndStatusOrderByWaitlistPositionAsc(session.getId(), RegistrationStatus.WAITLISTED);
                if (!waitlisted.isEmpty()) {
                    doc.add(pdfSectionTitle("Waitlisted Students", PDF_WAITLIST));
                    doc.add(pdfSpacer(4));
                    doc.add(pdfBuildWaitlistTable(waitlisted));
                }
                doc.add(pdfSpacer(14));
            }

            // Summary footer
            doc.add(pdfBuildSummaryFooter(sessions.size(), totalRegistered, 0, 0));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating course PDF for course {}: {}", courseId, e.getMessage(), e);
            return convertTxtToPdf(new String(generateCourseReport(courseId)), "Course Lab Registration Report");
        }
    }

    public byte[] generateDateRangeReportPdf(LocalDate startDate, LocalDate endDate) {
        List<TimeSlot> slots = timeSlotRepository
                .findBySessionDateBetweenOrderBySessionDateAsc(startDate, endDate);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            // Header bar
            Table header = pdfFullWidthTable(2);
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .add(new Paragraph("DATE RANGE REGISTRATION REPORT")
                            .setFontColor(PDF_WHITE).setBold().setFontSize(16))
                    .add(new Paragraph(startDate.format(DATE_DISPLAY) + "  →  " + endDate.format(DATE_DISPLAY))
                            .setFontColor(PDF_LIGHT_BLUE).setFontSize(11)));
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Generated").setFontColor(PDF_LIGHT_BLUE).setFontSize(8))
                    .add(new Paragraph(LocalDateTime.now().format(DATETIME_DISPLAY))
                            .setFontColor(PDF_WHITE).setFontSize(9)));
            doc.add(header);
            doc.add(pdfSpacer(14));

            if (slots.isEmpty()) {
                doc.add(new Paragraph("No time slots found for the selected date range.")
                        .setFontColor(PDF_EMPTY).setItalic().setFontSize(10));
                doc.close();
                return baos.toByteArray();
            }

            // Overview table — all slots in range
            doc.add(pdfSectionTitle("Slots in Range (" + slots.size() + " total)", PDF_NAVY));
            doc.add(pdfSpacer(4));
            doc.add(pdfBuildGroupsOverviewTable(slots));
            doc.add(pdfSpacer(16));

            // Per-slot rosters
            doc.add(pdfSectionTitle("Student Rosters by Group", PDF_NAVY));
            doc.add(pdfSpacer(4));

            int totalRegistered = 0;

            // Group slots by session for cleaner organisation
            LabSession currentSession = null;
            for (TimeSlot slot : slots) {
                LabSession session = slot.getLabSession();

                // Print a session divider when session changes
                if (currentSession == null || !currentSession.getId().equals(session.getId())) {
                    if (currentSession != null) doc.add(pdfSpacer(6));
                    Table sessionBanner = pdfFullWidthTable(1);
                    sessionBanner.addCell(new Cell().setBackgroundColor(PDF_NAVY).setBorder(null)
                            .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(12).setPaddingRight(12)
                            .add(new Paragraph(
                                    session.getCourse().getCourseCode() + "  –  " + session.getName()
                                            + "   |   " + session.getCourse().getDepartment())
                                    .setFontColor(PDF_WHITE).setBold().setFontSize(9)));
                    doc.add(sessionBanner);
                    doc.add(pdfSpacer(4));
                    currentSession = session;
                }

                List<Registration> regs = registrationRepository.findByTimeSlotId(slot.getId())
                        .stream()
                        .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                || r.getStatus() == RegistrationStatus.PENDING)
                        .collect(Collectors.toList());
                totalRegistered += regs.size();

                doc.add(pdfBuildGroupBanner(slot, regs.size()));
                doc.add(pdfSpacer(3));
                if (regs.isEmpty()) {
                    doc.add(new Paragraph("No students registered for this group.")
                            .setFontColor(PDF_EMPTY).setItalic().setFontSize(8).setPaddingLeft(10));
                } else {
                    doc.add(pdfBuildRosterTable(regs));
                }
                doc.add(pdfSpacer(8));
            }

            // Summary footer
            doc.add(pdfBuildSummaryFooter(slots.size(), totalRegistered, 0, 0));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating date range PDF ({} to {}): {}", startDate, endDate, e.getMessage(), e);
            return convertTxtToPdf(new String(generateDateRangeReport(startDate, endDate)),
                    "Registrations: " + startDate.format(DATE_FORMAT) + " to " + endDate.format(DATE_FORMAT));
        }
    }

    public byte[] generateDepartmentReportPdf(String departmentName) {
        List<Course> courses = courseRepository.findByDepartmentName(departmentName);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(36, 36, 36, 36);

            // Header bar
            Table header = pdfFullWidthTable(2);
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .add(new Paragraph("DEPARTMENT REGISTRATION REPORT")
                            .setFontColor(PDF_WHITE).setBold().setFontSize(16))
                    .add(new Paragraph(departmentName)
                            .setFontColor(PDF_LIGHT_BLUE).setFontSize(11)));
            header.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Generated").setFontColor(PDF_LIGHT_BLUE).setFontSize(8))
                    .add(new Paragraph(LocalDateTime.now().format(DATETIME_DISPLAY))
                            .setFontColor(PDF_WHITE).setFontSize(9)));
            doc.add(header);
            doc.add(pdfSpacer(14));

            int totalRegistered = 0;
            int totalGroups = 0;

            for (Course course : courses) {
                List<LabSession> sessions = labSessionRepository.findByCourseId(course.getId());
                if (sessions.isEmpty()) continue;

                // Course banner — indigo
                DeviceRgb courseColor = new DeviceRgb(0x3B, 0x27, 0x8C); // deep indigo
                Table courseBanner = pdfFullWidthTable(1);
                courseBanner.addCell(new Cell().setBackgroundColor(courseColor).setBorder(null)
                        .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(12).setPaddingRight(12)
                        .add(new Paragraph(
                                course.getCourseCode() + "  –  " + course.getCourseName()
                                        + "   |   " + course.getLevel().getDisplayName())
                                .setFontColor(PDF_WHITE).setBold().setFontSize(10)));
                doc.add(courseBanner);
                doc.add(pdfSpacer(4));

                for (LabSession session : sessions) {
                    List<TimeSlot> slots = timeSlotRepository.findByLabSessionIdOrderByDateAndSlot(session.getId());
                    totalGroups += slots.size();

                    // Session banner
                    Table sessionBanner = pdfFullWidthTable(1);
                    sessionBanner.addCell(new Cell().setBackgroundColor(PDF_BLUE).setBorder(null)
                            .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(12).setPaddingRight(12)
                            .add(new Paragraph(
                                    "Session: " + session.getName()
                                            + "   |   " + session.getStartDate().format(DATE_DISPLAY)
                                            + "  →  " + session.getEndDate().format(DATE_DISPLAY)
                                            + "   |   Days: " + String.join(", ", session.getSessionDaysSet()))
                                    .setFontColor(PDF_WHITE).setFontSize(8.5f)));
                    doc.add(sessionBanner);
                    doc.add(pdfSpacer(3));

                    // Groups overview
                    doc.add(pdfBuildGroupsOverviewTable(slots));
                    doc.add(pdfSpacer(8));

                    // Per-group rosters
                    for (TimeSlot slot : slots) {
                        List<Registration> regs = registrationRepository.findByTimeSlotId(slot.getId())
                                .stream()
                                .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED
                                        || r.getStatus() == RegistrationStatus.PENDING)
                                .collect(Collectors.toList());
                        totalRegistered += regs.size();

                        doc.add(pdfBuildGroupBanner(slot, regs.size()));
                        doc.add(pdfSpacer(3));
                        if (regs.isEmpty()) {
                            doc.add(new Paragraph("No students registered for this group.")
                                    .setFontColor(PDF_EMPTY).setItalic().setFontSize(8).setPaddingLeft(10));
                        } else {
                            doc.add(pdfBuildRosterTable(regs));
                        }
                        doc.add(pdfSpacer(6));
                    }

                    // Waitlist for this session
                    List<Registration> waitlisted = registrationRepository
                            .findByLabSessionIdAndStatusOrderByWaitlistPositionAsc(session.getId(), RegistrationStatus.WAITLISTED);
                    if (!waitlisted.isEmpty()) {
                        doc.add(pdfSectionTitle("Waitlisted Students", PDF_WAITLIST));
                        doc.add(pdfSpacer(4));
                        doc.add(pdfBuildWaitlistTable(waitlisted));
                    }
                    doc.add(pdfSpacer(10));
                }
                doc.add(pdfSpacer(8));
            }

            // Summary footer
            doc.add(pdfBuildSummaryFooter(totalGroups, totalRegistered, 0, 0));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating department PDF for {}: {}", departmentName, e.getMessage(), e);
            return convertTxtToPdf("Department Report: " + departmentName, "Department Report: " + departmentName);
        }
    }

    // =========================================================================
    // LEGACY ALIASES
    // =========================================================================

    public byte[] generateSessionReport(Long sessionId)  { return generateSessionReportTxt(sessionId); }
    public byte[] generateAllRegistrationsReport()       { return generateAllRegistrationsCsv(); }

    // =========================================================================
    // PDF BUILDER HELPERS (iText)
    // =========================================================================

    private Table pdfBuildReportHeader(LabSession session) {
        Table t = pdfFullWidthTable(2);
        t.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                .add(new Paragraph("LAB SESSION REGISTRATION REPORT")
                        .setFontColor(PDF_WHITE).setBold().setFontSize(16))
                .add(new Paragraph(session.getName())
                        .setFontColor(PDF_LIGHT_BLUE).setFontSize(11)));
        t.addCell(new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("Generated").setFontColor(PDF_LIGHT_BLUE).setFontSize(8))
                .add(new Paragraph(LocalDateTime.now().format(DATETIME_DISPLAY))
                        .setFontColor(PDF_WHITE).setFontSize(9)));
        return t;
    }

    private Table pdfBuildSessionInfoCard(LabSession session) {
        Table t = pdfFullWidthTable(4);
        t.setBorder(new SolidBorder(PDF_BORDER, 1));
        pdfAddInfoCell(t, "Course",        session.getCourse().getCourseCode() + " – " + session.getCourse().getCourseName());
        pdfAddInfoCell(t, "Level",         session.getCourse().getLevel().getDisplayName());
        pdfAddInfoCell(t, "Department",    session.getCourse().getDepartment().getName().toString());
        pdfAddInfoCell(t, "Lab Room",      session.getLabRoom());
        pdfAddInfoCell(t, "Date Range",    session.getStartDate().format(DATE_DISPLAY) + "  →  " + session.getEndDate().format(DATE_DISPLAY));
        pdfAddInfoCell(t, "Days",          String.join(", ", session.getSessionDaysSet()));
        pdfAddInfoCell(t, "Total Capacity", String.valueOf(session.getTotalCapacity()));
        pdfAddInfoCell(t, "Status",        session.getStatus().name());
        return t;
    }

    private Table pdfBuildGroupsOverviewTable(List<TimeSlot> slots) {
        Table t = pdfFullWidthTable(7);
        for (String h : new String[]{"Group #", "Date", "Day", "Time", "Registered", "Capacity", "Status"})
            t.addHeaderCell(pdfColHeaderCell(h, PDF_COL_HDR));

        int idx = 0;
        for (TimeSlot slot : slots) {
            int reg = slot.getCurrentCount();
            DeviceRgb bg = (idx++ % 2 == 1) ? PDF_ALT_ROW : PDF_WHITE;
            String statusText;
            DeviceRgb statusColor;
            if (reg >= slot.getMaxStudents()) { statusText = "FULL";      statusColor = PDF_FULL; }
            else if (reg == 0)               { statusText = "EMPTY";     statusColor = PDF_EMPTY; }
            else                             { statusText = "AVAILABLE"; statusColor = PDF_AVAILABLE; }

            t.addCell(pdfDataCell("Group #" + slot.getGroupNumber(), bg));
            t.addCell(pdfDataCell(slot.getSessionDate().format(DATE_DISPLAY), bg));
            t.addCell(pdfDataCell(slot.getSessionDate().getDayOfWeek().toString(), bg));
            t.addCell(pdfDataCell(slot.getStartTime().format(TIME_FORMAT) + " – " + slot.getEndTime().format(TIME_FORMAT), bg));
            t.addCell(pdfDataCell(String.valueOf(reg), bg).setTextAlignment(TextAlignment.CENTER));
            t.addCell(pdfDataCell(String.valueOf(slot.getMaxStudents()), bg).setTextAlignment(TextAlignment.CENTER));
            t.addCell(new Cell().setBackgroundColor(PDF_WHITE).setBorder(new SolidBorder(PDF_BORDER, 0.5f))
                    .setPadding(4).setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(statusText).setFontColor(statusColor).setBold().setFontSize(8)));
        }
        return t;
    }

    private Table pdfBuildGroupBanner(TimeSlot slot, int registered) {
        String fill = registered >= slot.getMaxStudents() ? "FULL"
                : registered == 0 ? "EMPTY" : "AVAILABLE";
        Table t = pdfFullWidthTable(1);
        t.addCell(new Cell().setBackgroundColor(PDF_BLUE).setBorder(null)
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(10).setPaddingRight(10)
                .add(new Paragraph(String.format(
                        "GROUP #%d   |   %s (%s)   |   %s – %s   |   %d / %d students   [%s]",
                        slot.getGroupNumber(),
                        slot.getSessionDate().format(DATE_DISPLAY),
                        slot.getSessionDate().getDayOfWeek(),
                        slot.getStartTime().format(TIME_FORMAT),
                        slot.getEndTime().format(TIME_FORMAT),
                        registered, slot.getMaxStudents(), fill))
                        .setFontColor(PDF_WHITE).setBold().setFontSize(9)));
        return t;
    }

    private Table pdfBuildRosterTable(List<Registration> regs) {
        float[] widths = {4f, 10f, 18f, 12f, 22f, 20f, 9f, 14f};
        Table t = new Table(UnitValue.createPercentArray(widths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(PDF_BORDER, 0.5f));
        for (String h : new String[]{"#", "Student ID", "Full Name", "Username", "Email", "Program", "Status", "Registered At"})
            t.addHeaderCell(pdfColHeaderCell(h, PDF_COL_HDR));

        int idx = 0;
        for (Registration reg : regs) {
            User s = reg.getStudent();
            DeviceRgb bg = (idx++ % 2 == 1) ? PDF_ALT_ROW : PDF_WHITE;
            DeviceRgb sc = "CONFIRMED".equals(reg.getStatus().name()) ? PDF_AVAILABLE
                    : "PENDING".equals(reg.getStatus().name())   ? PDF_ORANGE
                    : PDF_EMPTY;
            t.addCell(pdfDataCell(String.valueOf(idx), bg).setTextAlignment(TextAlignment.CENTER));
            t.addCell(pdfDataCell(nullSafe(s.getStudentId()), bg));
            t.addCell(pdfDataCell(nullSafe(s.getFullName()), bg));
            t.addCell(pdfDataCell(nullSafe(s.getUsername()), bg));
            t.addCell(pdfDataCell(nullSafe(s.getEmail()), bg).setFontSize(7.5f));
            t.addCell(pdfDataCell(nullSafe(s.getProgramName()), bg));
            t.addCell(new Cell().setBackgroundColor(PDF_WHITE).setBorder(new SolidBorder(PDF_BORDER, 0.5f))
                    .setPadding(4).setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(reg.getStatus().name()).setFontColor(sc).setBold().setFontSize(8)));
            t.addCell(pdfDataCell(reg.getRegisteredAt().format(DATETIME_DISPLAY), bg).setFontSize(7.5f));
        }
        return t;
    }

    private Table pdfBuildWaitlistTable(List<Registration> waitlisted) {
        float[] widths = {5f, 10f, 18f, 12f, 22f, 20f, 14f};
        Table t = new Table(UnitValue.createPercentArray(widths))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(PDF_BORDER, 0.5f));
        for (String h : new String[]{"Pos", "Student ID", "Full Name", "Username", "Email", "Program", "Registered At"})
            t.addHeaderCell(pdfColHeaderCell(h, PDF_WAITLIST));

        int idx = 0;
        for (Registration reg : waitlisted) {
            User s = reg.getStudent();
            DeviceRgb bg = (idx++ % 2 == 1) ? new DeviceRgb(0xFD, 0xF0, 0xE6) : PDF_WHITE;
            t.addCell(pdfDataCell("#" + reg.getWaitlistPosition(), bg).setTextAlignment(TextAlignment.CENTER));
            t.addCell(pdfDataCell(nullSafe(s.getStudentId()), bg));
            t.addCell(pdfDataCell(nullSafe(s.getFullName()), bg));
            t.addCell(pdfDataCell(nullSafe(s.getUsername()), bg));
            t.addCell(pdfDataCell(nullSafe(s.getEmail()), bg).setFontSize(7.5f));
            t.addCell(pdfDataCell(nullSafe(s.getProgramName()), bg));
            t.addCell(pdfDataCell(reg.getRegisteredAt().format(DATETIME_DISPLAY), bg).setFontSize(7.5f));
        }
        return t;
    }

    private Table pdfBuildSummaryFooter(int groups, int registered, int waitlisted, int capacity) {
        Table t = pdfFullWidthTable(4);
        t.addCell(pdfStatCell("Total Groups",     String.valueOf(groups)));
        t.addCell(pdfStatCell("Total Registered", String.valueOf(registered)));
        t.addCell(pdfStatCell("Total Waitlisted", String.valueOf(waitlisted)));
        t.addCell(pdfStatCell("Total Capacity",   String.valueOf(capacity)));
        return t;
    }

    // ── PDF cell / element helpers ────────────────────────────────────────────

    private Cell pdfColHeaderCell(String text, DeviceRgb bg) {
        return new Cell().setBackgroundColor(bg)
                .setBorder(new SolidBorder(PDF_BORDER, 0.5f)).setPadding(5)
                .add(new Paragraph(text).setFontColor(PDF_WHITE).setBold().setFontSize(8));
    }

    private Cell pdfDataCell(String text, DeviceRgb bg) {
        return new Cell().setBackgroundColor(bg)
                .setBorder(new SolidBorder(PDF_BORDER, 0.5f))
                .setPaddingTop(4).setPaddingBottom(4).setPaddingLeft(5).setPaddingRight(5)
                .add(new Paragraph(text != null ? text : "").setFontColor(PDF_TEXT).setFontSize(8));
    }

    private void pdfAddInfoCell(Table t, String label, String value) {
        t.addCell(new Cell().setBackgroundColor(PDF_INFO_BG)
                .setBorder(new SolidBorder(PDF_BORDER, 0.5f))
                .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(8).setPaddingRight(8)
                .add(new Paragraph(label).setFontColor(PDF_BLUE).setBold().setFontSize(7.5f))
                .add(new Paragraph(value != null ? value : "—").setFontColor(PDF_TEXT).setFontSize(9)));
    }

    private Cell pdfStatCell(String label, String value) {
        return new Cell().setBorder(null).setBackgroundColor(PDF_NAVY).setPadding(12)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(value).setFontColor(PDF_WHITE).setBold().setFontSize(20))
                .add(new Paragraph(label).setFontColor(PDF_LIGHT_BLUE).setFontSize(9));
    }

    private Paragraph pdfSectionTitle(String text, DeviceRgb color) {
        return new Paragraph(text).setFontColor(color).setBold().setFontSize(11)
                .setBorderBottom(new SolidBorder(color, 1.5f)).setPaddingBottom(3);
    }

    private Paragraph pdfSpacer(float h) {
        return new Paragraph("").setMarginTop(0).setMarginBottom(0).setHeight(h);
    }

    private Table pdfFullWidthTable(int cols) {
        return new Table(UnitValue.createPercentArray(cols))
                .setWidth(UnitValue.createPercentValue(100)).setBorder(null);
    }

    // =========================================================================
    // EXCEL STYLE HELPERS
    // =========================================================================

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(s); return s;
    }

    private CellStyle createColumnHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(s); return s;
    }

    private CellStyle createSlotTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(s); return s;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(s); return s;
    }

    private CellStyle createAltDataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(s); return s;
    }

    private CellStyle createColorStyle(Workbook wb, IndexedColors color) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(color.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(s); return s;
    }

    private CellStyle createBoldStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 13);
        s.setFont(f); return s;
    }

    private CellStyle getStatusStyle(Workbook wb, int registered, int maxSize) {
        if (registered >= maxSize) return createColorStyle(wb, IndexedColors.CORAL);
        if (registered == 0)       return createColorStyle(wb, IndexedColors.LIGHT_GREEN);
        return createColorStyle(wb, IndexedColors.LIGHT_YELLOW);
    }

    private void setBorders(CellStyle s) {
        s.setBorderBottom(BorderStyle.THIN); s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);  s.setBorderRight(BorderStyle.THIN);
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, String value, Workbook wb) {
        Row row = sheet.createRow(rowNum);
        CellStyle ls = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); ls.setFont(f);
        org.apache.poi.ss.usermodel.Cell lc = row.createCell(0); lc.setCellValue(label); lc.setCellStyle(ls);
        row.createCell(1).setCellValue(value);
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    // =========================================================================
    // SHARED HELPERS
    // =========================================================================

    private String nullSafe(String val)          { return val != null ? val : ""; }

    private String truncate(String str, int max) {
        if (str == null) return "";
        return str.length() <= max ? str : str.substring(0, max - 3) + "...";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private byte[] convertTxtToPdf(String content, String title) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);
            document.add(new Paragraph(title).setFontSize(16).setBold());
            document.add(new Paragraph("Generated: " + LocalDateTime.now().format(DATETIME_FORMAT)).setFontSize(10));
            document.add(new Paragraph(""));
            for (String line : content.split("\n"))
                document.add(new Paragraph(line).setFontSize(9));
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error converting to PDF: {}", e.getMessage());
            return content.getBytes();
        }
    }
}