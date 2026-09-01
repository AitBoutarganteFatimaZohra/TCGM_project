package com.tcgm.service;

import com.tcgm.dto.response.NotificationCountsResponse;

public interface NotificationService {
    NotificationCountsResponse getPendingCounts();
}