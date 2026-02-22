package com.labregistration.config;

import com.labregistration.model.*;
import com.labregistration.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final LabSessionRepository labSessionRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping...");
            return;
        }

        log.info("Initializing sample data...");

        // Create Faculty
        Faculty scienceFaculty = createFaculty("FSCI", "Faculty of Science", "Prof. John Mensah");

        // Create Departments
        Department physicsDept = createDepartment("PHY", "Physics", scienceFaculty, "Dr. Kwame Asante");
        Department csDept = createDepartment("CS", "Computer Science", scienceFaculty, "Dr. Ama Serwaa");
        Department chemDept = createDepartment("CHM", "Chemistry", scienceFaculty, "Dr. Kofi Boateng");
        Department mathDept = createDepartment("MTH", "Mathematics", scienceFaculty, "Dr. Yaw Mensah");

        // Create Programs
        Program csProgram = createProgram("BSC-CS", "BSc Computer Science", csDept, 4, "BSc");
        Program physicsProgram = createProgram("BSC-PHY", "BSc Physics", physicsDept, 4, "BSc");
        Program chemProgram = createProgram("BSC-CHM", "BSc Chemistry", chemDept, 4, "BSc");
        Program labTechProgram = createProgram("BSC-LT", "BSc Laboratory Technology", physicsDept, 4, "BSc");
        Program mathProgram = createProgram("BSC-MTH", "BSc Mathematics", mathDept, 4, "BSc");

        // Create Super Admin
        User superAdmin = createUser("ADMIN001", "admin@university.edu", "admin123", 
                "Super", "Admin", null, Level.LEVEL_100, Role.SUPER_ADMIN);

        // Create Lab Manager
        User labManager = createUser("LABMGR001", "labmanager@university.edu", "manager123",
                "Lab", "Manager", null, Level.LEVEL_100, Role.LAB_MANAGER);

        // Create Students from different programs
        User student1 = createUser("STU001", "john.doe@university.edu", "password123",
                "John", "Doe", csProgram, Level.LEVEL_200, Role.STUDENT);
        User student2 = createUser("STU002", "jane.smith@university.edu", "password123",
                "Jane", "Smith", physicsProgram, Level.LEVEL_200, Role.STUDENT);
        User student3 = createUser("STU003", "bob.wilson@university.edu", "password123",
                "Bob", "Wilson", labTechProgram, Level.LEVEL_200, Role.STUDENT);
        User student4 = createUser("STU004", "alice.brown@university.edu", "password123",
                "Alice", "Brown", chemProgram, Level.LEVEL_200, Role.STUDENT);
        User student5 = createUser("STU005", "charlie.davis@university.edu", "password123",
                "Charlie", "Davis", mathProgram, Level.LEVEL_200, Role.STUDENT);

        // Create Courses
        Course physicsCourse = createCourse("PHY101", "Introduction to Physics", 
                physicsDept, Level.LEVEL_100, Semester.FIRST_SEMESTER, "2024/2025", superAdmin);
        Course chemCourse = createCourse("CHM101", "General Chemistry",
                chemDept, Level.LEVEL_100, Semester.FIRST_SEMESTER, "2024/2025", superAdmin);

        // Create Lab Sessions
        LocalDate startDate = getNextMonday();
        LocalDate endDate = startDate.plusWeeks(3).minusDays(1);

        // Physics Lab - 3 weeks, Monday and Wednesday
        // Allowed programs: CS, Physics, Lab Technology
        Set<Program> physicsLabPrograms = new HashSet<>();
        physicsLabPrograms.add(csProgram);
        physicsLabPrograms.add(physicsProgram);
        physicsLabPrograms.add(labTechProgram);

        LabSession physicsLab = createLabSession(
                "Physics Lab - Mechanics",
                "Introduction to laboratory equipment and mechanics experiments. Students will attend the SAME time slot every week for 3 weeks.",
                "Science Building Room 101",
                startDate, endDate,
                LocalTime.of(9, 0), LocalTime.of(12, 0),
                Set.of("MONDAY", "WEDNESDAY"),
                4, 3,
                physicsCourse, labManager,
                physicsLabPrograms, false
        );

        // Chemistry Lab - 2 weeks, Tuesday - Open to all programs
        LabSession chemLab = createLabSession(
                "Chemistry Lab - Basic Techniques",
                "Basic chemistry laboratory techniques. Open to ALL science students.",
                "Chemistry Building Room 201",
                startDate.plusDays(1), startDate.plusDays(1).plusWeeks(2),
                LocalTime.of(14, 0), LocalTime.of(17, 0),
                Set.of("TUESDAY"),
                5, 2,
                chemCourse, labManager,
                new HashSet<>(), true  // Open to all programs
        );

        log.info("=========================================================");
        log.info("DATA INITIALIZATION COMPLETE!");
        log.info("=========================================================");
        log.info("");
        log.info("TEST CREDENTIALS:");
        log.info("---------------------------------------------------------");
        log.info("SUPER ADMIN:  admin@university.edu / admin123");
        log.info("LAB MANAGER:  labmanager@university.edu / manager123");
        log.info("");
        log.info("STUDENTS:");
        log.info("  - john.doe@university.edu / password123 (Computer Science)");
        log.info("  - jane.smith@university.edu / password123 (Physics)");
        log.info("  - bob.wilson@university.edu / password123 (Lab Technology)");
        log.info("  - alice.brown@university.edu / password123 (Chemistry)");
        log.info("  - charlie.davis@university.edu / password123 (Mathematics)");
        log.info("");
        log.info("LAB SESSIONS CREATED:");
        log.info("---------------------------------------------------------");
        log.info("1. Physics Lab - Mechanics");
        log.info("   Duration: 3 weeks ({} to {})", startDate, endDate);
        log.info("   Days: Monday & Wednesday, 9:00 AM - 12:00 PM");
        log.info("   Slots: 3 time slots per day, 4 students per slot");
        log.info("   Allowed Programs: Computer Science, Physics, Lab Technology");
        log.info("");
        log.info("2. Chemistry Lab - Basic Techniques");
        log.info("   Duration: 2 weeks");
        log.info("   Days: Tuesday, 2:00 PM - 5:00 PM");
        log.info("   Slots: 2 time slots per day, 5 students per slot");
        log.info("   Open to: ALL PROGRAMS");
        log.info("=========================================================");
    }

    private LocalDate getNextMonday() {
        LocalDate date = LocalDate.now().plusDays(7);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private Faculty createFaculty(String code, String name, String dean) {
        Faculty faculty = Faculty.builder()
                .code(code)
                .name(name)
                .dean(dean)
                .active(true)
                .build();
        return facultyRepository.save(faculty);
    }

    private Department createDepartment(String code, String name, Faculty faculty, String hod) {
        Department dept = Department.builder()
                .code(code)
                .name(name)
                .faculty(faculty)
                .headOfDepartment(hod)
                .active(true)
                .build();
        return departmentRepository.save(dept);
    }

    private Program createProgram(String code, String name, Department dept, int years, String degree) {
        Program program = Program.builder()
                .code(code)
                .name(name)
                .department(dept)
                .durationYears(years)
                .degreeType(degree)
                .active(true)
                .build();
        return programRepository.save(program);
    }

    private User createUser(String studentId, String email, String password, String firstName, 
                           String lastName, Program program, Level level, Role role) {
        User user = User.builder()
                .studentId(studentId)
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .program(program)
                .level(level)
                .role(role)
                .enabled(true)
                .emailVerified(true)
                .build();
        return userRepository.save(user);
    }

    private Course createCourse(String code, String name, Department dept, Level level, 
                               Semester semester, String year, User instructor) {
        Course course = Course.builder()
                .courseCode(code)
                .courseName(name)
                .department(dept)
                .level(level)
                .semester(semester)
                .academicYear(year)
                .creditHours(3)
                .instructor(instructor)
                .active(true)
                .hasLab(true)
                .build();
        return courseRepository.save(course);
    }

    private LabSession createLabSession(String name, String description, String labRoom,
                                        LocalDate startDate, LocalDate endDate,
                                        LocalTime startTime, LocalTime endTime,
                                        Set<String> sessionDays, int maxStudents, int slotsPerDay,
                                        Course course, User createdBy,
                                        Set<Program> allowedPrograms, boolean openToAll) {
        LabSession session = LabSession.builder()
                .name(name)
                .description(description)
                .labRoom(labRoom)
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .sessionDays(String.join(",", sessionDays))
                .maxStudentsPerSlot(maxStudents)
                .slotsPerDay(slotsPerDay)
                .status(SessionStatus.OPEN)
                .course(course)
                .createdBy(createdBy)
                .allowedPrograms(allowedPrograms)
                .openToAllPrograms(openToAll)
                .build();
        session = labSessionRepository.save(session);

        // Create time slots
        createTimeSlotsForSession(session, sessionDays, startDate, endDate, startTime, endTime, slotsPerDay, maxStudents);
        
        return session;
    }

    private void createTimeSlotsForSession(LabSession session, Set<String> days,
                                          LocalDate startDate, LocalDate endDate,
                                          LocalTime startTime, LocalTime endTime,
                                          int slotsPerDay, int maxStudents) {
        Set<DayOfWeek> sessionDays = new HashSet<>();
        for (String day : days) {
            sessionDays.add(DayOfWeek.valueOf(day));
        }

        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        long slotDuration = totalMinutes / slotsPerDay;

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (sessionDays.contains(currentDate.getDayOfWeek())) {
                LocalTime slotStart = startTime;
                for (int i = 1; i <= slotsPerDay; i++) {
                    LocalTime slotEnd = slotStart.plusMinutes(slotDuration);
                    TimeSlot slot = TimeSlot.builder()
                            .labSession(session)
                            .sessionDate(currentDate)
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .groupNumber(i)
                            .maxStudents(maxStudents)
                            .currentCount(0)
                            .active(true)
                            .build();
                    timeSlotRepository.save(slot);
                    slotStart = slotEnd;
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }
}
