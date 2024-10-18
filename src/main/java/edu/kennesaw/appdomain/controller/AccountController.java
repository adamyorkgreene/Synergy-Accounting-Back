package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.*;
import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.entity.Transaction;
import edu.kennesaw.appdomain.repository.AccountRepository;
import edu.kennesaw.appdomain.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @PreAuthorize("hasRole('ADMINISTRATOR')")
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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
    @PostMapping("/chart-of-accounts/deactivate-accounts")
    public ResponseEntity<?> deactivateAccounts(@RequestBody AccountResponseDTO[] accounts) {
        if (accounts.length == 0) return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Selection is Empty!");
        if (accountService.deactivateAccounts(accounts)) return ResponseEntity.ok(new MessageResponse("Accounts deactivated successfully!"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Accounts could not be deleted!"));
    }

}
