package com.ainjob.controller;

import com.ainjob.dto.TransitionRequest;
import com.ainjob.dto.TransitionResponse;
import com.ainjob.service.TransitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/applications")
@Validated
public class TransitionController {

    private final TransitionService transitionService;

    public TransitionController(TransitionService transitionService) {
        this.transitionService = transitionService;
    }

    /**
     * 단계/상태 전이
     * PATCH /api/v1/applications/{applicationId}/transition
     */
    @PatchMapping("/{applicationId}/transition")
    public ResponseEntity<TransitionResponse> transition(
            @PathVariable Long applicationId,
            @Valid @RequestBody TransitionRequest request) {
        return ResponseEntity.ok(transitionService.transition(applicationId, request));
    }
}
