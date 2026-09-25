package com.example.mockserver.application;

import com.example.mockserver.application.booking.contract.CatsBookingContracts;
import com.example.mockserver.application.booking.contract.OrgAReservationContracts;
import com.example.mockserver.application.support.contract.OrgBSupportContracts;
import com.example.mockserver.application.visit.contract.OrgBVisitContracts;
import com.example.mockserver.domain.booking.model.BookingDomainModels;
import com.example.mockserver.presentation.booking.web.dto.CatsBookingServiceDtos;
import com.example.mockserver.presentation.booking.web.dto.OrgAReservationServiceDtos;
import com.example.mockserver.presentation.support.web.dto.OrgBSupportServiceDtos;
import com.example.mockserver.presentation.visit.web.dto.OrgBVisitServiceDtos;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseSnapshotTest {
    @Test
    void listResponsesAtEveryBoundaryKeepCallerOwnedCollectionsIsolated() {
        this.<OrgBSupportServiceDtos.DeviceSummary>assertSnapshot(items ->
                new OrgBSupportServiceDtos.DeviceListResponse(items, 0, 0, 0).items());
        this.<OrgBVisitServiceDtos.SiteSummary>assertSnapshot(items ->
                new OrgBVisitServiceDtos.SiteListResponse(items, 0, 0, 0).items());
        this.<CatsBookingServiceDtos.ResourceSummary>assertSnapshot(items ->
                new CatsBookingServiceDtos.ResourceListResponse(items, 0, 0, 0).items());
        this.<CatsBookingServiceDtos.ScheduleItem>assertSnapshot(items ->
                new CatsBookingServiceDtos.ScheduleListResponse(null, items).items());
        this.<CatsBookingServiceDtos.DailyScheduleItem>assertSnapshot(items ->
                new CatsBookingServiceDtos.DailyScheduleListResponse(null, items).items());
        this.<OrgAReservationServiceDtos.ResourceSummary>assertSnapshot(items ->
                new OrgAReservationServiceDtos.ResourceListResponse(items, 0, 0, 0).items());
        this.<OrgAReservationServiceDtos.ScheduleItem>assertSnapshot(items ->
                new OrgAReservationServiceDtos.ScheduleListResponse(null, items).items());
        this.<OrgAReservationServiceDtos.DailyScheduleItem>assertSnapshot(items ->
                new OrgAReservationServiceDtos.DailyScheduleListResponse(null, items).items());
        this.<BookingDomainModels.ResourceSummary>assertSnapshot(items ->
                new BookingDomainModels.ResourceListResponse(items, 0, 0, 0).items());
        this.<BookingDomainModels.ScheduleItem>assertSnapshot(items ->
                new BookingDomainModels.ScheduleListResponse(null, items).items());
        this.<BookingDomainModels.DailyScheduleItem>assertSnapshot(items ->
                new BookingDomainModels.DailyScheduleListResponse(null, items).items());
        this.<OrgBSupportContracts.DeviceSummary>assertSnapshot(items ->
                new OrgBSupportContracts.DeviceListResult(items, 0, 0, 0).items());
        this.<OrgBVisitContracts.SiteSummary>assertSnapshot(items ->
                new OrgBVisitContracts.SiteListResult(items, 0, 0, 0).items());
        this.<CatsBookingContracts.ResourceSummary>assertSnapshot(items ->
                new CatsBookingContracts.ResourceListResult(items, 0, 0, 0).items());
        this.<CatsBookingContracts.ScheduleItem>assertSnapshot(items ->
                new CatsBookingContracts.ScheduleListResult(null, items).items());
        this.<CatsBookingContracts.DailyScheduleItem>assertSnapshot(items ->
                new CatsBookingContracts.DailyScheduleListResult(null, items).items());
        this.<OrgAReservationContracts.ResourceSummary>assertSnapshot(items ->
                new OrgAReservationContracts.ResourceListResult(items, 0, 0, 0).items());
        this.<OrgAReservationContracts.ScheduleItem>assertSnapshot(items ->
                new OrgAReservationContracts.ScheduleListResult(null, items).items());
        this.<OrgAReservationContracts.DailyScheduleItem>assertSnapshot(items ->
                new OrgAReservationContracts.DailyScheduleListResult(null, items).items());
    }

    private <T> void assertSnapshot(Function<List<T>, List<T>> createResponse) {
        List<T> original = new ArrayList<>();
        List<T> snapshot = createResponse.apply(original);
        original.add(null);
        assertThat(snapshot).isEmpty();
        assertThatThrownBy(() -> snapshot.add(null)).isInstanceOf(UnsupportedOperationException.class);
    }
}
