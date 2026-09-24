package com.attendance.attendance_management.services;

import com.attendance.attendance_management.dto.AttendanceDto;
import com.attendance.attendance_management.exceptionhandler.customexceptions.UserNotFoundException;
import com.attendance.attendance_management.mapper.AttendanceMapper;
import com.attendance.attendance_management.repository.AttendanceRepository;
import com.attendance.attendance_management.repository.UserRepository;
import com.attendance.attendance_management.table.AttendanceInfo;
import com.attendance.attendance_management.table.LeaveInfo;
import com.attendance.attendance_management.table.UserInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final LeaveService leaveService;
    private final UserRepository userRepository;

    public List<AttendanceDto> getAttendanceRecord() {
        List<AttendanceInfo> attendanceInfoList = attendanceRepository.findAll();
        List<AttendanceDto>attendanceDtoList=new ArrayList<>();
        for(AttendanceInfo attendanceInfo : attendanceInfoList) {
            attendanceDtoList.add(attendanceMapper.setDto(attendanceInfo));
        }
        return attendanceDtoList;
    }



    @Transactional
    public void postAttendanceRecord(final AttendanceDto attendanceDto) {
        boolean attendanceExists = this.attendanceRepository.existsByUser_UserIdAndDate(
                attendanceDto.getUser().getUserId(), attendanceDto.getDate());
        if (attendanceExists) {
            throw new RuntimeException("Attendance record already exists for the given user and date.");
        }
       else {
            AttendanceInfo attendanceInfo = this.attendanceMapper.setEntity(attendanceDto);
            this.attendanceRepository.save(attendanceInfo);

            this.userRepository.markAttendance(attendanceDto.getUser().getUserId());

            if (("absent".equalsIgnoreCase(attendanceDto.getStatus()) ||
                    "Planned leave".equalsIgnoreCase(attendanceDto.getStatus()) ||
                    "Sick leave".equalsIgnoreCase(attendanceDto.getStatus())) &&
                    "-".equals(attendanceDto.getRecordIn()) && "-".equals(attendanceDto.getRecordOut())) {

                LeaveInfo leaveInfo = new LeaveInfo();
                leaveInfo.setLeaveDate(attendanceDto.getDate());
                leaveInfo.setUser(this.userRepository.findById(attendanceDto.getUser().getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found")));
                leaveInfo.setLeaveReason(attendanceDto.getLeaveReason());
                this.leaveService.addLeaveForm(leaveInfo);
            }
        }
    }


    public List<AttendanceDto> getPlannedLeave() {
        return getRecordByStatus("Planned leave");
    }

    public List<AttendanceDto> getSickLeave() {
        return getRecordByStatus("Sick Leave");
    }

    public List<AttendanceDto> getRecordsByAbsent() {
        return getRecordByStatus("absent");
    }

    public List<AttendanceDto> getRecordsByPresent() {
        return getRecordByStatus("present");
    }

    private List<AttendanceDto> getRecordByStatus(String status) {
        List<AttendanceInfo> attendanceInfoList = attendanceRepository.findByStatus(status);
        List<AttendanceDto>attendanceDtoList=new ArrayList<>();
        for(AttendanceInfo attendanceInfo : attendanceInfoList) {
           attendanceDtoList.add(attendanceMapper.setDto(attendanceInfo));
        }
        return attendanceDtoList;
    }

    public AttendanceDto getAttendanceById(Long id) {
       Optional<AttendanceInfo>attendanceInfo=this.attendanceRepository.findById(id);
       if(attendanceInfo.isEmpty()) {
          throw new UserNotFoundException("User Not Found");
       }
        return attendanceInfo.map(this.attendanceMapper::setDto).orElse(null);
    }

    public String getDeleteById(Long id) {
        Optional<AttendanceInfo>attendanceInfo=this.attendanceRepository.findById(id);
        if(attendanceInfo.isPresent())
        {
            this.attendanceRepository.deleteById(id);
            return "Deleted";
        }
        throw new UserNotFoundException("User Not Found");
    }
}
