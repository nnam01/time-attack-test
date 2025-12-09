package com.nnam01.study.repository;

import com.nnam01.study.domain.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByPhoneNumber(String phoneNumber);
    List<Participation> findAllByOrderByParticipationTimeAsc();
}
