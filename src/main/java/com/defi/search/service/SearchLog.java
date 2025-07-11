package com.defi.search.service;

import com.defi.common.util.log.entity.EventLog;
import com.defi.search.dto.TargetCount;
import com.defi.search.dto.TypeCount;
import com.defi.search.dto.UserActivityCount;

import java.util.List;

public interface SearchLog {
    List<EventLog> findByDateRange(Long startTime, Long endTime);
    List<EventLog> findUserByType(String type, Long startTime, Long endTime);
    List<EventLog> findUserByTargetType(String targetType, Long startTime, Long endTime);
    List<EventLog> findUserBySubjectType(String subjectType, Long startTime, Long endTime);
    List<TargetCount> countEventsByTarget(Long startTime, Long endTime, int size);
    List<TypeCount> countEventsByType(Long startTime, Long endTime, int size);
    List<UserActivityCount> findTopUsersByEventCount(Long startTime, Long endTime, int size);
    List<EventLog> findEventLogBySubjectId(String subjectId);
}
