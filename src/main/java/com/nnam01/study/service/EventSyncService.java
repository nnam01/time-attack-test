package com.nnam01.study.service;

import com.nnam01.study.domain.Participation;
import com.nnam01.study.repository.ParticipationJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventSyncService {

    private final StringRedisTemplate redisTemplate;
    private final ParticipationJdbcRepository participationJdbcRepository;

    private static final String PARTICIPATION_KEY = "time-attack:participants";
    private static final String USER_INFO_KEY = "time-attack:user-info";

    public int syncRedisToDatabase() {
        Set<String> participants = redisTemplate.opsForZSet().range(PARTICIPATION_KEY, 0, -1);
        if (participants == null || participants.isEmpty()) {
            return 0;
        }

        List<Participation> entities = new ArrayList<>();
        for (String phone : participants) {
            Double score = redisTemplate.opsForZSet().score(PARTICIPATION_KEY, phone);
            LocalDateTime time = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(score.longValue()), ZoneId.of("Asia/Seoul"));

            Object nameObj = redisTemplate.opsForHash().get(USER_INFO_KEY, phone);
            String name = (nameObj != null) ? nameObj.toString() : "Unknown";

            entities.add(new Participation(phone, name, time));
        }

        participationJdbcRepository.batchInsert(entities);
        redisTemplate.delete(List.of(PARTICIPATION_KEY, USER_INFO_KEY));

        return entities.size();
    }
}
