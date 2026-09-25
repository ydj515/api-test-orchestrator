package com.example.mockserver.application;

import com.example.mockserver.application.support.contract.OrgBSupportContracts;
import com.example.mockserver.application.support.service.OrgBSupportService;
import com.example.mockserver.application.visit.contract.OrgBVisitContracts;
import com.example.mockserver.application.visit.service.OrgBVisitService;
import com.example.mockserver.domain.shared.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisitAndSupportServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-25T01:00:00Z"), ZoneOffset.UTC);

    @Test
    void visitUsesInjectedTimeAndPreservesCancellation() {
        var visits = new OrgBVisitService(clock, UUID::randomUUID);
        var visit = visits.createVisit(new OrgBVisitContracts.VisitCreateCommand(
                "SITE-01", LocalDate.now(clock), "Visitor", "01000000000", 3, "demo"));
        assertThat(visit.createdAt()).isEqualTo(OffsetDateTime.now(clock));
        var cancel = new OrgBVisitContracts.VisitCancelCommand("cancel");
        assertThat(visits.cancelVisit(visit.visitId(), cancel).canceledAt()).isEqualTo(OffsetDateTime.now(clock));
        assertThat(visits.cancelVisit(visit.visitId(), cancel).status()).isEqualTo("CANCELED");
        assertThat(visits.getSlotStatus("SITE-01", LocalDate.now(clock)).availableSlots()).isEqualTo(20);
    }

    @Test
    void supportUsesInjectedTimeAndKeepsFirstResolution() {
        var support = new OrgBSupportService(clock, UUID::randomUUID);
        var ticket = support.createTicket(new OrgBSupportContracts.SupportTicketCreateCommand(
                "DEV-01", "requester", "DEVICE_OFFLINE", "Offline"));
        assertThat(ticket.createdAt()).isEqualTo(OffsetDateTime.now(clock));
        var resolved = support.resolveTicket(ticket.ticketId(), new OrgBSupportContracts.SupportResolveCommand("Restart"));
        assertThat(resolved.resolvedAt()).isEqualTo(OffsetDateTime.now(clock));
        support.resolveTicket(ticket.ticketId(), new OrgBSupportContracts.SupportResolveCommand("Replace"));
        assertThat(support.getTicket(ticket.ticketId()).resolutionNote()).isEqualTo("Restart");
        assertThatThrownBy(() -> support.resolveTicket(ticket.ticketId(), null)).isInstanceOf(BusinessException.class);
    }
}
