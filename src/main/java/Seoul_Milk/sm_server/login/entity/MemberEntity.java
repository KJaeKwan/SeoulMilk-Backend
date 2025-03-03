package Seoul_Milk.sm_server.login.entity;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.login.constant.Role;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

import java.util.ArrayList;

@Entity
@Table(name = "MEMBER")
@Getter
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaxInvoice> taxInvoices = new ArrayList<>();

    public static MemberEntity createUnverifiedMember(String employeeId, Role role){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.employeeId = employeeId;
        memberEntity.role = role;
        return memberEntity;
    }

    public static MemberEntity createVerifiedMember(String employeeId, String name, String password, Role role){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.employeeId = employeeId;
        memberEntity.name = name;
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

    /** 권한 수정 */
    public void updateRole(Role role) {
        this.role = role;
    }


    /** 연관 관계 편의 메서드 */
    public void addTaxInvoice(TaxInvoice taxInvoice) {
        this.taxInvoices.add(taxInvoice);
        taxInvoice.attachMember(this);
    }

    /**
     * codef api 요청 시 id값 생성
     */
    public String makeUniqueId(){
        return this.employeeId + UUID.randomUUID();
    }
}
