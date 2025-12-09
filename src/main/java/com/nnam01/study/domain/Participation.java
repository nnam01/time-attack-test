package com.nnam01.study.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private String name;
    private LocalDateTime participationTime;

    public Participation(String phoneNumber, String name, LocalDateTime participationTime) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.participationTime = participationTime;
    }
}
