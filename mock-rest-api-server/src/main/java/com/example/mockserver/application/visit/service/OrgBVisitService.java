package com.example.mockserver.application.visit.service;

import com.example.mockserver.application.visit.contract.OrgBVisitContracts;
import com.example.mockserver.domain.shared.BusinessException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class OrgBVisitService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_PAGE_NUMBER = 1_000;
    private static final int TOTAL_DAILY_SLOTS = 20;
    private static final Set<String> ALLOWED_CITIES = Set.of("Seoul", "Busan", "Daejeon");
    private static final Set<String> ALLOWED_VISIT_PURPOSES = Set.of("demo", "business", "audit");

    private final Map<String, OrgBVisitContracts.SiteSummary> sites = new ConcurrentHashMap<>();
    private final Map<String, VisitEntity> visits = new ConcurrentHashMap<>();

    private final Clock clock;
    private final Supplier<UUID> identifiers;

    public OrgBVisitService(Clock clock, Supplier<UUID> identifiers) {
        this.clock = clock;
        this.identifiers = identifiers;
        initializeSites();
    }

    public OrgBVisitContracts.SiteListResult listSites(int page, int size, String city) {
        validatePageRequest(page, size);
        validateCity(city);

        List<OrgBVisitContracts.SiteSummary> filtered = sites.values().stream()
                .filter(site -> city == null || city.isBlank() || site.city().equalsIgnoreCase(city))
                .sorted(Comparator.comparing(OrgBVisitContracts.SiteSummary::siteId))
                .toList();

        long offset = (long) page * size;
        int from = (int) Math.min(offset, filtered.size());
        int to = (int) Math.min(offset + size, filtered.size());
        return new OrgBVisitContracts.SiteListResult(filtered.subList(from, to), page, size, filtered.size());
    }

    public OrgBVisitContracts.VisitSlotStatus getSlotStatus(String siteId, LocalDate date) {
        getSite(siteId);

        int reserved = visits.values().stream()
                .filter(visit -> visit.siteId().equals(siteId))
                .filter(visit -> visit.visitDate().equals(date))
                .filter(visit -> !"CANCELED".equals(visit.status()))
                .mapToInt(VisitEntity::partySize)
                .sum();

        int available = Math.max(0, TOTAL_DAILY_SLOTS - reserved);
        return new OrgBVisitContracts.VisitSlotStatus(siteId, date, TOTAL_DAILY_SLOTS, available, reserved);
    }

    public synchronized OrgBVisitContracts.VisitCreateResult createVisit(OrgBVisitContracts.VisitCreateCommand request) {
        validateCreateRequest(request);
        getSite(request.siteId());

        int available = getSlotStatus(request.siteId(), request.visitDate()).availableSlots();
        if (request.partySize() > available) {
            throw new BusinessException("VISIT_CONFLICT", "Insufficient visit slots", BusinessException.Kind.CONFLICT);
        }

        String visitId = "VIS-" + identifiers.get();
        OffsetDateTime now = OffsetDateTime.now(clock);
        visits.put(visitId, new VisitEntity(
                visitId,
                request.siteId(),
                request.visitDate(),
                request.visitorName(),
                request.visitorPhone(),
                request.partySize(),
                request.purpose(),
                "REQUESTED",
                now,
                null
        ));

        return new OrgBVisitContracts.VisitCreateResult(visitId, "REQUESTED", now);
    }

    public OrgBVisitContracts.VisitDetailResult getVisit(String visitId) {
        VisitEntity visit = getVisitEntity(visitId);
        return new OrgBVisitContracts.VisitDetailResult(
                visit.visitId(),
                visit.siteId(),
                visit.visitDate(),
                visit.visitorName(),
                visit.visitorPhone(),
                visit.partySize(),
                visit.purpose(),
                visit.status(),
                visit.createdAt(),
                visit.canceledAt()
        );
    }

    public synchronized OrgBVisitContracts.VisitCancelResult cancelVisit(String visitId, OrgBVisitContracts.VisitCancelCommand request) {
        if (request == null || isBlank(request.reason())) {
            throw new BusinessException("INVALID_REQUEST", "Cancel reason is required", BusinessException.Kind.INVALID_INPUT);
        }

        VisitEntity current = getVisitEntity(visitId);
        if ("CANCELED".equals(current.status())) {
            return new OrgBVisitContracts.VisitCancelResult(visitId, "CANCELED", current.canceledAt());
        }

        OffsetDateTime canceledAt = OffsetDateTime.now(clock);
        visits.put(visitId, current.withStatus("CANCELED", canceledAt));
        return new OrgBVisitContracts.VisitCancelResult(visitId, "CANCELED", canceledAt);
    }

    private OrgBVisitContracts.SiteSummary getSite(String siteId) {
        OrgBVisitContracts.SiteSummary site = sites.get(siteId);
        if (site == null) {
            throw new BusinessException("SITE_NOT_FOUND", "Site not found: " + siteId, BusinessException.Kind.NOT_FOUND);
        }
        return site;
    }

    private VisitEntity getVisitEntity(String visitId) {
        VisitEntity entity = visits.get(visitId);
        if (entity == null) {
            throw new BusinessException("VISIT_NOT_FOUND", "Visit not found: " + visitId, BusinessException.Kind.NOT_FOUND);
        }
        return entity;
    }

    private void validateCreateRequest(OrgBVisitContracts.VisitCreateCommand request) {
        if (request == null) {
            throw new BusinessException("INVALID_REQUEST", "Request body is required", BusinessException.Kind.INVALID_INPUT);
        }
        if (isBlank(request.siteId()) || request.visitDate() == null || isBlank(request.visitorName())) {
            throw new BusinessException("INVALID_REQUEST", "siteId, visitDate, visitorName are required", BusinessException.Kind.INVALID_INPUT);
        }
        if (request.partySize() == null || request.partySize() < 1 || request.partySize() > 8) {
            throw new BusinessException("INVALID_REQUEST", "partySize must be 1..8", BusinessException.Kind.INVALID_INPUT);
        }
        if (!isBlank(request.purpose()) && !ALLOWED_VISIT_PURPOSES.contains(request.purpose())) {
            throw new BusinessException("INVALID_REQUEST", "purpose must be one of " + ALLOWED_VISIT_PURPOSES, BusinessException.Kind.INVALID_INPUT);
        }
    }

    private void validateCity(String city) {
        if (city != null && !city.isBlank() && ALLOWED_CITIES.stream().noneMatch(value -> value.equalsIgnoreCase(city))) {
            throw new BusinessException("INVALID_REQUEST", "city must be one of " + ALLOWED_CITIES, BusinessException.Kind.INVALID_INPUT);
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

    private void initializeSites() {
        sites.put("SITE-01", new OrgBVisitContracts.SiteSummary("SITE-01", "Main Campus", "Seoul", true));
        sites.put("SITE-02", new OrgBVisitContracts.SiteSummary("SITE-02", "Research Lab", "Seoul", true));
        sites.put("SITE-03", new OrgBVisitContracts.SiteSummary("SITE-03", "Regional Center", "Busan", true));
        sites.put("SITE-04", new OrgBVisitContracts.SiteSummary("SITE-04", "Archive Hall", "Daejeon", true));
    }

    private record VisitEntity(String visitId,
                               String siteId,
                               LocalDate visitDate,
                               String visitorName,
                               String visitorPhone,
                               int partySize,
                               String purpose,
                               String status,
                               OffsetDateTime createdAt,
                               OffsetDateTime canceledAt) {
        private VisitEntity withStatus(String nextStatus, OffsetDateTime nextCanceledAt) {
            return new VisitEntity(
                    visitId,
                    siteId,
                    visitDate,
                    visitorName,
                    visitorPhone,
                    partySize,
                    purpose,
                    nextStatus,
                    createdAt,
                    nextCanceledAt
            );
        }
    }
}
