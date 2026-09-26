package com.example.mockserver.application.booking.contract;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class OrgAReservationContracts {

    public record ResourceSummary(String resourceId, String name, String category, boolean active) {
    }

    public record ResourceDetail(String resourceId,
                                 String name,
                                 String category,
                                 boolean active,
                                 String description,
                                 String location,
                                 String timezone) {
    }

    public record ResourceListResult(List<ResourceSummary> items, int page, int size, long total) {
        public ResourceListResult {
            items = List.copyOf(items);
        }
    }

    public record InventoryStatus(String resourceId,
                                  LocalDate date,
                                  int totalQuantity,
                                  int availableQuantity,
                                  int reservedQuantity) {
    }

    public record ScheduleItem(String scheduleId, OffsetDateTime startAt, OffsetDateTime endAt, String status) {
    }

    public record ScheduleListResult(String resourceId, List<ScheduleItem> items) {
        public ScheduleListResult {
            items = List.copyOf(items);
        }
    }

    public record DailyScheduleItem(String resourceId,
                                    String scheduleId,
                                    OffsetDateTime startAt,
                                    OffsetDateTime endAt,
                                    String status) {
    }

    public record DailyScheduleListResult(LocalDate date, List<DailyScheduleItem> items) {
        public DailyScheduleListResult {
            items = List.copyOf(items);
        }
    }

    public record ReservationCreateCommand(String resourceId,
                                           String scheduleId,
                                           String userId,
                                           Integer quantity,
                                           String memo) {
    }

    public record ReservationCreateResult(String reservationId, String status, OffsetDateTime createdAt) {
    }

    public record ReservationCancelCommand(String reason) {
    }

    public record ReservationCancelResult(String reservationId, String status, OffsetDateTime canceledAt) {
    }

    public record ReservationDetailResult(String reservationId,
                                            String resourceId,
                                            String scheduleId,
                                            String userId,
                                            int quantity,
                                            String status,
                                            OffsetDateTime createdAt,
                                            OffsetDateTime canceledAt) {
    }
}
