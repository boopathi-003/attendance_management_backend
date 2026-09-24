package com.attendance.attendance_management.services;

import com.attendance.attendance_management.dto.LeaveDto;
import com.attendance.attendance_management.mapper.LeaveMapper;
import com.attendance.attendance_management.repository.LeaveRepository;
import com.attendance.attendance_management.table.LeaveInfo;
import com.attendance.attendance_management.table.UserInfo;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtensionMethod(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private LeaveMapper leaveMapper;

    @InjectMocks
    private LeaveService leaveService;
    LeaveInfo leaveInfo;
    LeaveDto leaveDto;

    @BeforeEach
    void setUp() {

        UserInfo userInfo = new UserInfo(2L, "mano",
                "teacher", "cse", true);
        MockitoAnnotations.openMocks(this);
        this.leaveInfo = new LeaveInfo(1L, userInfo, "23-12-23", "sick leave");
        this.leaveDto = new LeaveDto(1L, userInfo, "23-12-23", "sick leave");
    }

    @Test
    void getLeaveData() {
        leaveService.getLeaveData();
        verify(leaveRepository).findAll();
    }

    @Test
    void getRecordById() {
        when(this.leaveRepository.findById(1L)).thenReturn(Optional.ofNullable(this.leaveInfo));
        when(leaveMapper.setDto(this.leaveInfo)).thenReturn(this.leaveDto);
        assertEquals(this.leaveDto, this.leaveService.getRecordById(1));
        verify(this.leaveRepository, times(1)).findById(1L);
    }

    @Test
    void getRecordByDate() {

        List<LeaveInfo> leaveInfoList = new ArrayList<>();
        leaveInfoList.add(this.leaveInfo);

        List<LeaveDto> leaveDtoList = new ArrayList<>();
        leaveDtoList.add(this.leaveDto);

        when(this.leaveRepository.findByLeaveDate("23-12-23")).thenReturn(leaveInfoList);
        when(this.leaveMapper.setDto(this.leaveInfo)).thenReturn(this.leaveDto);
        assertEquals(leaveDtoList, this.leaveService.getRecordByDate("23-12-23"));
        verify(this.leaveRepository, times(1)).findByLeaveDate("23-12-23");
    }

    @Test
    void addLeaveForm() {
        this.leaveService.addLeaveForm(this.leaveInfo);
        verify(this.leaveRepository).save(this.leaveInfo);
    }

    @Test
    void getDelete() {
        this.leaveService.getDelete(1L);
        verify(this.leaveRepository).deleteById(1L);
    }

}