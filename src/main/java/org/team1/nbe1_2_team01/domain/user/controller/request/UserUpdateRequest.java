package org.team1.nbe1_2_team01.domain.user.controller.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 2, message = "이름은 2자 이상이어야 합니다.")
        String name,

        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$", message = "비밀번호는 영문과 숫자를 포함해야 합니다.")
        String password
) {
}
