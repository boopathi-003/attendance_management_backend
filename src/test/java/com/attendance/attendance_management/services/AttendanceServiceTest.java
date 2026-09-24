package com.attendance.attendance_management.services;

import com.attendance.attendance_management.dto.AttendanceDto;
import com.attendance.attendance_management.exceptionhandler.customexceptions.UserNotFoundException;
import com.attendance.attendance_management.mapper.AttendanceMapper;
import com.attendance.attendance_management.repository.AttendanceRepository;
import com.attendance.attendance_management.repository.UserRepository;
import com.attendance.attendance_management.table.AttendanceInfo;
import com.attendance.attendance_management.table.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private AttendanceService attendanceService;

    private AttendanceInfo attendanceInfo;
    private AttendanceDto attendanceDto;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userInfo = new UserInfo(2L, "mano", "teacher", "cse", true);
        attendanceDto = new AttendanceDto(1L, "23-12-23", "9:00 AM", userInfo, "5:00 PM", "present", null);
        attendanceInfo = new AttendanceInfo(1L, userInfo, "23-12-23", "9:00 AM", "5:00 PM", "present", null);
    }

    @Test
    void getAttendanceRecord() {
        when(attendanceRepository.findAll()).thenReturn(Collections.singletonList(attendanceInfo));
        when(attendanceMapper.setDto(attendanceInfo)).thenReturn(attendanceDto);

        List<AttendanceDto> result = attendanceService.getAttendanceRecord();

        assertEquals(1, result.size());
        assertEquals(attendanceDto, result.get(0));
        verify(attendanceRepository, times(1)).findAll();
        verify(attendanceMapper, times(1)).setDto(attendanceInfo);
    }

    @Test
    void postAttendanceRecord_WhenAttendanceExists_ThrowsException() {
        when(attendanceRepository.existsByUser_UserIdAndDate(userInfo.getUserId(), attendanceDto.getDate()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                attendanceService.postAttendanceRecord(attendanceDto));

        assertEquals("Attendance record already exists for the given user and date.", exception.getMessage());
        verify(attendanceRepository, times(1))
                .existsByUser_UserIdAndDate(userInfo.getUserId(), attendanceDto.getDate());
        verify(attendanceRepository, never()).save(any(AttendanceInfo.class));
    }

    @Test
    void postAttendanceRecord_WhenAttendanceDoesNotExist_SavesAttendance() {
        when(attendanceRepository.existsByUser_UserIdAndDate(userInfo.getUserId(), attendanceDto.getDate()))
                .thenReturn(false);
        when(attendanceMapper.setEntity(attendanceDto)).thenReturn(attendanceInfo);

        attendanceService.postAttendanceRecord(attendanceDto);

        verify(attendanceRepository, times(1)).save(attendanceInfo);
        verify(userRepository, times(1)).markAttendance(userInfo.getUserId());
    }

    @Test
    void getAttendanceById_WhenAttendanceExists_ReturnsAttendanceDto() {
        when(attendanceRepository.findById(attendanceInfo.getUser().getUserId())).thenReturn(Optional.of(attendanceInfo));
        when(attendanceMapper.setDto(attendanceInfo)).thenReturn(attendanceDto);

        AttendanceDto result = attendanceService.getAttendanceById(attendanceInfo.getUser().getUserId());

        assertEquals(attendanceDto, result);
        verify(attendanceRepository, times(1)).findById(attendanceInfo.getUser().getUserId());
        verify(attendanceMapper, times(1)).setDto(attendanceInfo);
    }

    @Test
    void getAttendanceById_WhenAttendanceDoesNotExist_ThrowsException() {
        when(attendanceRepository.findById(attendanceInfo.getUser().getUserId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                attendanceService.getAttendanceById(attendanceInfo.getUser().getUserId()));

        verify(attendanceRepository, times(1)).findById(attendanceInfo.getUser().getUserId());
        verify(attendanceMapper, never()).setDto(any(AttendanceInfo.class));
    }

    @Test
    void getDeleteById_WhenAttendanceExists_DeletesAttendance() {
        when(attendanceRepository.findById(attendanceInfo.getUser().getUserId())).thenReturn(Optional.of(attendanceInfo));

        String result = attendanceService.getDeleteById(attendanceInfo.getUser().getUserId());

        assertEquals("Deleted", result);
        verify(attendanceRepository, times(1)).deleteById(attendanceInfo.getUser().getUserId());
    }

    @Test
    void getDeleteById_WhenAttendanceDoesNotExist_ThrowsException() {
        when(attendanceRepository.findById(attendanceInfo.getUser().getUserId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                attendanceService.getDeleteById(attendanceInfo.getUser().getUserId()));

        verify(attendanceRepository, never()).deleteById(attendanceInfo.getUser().getUserId());
    }
}
