package com.nnam01.study.controller;

import com.nnam01.study.dto.EventResultDto;
import com.nnam01.study.dto.EventTimeRequestDto;
import com.nnam01.study.dto.ParticipationRequest;
import com.nnam01.study.service.TimeAttackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/time-attack")
@RequiredArgsConstructor
public class TimeAttackController {

    private final TimeAttackService timeAttackService;

    @PostMapping("/event/time")
    public ResponseEntity<String> setEventTime(@RequestBody EventTimeRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
        LocalDateTime ldt = LocalDateTime.parse(request.getEventTime(), formatter);
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of("Asia/Seoul"));
        timeAttackService.setEventTime(zdt);
        return ResponseEntity.ok("이벤트 시간이 설정되었습니다.");
    }

    /**
     * 선착순 요청 처리 API (Redis 사용)
     */
    @PostMapping("/participate/redis")
    public ResponseEntity<String> participateWithRedis(@RequestBody ParticipationRequest request) {
        try {
            String result = timeAttackService.participateWithRedis(request.getPhoneNumber(), request.getName());
            return ResponseEntity.accepted().body(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 선착순 요청 처리 API (H2 DB 사용)
     */
    @PostMapping("/participate/h2")
    public ResponseEntity<String> participateWithH2(@RequestBody ParticipationRequest request) {
        try {
            String result = timeAttackService.participateWithH2(request.getPhoneNumber(), request.getName());
            return ResponseEntity.accepted().body(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 이벤트 상태 조회 API
     */
    @GetMapping("/event/status")
    public ResponseEntity<String> getEventStatus() {
        return ResponseEntity.ok(timeAttackService.getEventStatus());
    }

    /**
     * 선착순 결과 조회 API
     */
    @GetMapping("/event/results")
    public ResponseEntity<List<EventResultDto>> getEventResults() {
        return ResponseEntity.ok(timeAttackService.getEventResults());
    }

    /**
     * 서버 현재 시간 조회 API
     */
    @GetMapping("/server-time")
    public ResponseEntity<String> getServerTime() {
        return ResponseEntity.ok(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}