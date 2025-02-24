package Seoul_Milk.sm_server.login.entity;

import Seoul_Milk.sm_server.global.refresh.RefreshToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "REFRESH_REPOSITORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REPOSITORY_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "MEMBER_ID")
    private MemberEntity memberEntity;

    @Column(name = "REFRESH_TOKEN")
    private String refreshToken;
    @Column(name = "EXPIRATION")
    private Date expiration;

    public static RefreshEntity createRefreshEntity(MemberEntity memberEntity, String refreshToken, Date expiration){
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.memberEntity = memberEntity;
        refreshEntity.refreshToken = refreshToken;
        refreshEntity.expiration = expiration;
        return refreshEntity;
    }
}
