package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.*;
import edu.kennesaw.appdomain.entity.*;
import edu.kennesaw.appdomain.repository.AccountRepository;
import edu.kennesaw.appdomain.repository.TransactionRequestRepository;
import edu.kennesaw.appdomain.service.AccountService;
import edu.kennesaw.appdomain.service.EmailService;
import edu.kennesaw.appdomain.types.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TransactionRequestRepository transactionRequestRepository;

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @GetMapping("/chart-of-accounts")
    public List<AccountResponseDTO> getChartOfAccounts() {
        return accountService.getChartOfAccountsWithUsername();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/chart-of-accounts/add-account")
    public ResponseEntity<?> addAccount(@RequestBody AccountDTO accountDTO) {
        return accountService.addAccount(accountDTO.getAccountName(), accountDTO.getAccountDescription(),
                accountDTO.getNormalSide(), accountDTO.getAccountCategory(), accountDTO.getAccountSubCategory(),
                accountDTO.getInitialBalance(), accountDTO.getCreator());
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/chart-of-accounts/add-transaction")
    public ResponseEntity<?> addTransaction(@RequestBody TransactionDTO transactionDTO) {
        try {
            Transaction transaction = accountService.addTransaction(transactionDTO.getAccount(),
                    transactionDTO.getTransactionDescription(), transactionDTO.getTransactionAmount(),
                    transactionDTO.getTransactionType());
            if (transaction == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("This account is disabled."));
            }
            return ResponseEntity.ok(transaction);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("This account cannot be found."));
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @PostMapping("/chart-of-accounts/add-journal-entry")
    public ResponseEntity<?> addJournalEntry(@RequestBody JournalEntryRequest jer) {
        TransactionDTO[] transactionDTOs = jer.getTransactions();
        User user = jer.getUser();
        try {
            String token = UUID.randomUUID().toString();
            for (TransactionDTO transactionDTO : transactionDTOs) {
                TransactionRequest transaction = accountService.addTransactionRequest(transactionDTO.getAccount(),
                        transactionDTO.getTransactionDescription(), transactionDTO.getTransactionAmount(),
                        transactionDTO.getTransactionType(), token, user);
                if (transaction == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("This account is disabled."));
                }
            }
            List<TransactionRequest> trs = transactionRequestRepository.findAllByToken(token);
            accountService.approveJournalEntry(trs);
            return ResponseEntity.ok().body(new MessageResponse("Your journal entry has been added."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("This account cannot be found."));
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @PostMapping("/chart-of-accounts/request-journal-entry")
    public ResponseEntity<?> requestJournalEntry(@RequestBody JournalEntryRequest jer) {
        TransactionDTO[] transactionDTOs = jer.getTransactions();
        User user = jer.getUser();
        try {
            String token = UUID.randomUUID().toString();
            StringBuilder body = new StringBuilder();
            body.append("Total transactions included: ").append(transactionDTOs.length).append("\n\n");
            int i = 1;
            for (TransactionDTO transactionDTO : transactionDTOs) {
                TransactionRequest transaction = accountService.addTransactionRequest(transactionDTO.getAccount(),
                        transactionDTO.getTransactionDescription(), transactionDTO.getTransactionAmount(),
                        transactionDTO.getTransactionType(), token, user);
                if (transaction == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("This account is disabled."));
                }
                body.append("Transaction ").append(i++).append(":\n");
                body.append("- Account: ").append(transactionDTO.getAccount()).append("\n");
                body.append("- Description: ").append(transactionDTO.getTransactionDescription()).append("\n");
                body.append("- Amount: ").append(transactionDTO.getTransactionAmount()).append("\n");
                body.append("- Type: ").append(transactionDTO.getTransactionType()).append("\n\n");
            }
            body.append("You may approve or deny this journal entry through the \"pending journal entries\" button" +
                    " in the manager panel.").append("\n");
            body.append("You may approve this journal entry directly from this email using this link:").append("\n");
            body.append("https://synergyaccounting.app/approve-journal-entry?token=").append(token);
            emailService.sendMassManagerEmail("Journal Entry Request: " + user.getUsername(), body.toString());
            return ResponseEntity.ok().body(new MessageResponse("Your journal entry has been added and will be visible once approved" +
                    " by a manager."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("This account cannot be found."));
        }
    }

    @GetMapping("/approve-journal-entry")
    public ResponseEntity<?> approveJournalEntry(@RequestParam("token") String token) {
        List<TransactionRequest> trs = transactionRequestRepository.findAllByToken(token);
        if (trs == null || trs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Invalid Transaction" +
                    " Request Token\nThis transaction has already been responded to, or the link is incorrect."));
        }
        return accountService.approveJournalEntry(trs);
    }

    @GetMapping("/reject-journal-entry")
    public ResponseEntity<?> rejectJournalEntry(@RequestParam("token") String token) {
        List<TransactionRequest> trs = transactionRequestRepository.findAllByToken(token);
        if (trs == null || trs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Invalid Transaction" +
                    " Request Token\nThis transaction has already been responded to, or the link is incorrect."));
        }
        return accountService.rejectJournalEntry(trs);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/chart-of-accounts/update-transaction")
    public ResponseEntity<Transaction> updateTransaction(@RequestBody TransactionDTO transactionDTO) {
        Transaction updatedTransaction = accountService.updateTransaction(transactionDTO);
        if (updatedTransaction != null) {
            return ResponseEntity.ok(updatedTransaction);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/chart-of-accounts/update-account")
    public ResponseEntity<Account> updateAccount(@RequestBody UpdateAccountDTO accountDTO) {
        Account updatedAccount = accountService.updateAccount(accountDTO);
        if (updatedAccount != null) {
            return ResponseEntity.ok(updatedAccount);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/chart-of-accounts/{accountNumber}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccount(@PathVariable Long accountNumber) {
        return ResponseEntity.ok(accountService.getTransactionsByAccountNumber(accountNumber));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/chart-of-accounts/delete-transactions")
    public ResponseEntity<?> deleteTransaction(@RequestBody TransactionResponseDTO[] transactions) {
        if (transactions.length == 0) return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Selection is Empty!");
        if (accountService.deleteTransactions(transactions)) return ResponseEntity.ok(new MessageResponse("Transactions deleted successfully!"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Transactions could not be deleted!"));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/chart-of-accounts/update-activation")
    public ResponseEntity<?> deactivateAccounts(@RequestBody AccountResponseDTO account) {
        return accountService.deactivateAccount(account);
    }

}
