package com.finsight.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flow-ai")
@CrossOrigin(origins = "*")
public class FlowAiController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Flow AI Controller");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Flow AI Controller is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processFlow(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Flow AI processing endpoint");
        response.put("received", request != null ? request : "No data received");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
