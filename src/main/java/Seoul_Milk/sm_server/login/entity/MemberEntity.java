package Seoul_Milk.sm_server.login.entity;

import Seoul_Milk.sm_server.login.constant.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "MEMBER")
@Getter
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "EMPLOYEE_ID")
    private String employeeId;

    @Column(name = "PASSWORD")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE")
    private Role role;

    public static MemberEntity createUnverifiedMember(String employeeId, Role role){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.employeeId = employeeId;
        memberEntity.role = role;
        return memberEntity;
    }

    public static MemberEntity createVerifiedMember(String employeeId, String password, Role role){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.employeeId = employeeId;
        memberEntity.password = password;
        memberEntity.role = role;
        return memberEntity;
    }

    /**
     * 비밀번호 수정
     */
    public void updatePassword(String password) {
        this.password = password;
    }

    /**
     * codef api 요청 시 id값 생성
     */
    public String makeUniqueId(){
        return this.employeeId + UUID.randomUUID();
    }
}
