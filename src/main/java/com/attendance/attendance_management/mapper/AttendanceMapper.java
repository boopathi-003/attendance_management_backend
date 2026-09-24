package com.attendance.attendance_management.mapper;

import com.attendance.attendance_management.dto.AttendanceDto;
import com.attendance.attendance_management.repository.AttendanceRepository;
import com.attendance.attendance_management.repository.UserRepository;
import com.attendance.attendance_management.table.AttendanceInfo;
import com.attendance.attendance_management.table.UserInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class AttendanceMapper {
    private final AttendanceRepository attendanceRepo;
    private final UserRepository userRepository;

    public AttendanceDto setDto(final AttendanceInfo att) {
                if(att.getAttendanceId()==null)
                {
                    throw new NullPointerException("Input cannot be null");
                }
                else {
                    AttendanceDto attendanceDto = new AttendanceDto();
                    attendanceDto.setAttendanceId(att.getAttendanceId());
                    attendanceDto.setRecordIn(att.getRecordIn());
                    attendanceDto.setRecordOut(att.getRecordOut());
                    attendanceDto.setStatus(att.getStatus());
                    attendanceDto.setDate(att.getDate());
                    attendanceDto.setUser(att.getUser());
                    attendanceDto.setLeaveReason(att.getReason());
                    return attendanceDto;
                }
    }

    public AttendanceInfo setEntity(final AttendanceDto attendanceDto) {
        AttendanceInfo attendanceDetails = new AttendanceInfo();
        attendanceDetails.setAttendanceId(attendanceDto.getAttendanceId());
        attendanceDetails.setDate(attendanceDto.getDate());
        attendanceDetails.setStatus(attendanceDto.getStatus());
        attendanceDetails.setRecordIn(attendanceDto.getRecordIn());
        attendanceDetails.setRecordOut(attendanceDto.getRecordOut());
        attendanceDetails.setReason(attendanceDto.getLeaveReason());
        UserInfo userInfo = userRepository.findById(attendanceDto.getUser().getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        attendanceDetails.setUser(userInfo);

        return attendanceDetails;
    }
}
