package com.smartcalendar.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "statistics")
@Data
@NoArgsConstructor
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private long totalCommon;
    private long totalWork;
    private long totalStudy;
    private long totalFitness;

    private long weekTime;

    private long todayPlanned;
    private long todayCompleted;

    private int continuesRecord;
    private int continuesNow;

    private long averageWorkMinutes;
    private LocalDate firstDay;
}