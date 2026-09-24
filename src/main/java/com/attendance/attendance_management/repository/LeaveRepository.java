package com.attendance.attendance_management.repository;

import com.attendance.attendance_management.table.LeaveInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveInfo,Long> {
    List<LeaveInfo> findByLeaveDate(String date);

}
