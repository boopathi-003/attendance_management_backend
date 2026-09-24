package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.AttendanceDto;
import com.attendance.attendance_management.dto.LeaveDto;
import com.attendance.attendance_management.dto.UserDto;
import com.attendance.attendance_management.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j
class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<AttendanceDto> getAttendanceRecord() {
        return this.attendanceService.getAttendanceRecord();
    }

    @GetMapping("/planned-leave")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<AttendanceDto> getPlannedLeave() {
        return attendanceService.getPlannedLeave();
    }

    @RequestMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<AttendanceDto> getAttendanceById(@PathVariable String id) {
        log.info("getAttendanceById");
        if (id == null || id.trim().isEmpty() || id.equalsIgnoreCase("null")) {
            log.warn("getAttendanceById: id is null or empty");
            return ResponseEntity.badRequest().body(null);
        }

        try {
            long userId = Long.parseLong(id);
            AttendanceDto attendance = attendanceService.getAttendanceById(userId);
            return ResponseEntity.ok(attendance);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/sick")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<AttendanceDto> getSickLeave() {
        return attendanceService.getSickLeave();
    }


    @GetMapping("/absent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<AttendanceDto> getAbsentRecords() {
        return attendanceService.getRecordsByAbsent();
    }

    @GetMapping("/present")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<AttendanceDto> getPresentRecords() {
        return attendanceService.getRecordsByPresent();
    }

    @PostMapping("/add-attendance")
    @PreAuthorize("hasRole('ADMIN')")
    public String addAttendanceRecord(@RequestBody AttendanceDto attendanceDto) {
        this.attendanceService.postAttendanceRecord(attendanceDto);
        return "Attendance record added successfully";
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String getDeleteById(@PathVariable Long id)
    {
        return  this.attendanceService.getDeleteById(id);
    }


}

