package com.example.mockserver.application.support.contract;

import java.time.OffsetDateTime;
import java.util.List;

public class OrgBSupportContracts {

    public record DeviceSummary(String deviceId, String name, String model, String status) {
    }

    public record DeviceListResult(List<DeviceSummary> items, int page, int size, long total) {
        public DeviceListResult {
            items = List.copyOf(items);
        }
    }

    public record SupportTicketCreateCommand(String deviceId,
                                             String requesterId,
                                             String issueType,
                                             String description) {
    }

    public record SupportTicketCreateResult(String ticketId, String status, OffsetDateTime createdAt) {
    }

    public record SupportResolveCommand(String resolutionNote) {
    }

    public record SupportResolveResult(String ticketId, String status, OffsetDateTime resolvedAt) {
    }

    public record SupportTicketDetailResult(String ticketId,
                                              String deviceId,
                                              String requesterId,
                                              String issueType,
                                              String description,
                                              String resolutionNote,
                                              String status,
                                              OffsetDateTime createdAt,
                                              OffsetDateTime resolvedAt) {
    }
}
