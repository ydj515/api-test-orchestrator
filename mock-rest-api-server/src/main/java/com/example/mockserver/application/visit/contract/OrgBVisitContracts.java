package com.example.mockserver.application.visit.contract;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class OrgBVisitContracts {

    public record SiteSummary(String siteId, String name, String city, boolean active) {
    }

    public record SiteListResult(List<SiteSummary> items, int page, int size, long total) {
        public SiteListResult {
            items = List.copyOf(items);
        }
    }

    public record VisitSlotStatus(String siteId, LocalDate date, int totalSlots, int availableSlots, int reservedSlots) {
    }

    public record VisitCreateCommand(String siteId,
                                     LocalDate visitDate,
                                     String visitorName,
                                     String visitorPhone,
                                     Integer partySize,
                                     String purpose) {
    }

    public record VisitCreateResult(String visitId, String status, OffsetDateTime createdAt) {
    }

    public record VisitCancelCommand(String reason) {
    }

    public record VisitCancelResult(String visitId, String status, OffsetDateTime canceledAt) {
    }

    public record VisitDetailResult(String visitId,
                                      String siteId,
                                      LocalDate visitDate,
                                      String visitorName,
                                      String visitorPhone,
                                      int partySize,
                                      String purpose,
                                      String status,
                                      OffsetDateTime createdAt,
                                      OffsetDateTime canceledAt) {
    }
}
