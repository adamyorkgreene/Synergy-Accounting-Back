package edu.kennesaw.appdomain.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kennesaw.appdomain.dto.GenMessageDTO;
import edu.kennesaw.appdomain.dto.JournalEntryRequestDTO;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.entity.TransactionRequest;
import edu.kennesaw.appdomain.repository.TransactionRequestRepository;
import edu.kennesaw.appdomain.service.AccountService;
import edu.kennesaw.appdomain.service.GeneralMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRequestRepository transactionRequestRepository;

    @Autowired
    private GeneralMessageService generalMessageService;

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @GetMapping("/journal-entry-requests/approved")
    public ResponseEntity<?> requestApprovedJournalEntries() {
        return ResponseEntity.ok(accountService.getJournalEntryRequests(true));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @GetMapping("/journal-entry-requests/pending")
    public ResponseEntity<?> requestPendingJournalEntries() {
        return ResponseEntity.ok(accountService.getJournalEntryRequests(null));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @GetMapping("/journal-entry-requests/rejected")
    public ResponseEntity<?> requestRejectedJournalEntries() {
        return ResponseEntity.ok(accountService.getJournalEntryRequests(false));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @PostMapping("/approve-journal-entry")
    public ResponseEntity<?> approveJournalEntryByIds(@RequestBody JournalEntryRequestDTO jerDTO) throws JsonProcessingException {
        List<TransactionRequest> trs = new ArrayList<>();
        for (Long id : jerDTO.getIds()) {
            TransactionRequest tr = transactionRequestRepository.findByTransactionId(id);
            if (tr != null) {
                trs.add(tr);
            }
        }
        if (trs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("These transactions cannot be found."));
        }
        return accountService.approveJournalEntry(trs, jerDTO.getComments());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @PostMapping("/reject-journal-entry")
    public ResponseEntity<?> rejectJournalEntryByIds(@RequestBody JournalEntryRequestDTO jerDTO) {
        List<TransactionRequest> trs = new ArrayList<>();
        for (Long id : jerDTO.getIds()) {
            TransactionRequest tr = transactionRequestRepository.findByTransactionId(id);
            if (tr != null) {
                trs.add(tr);
            }
        }
        if (trs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("These transactions cannot be found."));
        }
        return accountService.rejectJournalEntry(trs, jerDTO.getComments());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @PostMapping("/post-announcement")
    public ResponseEntity<?> postAnnouncement(@RequestBody GenMessageDTO message) {
        boolean success = generalMessageService.postGeneralMessage(message);
        if (success) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().build();
    }

}
