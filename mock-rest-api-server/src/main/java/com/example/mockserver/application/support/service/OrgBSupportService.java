package com.example.mockserver.application.support.service;

import com.example.mockserver.application.support.contract.OrgBSupportContracts;
import com.example.mockserver.domain.shared.BusinessException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class OrgBSupportService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_PAGE_NUMBER = 1_000;
    private static final Set<String> ALLOWED_DEVICE_STATUSES = Set.of("ACTIVE", "MAINTENANCE");
    private static final Set<String> ALLOWED_ISSUE_TYPES = Set.of("DISPLAY_ERROR", "DEVICE_OFFLINE", "ACCESS_ISSUE");

    private final Map<String, OrgBSupportContracts.DeviceSummary> devices = new ConcurrentHashMap<>();
    private final Map<String, SupportTicketEntity> tickets = new ConcurrentHashMap<>();

    private final Clock clock;
    private final Supplier<UUID> identifiers;

    public OrgBSupportService(Clock clock, Supplier<UUID> identifiers) {
        this.clock = clock;
        this.identifiers = identifiers;
        initializeDevices();
    }

    public OrgBSupportContracts.DeviceListResult listDevices(int page, int size, String status) {
        validatePageRequest(page, size);
        validateDeviceStatus(status);

        List<OrgBSupportContracts.DeviceSummary> filtered = devices.values().stream()
                .filter(device -> status == null || status.isBlank() || device.status().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(OrgBSupportContracts.DeviceSummary::deviceId))
                .toList();

        long offset = (long) page * size;
        int from = (int) Math.min(offset, filtered.size());
        int to = (int) Math.min(offset + size, filtered.size());
        return new OrgBSupportContracts.DeviceListResult(filtered.subList(from, to), page, size, filtered.size());
    }

    public OrgBSupportContracts.SupportTicketCreateResult createTicket(OrgBSupportContracts.SupportTicketCreateCommand request) {
        validateCreateRequest(request);
        getDevice(request.deviceId());

        String ticketId = "TCK-" + identifiers.get();
        OffsetDateTime now = OffsetDateTime.now(clock);
        tickets.put(ticketId, new SupportTicketEntity(
                ticketId,
                request.deviceId(),
                request.requesterId(),
                request.issueType(),
                request.description(),
                null,
                "OPEN",
                now,
                null
        ));

        return new OrgBSupportContracts.SupportTicketCreateResult(ticketId, "OPEN", now);
    }

    public OrgBSupportContracts.SupportTicketDetailResult getTicket(String ticketId) {
        SupportTicketEntity entity = getTicketEntity(ticketId);
        return new OrgBSupportContracts.SupportTicketDetailResult(
                entity.ticketId(),
                entity.deviceId(),
                entity.requesterId(),
                entity.issueType(),
                entity.description(),
                entity.resolutionNote(),
                entity.status(),
                entity.createdAt(),
                entity.resolvedAt()
        );
    }

    public synchronized OrgBSupportContracts.SupportResolveResult resolveTicket(String ticketId, OrgBSupportContracts.SupportResolveCommand request) {
        if (request == null || isBlank(request.resolutionNote())) {
            throw new BusinessException("INVALID_REQUEST", "resolutionNote is required", BusinessException.Kind.INVALID_INPUT);
        }

        SupportTicketEntity current = getTicketEntity(ticketId);
        if ("RESOLVED".equals(current.status())) {
            return new OrgBSupportContracts.SupportResolveResult(ticketId, "RESOLVED", current.resolvedAt());
        }

        OffsetDateTime resolvedAt = OffsetDateTime.now(clock);
        tickets.put(ticketId, current.resolve(request.resolutionNote(), resolvedAt));
        return new OrgBSupportContracts.SupportResolveResult(ticketId, "RESOLVED", resolvedAt);
    }

    private OrgBSupportContracts.DeviceSummary getDevice(String deviceId) {
        OrgBSupportContracts.DeviceSummary device = devices.get(deviceId);
        if (device == null) {
            throw new BusinessException("DEVICE_NOT_FOUND", "Device not found: " + deviceId, BusinessException.Kind.NOT_FOUND);
        }
        return device;
    }

    private SupportTicketEntity getTicketEntity(String ticketId) {
        SupportTicketEntity entity = tickets.get(ticketId);
        if (entity == null) {
            throw new BusinessException("TICKET_NOT_FOUND", "Ticket not found: " + ticketId, BusinessException.Kind.NOT_FOUND);
        }
        return entity;
    }

    private void validateCreateRequest(OrgBSupportContracts.SupportTicketCreateCommand request) {
        if (request == null) {
            throw new BusinessException("INVALID_REQUEST", "Request body is required", BusinessException.Kind.INVALID_INPUT);
        }
        if (isBlank(request.deviceId()) || isBlank(request.requesterId()) || isBlank(request.issueType())) {
            throw new BusinessException("INVALID_REQUEST", "deviceId, requesterId, issueType are required", BusinessException.Kind.INVALID_INPUT);
        }
        if (!ALLOWED_ISSUE_TYPES.contains(request.issueType())) {
            throw new BusinessException("INVALID_REQUEST", "issueType must be one of " + ALLOWED_ISSUE_TYPES, BusinessException.Kind.INVALID_INPUT);
        }
        if (isBlank(request.description())) {
            throw new BusinessException("INVALID_REQUEST", "description is required", BusinessException.Kind.INVALID_INPUT);
        }
    }

    private void validateDeviceStatus(String status) {
        if (status != null && !status.isBlank() && !ALLOWED_DEVICE_STATUSES.contains(status.toUpperCase(Locale.ROOT))) {
            throw new BusinessException("INVALID_REQUEST", "status must be one of " + ALLOWED_DEVICE_STATUSES, BusinessException.Kind.INVALID_INPUT);
        }
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || page > MAX_PAGE_NUMBER) {
            throw new BusinessException("INVALID_REQUEST", "page must be between 0 and " + MAX_PAGE_NUMBER, BusinessException.Kind.INVALID_INPUT);
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException("INVALID_REQUEST", "size must be between 1 and " + MAX_PAGE_SIZE, BusinessException.Kind.INVALID_INPUT);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void initializeDevices() {
        devices.put("DEV-01", new OrgBSupportContracts.DeviceSummary("DEV-01", "Entrance Kiosk", "K-100", "ACTIVE"));
        devices.put("DEV-02", new OrgBSupportContracts.DeviceSummary("DEV-02", "Badge Printer", "P-220", "ACTIVE"));
        devices.put("DEV-03", new OrgBSupportContracts.DeviceSummary("DEV-03", "Visitor Tablet", "T-330", "MAINTENANCE"));
        devices.put("DEV-04", new OrgBSupportContracts.DeviceSummary("DEV-04", "Lobby Display", "D-410", "ACTIVE"));
    }

    private record SupportTicketEntity(String ticketId,
                                       String deviceId,
                                       String requesterId,
                                       String issueType,
                                       String description,
                                       String resolutionNote,
                                       String status,
                                       OffsetDateTime createdAt,
                                       OffsetDateTime resolvedAt) {
        private SupportTicketEntity resolve(String nextResolutionNote, OffsetDateTime nextResolvedAt) {
            return new SupportTicketEntity(
                    ticketId,
                    deviceId,
                    requesterId,
                    issueType,
                    description,
                    nextResolutionNote,
                    "RESOLVED",
                    createdAt,
                    nextResolvedAt
            );
        }
    }
}
