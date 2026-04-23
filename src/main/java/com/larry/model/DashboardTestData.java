package com.larry.model;

public record DashboardTestData(
    String testCaseName,
    String testDescription,
    Boolean expectStatisticsVisible,
    Integer expectedMinimumTotalBookings,
    Integer expectedMinimumPendingBookings,
    Integer expectedMinimumRoomTypes,
    Boolean expectBookingRecords,
    Integer expectedMinimumRecords,
    Boolean testRefreshButton,
    String expectedResult
) {
}
