package com.nnam01.study.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/time-attack")
public class TimeAttackController {

    /**
     * 선착순 요청 처리 API (Redis 사용)
     */
    @PostMapping("/participate/redis")
    public ResponseEntity<String> participateWithRedis() {
        // TODO: Redis에 사용자 요청을 저장하는 Service 로직 호출
        return ResponseEntity.accepted().body("[Redis] 참여 요청이 성공적으로 접수되었습니다.");
    }

    /**
     * 선착순 요청 처리 API (H2 DB 사용)
     */
    @PostMapping("/participate/h2")
    public ResponseEntity<String> participateWithH2() {
        // TODO: H2 DB에 사용자 요청을 저장하는 Service 로직 호출
        return ResponseEntity.ok().body("[H2] 참여 요청이 성공적으로 처리되었습니다.");
    }

    /**
     * 이벤트 상태 조회 API
     */
    @GetMapping("/event/status")
    public ResponseEntity<String> getEventStatus() {
        // TODO: 현재 이벤트 상태(시작 전, 진행 중, 종료)를 반환하는 로직 구현
        return ResponseEntity.ok("");
    }

    /**
     * 선착순 결과 조회 API
     */
    @GetMapping("/event/results")
    public ResponseEntity<String> getEventResults() {
        // TODO: 이벤트 결과를 조회하는 로직 구현 (e.g., 상위 5명)
        return ResponseEntity.ok("");
    }

    /**
     * 자신의 순위/결과 확인 API
     */
    @GetMapping("/me/results")
    public ResponseEntity<String> getMyResults() {
        // TODO: 현재 로그인된 사용자의 순위 또는 결과를 조회하는 로직 구현
        return ResponseEntity.ok("");
    }

    /**
     * 서버 현재 시간 조회 API
     */
    @GetMapping("/server-time")
    public ResponseEntity<String> getServerTime() {
        return ResponseEntity.ok(java.time.LocalDateTime.now().toString());
    }
}
