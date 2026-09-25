package com.example.mockserver.application.booking.service;

import com.example.mockserver.application.booking.contract.CatsBookingContracts;
import com.example.mockserver.domain.booking.BookingOperations;
import com.example.mockserver.domain.booking.model.BookingDomainModels;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class CatsBookingService {

    private final BookingOperations bookings;

    public CatsBookingService(Clock clock, Supplier<UUID> identifiers) {
        this.bookings = new BookingOperations(clock, identifiers);
    }

    public CatsBookingContracts.ResourceListResult listResources(int page, int size, String category) {
        return toCats(bookings.listResourcesModel(page, size, category));
    }

    public CatsBookingContracts.ResourceDetail getResource(String resourceId) {
        return toCats(bookings.getResourceModel(resourceId));
    }

    public CatsBookingContracts.InventoryStatus getInventory(String resourceId, java.time.LocalDate date) {
        return toCats(bookings.getInventoryModel(resourceId, date));
    }

    public CatsBookingContracts.ScheduleListResult getSchedules(String resourceId,
                                                                    java.time.OffsetDateTime from,
                                                                    java.time.OffsetDateTime to) {
        return toCats(bookings.getSchedulesModel(resourceId, from, to));
    }

    public CatsBookingContracts.DailyScheduleListResult listDailySchedules(java.time.LocalDate date) {
        return toCats(bookings.listDailySchedulesModel(date));
    }

    public CatsBookingContracts.ReservationCreateResult createReservation(CatsBookingContracts.ReservationCreateCommand request) {
        return toCats(bookings.createReservationModel(toDomain(request)));
    }

    public CatsBookingContracts.ReservationCancelResult cancelReservation(String reservationId,
                                                                              CatsBookingContracts.ReservationCancelCommand request) {
        return toCats(bookings.cancelReservationModel(reservationId, toDomain(request)));
    }

    public CatsBookingContracts.ReservationDetailResult getReservation(String reservationId) {
        return toCats(bookings.getReservationModel(reservationId));
    }

    private BookingDomainModels.ReservationCreateRequest toDomain(CatsBookingContracts.ReservationCreateCommand request) {
        return new BookingDomainModels.ReservationCreateRequest(
                request.resourceId(),
                request.scheduleId(),
                request.userId(),
                request.quantity(),
                request.memo()
        );
    }

    private BookingDomainModels.ReservationCancelRequest toDomain(CatsBookingContracts.ReservationCancelCommand request) {
        return new BookingDomainModels.ReservationCancelRequest(request.reason());
    }

    private CatsBookingContracts.ResourceListResult toCats(BookingDomainModels.ResourceListResponse response) {
        return new CatsBookingContracts.ResourceListResult(
                response.items().stream().map(this::toCats).toList(),
                response.page(),
                response.size(),
                response.total()
        );
    }

    private CatsBookingContracts.ResourceSummary toCats(BookingDomainModels.ResourceSummary summary) {
        return new CatsBookingContracts.ResourceSummary(summary.resourceId(), summary.name(), summary.category(), summary.active());
    }

    private CatsBookingContracts.ResourceDetail toCats(BookingDomainModels.ResourceDetail detail) {
        return new CatsBookingContracts.ResourceDetail(
                detail.resourceId(),
                detail.name(),
                detail.category(),
                detail.active(),
                detail.description(),
                detail.location(),
                detail.timezone()
        );
    }

    private CatsBookingContracts.InventoryStatus toCats(BookingDomainModels.InventoryStatus status) {
        return new CatsBookingContracts.InventoryStatus(
                status.resourceId(),
                status.date(),
                status.totalQuantity(),
                status.availableQuantity(),
                status.reservedQuantity()
        );
    }

    private CatsBookingContracts.ScheduleListResult toCats(BookingDomainModels.ScheduleListResponse response) {
        return new CatsBookingContracts.ScheduleListResult(
                response.resourceId(),
                response.items().stream().map(this::toCats).toList()
        );
    }

    private CatsBookingContracts.ScheduleItem toCats(BookingDomainModels.ScheduleItem item) {
        return new CatsBookingContracts.ScheduleItem(item.scheduleId(), item.startAt(), item.endAt(), item.status());
    }

    private CatsBookingContracts.DailyScheduleListResult toCats(BookingDomainModels.DailyScheduleListResponse response) {
        return new CatsBookingContracts.DailyScheduleListResult(
                response.date(),
                response.items().stream().map(this::toCats).toList()
        );
    }

    private CatsBookingContracts.DailyScheduleItem toCats(BookingDomainModels.DailyScheduleItem item) {
        return new CatsBookingContracts.DailyScheduleItem(
                item.resourceId(),
                item.scheduleId(),
                item.startAt(),
                item.endAt(),
                item.status()
        );
    }

    private CatsBookingContracts.ReservationCreateResult toCats(BookingDomainModels.ReservationCreateResponse response) {
        return new CatsBookingContracts.ReservationCreateResult(response.reservationId(), response.status(), response.createdAt());
    }

    private CatsBookingContracts.ReservationCancelResult toCats(BookingDomainModels.ReservationCancelResponse response) {
        return new CatsBookingContracts.ReservationCancelResult(response.reservationId(), response.status(), response.canceledAt());
    }

    private CatsBookingContracts.ReservationDetailResult toCats(BookingDomainModels.ReservationDetailResponse response) {
        return new CatsBookingContracts.ReservationDetailResult(
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
