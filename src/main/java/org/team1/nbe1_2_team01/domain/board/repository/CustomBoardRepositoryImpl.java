package org.team1.nbe1_2_team01.domain.board.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.team1.nbe1_2_team01.domain.board.constants.CommonBoardType;
import org.team1.nbe1_2_team01.domain.board.service.response.BoardDetailResponse;
import org.team1.nbe1_2_team01.domain.board.service.response.BoardResponse;
import org.team1.nbe1_2_team01.domain.user.entity.Role;

import java.util.List;
import java.util.Optional;

import static org.team1.nbe1_2_team01.domain.board.entity.QBoard.board;
import static org.team1.nbe1_2_team01.domain.board.entity.QComment.comment;
import static org.team1.nbe1_2_team01.domain.user.entity.QUser.user;


@RequiredArgsConstructor
public class CustomBoardRepositoryImpl implements CustomBoardRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<List<BoardResponse>> findAllCommonBoard(CommonBoardType type, long belongingId, Pageable pageable) {
        //inner join와 left join의 차이? 일단 두고 보자. + 나중을 위해 쿼리 튜닝 필요
        //Querydsl로 데이터를 Tuple로 가져옴
        List<Tuple> results = buildBoardQueryByType(type)
                .where(board.belonging.id.eq(belongingId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .groupBy(board.id, board.title, user.username, board.createdAt)
                .orderBy(board.createdAt.desc())
                .fetch();

        List<BoardResponse> boards = results.stream()
                .map(tuple -> BoardResponse.of(
                        tuple.get(board.id),
                        tuple.get(board.title),
                        tuple.get(user.username),
                        null, // 카테고리 이름은 지금 null로 설정
                        tuple.get(board.createdAt),
                        tuple.get(comment.count())
                ))
                .toList();

        return Optional.of(boards);
    }

    private JPAQuery<Tuple> buildBoardQueryByType(CommonBoardType type) {
        JPAQuery<Tuple> commonQuery = queryFactory
                .select(
                    board.id,
                    board.title,
                    user.username,
                    board.createdAt,
                    comment.count())
                .from(board)
                .innerJoin(user).on(board.user.eq(user))
                .leftJoin(comment).on(comment.board.eq(board));

        if(type.equals(CommonBoardType.NOTICE)) {
            return commonQuery.where(user.role.eq(Role.ADMIN));
        }

        return commonQuery.where(user.role.eq(Role.USER));
    }

    @Override
    public Optional<BoardDetailResponse> findBoardDetailExcludeComments(Long id) {
        Tuple tuple = queryFactory.select(
                        board.id,
                        board.title,
                        board.content,
                        user.username,
                        board.createdAt)
                .from(board)
                .innerJoin(user).on(board.user.eq(user))
                .where(board.id.eq(id))
                .fetchOne();

        if (tuple == null) {
            return Optional.empty(); // 결과가 없을 경우 빈 Optional 반환
        }

        BoardDetailResponse boardDetailResponse = BoardDetailResponse.of(
                tuple.get(board.id),
                tuple.get(board.title),
                tuple.get(board.content),
                tuple.get(user.username),
                tuple.get(board.createdAt)
        );
        return Optional.ofNullable(boardDetailResponse);
    }
}