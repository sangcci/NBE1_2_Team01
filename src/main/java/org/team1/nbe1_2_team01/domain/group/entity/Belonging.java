package org.team1.nbe1_2_team01.domain.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.team1.nbe1_2_team01.domain.board.entity.Board;
import org.team1.nbe1_2_team01.domain.calendar.entity.Calendar;
import org.team1.nbe1_2_team01.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "belonging")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Belonging {

    @Id
    @SequenceGenerator(
            name = "BELONGING_SEQ_GENERATOR",   // 식별자 생성기 이름
            sequenceName = "BELONGING_SEQ",  // 데이터베이스에 등록되어있는 시퀀스 이름: DB에는 해당 이름으로 매핑된다.
            initialValue = 1,  // DDL 생성시에만 사용되며 시퀀스 DDL을 생성할 때 처음 시작하는 수를 지정
            allocationSize = 50  // 시퀀스 한 번 호출에 증가하는 수
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "BELONGING_SEQ_GENERATOR")
    private Long id;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isOwner;

    private String course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "belonging")
    private List<Board> boards = new ArrayList<>();

    @Builder
    private Belonging(User user,
                      boolean isOwner,
                      String course) {
        this.user = user;
        this.course = course;
        this.isOwner = isOwner;
        if (user != null) user.addBelonging(this);
    }

    public static Belonging createBelongingOf(boolean isOwner, String course, User user) {
        return Belonging.builder()
                .isOwner(isOwner)
                .course(course)
                .user(user)
                .build();
    }

    public void assignTeam(Team team){
        this.team = team;
    }

    public void addBoards(Board board) {
        this.boards.add(board);
    }
}
