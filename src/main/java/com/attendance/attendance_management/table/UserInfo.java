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
@Table(name = "user_info")
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "roll", nullable = false)
    private String roll;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_marked")
    private boolean isMarked = false;

    public UserInfo(long l, String john, String teacher, String cse, boolean b) {
    }
}
