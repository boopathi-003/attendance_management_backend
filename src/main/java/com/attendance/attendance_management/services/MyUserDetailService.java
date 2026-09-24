package com.attendance.attendance_management.services;

import com.attendance.attendance_management.exceptionhandler.customexceptions.UserNotFoundException;
import com.attendance.attendance_management.repository.UserAuthRepository;
import com.attendance.attendance_management.table.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {


    private final UserAuthRepository userAuthRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        UserAuth userAuth = this.userAuthRepository.findByUserName(username);
        if (userAuth == null) {
            throw new UserNotFoundException("User Not Found");
        }
        return userAuth;
    }
}
