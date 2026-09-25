package com.example.mockserver.presentation.booking.web;

import com.example.mockserver.application.booking.contract.OrgAReservationContracts;
import com.example.mockserver.presentation.booking.web.dto.OrgAReservationServiceDtos;

final class OrgAReservationWebMapper {
    private OrgAReservationWebMapper() {
    }

    public static OrgAReservationServiceDtos.ResourceSummary toResponse(OrgAReservationContracts.ResourceSummary value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ResourceSummary(value.resourceId(), value.name(), value.category(), value.active());
    }

    public static OrgAReservationServiceDtos.ResourceDetail toResponse(OrgAReservationContracts.ResourceDetail value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ResourceDetail(value.resourceId(), value.name(), value.category(), value.active(), value.description(), value.location(), value.timezone());
    }

    public static OrgAReservationServiceDtos.ResourceListResponse toResponse(OrgAReservationContracts.ResourceListResult value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ResourceListResponse(value.items() == null ? null : value.items().stream().map(OrgAReservationWebMapper::toResponse).toList(), value.page(), value.size(), value.total());
    }

    public static OrgAReservationServiceDtos.InventoryStatus toResponse(OrgAReservationContracts.InventoryStatus value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.InventoryStatus(value.resourceId(), value.date(), value.totalQuantity(), value.availableQuantity(), value.reservedQuantity());
    }

    public static OrgAReservationServiceDtos.ScheduleItem toResponse(OrgAReservationContracts.ScheduleItem value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ScheduleItem(value.scheduleId(), value.startAt(), value.endAt(), value.status());
    }

    public static OrgAReservationServiceDtos.ScheduleListResponse toResponse(OrgAReservationContracts.ScheduleListResult value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ScheduleListResponse(value.resourceId(), value.items() == null ? null : value.items().stream().map(OrgAReservationWebMapper::toResponse).toList());
    }

    public static OrgAReservationServiceDtos.DailyScheduleItem toResponse(OrgAReservationContracts.DailyScheduleItem value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.DailyScheduleItem(value.resourceId(), value.scheduleId(), value.startAt(), value.endAt(), value.status());
    }

    public static OrgAReservationServiceDtos.DailyScheduleListResponse toResponse(OrgAReservationContracts.DailyScheduleListResult value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.DailyScheduleListResponse(value.date(), value.items() == null ? null : value.items().stream().map(OrgAReservationWebMapper::toResponse).toList());
    }

    public static OrgAReservationContracts.ReservationCreateCommand toCommand(OrgAReservationServiceDtos.ReservationCreateRequest value) {
        if (value == null) return null;
        return new OrgAReservationContracts.ReservationCreateCommand(value.resourceId(), value.scheduleId(), value.userId(), value.quantity(), value.memo());
    }

    public static OrgAReservationServiceDtos.ReservationCreateResponse toResponse(OrgAReservationContracts.ReservationCreateResult value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ReservationCreateResponse(value.reservationId(), value.status(), value.createdAt());
    }

    public static OrgAReservationContracts.ReservationCancelCommand toCommand(OrgAReservationServiceDtos.ReservationCancelRequest value) {
        if (value == null) return null;
        return new OrgAReservationContracts.ReservationCancelCommand(value.reason());
    }

    public static OrgAReservationServiceDtos.ReservationCancelResponse toResponse(OrgAReservationContracts.ReservationCancelResult value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ReservationCancelResponse(value.reservationId(), value.status(), value.canceledAt());
    }

    public static OrgAReservationServiceDtos.ReservationDetailResponse toResponse(OrgAReservationContracts.ReservationDetailResult value) {
        if (value == null) return null;
        return new OrgAReservationServiceDtos.ReservationDetailResponse(value.reservationId(), value.resourceId(), value.scheduleId(), value.userId(), value.quantity(), value.status(), value.createdAt(), value.canceledAt());
    }
}
