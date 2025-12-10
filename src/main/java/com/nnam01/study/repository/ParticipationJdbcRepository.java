package com.nnam01.study.repository;

import com.nnam01.study.domain.Participation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParticipationJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(List<Participation> list) {
        // JDBC로 직접 쿼리 날림
        String sql = "INSERT INTO participation (phone_number, name, participation_time) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql,
                list,
                list.size(),
                (PreparedStatement ps, Participation participation) -> {
                    ps.setString(1, participation.getPhoneNumber());
                    ps.setString(2, participation.getName());
                    ps.setTimestamp(3, Timestamp.valueOf(participation.getParticipationTime()));
                });
    }
}