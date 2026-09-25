package com.example.mockserver.presentation.visit.web;

import com.example.mockserver.application.visit.contract.OrgBVisitContracts;
import com.example.mockserver.presentation.visit.web.dto.OrgBVisitServiceDtos;

final class OrgBVisitWebMapper {
    private OrgBVisitWebMapper() {
    }

    public static OrgBVisitServiceDtos.SiteSummary toResponse(OrgBVisitContracts.SiteSummary value) {
        if (value == null) return null;
        return new OrgBVisitServiceDtos.SiteSummary(value.siteId(), value.name(), value.city(), value.active());
    }

    public static OrgBVisitServiceDtos.SiteListResponse toResponse(OrgBVisitContracts.SiteListResult value) {
        if (value == null) return null;
        return new OrgBVisitServiceDtos.SiteListResponse(value.items() == null ? null : value.items().stream().map(OrgBVisitWebMapper::toResponse).toList(), value.page(), value.size(), value.total());
    }

    public static OrgBVisitServiceDtos.VisitSlotStatus toResponse(OrgBVisitContracts.VisitSlotStatus value) {
        if (value == null) return null;
        return new OrgBVisitServiceDtos.VisitSlotStatus(value.siteId(), value.date(), value.totalSlots(), value.availableSlots(), value.reservedSlots());
    }

    public static OrgBVisitContracts.VisitCreateCommand toCommand(OrgBVisitServiceDtos.VisitCreateRequest value) {
        if (value == null) return null;
        return new OrgBVisitContracts.VisitCreateCommand(value.siteId(), value.visitDate(), value.visitorName(), value.visitorPhone(), value.partySize(), value.purpose());
    }

    public static OrgBVisitServiceDtos.VisitCreateResponse toResponse(OrgBVisitContracts.VisitCreateResult value) {
        if (value == null) return null;
        return new OrgBVisitServiceDtos.VisitCreateResponse(value.visitId(), value.status(), value.createdAt());
    }

    public static OrgBVisitContracts.VisitCancelCommand toCommand(OrgBVisitServiceDtos.VisitCancelRequest value) {
        if (value == null) return null;
        return new OrgBVisitContracts.VisitCancelCommand(value.reason());
    }

    public static OrgBVisitServiceDtos.VisitCancelResponse toResponse(OrgBVisitContracts.VisitCancelResult value) {
        if (value == null) return null;
        return new OrgBVisitServiceDtos.VisitCancelResponse(value.visitId(), value.status(), value.canceledAt());
    }

    public static OrgBVisitServiceDtos.VisitDetailResponse toResponse(OrgBVisitContracts.VisitDetailResult value) {
        if (value == null) return null;
        return new OrgBVisitServiceDtos.VisitDetailResponse(value.visitId(), value.siteId(), value.visitDate(), value.visitorName(), value.visitorPhone(), value.partySize(), value.purpose(), value.status(), value.createdAt(), value.canceledAt());
    }
}
