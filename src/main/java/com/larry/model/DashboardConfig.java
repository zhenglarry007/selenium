package com.larry.model;

public record DashboardConfig(
    Integer expectedMinimumTotalBookings,
    Integer expectedMinimumPendingBookings,
    Integer expectedMinimumRoomTypes,
    Integer expectedMinimumRecords
) {
}
