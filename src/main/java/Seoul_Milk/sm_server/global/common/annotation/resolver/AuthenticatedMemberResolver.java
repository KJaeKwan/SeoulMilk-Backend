package Seoul_Milk.sm_server.global.common.annotation.resolver;

import Seoul_Milk.sm_server.global.common.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.security.jwt.JWTUtil;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedMemberResolver implements HandlerMethodArgumentResolver {

    private final JWTUtil jwtUtil;
    private final MemberService memberService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class) && parameter.getParameterType().isAssignableFrom(MemberEntity.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String header = webRequest.getHeader("Authorization");

        if (header != null) {
            String token = header.split(" ")[1];
            String employeeId = jwtUtil.getEmployeeId(token);
            return memberService.getMemberEntity(employeeId);
        }

        return null;
    }

}
