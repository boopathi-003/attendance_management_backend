package com.attendance.attendance_management.mapper;

import com.attendance.attendance_management.dto.LeaveDto;
import com.attendance.attendance_management.table.LeaveInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LeaveMapper {


    public LeaveDto setDto(final LeaveInfo leaveInfo) {
//        LeaveDto leaveDto = new LeaveDto();
//        leaveDto.setLeaveId(leaveInfo.getLeaveId());
//        leaveDto.setLeaveDate(leaveInfo.getLeaveDate());
//        leaveDto.setUser(leaveInfo.getUser());
//        leaveDto.setReason(leaveInfo.getLeaveReason());
//        return leaveDto;
        return new LeaveDto(
                leaveInfo.getLeaveId(),
                leaveInfo.getUser(),
                leaveInfo.getLeaveDate(),
                leaveInfo.getLeaveReason()
        );
    }

    public List<LeaveDto> setDtoList(final List<LeaveInfo> leaveInfoList) {
        List<LeaveDto> leaveDtoList = new ArrayList<>();
        for (LeaveInfo leaveInfo1 : leaveInfoList)
        {
            LeaveDto leaveDto = new LeaveDto();
            leaveDto.setLeaveId(leaveInfo1.getLeaveId());
            leaveDto.setLeaveDate(leaveInfo1.getLeaveDate());
            leaveDto.setUser(leaveInfo1.getUser());
            leaveDto.setReason(leaveInfo1.getLeaveReason());
            leaveDtoList.add(leaveDto);
        }
        return leaveDtoList;
    }

}
