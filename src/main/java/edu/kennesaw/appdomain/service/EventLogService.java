package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.entity.EventLog;
import edu.kennesaw.appdomain.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventLogService {

    @Autowired
    private EventLogRepository eventLogRepository;

    public void logAccountEvent(Account account, String action, Long userId, String beforeState, String afterState) {
        EventLog log = new EventLog();
        log.setAccountId(account.getAccountNumber());
        log.setAction(action);
        log.setUserId(userId);
        log.setBeforeState(beforeState);
        log.setAfterState(afterState);
        log.setTimestamp(LocalDateTime.now());

        eventLogRepository.save(log);
    }
}
