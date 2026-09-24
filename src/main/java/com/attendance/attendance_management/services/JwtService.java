package com.attendance.attendance_management.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class JwtService {

    @Value("${jwt.secret:defaultSecretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public JwtService(String secretKey) {
        this.secretKey = secretKey;
    }

    //    public JwtService() {
    //        KeyGenerator keyGenerator;
    //        try {
    //            keyGenerator = KeyGenerator.getInstance("HmacSHA256");
    //            SecretKey sk = keyGenerator.generateKey();// generate key using algorithm
    //            this.secretKey = Base64.getEncoder().encodeToString(sk.getEncoded()); // encoded to string
    //        } catch (NoSuchAlgorithmException e) {
    //            throw new RuntimeException(e);
    //        }
    //
    //    }

    public String getToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",role);
        return Jwts.builder().claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)).and()
                .signWith(
                        SignatureAlgorithm.HS256, secretKey
                ).compact();

    }
    //to create secure sign key
    private SecretKey getkey() {
        byte[] byteSec = Decoders.BASE64.decode(secretKey);
        //System.out.println("decode  "+Keys.hmacShaKeyFor(byteSec));
        return Keys.hmacShaKeyFor(byteSec);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }


    public String extractUserName(String token) {
        //System.out.println("exname  "+extractClaim(token, Claims::getSubject));
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        Claims claims = extractAllClaims(token);
        //   System.out.println("extractall  "+claims);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getkey())   // retrieve secret key
                .build()
                .parseSignedClaims(token) //verify the token
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        // System.out.println("validate  " + (userName.equals(userDetails.getUsername()) && !isTokenExpired(token)));
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        // System.out.println("is expire " +extractExpiration(token).before(new Date()));
        if (extractExpiration(token).before(new Date())) {
            throw new SecurityException("Token Expires ");
        }
        return false;
    }

    private Date extractExpiration(String token) {
        // System.out.println("ex exp " + extractClaim(token, Claims::getExpiration));
        return extractClaim(token, Claims::getExpiration);
    }

}
