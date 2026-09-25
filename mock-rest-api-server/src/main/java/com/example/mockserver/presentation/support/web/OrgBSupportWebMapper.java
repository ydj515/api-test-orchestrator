package com.example.mockserver.presentation.support.web;

import com.example.mockserver.application.support.contract.OrgBSupportContracts;
import com.example.mockserver.presentation.support.web.dto.OrgBSupportServiceDtos;

final class OrgBSupportWebMapper {
    private OrgBSupportWebMapper() {
    }

    public static OrgBSupportServiceDtos.DeviceSummary toResponse(OrgBSupportContracts.DeviceSummary value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportServiceDtos.DeviceSummary(value.deviceId(), value.name(), value.model(), value.status());
    }

    public static OrgBSupportServiceDtos.DeviceListResponse toResponse(OrgBSupportContracts.DeviceListResult value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportServiceDtos.DeviceListResponse(value.items() == null ? null : value.items().stream().map(OrgBSupportWebMapper::toResponse).toList(), value.page(), value.size(), value.total());
    }

    public static OrgBSupportContracts.SupportTicketCreateCommand toCommand(OrgBSupportServiceDtos.SupportTicketCreateRequest value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportContracts.SupportTicketCreateCommand(value.deviceId(), value.requesterId(), value.issueType(), value.description());
    }

    public static OrgBSupportServiceDtos.SupportTicketCreateResponse toResponse(OrgBSupportContracts.SupportTicketCreateResult value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportServiceDtos.SupportTicketCreateResponse(value.ticketId(), value.status(), value.createdAt());
    }

    public static OrgBSupportContracts.SupportResolveCommand toCommand(OrgBSupportServiceDtos.SupportResolveRequest value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportContracts.SupportResolveCommand(value.resolutionNote());
    }

    public static OrgBSupportServiceDtos.SupportResolveResponse toResponse(OrgBSupportContracts.SupportResolveResult value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportServiceDtos.SupportResolveResponse(value.ticketId(), value.status(), value.resolvedAt());
    }

    public static OrgBSupportServiceDtos.SupportTicketDetailResponse toResponse(OrgBSupportContracts.SupportTicketDetailResult value) {
        if (value == null) {
            return null;
        }
        return new OrgBSupportServiceDtos.SupportTicketDetailResponse(value.ticketId(), value.deviceId(), value.requesterId(), value.issueType(), value.description(), value.resolutionNote(), value.status(), value.createdAt(), value.resolvedAt());
    }
}
