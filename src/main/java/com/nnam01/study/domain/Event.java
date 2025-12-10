package com.nnam01.study.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    private Long id;

    private int limitCount;   // 선착순 제한 (500)
    private int currentCount; // 현재 당첨자 수

    public Event(Long id, int limitCount) {
        this.id = id;
        this.limitCount = limitCount;
        this.currentCount = 0;
    }

    // 비즈니스 로직 (수량 증가)
    public boolean increase() {
        if (currentCount < limitCount) {
            currentCount++;
            return true;
        }
        return false;
    }
}