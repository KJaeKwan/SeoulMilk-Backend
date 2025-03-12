package Seoul_Milk.sm_server.domain.image.repository;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.image.entity.QImage;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepository {

    private final ImageJpaRepository imageJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Image> searchTempImages(MemberEntity member, Pageable pageable) {
        QImage image = QImage.image;
        BooleanBuilder whereClause = new BooleanBuilder();

        whereClause.and(image.member.eq(member))
                .and(image.temporary.isTrue());

        long total = Optional.ofNullable(
                queryFactory
                        .select(Wildcard.count)
                        .from(image)
                        .where(whereClause)
                        .fetchOne()
        ).orElse(0L);

        List<Image> results = queryFactory
                .selectFrom(image)
                .where(whereClause)
                .orderBy(image.uploadDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    /** 여러 이미지 저장 */
    @Override
    public void saveAll(List<Image> images) {
        imageJpaRepository.saveAll(images);
    }

    /** 특정 멤버의 임시 저장된 이미지 조회 */
    @Override
    public List<Image> findTmpAllByMember(MemberEntity member) {
        QImage image = QImage.image;
        BooleanBuilder whereClause = new BooleanBuilder();

        whereClause.and(image.member.eq(member))
                .and(image.temporary.isTrue());

        return queryFactory
                .selectFrom(image)
                .where(whereClause)
                .orderBy(image.uploadDate.desc())
                .fetch();
    }

    @Override
    public List<Image> findByMemberAndIds(MemberEntity member, List<Long> imageIds) {
        QImage image = QImage.image;
        BooleanBuilder whereClause = new BooleanBuilder();

        whereClause.and(image.member.eq(member))
                .and(image.id.in(imageIds))
                .and(image.temporary.isTrue());

        return queryFactory
                .selectFrom(image)
                .where(whereClause)
                .orderBy(image.uploadDate.desc())
                .fetch();
    }
}
