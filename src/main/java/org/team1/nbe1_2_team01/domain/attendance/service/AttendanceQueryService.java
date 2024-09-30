package org.team1.nbe1_2_team01.domain.attendance.service;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team1.nbe1_2_team01.domain.attendance.entity.Attendance;
import org.team1.nbe1_2_team01.domain.attendance.repository.AttendanceRepository;
import org.team1.nbe1_2_team01.domain.attendance.service.response.AttendanceResponse;
import org.team1.nbe1_2_team01.domain.user.entity.User;
import org.team1.nbe1_2_team01.domain.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public List<AttendanceResponse> getAll() {
        List<Attendance> attendances = attendanceRepository.findAll();

        return attendances.stream()
                .map(attendance -> AttendanceResponse.from(attendance.getUser(), attendance))
                .toList();
    }

    public List<AttendanceResponse> getMyAttendances(String currentUsername) {
        User currentUser = getUserByUsername(currentUsername);

        List<Attendance> myAttendances = attendanceRepository.findAttendancesByUserId(currentUser.getId());

        return myAttendances.stream()
                .map(attendance -> AttendanceResponse.from(attendance.getUser(), attendance))
                .toList();
    }

    public AttendanceResponse getById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("출결 요청을 찾을 수 없습니다."));

        return AttendanceResponse.from(attendance.getUser(), attendance);
    }

    public AttendanceResponse getByIdAndUserId(Long attendanceId, String currentUsername) {
        User currentUser = getUserByUsername(currentUsername);

        Attendance attendance = attendanceRepository.findByIdAndUserId(attendanceId, currentUser.getId())
                .orElseThrow(() -> new NoSuchElementException("출결 요청을 찾을 수 없습니다."));

        return AttendanceResponse.from(attendance.getUser(), attendance);
    }

    // 타 서비스 메서드
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));
    }
}