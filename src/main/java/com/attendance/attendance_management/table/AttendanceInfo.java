package com.attendance.attendance_management.table;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Entity
@Getter
@Setter
@Table(name = "attendance_info")
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserInfo user;
    private String date;
    private String recordIn;
    private String recordOut;
    private String status;
    private String reason;

}
