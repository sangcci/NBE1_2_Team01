package org.team1.nbe1_2_team01.domain.chat.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.team1.nbe1_2_team01.domain.chat.controller.request.ChannelRequest;
import org.team1.nbe1_2_team01.domain.chat.controller.request.InviteRequest;
import org.team1.nbe1_2_team01.domain.chat.controller.request.KickOutRequest;
import org.team1.nbe1_2_team01.domain.chat.entity.Channel;
import org.team1.nbe1_2_team01.domain.chat.entity.Participant;
import org.team1.nbe1_2_team01.domain.chat.entity.ParticipantPK;
import org.team1.nbe1_2_team01.domain.chat.service.ParticipantService;
import org.team1.nbe1_2_team01.domain.chat.service.response.ChannelResponse;
import org.team1.nbe1_2_team01.domain.chat.service.response.ParticipantResponse;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/participants")
@RestController
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    // 참여자가 스스로 방 아이디를 치고 들어오는 경우
    @PostMapping("/join")
    public ResponseEntity<ParticipantResponse> joinChannel(@RequestBody ChannelRequest channelRequest) {
        ParticipantResponse participantResponse = participantService.joinChannel(channelRequest.getChannelId(), channelRequest.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(participantResponse);
    }

    // 방장(생성자) -> 참여자를 초대
    @PostMapping("/invite")
    public ResponseEntity<Void> inviteUser(@RequestBody InviteRequest inviteRequest) {
        try {
            participantService.inviteUser(inviteRequest);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 참여자가 속해 있는 채널들 보기
    @GetMapping("/{userId}/show")
    public ResponseEntity<List<ChannelResponse>> checkUserChannels(@PathVariable("userId") Long userId) {
        List<Channel> channels = participantService.checkUserChannel(userId);

        List<ChannelResponse> responses = channels.stream()
                .map(channel -> new ChannelResponse(channel.getId(), channel.getChannelName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // 참여자가 스스로 방을 나가기 (채널 탈퇴)
    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveChannel(@RequestBody ChannelRequest channelRequest) {
        ParticipantPK participantPK = new ParticipantPK(channelRequest.getUserId(), channelRequest.getChannelId());
        participantService.leaveChannel(participantPK);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 방장이 참여자를 내보내기 (강퇴)
    @DeleteMapping("/kickOut")
    public ResponseEntity<String> removeParticipant(@RequestBody KickOutRequest kickOutRequest) {
        ParticipantPK creator = new ParticipantPK(kickOutRequest.getCreatorId(), kickOutRequest.getChannelId());
        participantService.removeParticipant(creator, kickOutRequest.getParticipantIdToRemove());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}