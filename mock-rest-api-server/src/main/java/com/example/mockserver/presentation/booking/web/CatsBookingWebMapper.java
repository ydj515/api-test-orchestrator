package com.example.mockserver.presentation.booking.web;

import com.example.mockserver.application.booking.contract.CatsBookingContracts;
import com.example.mockserver.presentation.booking.web.dto.CatsBookingServiceDtos;

final class CatsBookingWebMapper {
    private CatsBookingWebMapper() {
    }

    public static CatsBookingServiceDtos.ResourceSummary toResponse(CatsBookingContracts.ResourceSummary value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ResourceSummary(value.resourceId(), value.name(), value.category(), value.active());
    }

    public static CatsBookingServiceDtos.ResourceDetail toResponse(CatsBookingContracts.ResourceDetail value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ResourceDetail(value.resourceId(), value.name(), value.category(), value.active(), value.description(), value.location(), value.timezone());
    }

    public static CatsBookingServiceDtos.ResourceListResponse toResponse(CatsBookingContracts.ResourceListResult value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ResourceListResponse(value.items() == null ? null : value.items().stream().map(CatsBookingWebMapper::toResponse).toList(), value.page(), value.size(), value.total());
    }

    public static CatsBookingServiceDtos.InventoryStatus toResponse(CatsBookingContracts.InventoryStatus value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.InventoryStatus(value.resourceId(), value.date(), value.totalQuantity(), value.availableQuantity(), value.reservedQuantity());
    }

    public static CatsBookingServiceDtos.ScheduleItem toResponse(CatsBookingContracts.ScheduleItem value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ScheduleItem(value.scheduleId(), value.startAt(), value.endAt(), value.status());
    }

    public static CatsBookingServiceDtos.ScheduleListResponse toResponse(CatsBookingContracts.ScheduleListResult value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ScheduleListResponse(value.resourceId(), value.items() == null ? null : value.items().stream().map(CatsBookingWebMapper::toResponse).toList());
    }

    public static CatsBookingServiceDtos.DailyScheduleItem toResponse(CatsBookingContracts.DailyScheduleItem value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.DailyScheduleItem(value.resourceId(), value.scheduleId(), value.startAt(), value.endAt(), value.status());
    }

    public static CatsBookingServiceDtos.DailyScheduleListResponse toResponse(CatsBookingContracts.DailyScheduleListResult value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.DailyScheduleListResponse(value.date(), value.items() == null ? null : value.items().stream().map(CatsBookingWebMapper::toResponse).toList());
    }

    public static CatsBookingContracts.ReservationCreateCommand toCommand(CatsBookingServiceDtos.ReservationCreateRequest value) {
        if (value == null) return null;
        return new CatsBookingContracts.ReservationCreateCommand(value.resourceId(), value.scheduleId(), value.userId(), value.quantity(), value.memo());
    }

    public static CatsBookingServiceDtos.ReservationCreateResponse toResponse(CatsBookingContracts.ReservationCreateResult value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ReservationCreateResponse(value.reservationId(), value.status(), value.createdAt());
    }

    public static CatsBookingContracts.ReservationCancelCommand toCommand(CatsBookingServiceDtos.ReservationCancelRequest value) {
        if (value == null) return null;
        return new CatsBookingContracts.ReservationCancelCommand(value.reason());
    }

    public static CatsBookingServiceDtos.ReservationCancelResponse toResponse(CatsBookingContracts.ReservationCancelResult value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ReservationCancelResponse(value.reservationId(), value.status(), value.canceledAt());
    }

    public static CatsBookingServiceDtos.ReservationDetailResponse toResponse(CatsBookingContracts.ReservationDetailResult value) {
        if (value == null) return null;
        return new CatsBookingServiceDtos.ReservationDetailResponse(value.reservationId(), value.resourceId(), value.scheduleId(), value.userId(), value.quantity(), value.status(), value.createdAt(), value.canceledAt());
    }
}
