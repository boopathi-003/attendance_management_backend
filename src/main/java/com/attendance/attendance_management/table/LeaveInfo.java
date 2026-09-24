package com.attendance.attendance_management.table;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "leave_info")
@AllArgsConstructor
@NoArgsConstructor
public class LeaveInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserInfo user;
    @Column(name = "leave_date")
    private String leaveDate;
    private String  leaveReason;
}
