package com.nnam01.study.service;

import com.nnam01.study.domain.Event;
import com.nnam01.study.domain.Participation;
import com.nnam01.study.dto.EventResultDto;
import com.nnam01.study.repository.EventRepository;
import com.nnam01.study.repository.ParticipationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeAttackService {

    private ZonedDateTime eventStartTime;
    private static final String PARTICIPATION_KEY = "time-attack:participants";
    private static final String USER_INFO_KEY = "time-attack:user-info"; // [추가] 이름 저장용 Hash Key
    private static final int MAX_PARTICIPANTS = 500;

    private final StringRedisTemplate redisTemplate;
    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;

    private final String ADD_PARTICIPANT_SCRIPT =
            "if redis.call('ZCARD', KEYS[1]) >= tonumber(ARGV[1]) then return -1 end " +    // 마감 시 -1 (숫자)
                    "if redis.call('ZSCORE', KEYS[1], ARGV[2]) then return -2 end " +       // 중복 시 -2 (숫자)
                    "redis.call('ZADD', KEYS[1], ARGV[3], ARGV[2]) " +
                    "redis.call('HSET', KEYS[2], ARGV[2], ARGV[4]) " +
                    "return redis.call('ZRANK', KEYS[1], ARGV[2]) + 1";                     // 등수(숫자) 리턴

    @PostConstruct
    public void init() {
        if (!eventRepository.existsById(1L)) {
            eventRepository.save(new Event(1L, MAX_PARTICIPANTS));
        }
    }

    public void setEventTime(ZonedDateTime eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String participateWithRedis(String phoneNumber, String name) {
        validateEventTime();
        long timestamp = System.currentTimeMillis();

        // 리턴 타입을 Long으로 설정
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(ADD_PARTICIPANT_SCRIPT, Long.class);

        Long result = redisTemplate.execute(redisScript,
                List.of(PARTICIPATION_KEY, USER_INFO_KEY),
                String.valueOf(MAX_PARTICIPANTS),
                phoneNumber,
                String.valueOf(timestamp),
                name
        );

        if (result == null) {
            return "참여 실패: 시스템 오류";
        }

        if (result == -1) {
            return "참여 실패: 선착순 마감";
        } else if (result == -2) {
            return "참여 실패: 이미 참여했습니다.";
        } else {
            // result는 1부터 시작하는 등수
            return "참여 완료: " + name + "님 (" + result + "번째)";
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

        Event event = eventRepository.findByIdForUpdate(1L)
                .orElseThrow(() -> new IllegalStateException("이벤트가 존재하지 않습니다."));
        if (!event.increase()) {
            return "참여 실패: 선착순 마감";
        }
        if (participationRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalStateException("참여 실패: 이미 참여했습니다.");
        }
        Participation participation = new Participation(phoneNumber, name, now.toLocalDateTime());
        participationRepository.save(participation);

        return "참여 완료: " + name + "님 (" + event.getCurrentCount() + "번째)";
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
