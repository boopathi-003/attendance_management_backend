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
public class LeaveDto {
    private Long leaveId;
    private UserInfo user;
    private String leaveDate;
    private String reason;

}
