package org.team1.nbe1_2_team01.domain.board.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.team1.nbe1_2_team01.domain.board.constants.CommonBoardType;
import org.team1.nbe1_2_team01.domain.board.service.response.BoardDetailResponse;
import org.team1.nbe1_2_team01.domain.board.service.response.CourseBoardResponse;
import org.team1.nbe1_2_team01.domain.user.entity.Role;
import org.team1.nbe1_2_team01.domain.user.entity.User;
import org.team1.nbe1_2_team01.global.util.SecurityUtil;

import static org.team1.nbe1_2_team01.domain.board.entity.QCourseBoard.courseBoard;
import static org.team1.nbe1_2_team01.domain.user.entity.QUser.user;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomCourseBoardRepositoryImpl implements CustomCourseBoardRepository {

    private final JPAQueryFactory queryFactory;
    private static final int PAGE_SIZE = 10;

    @Override
    public List<CourseBoardResponse> findAllCourseBoard(
            CommonBoardType type,
            Long courseId,
            Long boardId
    ) {
        JPAQuery<Tuple> query = buildBoardQueryByType(type, courseId);
        setPagingStart(query, boardId);

        List<Tuple> results = query
                .limit(PAGE_SIZE)
                .orderBy(courseBoard.createdAt.desc(), courseBoard.id.desc())
                .fetch();

        return results.stream()
                .map(this::convertToCourseBoardResponse)
                .toList();
    }

    @Override
    public Optional<BoardDetailResponse> findCourseBoardDetailById(Long courseId) {
        Tuple tuple = queryFactory.select(
                        courseBoard.id,
                        courseBoard.title,
                        courseBoard.readCount,
                        courseBoard.content,
                        user.name,
                        courseBoard.createdAt,
                        courseBoard.user.id
                )
                .from(courseBoard)
                .innerJoin(user).on(courseBoard.user.eq(user))
                .where(courseBoard.id.eq(courseId))
                .fetchOne();

        if (tuple == null) {
            return Optional.empty(); // 결과가 없을 경우 빈 Optional 반환
        }

        User currentUser = findCurrentUser();
        BoardDetailResponse boardDetailResponse = convertToBoardDetailResponse(tuple, currentUser);

        return Optional.ofNullable(boardDetailResponse);

    }

    private JPAQuery<Tuple> buildBoardQueryByType(CommonBoardType type, Long courseId) {
        JPAQuery<Tuple> commonQuery = queryFactory
                .select(
                        courseBoard.id,
                        courseBoard.title,
                        user.name,
                        courseBoard.createdAt
                )
                .from(courseBoard)
                .innerJoin(user).on(courseBoard.user.eq(user))
                .where(courseBoard.course.id.eq(courseId));

        if(type.equals(CommonBoardType.NOTICE)) {
            return commonQuery.where(user.role.eq(Role.ADMIN));
        }

        return commonQuery.where(user.role.eq(Role.USER));
    }

    private User findCurrentUser() {
        String currentUsername = SecurityUtil.getCurrentUsername();
        return queryFactory.selectFrom(user).where(user.username.eq(currentUsername)).fetchOne();
    }

    private void setPagingStart(JPAQuery<Tuple> commonQuery, Long boardId) {
        if (boardId != null) {
            commonQuery.where(courseBoard.id.lt(boardId));
        }
    }

    private CourseBoardResponse convertToCourseBoardResponse(Tuple tuple) {
        return CourseBoardResponse.builder()
                .id(tuple.get(courseBoard.id))
                .title(tuple.get(courseBoard.title))
                .writer(tuple.get(user.name))
                .createdAt(tuple.get(courseBoard.createdAt))
                .build();
    }

    private BoardDetailResponse convertToBoardDetailResponse(Tuple tuple, User currentUser) {
        return BoardDetailResponse.builder()
                .id(tuple.get(courseBoard.id))
                .title(tuple.get(courseBoard.title))
                .readCount(Optional.ofNullable(tuple.get(courseBoard.readCount)).orElse(0L))
                .content(tuple.get(courseBoard.content))
                .writer(tuple.get(user.name))
                .createdAt(tuple.get(courseBoard.createdAt))
                .isAdmin(Objects.equals(currentUser.getRole(), Role.ADMIN))
                .isMine(Objects.equals(tuple.get(courseBoard.user.id), currentUser.getId()))
                .build();
    }
}