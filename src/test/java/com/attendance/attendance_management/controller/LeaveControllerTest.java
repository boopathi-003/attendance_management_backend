package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.LeaveDto;
import com.attendance.attendance_management.services.LeaveService;
import com.attendance.attendance_management.table.LeaveInfo;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveControllerTest {

    @Mock
    private LeaveService leaveService;

    @InjectMocks
    private LeaveController leaveController;

    private LeaveDto leaveDto;
    private LeaveInfo leaveInfo;


    @BeforeEach
    void setUp() {
        UserInfo userInfo = new UserInfo(1L, "john", "teacher", "cse", true);
        leaveDto = new LeaveDto();
        leaveDto.setLeaveId(1L);
        leaveDto.setUser(userInfo);
        leaveDto.setLeaveDate("2024-10-01");


        leaveInfo = new LeaveInfo();
        leaveInfo.setLeaveId(1L);
        leaveInfo.setUser(userInfo);
        leaveInfo.setLeaveDate("2024-10-01");
    }

    @Test
    void getLeaveData() {
        List<LeaveDto> leaveList = Collections.singletonList(this.leaveDto);
        when(this.leaveService.getLeaveData()).thenReturn(leaveList);
        List<LeaveDto> result = this.leaveController.getLeaveData();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(this.leaveDto, result.getFirst());
    }

    @Test
    void getRecordById() {
        when(this.leaveService.getRecordById(1)).thenReturn(this.leaveDto);
        LeaveDto result = this.leaveController.getRecordById("1");
        assertNotNull(result);
        assertEquals(this.leaveDto, result);
    }

    @Test
    void getRecordByDate() {
        List<LeaveDto> leaveList = Collections.singletonList(this.leaveDto);
        when(this.leaveService.getRecordByDate("2024-10-01")).thenReturn(leaveList);

        List<LeaveDto> result = this.leaveController.getRecordByDate("2024-10-01");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(this.leaveDto, result.getFirst());
    }

    @Test
    void addLeaveForm() {

        this.leaveController.addLeaveForm(this.leaveInfo);
        verify(leaveService, times(1)).addLeaveForm(this.leaveInfo);
    }

    @Test
    void getDelete() {

        String result = this.leaveController.getDelete(1L);
        assertEquals("Deleted", result);
        verify(this.leaveService, times(1)).getDelete(1L);
    }
}
