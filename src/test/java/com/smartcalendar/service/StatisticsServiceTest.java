package com.smartcalendar.service;

import com.smartcalendar.dto.*;
import com.smartcalendar.model.Statistics;
import com.smartcalendar.model.User;
import com.smartcalendar.repository.StatisticsRepository;
import com.smartcalendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStatistics_WhenStatsExist() {
        Statistics stats = new Statistics();
        stats.setTotalCommon(1);
        stats.setTotalWork(2);
        stats.setTotalStudy(3);
        stats.setTotalFitness(4);
        stats.setWeekTime(5);
        stats.setTodayPlanned(6);
        stats.setTodayCompleted(7);
        stats.setContinuesRecord(8);
        stats.setContinuesNow(9);
        stats.setAverageWorkMinutes(10);
        stats.setFirstDay(LocalDate.of(2024, 1, 1));

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));

        StatisticsData data = statisticsService.getStatistics(1L);

        assertEquals(1, data.getTotalTime().getCommon());
        assertEquals(2, data.getTotalTime().getWork());
        assertEquals(3, data.getTotalTime().getStudy());
        assertEquals(4, data.getTotalTime().getFitness());
        assertEquals(5, data.getWeekTime());
        assertEquals(6, data.getTodayTime().getPlanned());
        assertEquals(7, data.getTodayTime().getCompleted());
        assertEquals(8, data.getContinuesSuccessDays().getRecord());
        assertEquals(9, data.getContinuesSuccessDays().getNow());
        assertEquals(10, data.getAverageDayTime().getTotalWorkMinutes());
        assertEquals(LocalDate.of(2024, 1, 1), data.getAverageDayTime().getFirstDay());
        assertNotNull(data.getJsonDate()); // Новая проверка
        assertTrue(data.getJsonDate().getTime() <= new Date().getTime()); // jsonDate не в будущем
    }

    @Test
    void testGetStatistics_WhenStatsNotExist() {
        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        StatisticsData data = statisticsService.getStatistics(1L);
        assertNotNull(data);
        assertEquals(0, data.getTotalTime().getCommon());
        assertNull(data.getAverageDayTime().getFirstDay());
        assertNotNull(data.getJsonDate()); // Новая проверка
        assertTrue(data.getJsonDate().getTime() <= new Date().getTime());
    }

    @Test
    void testUpdateStatistics_NewStats() {
        User user = new User();
        user.setId(1L);

        StatisticsData dto = new StatisticsData(
                new TotalTimeTaskTypesDto(1, 2, 3, 4),
                5L,
                new TodayTimeDto(6, 7),
                new ContinuesSuccessDaysDto(8, 9),
                new AverageDayTimeDto(10, LocalDate.of(2024, 1, 1)),
                new Date()
        );

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statisticsRepository.save(any(Statistics.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> statisticsService.updateStatistics(1L, dto));
        verify(statisticsRepository).save(any(Statistics.class));
    }

    @Test
    void testUpdateStatistics_ExistingStats() {
        User user = new User();
        user.setId(1L);
        Statistics stats = new Statistics();
        stats.setUser(user);

        StatisticsData dto = new StatisticsData(
                new TotalTimeTaskTypesDto(1, 2, 3, 4),
                5L,
                new TodayTimeDto(6, 7),
                new ContinuesSuccessDaysDto(8, 9),
                new AverageDayTimeDto(10, LocalDate.of(2024, 1, 1)),
                new Date()
        );

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        when(statisticsRepository.save(any(Statistics.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> statisticsService.updateStatistics(1L, dto));
        verify(statisticsRepository).save(any(Statistics.class));
    }

    @Test
    void testGetTotalTimeTaskTypes() {
        Statistics stats = new Statistics();
        stats.setTotalCommon(1);
        stats.setTotalWork(2);
        stats.setTotalStudy(3);
        stats.setTotalFitness(4);

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        TotalTimeTaskTypesDto dto = statisticsService.getTotalTimeTaskTypes(1L);
        assertEquals(1, dto.getCommon());
        assertEquals(2, dto.getWork());
        assertEquals(3, dto.getStudy());
        assertEquals(4, dto.getFitness());
    }

    @Test
    void testGetTodayTimeDto() {
        Statistics stats = new Statistics();
        stats.setTodayPlanned(5);
        stats.setTodayCompleted(6);

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        TodayTimeDto dto = statisticsService.getTodayTimeDto(1L);
        assertEquals(5, dto.getPlanned());
        assertEquals(6, dto.getCompleted());
    }

    @Test
    void testGetContinuesSuccessDaysDto() {
        Statistics stats = new Statistics();
        stats.setContinuesRecord(7);
        stats.setContinuesNow(8);

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        ContinuesSuccessDaysDto dto = statisticsService.getContinuesSuccessDaysDto(1L);
        assertEquals(7, dto.getRecord());
        assertEquals(8, dto.getNow());
    }

    @Test
    void testGetAverageDayTimeDto() {
        Statistics stats = new Statistics();
        stats.setAverageWorkMinutes(9);
        stats.setFirstDay(LocalDate.of(2024, 2, 2));

        when(statisticsRepository.findByUserId(1L)).thenReturn(Optional.of(stats));
        AverageDayTimeDto dto = statisticsService.getAverageDayTimeDto(1L);
        assertEquals(9, dto.getTotalWorkMinutes());
        assertEquals(LocalDate.of(2024, 2, 2), dto.getFirstDay());
    }
}
