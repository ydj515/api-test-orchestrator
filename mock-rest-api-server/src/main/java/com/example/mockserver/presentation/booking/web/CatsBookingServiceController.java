package com.example.mockserver.presentation.booking.web;

import com.example.mockserver.application.booking.service.CatsBookingService;
import com.example.mockserver.presentation.booking.web.dto.CatsBookingServiceDtos;
import com.example.mockserver.presentation.shared.web.PayloadCryptoAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cats/catsOrg/booking")
public class CatsBookingServiceController extends AbstractBookingServiceController {

    private final CatsBookingService catsBookingService;

    public CatsBookingServiceController(CatsBookingService catsBookingService,
                                        PayloadCryptoAdapter payloadCryptoAdapter,
                                        ObjectMapper objectMapper) {
        super(payloadCryptoAdapter, objectMapper);
        this.catsBookingService = catsBookingService;
    }

    @Override
    protected Object listResourcesInternal(int page, int size, String category) {
        return CatsBookingWebMapper.toResponse(catsBookingService.listResources(page, size, category));
    }

    @Override
    protected Object getResourceInternal(String resourceId) {
        return CatsBookingWebMapper.toResponse(catsBookingService.getResource(resourceId));
    }

    @Override
    protected Object getInventoryInternal(String resourceId, java.time.LocalDate date) {
        return CatsBookingWebMapper.toResponse(catsBookingService.getInventory(resourceId, date));
    }

    @Override
    protected Object getSchedulesInternal(String resourceId, java.time.OffsetDateTime from, java.time.OffsetDateTime to) {
        return CatsBookingWebMapper.toResponse(catsBookingService.getSchedules(resourceId, from, to));
    }

    @Override
    protected Object listDailySchedulesInternal(java.time.LocalDate date) {
        return CatsBookingWebMapper.toResponse(catsBookingService.listDailySchedules(date));
    }

    @Override
    protected Class<?> reservationCreateRequestType() {
        return CatsBookingServiceDtos.ReservationCreateRequest.class;
    }

    @Override
    protected Object createReservationInternal(Object request) {
        return CatsBookingWebMapper.toResponse(catsBookingService.createReservation(CatsBookingWebMapper.toCommand((CatsBookingServiceDtos.ReservationCreateRequest) request)));
    }

    @Override
    protected Class<?> reservationCancelRequestType() {
        return CatsBookingServiceDtos.ReservationCancelRequest.class;
    }

    @Override
    protected Object cancelReservationInternal(String reservationId, Object request) {
        return CatsBookingWebMapper.toResponse(catsBookingService.cancelReservation(reservationId, CatsBookingWebMapper.toCommand((CatsBookingServiceDtos.ReservationCancelRequest) request)));
    }

    @Override
    protected Object getReservationInternal(String reservationId) {
        return CatsBookingWebMapper.toResponse(catsBookingService.getReservation(reservationId));
    }
}
