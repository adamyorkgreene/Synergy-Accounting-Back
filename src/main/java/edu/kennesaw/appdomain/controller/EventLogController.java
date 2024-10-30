package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.entity.EventLog;
import edu.kennesaw.appdomain.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class EventLogController {

    @Autowired
    private EventLogRepository eventLogRepository;

    @GetMapping("/logs/{accountId}")
    public List<EventLog> getEventLogs(@PathVariable Long accountId) {
        return eventLogRepository.findByAccountId(accountId);
    }
}
