package com.cardwise.controller;

import com.cardwise.config.JwtAuthenticationToken;
import com.cardwise.dto.StatsResponse;
import com.cardwise.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        return ResponseEntity.ok(statsService.getStats(auth.getUserId()));
    }
}
