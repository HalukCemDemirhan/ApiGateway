package rs.apigateway.ApiGateway.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import rs.apigateway.ApiGateway.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {


    public static final String SECRET = "OmllU2syY0dsN1Q2Tm5GSkhaRzN4Z1VZbWhuYzNRZFFMbGFWbUxhbWx0Y3pFbXJ6eHpFbWdZbXJ5ZkFzNXRiYg==";


    public boolean validateToken(String token) {
        try {
            // Parse the token.
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            // log the exception
            System.out.println("Invalid JWT signature");
        } catch (ExpiredJwtException ex) {
            // log the exception
            System.out.println("Expired JWT token");
        } catch (IllegalArgumentException ex) {
            // log tahe exception
            System.out.println("JWT token compact of handler are invalid");
        } catch (Exception ex) {
            // log other exceptions
            System.out.println("Error in validating JWT token");
        }
        return false;
    }




    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public User extractUserDetails(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSignKey())
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();
        String role = claims.get("role", String.class);
        return new User(username, role);
    }


}
