package Seoul_Milk.sm_server.global.jwt;

import static Seoul_Milk.sm_server.global.cookie.CookieClass.createCookie;
import static Seoul_Milk.sm_server.global.token.Token.ACCESS_TOKEN;
import static Seoul_Milk.sm_server.global.token.Token.REFRESH_TOKEN;

import Seoul_Milk.sm_server.global.response.ErrorResponse;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.refresh.RefreshToken;
import Seoul_Milk.sm_server.global.auth.dto.CustomUserDetails;
import Seoul_Milk.sm_server.domain.member.dto.response.LoginResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshToken refreshToken;
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //클라이언트 요청에서 사번, 비밀번호 추출
        Map<String, String> requestBody;
        try {
            requestBody = new ObjectMapper().readValue(request.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String employeeId = requestBody.get("employeeId");
        String password = requestBody.get("password");

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(employeeId, password);

        //token에 담은 검증을 위한 AuthenticationManager로 전달

        return authenticationManager.authenticate(authToken);

    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication)
            throws IOException {
        //유저 정보
        String employeeId = ((CustomUserDetails)authentication.getPrincipal()).getEmployeeId();
        String name = ((CustomUserDetails)authentication.getPrincipal()).getUsername();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt(ACCESS_TOKEN.category(), employeeId, role, ACCESS_TOKEN.expireMs());
        String refresh = jwtUtil.createJwt(REFRESH_TOKEN.category(), employeeId, role, REFRESH_TOKEN.expireMs());

        refreshToken.addRefreshEntity(employeeId, refresh, REFRESH_TOKEN.expireMs());

        //응답 설정
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.of(name, role);
        ObjectMapper objectMapper = new ObjectMapper();
        response.setCharacterEncoding("UTF-8");  // ✅ 응답 인코딩을 UTF-8로 설정
        response.setContentType("application/json; charset=UTF-8");  // ✅ Content-Type 설정
        response.getWriter().write(objectMapper.writeValueAsString(loginResponseDTO));
        response.setHeader(ACCESS_TOKEN.category(), access);
        response.addCookie(createCookie(REFRESH_TOKEN.category(), refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorCode errorCode;

        if (failed instanceof UsernameNotFoundException) {
            errorCode = ErrorCode.USER_EMPLOYEE_ID_NOT_EXIST;
        } else if (failed instanceof BadCredentialsException) {
            errorCode = ErrorCode.USER_WRONG_PASSWORD;
        } else {
            errorCode = ErrorCode.INVALID_REQUEST;
        }

        response.setStatus(errorCode.getStatus().value());
        // ErrorResponse 객체 생성
        ErrorResponse<Void> errorResponse = ErrorResponse.of(errorCode.getErrorCode(), errorCode.getMessage());

        // JSON 응답 반환
        try {
            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
        } catch (IOException e) {
            throw new RuntimeException("Response 출력 중 오류 발생", e);
        }
    }
}
