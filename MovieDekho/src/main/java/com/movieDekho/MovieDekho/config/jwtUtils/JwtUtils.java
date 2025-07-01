package com.movieDekho.MovieDekho.config.jwtUtils;

import com.movieDekho.MovieDekho.config.userImplementation.UserDetailsImplement;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer")) ? bearerToken.substring(7) : null;
    }

    public String getNameFromJwt(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

    }

    public String generateToken(UserDetailsImplement userDetails) {

        String email = userDetails.getEmail();
        String roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder().subject(email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expirationTime))
                .compact();

    }

    public boolean validateToken(String token) {
        

        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload().getSubject();

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

}
