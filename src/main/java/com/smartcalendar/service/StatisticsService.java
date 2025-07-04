package com.smartcalendar.service;

import com.smartcalendar.dto.*;
import com.smartcalendar.model.Statistics;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.StatisticsRepository;
import com.smartcalendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public StatisticsData getStatistics(Long userId) {
        Statistics stats = statisticsRepository.findByUserId(userId)
                .orElse(null);

        Date now = new Date();

        if (stats == null) {
            return new StatisticsData(
                    new TotalTimeTaskTypesDto(0, 0, 0, 0),
                    0L,
                    new TodayTimeDto(0, 0),
                    new ContinuesSuccessDaysDto(0, 0),
                    new AverageDayTimeDto(0, null),
                    now
            );
        }

        return new StatisticsData(
                new TotalTimeTaskTypesDto(stats.getTotalCommon(), stats.getTotalWork(), stats.getTotalStudy(), stats.getTotalFitness()),
                stats.getWeekTime(),
                new TodayTimeDto(stats.getTodayPlanned(), stats.getTodayCompleted()),
                new ContinuesSuccessDaysDto(stats.getContinuesRecord(), stats.getContinuesNow()),
                new AverageDayTimeDto(stats.getAverageWorkMinutes(), stats.getFirstDay()),
                now
        );
    }

    @Transactional
    public void updateStatistics(Long userId, StatisticsData statisticsData) {
        Statistics stats = statisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Statistics s = new Statistics();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    s.setUser(user);
                    return s;
                });

        stats.setTotalCommon(statisticsData.getTotalTime().getCommon());
        stats.setTotalWork(statisticsData.getTotalTime().getWork());
        stats.setTotalStudy(statisticsData.getTotalTime().getStudy());
        stats.setTotalFitness(statisticsData.getTotalTime().getFitness());

        stats.setWeekTime(statisticsData.getWeekTime());

        stats.setTodayPlanned(statisticsData.getTodayTime().getPlanned());
        stats.setTodayCompleted(statisticsData.getTodayTime().getCompleted());

        stats.setContinuesRecord(statisticsData.getContinuesSuccessDays().getRecord());
        stats.setContinuesNow(statisticsData.getContinuesSuccessDays().getNow());

        stats.setAverageWorkMinutes(statisticsData.getAverageDayTime().getTotalWorkMinutes());
        stats.setFirstDay(statisticsData.getAverageDayTime().getFirstDay());

        statisticsRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public TotalTimeTaskTypesDto getTotalTimeTaskTypes(Long userId) {
        Statistics stats = statisticsRepository.findByUserId(userId)
                .orElse(null);

        if (stats == null) {
            return new TotalTimeTaskTypesDto(0, 0, 0, 0);
        }

        return new TotalTimeTaskTypesDto(
                stats.getTotalCommon(),
                stats.getTotalWork(),
                stats.getTotalStudy(),
                stats.getTotalFitness()
        );
    }

    @Transactional(readOnly = true)
    public TodayTimeDto getTodayTimeDto(Long userId) {
        Statistics stats = statisticsRepository.findByUserId(userId)
                .orElse(null);

        if (stats == null) {
            return new TodayTimeDto(0, 0);
        }

        return new TodayTimeDto(
                stats.getTodayPlanned(),
                stats.getTodayCompleted()
        );
    }

    @Transactional(readOnly = true)
    public ContinuesSuccessDaysDto getContinuesSuccessDaysDto(Long userId) {
        Statistics stats = statisticsRepository.findByUserId(userId)
                .orElse(null);

        if (stats == null) {
            return new ContinuesSuccessDaysDto(0, 0);
        }

        return new ContinuesSuccessDaysDto(
                stats.getContinuesRecord(),
                stats.getContinuesNow()
        );
    }

    @Transactional(readOnly = true)
    public AverageDayTimeDto getAverageDayTimeDto(Long userId) {
        Statistics stats = statisticsRepository.findByUserId(userId)
                .orElse(null);

        if (stats == null) {
            return new AverageDayTimeDto(0, null);
        }

        return new AverageDayTimeDto(
                stats.getAverageWorkMinutes(),
                stats.getFirstDay()
        );
    }
}