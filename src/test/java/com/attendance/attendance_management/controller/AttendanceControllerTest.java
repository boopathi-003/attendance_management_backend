package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.AttendanceDto;
import com.attendance.attendance_management.services.AttendanceService;
import com.attendance.attendance_management.table.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    private AttendanceDto attendanceDto;

    @BeforeEach
    void setUp() {
        UserInfo userInfo = new UserInfo(1L, "john", "teacher", "cse", true);
        attendanceDto = new AttendanceDto();
        attendanceDto.setAttendanceId(1L);
        attendanceDto.setUser(userInfo);
        attendanceDto.setDate("2024-10-23");
        attendanceDto.setStatus("Present");
    }

    @Test
    void getAttendanceRecord() {
        List<AttendanceDto> attendanceList = Collections.singletonList(this.attendanceDto);
        when(this.attendanceService.getAttendanceRecord()).thenReturn(attendanceList);
        List<AttendanceDto> result = this.attendanceController.getAttendanceRecord();

        assertEquals(1, result.size());
        assertEquals(this.attendanceDto, result.get(0));
        verify(this.attendanceService, times(1)).getAttendanceRecord();
    }

    @Test
    void addAttendanceRecord() {

        doNothing().when(this.attendanceService).postAttendanceRecord(this.attendanceDto);
        String expectedResponse = "Attendance record added successfully";
        String response = this.attendanceController.addAttendanceRecord(this.attendanceDto);
        assertEquals(expectedResponse, response);
        verify(this.attendanceService, times(1)).postAttendanceRecord(this.attendanceDto);
    }
}
