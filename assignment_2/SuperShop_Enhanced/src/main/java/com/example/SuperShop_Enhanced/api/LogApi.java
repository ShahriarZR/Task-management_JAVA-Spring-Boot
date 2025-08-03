package com.example.SuperShop_Enhanced.api;

import com.example.SuperShop_Enhanced.entity.Log;
import com.example.SuperShop_Enhanced.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/logs")
public class LogApi {

    @Autowired
    private LogService logService;

    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody Log log) {
        Log createdLog = logService.createLog(log);
        return ResponseEntity.ok(createdLog);
    }

    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        List<Log> logs = logService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/file")
    public ResponseEntity<?> getLogFile() {
        try {
            List<String> logLines = logService.readLogFile();
            return ResponseEntity.ok(logLines);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading log file: " + e.getMessage());
        }
    }
}
