package com.example.mockserver.application.booking.service;

import com.example.mockserver.application.booking.contract.OrgAReservationContracts;
import com.example.mockserver.domain.booking.BookingOperations;
import com.example.mockserver.domain.booking.model.BookingDomainModels;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class OrgAReservationService {

    private final BookingOperations bookings;

    public OrgAReservationService(Clock clock, Supplier<UUID> identifiers) {
        this.bookings = new BookingOperations(clock, identifiers);
    }

    public OrgAReservationContracts.ResourceListResult listResources(int page, int size, String category) {
        return toOrgA(bookings.listResourcesModel(page, size, category));
    }

    public OrgAReservationContracts.ResourceDetail getResource(String resourceId) {
        return toOrgA(bookings.getResourceModel(resourceId));
    }

    public OrgAReservationContracts.InventoryStatus getInventory(String resourceId, java.time.LocalDate date) {
        return toOrgA(bookings.getInventoryModel(resourceId, date));
    }

    public OrgAReservationContracts.ScheduleListResult getSchedules(String resourceId,
                                                                     java.time.OffsetDateTime from,
                                                                     java.time.OffsetDateTime to) {
        return toOrgA(bookings.getSchedulesModel(resourceId, from, to));
    }

    public OrgAReservationContracts.DailyScheduleListResult listDailySchedules(java.time.LocalDate date) {
        return toOrgA(bookings.listDailySchedulesModel(date));
    }

    public OrgAReservationContracts.ReservationCreateResult createReservation(OrgAReservationContracts.ReservationCreateCommand request) {
        return toOrgA(bookings.createReservationModel(toDomain(request)));
    }

    public OrgAReservationContracts.ReservationCancelResult cancelReservation(String reservationId,
                                                                               OrgAReservationContracts.ReservationCancelCommand request) {
        return toOrgA(bookings.cancelReservationModel(reservationId, toDomain(request)));
    }

    public OrgAReservationContracts.ReservationDetailResult getReservation(String reservationId) {
        return toOrgA(bookings.getReservationModel(reservationId));
    }

    private BookingDomainModels.ReservationCreateRequest toDomain(OrgAReservationContracts.ReservationCreateCommand request) {
        return new BookingDomainModels.ReservationCreateRequest(
                request.resourceId(),
                request.scheduleId(),
                request.userId(),
                request.quantity(),
                request.memo()
        );
    }

    private BookingDomainModels.ReservationCancelRequest toDomain(OrgAReservationContracts.ReservationCancelCommand request) {
        return new BookingDomainModels.ReservationCancelRequest(request.reason());
    }

    private OrgAReservationContracts.ResourceListResult toOrgA(BookingDomainModels.ResourceListResponse response) {
        return new OrgAReservationContracts.ResourceListResult(
                response.items().stream().map(this::toOrgA).toList(),
                response.page(),
                response.size(),
                response.total()
        );
    }

    private OrgAReservationContracts.ResourceSummary toOrgA(BookingDomainModels.ResourceSummary summary) {
        return new OrgAReservationContracts.ResourceSummary(summary.resourceId(), summary.name(), summary.category(), summary.active());
    }

    private OrgAReservationContracts.ResourceDetail toOrgA(BookingDomainModels.ResourceDetail detail) {
        return new OrgAReservationContracts.ResourceDetail(
                detail.resourceId(),
                detail.name(),
                detail.category(),
                detail.active(),
                detail.description(),
                detail.location(),
                detail.timezone()
        );
    }

    private OrgAReservationContracts.InventoryStatus toOrgA(BookingDomainModels.InventoryStatus status) {
        return new OrgAReservationContracts.InventoryStatus(
                status.resourceId(),
                status.date(),
                status.totalQuantity(),
                status.availableQuantity(),
                status.reservedQuantity()
        );
    }

    private OrgAReservationContracts.ScheduleListResult toOrgA(BookingDomainModels.ScheduleListResponse response) {
        return new OrgAReservationContracts.ScheduleListResult(
                response.resourceId(),
                response.items().stream().map(this::toOrgA).toList()
        );
    }

    private OrgAReservationContracts.ScheduleItem toOrgA(BookingDomainModels.ScheduleItem item) {
        return new OrgAReservationContracts.ScheduleItem(item.scheduleId(), item.startAt(), item.endAt(), item.status());
    }

    private OrgAReservationContracts.DailyScheduleListResult toOrgA(BookingDomainModels.DailyScheduleListResponse response) {
        return new OrgAReservationContracts.DailyScheduleListResult(
                response.date(),
                response.items().stream().map(this::toOrgA).toList()
        );
    }

    private OrgAReservationContracts.DailyScheduleItem toOrgA(BookingDomainModels.DailyScheduleItem item) {
        return new OrgAReservationContracts.DailyScheduleItem(
                item.resourceId(),
                item.scheduleId(),
                item.startAt(),
                item.endAt(),
                item.status()
        );
    }

    private OrgAReservationContracts.ReservationCreateResult toOrgA(BookingDomainModels.ReservationCreateResponse response) {
        return new OrgAReservationContracts.ReservationCreateResult(response.reservationId(), response.status(), response.createdAt());
    }

    private OrgAReservationContracts.ReservationCancelResult toOrgA(BookingDomainModels.ReservationCancelResponse response) {
        return new OrgAReservationContracts.ReservationCancelResult(response.reservationId(), response.status(), response.canceledAt());
    }

    private OrgAReservationContracts.ReservationDetailResult toOrgA(BookingDomainModels.ReservationDetailResponse response) {
        return new OrgAReservationContracts.ReservationDetailResult(
                response.reservationId(),
                response.resourceId(),
                response.scheduleId(),
                response.userId(),
                response.quantity(),
                response.status(),
                response.createdAt(),
                response.canceledAt()
        );
    }
}
