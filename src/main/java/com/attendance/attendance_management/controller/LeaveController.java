package com.attendance.attendance_management.controller;

import com.attendance.attendance_management.dto.LeaveDto;
import com.attendance.attendance_management.services.LeaveService;
import com.attendance.attendance_management.table.LeaveInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/leaverecord")
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<LeaveDto> getLeaveData() {
        return this.leaveService.getLeaveData();
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public LeaveDto getRecordById(@PathVariable String id) {
        return this.leaveService.getRecordById(Integer.parseInt(id));
    }

    @GetMapping("/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<LeaveDto> getRecordByDate(@PathVariable String date) {
        return this.leaveService.getRecordByDate(date);
    }


    public void addLeaveForm(LeaveInfo data) {
        this.leaveService.addLeaveForm(data);
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String getDelete(@PathVariable Long id) {
        this.leaveService.getDelete(id);
        return "Deleted";
    }
}
