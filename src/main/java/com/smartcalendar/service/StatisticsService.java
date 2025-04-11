package com.smartcalendar.service;

import com.smartcalendar.dto.*;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    public TotalTimeTaskTypes getTotalTimeTaskTypes() {
        long common = 120;
        long work = 300;
        long study = 180;
        long fitness = 60;
        return new TotalTimeTaskTypes(common, work, study, fitness);
    }

    public TodayTimeVars getTodayTimeVars() {
        long planned = 480;
        long completed = 300;
        return new TodayTimeVars(planned, completed);
    }

    public ContinuousSuccessDaysVars getContinuousSuccessDaysVars() {
        int record = 10;
        int now = 5;
        return new ContinuousSuccessDaysVars(record, now);
    }

    public AverageDayTimeVars getAverageDayTimeVars() {
        long totalWorkMinutes = 14400;
        long totalDays = 30;
        return new AverageDayTimeVars(totalWorkMinutes / totalDays);
    }
}