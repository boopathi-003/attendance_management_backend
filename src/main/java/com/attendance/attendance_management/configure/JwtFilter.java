package com.attendance.attendance_management.configure;

import com.attendance.attendance_management.dto.ApiResponse;
import com.attendance.attendance_management.services.JwtService;
import com.attendance.attendance_management.services.MyUserDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MyUserDetailService myUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            String token = null;
            String username = null;
            String role = null;


            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
                username = this.jwtService.extractUserName(token);
                role= jwtService.extractRole(token);
            }

            if (role!=null && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.myUserDetailService.loadUserByUsername(username);
                if (this.jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            ApiResponse<String> message = new ApiResponse<>();
            message.setCode(HttpStatus.UNAUTHORIZED.value());
            message.setStatus("error");
            message.setMessage("Token has expired. Please log in again.");
            message.setValidationErrors(null);
            message.setRequestedTime(System.currentTimeMillis());
            message.setExecutionTime("0 ms");
            message.setResponse(null);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(message));
            response.getWriter().flush();
            return;
        } catch (Exception e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
