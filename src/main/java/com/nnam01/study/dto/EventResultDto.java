package com.nnam01.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventResultDto {
    private long rank;
    private String phoneNumber;
    private double timestamp;
}
