package com.attendance.attendance_management.dto;

import com.attendance.attendance_management.table.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDto {
    private Long attendanceId;
    private String date;
    private String recordIn;
    private UserInfo user;
    private String recordOut;
    private String status;
    private  String leaveReason;


}

