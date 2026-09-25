package com.example.mockserver.presentation.visit.web;

import com.example.mockserver.application.visit.service.OrgBVisitService;
import com.example.mockserver.presentation.shared.web.AbstractMockApiController;
import com.example.mockserver.presentation.shared.web.PayloadCryptoAdapter;
import com.example.mockserver.presentation.visit.web.dto.OrgBVisitServiceDtos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cats/orgB/visit")
public class OrgBVisitServiceController extends AbstractMockApiController {

    private final OrgBVisitService visitService;

    public OrgBVisitServiceController(OrgBVisitService visitService,
                                      PayloadCryptoAdapter payloadCryptoAdapter,
                                      ObjectMapper objectMapper) {
        super(payloadCryptoAdapter, objectMapper);
        this.visitService = visitService;
    }

    @GetMapping("/sites")
    public OrgBVisitServiceDtos.SiteListResponse listSites(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(required = false) String city) {
        return OrgBVisitWebMapper.toResponse(visitService.listSites(page, size, city));
    }

    @PostMapping("/sites")
    public Object listSitesViaGateway(@RequestBody JsonNode body) {
        if (isEncryptedEnvelope(body)) {
            JsonNode plain = payloadCryptoAdapter.decryptToJson(body.get("data").asText());
            OrgBVisitServiceDtos.SiteListResponse response = OrgBVisitWebMapper.toResponse(visitService.listSites(
                    intValue(plain, "page", 0),
                    intValue(plain, "size", 20),
                    textValue(plain, "city", null)
            ));
            return payloadCryptoAdapter.encryptResponse(response);
        }

        return OrgBVisitWebMapper.toResponse(visitService.listSites(
                intValue(body, "page", 0),
                intValue(body, "size", 20),
                textValue(body, "city", null)
        ));
    }

    @GetMapping("/sites/{siteId}/slots")
    public OrgBVisitServiceDtos.VisitSlotStatus getSlotStatus(@PathVariable String siteId,
                                                              @RequestParam LocalDate date) {
        return OrgBVisitWebMapper.toResponse(visitService.getSlotStatus(siteId, date));
    }

    @PostMapping("/sites/{siteId}/slots")
    public Object getSlotStatusViaGateway(@PathVariable String siteId,
                                          @RequestBody JsonNode body) {
        LocalDate date;
        if (isEncryptedEnvelope(body)) {
            JsonNode plain = payloadCryptoAdapter.decryptToJson(body.get("data").asText());
            date = LocalDate.parse(requiredText(plain, "date"));
            return payloadCryptoAdapter.encryptResponse(OrgBVisitWebMapper.toResponse(visitService.getSlotStatus(siteId, date)));
        }

        date = LocalDate.parse(requiredText(body, "date"));
        return OrgBVisitWebMapper.toResponse(visitService.getSlotStatus(siteId, date));
    }

    @PostMapping("/visits")
    public ResponseEntity<?> createVisit(@RequestBody JsonNode body) {
        if (isEncryptedEnvelope(body)) {
            OrgBVisitServiceDtos.VisitCreateRequest request = payloadCryptoAdapter
                    .decryptToObject(body.get("data").asText(), OrgBVisitServiceDtos.VisitCreateRequest.class);
            OrgBVisitServiceDtos.VisitCreateResponse response = OrgBVisitWebMapper.toResponse(visitService.createVisit(OrgBVisitWebMapper.toCommand(request)));
            return ResponseEntity.status(HttpStatus.CREATED).body(payloadCryptoAdapter.encryptResponse(response));
        }

        OrgBVisitServiceDtos.VisitCreateRequest request = objectMapper.convertValue(body, OrgBVisitServiceDtos.VisitCreateRequest.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrgBVisitWebMapper.toResponse(visitService.createVisit(OrgBVisitWebMapper.toCommand(request))));
    }

    @GetMapping("/visits/{visitId}")
    public OrgBVisitServiceDtos.VisitDetailResponse getVisit(@PathVariable String visitId) {
        return OrgBVisitWebMapper.toResponse(visitService.getVisit(visitId));
    }

    @PostMapping("/visits/{visitId}")
    public Object getVisitViaGateway(@PathVariable String visitId,
                                     @RequestBody(required = false) JsonNode body) {
        OrgBVisitServiceDtos.VisitDetailResponse response = OrgBVisitWebMapper.toResponse(visitService.getVisit(visitId));
        if (isEncryptedEnvelope(body)) {
            return payloadCryptoAdapter.encryptResponse(response);
        }
        return response;
    }

    @PostMapping("/visits/{visitId}/cancel")
    public Object cancelVisit(@PathVariable String visitId,
                              @RequestBody JsonNode body) {
        if (isEncryptedEnvelope(body)) {
            OrgBVisitServiceDtos.VisitCancelRequest request = payloadCryptoAdapter
                    .decryptToObject(body.get("data").asText(), OrgBVisitServiceDtos.VisitCancelRequest.class);
            OrgBVisitServiceDtos.VisitCancelResponse response = OrgBVisitWebMapper.toResponse(visitService.cancelVisit(visitId, OrgBVisitWebMapper.toCommand(request)));
            return payloadCryptoAdapter.encryptResponse(response);
        }

        OrgBVisitServiceDtos.VisitCancelRequest request = objectMapper.convertValue(body, OrgBVisitServiceDtos.VisitCancelRequest.class);
        return OrgBVisitWebMapper.toResponse(visitService.cancelVisit(visitId, OrgBVisitWebMapper.toCommand(request)));
    }
}
