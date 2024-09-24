package org.team1.nbe1_2_team01.domain.group.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TeamType teamType;

    @Column(length = 50)
    private String name;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean creationWaiting;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean deletionWaiting;

    @OneToMany(mappedBy = "team")
    private List<Belonging> belongings = new ArrayList<>();

    @Builder
    private Team(TeamType teamType,
                 String name,
                 boolean creationWaiting,
                 boolean deletionWaiting) {
        this.teamType = teamType;
        this.name = name;
        this.creationWaiting = creationWaiting;
        this.deletionWaiting = deletionWaiting;
    }


    public void assignBelonging(Belonging belonging){
        this.belongings.add(belonging);
        belonging.assignTeam(this);
    }
}
