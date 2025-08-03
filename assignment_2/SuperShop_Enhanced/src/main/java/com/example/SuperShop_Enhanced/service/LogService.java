package com.example.SuperShop_Enhanced.service;

import com.example.SuperShop_Enhanced.entity.Log;
import com.example.SuperShop_Enhanced.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    public Log createLog(Log log) {
        return logRepository.save(log);
    }

    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }

    public List<String> readLogFile() throws IOException {
        // Read all lines from app.log file located in project root directory
        return Files.lines(Paths.get("app.log")).collect(Collectors.toList());
    }
}
