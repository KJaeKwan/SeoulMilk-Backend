package Seoul_Milk.sm_server.global.jwt;

import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.login.constant.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {
    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getEmployeeId(String token){
        try{
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("employeeId", String.class);
        }catch (Exception e){
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public Role getRole(String token) {
        try{
            return Role.valueOf(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class));
        }catch (Exception e){
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public String getCategory(String token) {
        try{
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
        }catch (Exception e){
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public Boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        }catch (SignatureException e){
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public String createJwt(String category, String employeeId, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("category", category)
                .claim("employeeId", employeeId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /** Request에서 Access Token 추출 */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
