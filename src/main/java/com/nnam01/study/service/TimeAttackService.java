package com.nnam01.study.service;

import com.nnam01.study.domain.Participation;
import com.nnam01.study.dto.EventResultDto;
import com.nnam01.study.repository.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeAttackService {

    private ZonedDateTime eventStartTime;
    private static final String PARTICIPATION_KEY = "time-attack:participants";
    private static final int MAX_PARTICIPANTS = 500;

    private final StringRedisTemplate redisTemplate;
    private final ParticipationRepository participationRepository;

    @Autowired
    public TimeAttackService(StringRedisTemplate redisTemplate, ParticipationRepository participationRepository) {
        this.redisTemplate = redisTemplate;
        this.participationRepository = participationRepository;
    }

    public void setEventTime(ZonedDateTime eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    @Transactional
    public String participateWithRedis(String phoneNumber, String name) {
        validateEventTime();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // Redis Sorted Set을 사용하여 선착순 참여를 처리
        Long currentParticipants = redisTemplate.opsForZSet().size(PARTICIPATION_KEY);

        if (currentParticipants != null && currentParticipants >= MAX_PARTICIPANTS) {
            return "참여 실패: 선착순 마감";
        }

        // 사용자 ID (여기서는 전화번호를 사용)가 이미 참여했는지 확인
        Double score = redisTemplate.opsForZSet().score(PARTICIPATION_KEY, phoneNumber);
        if (score != null) {
            return "참여 실패: 이미 참여했습니다.";
        }

        // 참여자 추가 (score는 현재 시간의 epoch second)
        long timestamp = now.toEpochSecond();
        Boolean added = redisTemplate.opsForZSet().add(PARTICIPATION_KEY, phoneNumber, timestamp);

        if (Boolean.TRUE.equals(added)) {
            // Redis에 성공적으로 추가되었을 때만 DB에 저장
            Participation participation = new Participation(phoneNumber, name, now.toLocalDateTime());
            participationRepository.save(participation);

            // 다시 한번 참여자 수 확인하여 MAX_PARTICIPANTS를 초과하는지 검증
            Long finalParticipantsCount = redisTemplate.opsForZSet().zCard(PARTICIPATION_KEY);
            if (finalParticipantsCount != null && finalParticipantsCount <= MAX_PARTICIPANTS) {
                return "참여 완료: " + name + "님 (" + finalParticipantsCount + "번째)";
            } else {
                // 경합 상황에서 초과된 경우, 추가된 사용자를 Redis와 DB에서 제거 (롤백)
                redisTemplate.opsForZSet().remove(PARTICIPATION_KEY, phoneNumber);
                throw new IllegalStateException("참여 실패: 선착순 마감 (동시성 오류 발생)");
            }
        } else {
            return "참여 실패: 알 수 없는 오류";
        }
    }

    public List<EventResultDto> getEventResults() {
        List<Participation> participants = participationRepository.findAllByOrderByParticipationTimeAsc();
        if (participants == null) {
            return new ArrayList<>();
        }

        List<EventResultDto> resultList = new ArrayList<>();
        long rank = 1;
        for (Participation participant : participants) {
            double timestamp = participant.getParticipationTime().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli() / 1000.0;
            resultList.add(new EventResultDto(rank++, participant.getPhoneNumber(), timestamp));
        }
        return resultList;
    }

    @Transactional
    public String participateWithH2(String phoneNumber, String name) {
        validateEventTime();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // H2 DB를 사용하여 선착순 참여를 처리
        long currentParticipants = participationRepository.count();

        if (currentParticipants >= MAX_PARTICIPANTS) {
            return "참여 실패: 선착순 마감";
        }

        // 사용자 ID (여기서는 전화번호를 사용)가 이미 참여했는지 확인
        if (participationRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            return "참여 실패: 이미 참여했습니다.";
        }

        // 참여자 추가
        Participation participation = new Participation(phoneNumber, name, now.toLocalDateTime());
        participationRepository.save(participation);

        // 다시 한번 참여자 수 확인하여 MAX_PARTICIPANTS를 초과하는지 검증
        long finalParticipantsCount = participationRepository.count();
        if (finalParticipantsCount <= MAX_PARTICIPANTS) {
            return "참여 완료: " + name + "님 (" + finalParticipantsCount + "번째)";
        } else {
            // 경합 상황에서 초과된 경우, 추가된 사용자를 DB에서 제거 (롤백)
            throw new IllegalStateException("참여 실패: 선착순 마감 (동시성 오류 발생)");
        }
    }

    private void validateEventTime() {
        if (eventStartTime == null) {
            throw new IllegalStateException("이벤트 시간이 설정되지 않았습니다.");
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime eventEndTime = eventStartTime.plusMinutes(30);

        if (now.isBefore(eventStartTime) || now.isAfter(eventEndTime)) {
            throw new IllegalStateException("지정된 참여 시간이 아닙니다. 이벤트는 " + eventStartTime + " 부터 " + eventEndTime + " 까지 진행됩니다.");
        }
    }

    public String getEventStatus() {
        if (eventStartTime == null) {
            return "이벤트 시간 미설정";
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime eventEndTime = eventStartTime.plusMinutes(30);

        if (now.isBefore(eventStartTime)) {
            return "이벤트 시작 전";
        } else if (now.isAfter(eventEndTime)) {
            return "이벤트 종료";
        } else {
            return "이벤트 진행 중";
        }
    }
}
