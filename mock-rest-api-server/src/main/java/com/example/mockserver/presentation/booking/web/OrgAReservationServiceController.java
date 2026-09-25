package com.example.mockserver.presentation.booking.web;

import com.example.mockserver.application.booking.service.OrgAReservationService;
import com.example.mockserver.presentation.booking.web.dto.OrgAReservationServiceDtos;
import com.example.mockserver.presentation.shared.web.PayloadCryptoAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cats/orgA/reservation")
public class OrgAReservationServiceController extends AbstractBookingServiceController {

    private final OrgAReservationService orgAReservationService;

    public OrgAReservationServiceController(OrgAReservationService orgAReservationService,
                                            PayloadCryptoAdapter payloadCryptoAdapter,
                                            ObjectMapper objectMapper) {
        super(payloadCryptoAdapter, objectMapper);
        this.orgAReservationService = orgAReservationService;
    }

    @Override
    protected Object listResourcesInternal(int page, int size, String category) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.listResources(page, size, category));
    }

    @Override
    protected Object getResourceInternal(String resourceId) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.getResource(resourceId));
    }

    @Override
    protected Object getInventoryInternal(String resourceId, java.time.LocalDate date) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.getInventory(resourceId, date));
    }

    @Override
    protected Object getSchedulesInternal(String resourceId, java.time.OffsetDateTime from, java.time.OffsetDateTime to) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.getSchedules(resourceId, from, to));
    }

    @Override
    protected Object listDailySchedulesInternal(java.time.LocalDate date) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.listDailySchedules(date));
    }

    @Override
    protected Class<?> reservationCreateRequestType() {
        return OrgAReservationServiceDtos.ReservationCreateRequest.class;
    }

    @Override
    protected Object createReservationInternal(Object request) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.createReservation(OrgAReservationWebMapper.toCommand((OrgAReservationServiceDtos.ReservationCreateRequest) request)));
    }

    @Override
    protected Class<?> reservationCancelRequestType() {
        return OrgAReservationServiceDtos.ReservationCancelRequest.class;
    }

    @Override
    protected Object cancelReservationInternal(String reservationId, Object request) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.cancelReservation(reservationId, OrgAReservationWebMapper.toCommand((OrgAReservationServiceDtos.ReservationCancelRequest) request)));
    }

    @Override
    protected Object getReservationInternal(String reservationId) {
        return OrgAReservationWebMapper.toResponse(orgAReservationService.getReservation(reservationId));
    }
}
