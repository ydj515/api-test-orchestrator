package com.example.mockserver.domain.booking;

import com.example.mockserver.application.booking.contract.CatsBookingContracts;
import com.example.mockserver.application.booking.contract.OrgAReservationContracts;
import com.example.mockserver.application.booking.service.CatsBookingService;
import com.example.mockserver.application.booking.service.OrgAReservationService;
import com.example.mockserver.domain.booking.model.BookingDomainModels;
import com.example.mockserver.domain.shared.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingOperationsTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-25T01:00:00Z"), ZoneOffset.UTC);
    private final AtomicLong sequence = new AtomicLong();
    private final Supplier<UUID> identifiers = () -> new UUID(0, sequence.incrementAndGet());

    @Test
    void scheduleResultsKeepSnapshotsOfCallerOwnedLists() {
        var items = new java.util.ArrayList<com.example.mockserver.domain.booking.model.BookingDomainModels.ScheduleItem>();
        var item = new com.example.mockserver.domain.booking.model.BookingDomainModels.ScheduleItem(
                "schedule", null, null, "OPEN");
        items.add(item);
        var result = new com.example.mockserver.domain.booking.model.BookingDomainModels.ScheduleListResponse("resource", items);
        items.clear();
        assertThat(result.items()).containsExactly(item);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> result.items().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void usesInjectedTimeAndReleasesInventoryOnlyOnce() {
        BookingOperations bookings = new BookingOperations(clock, identifiers);
        var created = bookings.createReservationModel(request(2));
        assertThat(created.createdAt()).isEqualTo(OffsetDateTime.now(clock));
        var canceled = bookings.cancelReservationModel(created.reservationId(),
                new BookingDomainModels.ReservationCancelRequest("test cancellation"));
        assertThat(canceled.canceledAt()).isEqualTo(OffsetDateTime.now(clock));
        assertThat(bookings.cancelReservationModel(created.reservationId(),
                new BookingDomainModels.ReservationCancelRequest("again"))).isEqualTo(canceled);
        assertThat(bookings.getInventoryModel("R-001", LocalDate.now(clock)).availableQuantity()).isEqualTo(100);
    }

    @Test
    void concurrentReservationsCannotExceedCapacity() throws Exception {
        BookingOperations bookings = new BookingOperations(clock, identifiers);
        var requests = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < 30; i++) {
            requests.add(() -> {
                try {
                    bookings.createReservationModel(request(10));
                    return true;
                } catch (BusinessException expected) {
                    return false;
                }
            });
        }
        int accepted = 0;
        try (var executor = Executors.newFixedThreadPool(8)) {
            for (var result : executor.invokeAll(requests)) {
                if (result.get()) {
                    accepted++;
                }
            }
        }
        assertThat(accepted).isEqualTo(10);
        assertThat(bookings.getInventoryModel("R-001", LocalDate.now(clock)).reservedQuantity()).isEqualTo(100);
    }

    @Test
    void bookingFacadesKeepIndependentStores() {
        var cats = new CatsBookingService(clock, identifiers);
        var orgA = new OrgAReservationService(clock, identifiers);
        cats.createReservation(new CatsBookingContracts.ReservationCreateCommand("R-001", "slot", "user", 3, null));
        assertThat(orgA.getInventory("R-001", LocalDate.now(clock)).reservedQuantity()).isZero();
        assertThatThrownBy(() -> orgA.createReservation(
                new OrgAReservationContracts.ReservationCreateCommand("R-001", "slot", "user", 0, null)))
                .isInstanceOf(BusinessException.class);
    }

    private BookingDomainModels.ReservationCreateRequest request(int quantity) {
        return new BookingDomainModels.ReservationCreateRequest("R-001", "slot", "user", quantity, null);
    }
}
