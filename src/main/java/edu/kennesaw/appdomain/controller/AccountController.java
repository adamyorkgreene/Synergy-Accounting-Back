package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.*;
import edu.kennesaw.appdomain.entity.*;
import edu.kennesaw.appdomain.repository.AttachmentRepository;
import edu.kennesaw.appdomain.repository.JournalEntryRepository;
import edu.kennesaw.appdomain.repository.TransactionRequestRepository;
import edu.kennesaw.appdomain.service.AccountService;
import edu.kennesaw.appdomain.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    @Autowired
    private AttachmentRepository attachmentRepository;

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
        JournalEntry je = new JournalEntry();
        je.setUser(user);
        je.setDate(new Date());
        journalEntryRepository.save(je);
        try {
            for (TransactionDTO transactionDTO : transactionDTOs) {
                TransactionRequest transaction = accountService.addTransactionRequest(transactionDTO.getAccount(),
                        transactionDTO.getTransactionDescription(), transactionDTO.getTransactionAmount(),
                        transactionDTO.getTransactionType(), je.getPr());
                if (transaction == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("This account is disabled."));
                }
            }
            List<TransactionRequest> trs = transactionRequestRepository.findAllByPr(je.getPr());
            accountService.approveJournalEntry(trs, "");
            MessageResponse msgResponse = new MessageResponse("Your journal entry has been added.");
            JournalEntryResponseDTO jerDTO = new JournalEntryResponseDTO();
            jerDTO.setMessageResponse(msgResponse);
            jerDTO.setId(je.getPr());
            return ResponseEntity.ok().body(jerDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("This account cannot be found."));
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @PostMapping("/chart-of-accounts/request-journal-entry")
    public ResponseEntity<?> requestJournalEntry(@RequestBody JournalEntryRequest jer) {
        TransactionDTO[] transactionDTOs = jer.getTransactions();
        User user = jer.getUser();
        JournalEntry je = new JournalEntry();
        je.setUser(user);
        je.setDate(new Date());
        journalEntryRepository.save(je);
        try {
            String token = UUID.randomUUID().toString();
            StringBuilder body = new StringBuilder();
            body.append("Total transactions included: ").append(transactionDTOs.length).append("\n\n");
            int i = 1;
            for (TransactionDTO transactionDTO : transactionDTOs) {
                TransactionRequest transaction = accountService.addTransactionRequest(transactionDTO.getAccount(),
                        transactionDTO.getTransactionDescription(), transactionDTO.getTransactionAmount(),
                        transactionDTO.getTransactionType(), je.getPr());
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
            MessageResponse msgResponse = new MessageResponse("Your journal entry has been added and will be visible once approved" +
                    " by a manager.");
            JournalEntryResponseDTO jerDTO = new JournalEntryResponseDTO();
            jerDTO.setMessageResponse(msgResponse);
            jerDTO.setId(je.getPr());
            return ResponseEntity.ok().body(jerDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("This account cannot be found."));
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @GetMapping("/general-ledger")
    public ResponseEntity<?> getGeneralLedger() {
        return ResponseEntity.ok(accountService.getJournalEntryRequests(true));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/trial-balance")
    public ResponseEntity<List<TrialBalanceDTO>> getTrialBalance(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<TrialBalanceDTO> trialBalance = accountService.getTrialBalance(startDate, endDate);
        return ResponseEntity.ok(trialBalance);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/income-statement")
    public ResponseEntity<IncomeStatementDTO> getIncomeStatement(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        IncomeStatementDTO incomeStatement = accountService.getIncomeStatement(startDate, endDate);
        return ResponseEntity.ok(incomeStatement);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetDTO> getBalanceSheet(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        BalanceSheetDTO balanceSheet = accountService.getBalanceSheet(startDate, endDate);
        return ResponseEntity.ok(balanceSheet);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/total-assets")
    public ResponseEntity<NumberDTO> getTotalAssets() {
        NumberDTO totalAssets = accountService.getTotalAssets();
        return ResponseEntity.ok(totalAssets);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/total-liabilities")
    public ResponseEntity<NumberDTO> getTotalLiabilities() {
        NumberDTO totalLiabilities = accountService.getTotalLiabilities();
        return ResponseEntity.ok(totalLiabilities);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/current-ratio")
    public ResponseEntity<CurrentRatioDTO> getCurrentRatio() {
        double totalAssets = accountService.getTotalAssets().getNumber();
        double totalLiabilities = accountService.getTotalLiabilities().getNumber();
        return ResponseEntity.ok(new CurrentRatioDTO(totalAssets, totalLiabilities));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/quick-ratio")
    public ResponseEntity<QuickRatioDTO> getQuickRatio() {
        double totalAssets = accountService.getTotalAssets().getNumber();
        double totalLiabilities = accountService.getTotalLiabilities().getNumber();
        double totalInventory = accountService.getTotalInventory().getNumber();
        return ResponseEntity.ok(new QuickRatioDTO(totalAssets, totalLiabilities, totalInventory));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/debt-to-equity-ratio")
    public ResponseEntity<DebtToEquityRatioDTO> getDebtToEquityRatio() {
        double totalAssets = accountService.getTotalAssets().getNumber();
        double totalLiabilities = accountService.getTotalLiabilities().getNumber();
        return ResponseEntity.ok(new DebtToEquityRatioDTO(totalAssets, totalLiabilities));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/return-on-assets")
    public ResponseEntity<ReturnOnAssetsDTO> getReturnOnAssets() {
        double netIncome = accountService.getIncomeStatement().getNetIncome();
        double totalAssets = accountService.getTotalAssets().getNumber();
        return ResponseEntity.ok(new ReturnOnAssetsDTO(netIncome, totalAssets));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/return-on-equity")
    public ResponseEntity<ReturnOnEquityDTO> getReturnOnEquity() {
        double netIncome = accountService.getIncomeStatement().getNetIncome();
        double totalLiabilities = accountService.getTotalLiabilities().getNumber();
        return ResponseEntity.ok(new ReturnOnEquityDTO(netIncome, totalLiabilities));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'ADMINISTRATOR')")
    @GetMapping("/retained-earnings")
    public ResponseEntity<RetainedEarningsDTO> getRetainedEarnings(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        RetainedEarningsDTO retainedEarnings = accountService.getRetainedEarnings(startDate, endDate);
        return ResponseEntity.ok(retainedEarnings);
    }

    @GetMapping("/approve-journal-entry")
    public ResponseEntity<?> approveJournalEntry(@RequestParam("token") Long pr) {
        List<TransactionRequest> trs = transactionRequestRepository.findAllByPr(pr);
        if (trs == null || trs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Invalid Transaction" +
                    " Request Token\nThis transaction has already been responded to, or the link is incorrect."));
        }
        return accountService.approveJournalEntry(trs, "");
    }

    @GetMapping("/reject-journal-entry")
    public ResponseEntity<?> rejectJournalEntry(@RequestParam("token") Long pr) {
        List<TransactionRequest> trs = transactionRequestRepository.findAllByPr(pr);
        if (trs == null || trs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: Invalid Transaction" +
                    " Request Token\nThis transaction has already been responded to, or the link is incorrect."));
        }
        return accountService.rejectJournalEntry(trs, "");
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

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER')")
    @GetMapping("/journal-entry/{pr}")
    public ResponseEntity<?> journalEntryByToken(@PathVariable Long pr) {
        return ResponseEntity.ok(accountService.getJournalEntry(pr));
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

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @PostMapping("/upload-attachments")
    public ResponseEntity<String> uploadAttachments(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("journalEntryId") Long journalEntryId,
            HttpServletRequest request) {
        if (files.isEmpty()) {
            return new ResponseEntity<>("No files selected", HttpStatus.BAD_REQUEST);
        }
        String uploadDir = "/home/sweappdomain/demobackend/je_attachments/" + journalEntryId + "/";
        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            return new ResponseEntity<>("Could not create upload directory", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                File destinationFile = new File(uploadDir + UUID.randomUUID() + "_" + originalFilename);
                try {
                    file.transferTo(destinationFile);
                    uploadedFileNames.add(destinationFile.getName());

                    Attachment attachment = new Attachment();
                    attachment.setJournalEntry(journalEntryRepository.findByPr(journalEntryId));
                    attachment.setFileName(destinationFile.getName());
                    attachment.setFilePath(destinationFile.getAbsolutePath());
                    attachmentRepository.save(attachment);
                } catch (IOException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>("Could not upload the file: " + originalFilename, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return ResponseEntity.ok("Files uploaded successfully: " + String.join(", ", uploadedFileNames));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @GetMapping("/uploads/{journalEntryId}/{filename:.+}")
    public ResponseEntity<?> serveAttachment(
            @PathVariable Long journalEntryId,
            @PathVariable String filename) throws IOException {

        Path filePath = Paths.get("/home/sweappdomain/demobackend/je_attachments/" + journalEntryId + "/").resolve(filename);
        if (Files.exists(filePath)) {
            Resource resource = new FileSystemResource(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath))
                    .body(resource);
        } else {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @GetMapping("/uploads/{journalEntryId}")
    public ResponseEntity<List<String>> getAttachmentsForJournalEntry(@PathVariable Long journalEntryId) {
        List<Attachment> attachments = attachmentRepository.findAllByJe(journalEntryRepository.findByPr(journalEntryId));
        if (attachments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<String> attachmentFileNames = attachments.stream()
                .map(Attachment::getFileName)
                .collect(Collectors.toList());

        return new ResponseEntity<>(attachmentFileNames, HttpStatus.OK);
    }

}
