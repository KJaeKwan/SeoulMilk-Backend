package Seoul_Milk.sm_server.domain.image.entity;

import Seoul_Milk.sm_server.login.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "IMAGE")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "IMAGE_ID")
    private Long id;

    @Column(name = "IMAGE_URL", nullable = false)
    private String imageUrl;

    @Column(name = "TEMPORARY", nullable = false)
    private boolean temporary;

    @Column(name = "UPLOAD_DATE", nullable = false)
    private LocalDate uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private MemberEntity member;

    public static Image create(String imageUrl, MemberEntity member) {
        Image image = Image.builder()
                .imageUrl(imageUrl)
                .temporary(false)
                .uploadDate(LocalDate.now())
                .build();
        image.attachMember(member);
        return image;
    }

    /** 임시 저장 상태로 변경 */
    public void markAsTemporary() {
        this.temporary = true;
    }

    /** 임시 저장 해제 (최종 저장) */
    public void removeFromTemporary() {
        this.temporary = false;
    }

    /** 연관관계 편의 메서드 */
    public void attachMember(MemberEntity member) {
        this.member = member;
        member.addImage(this);
    }
}
