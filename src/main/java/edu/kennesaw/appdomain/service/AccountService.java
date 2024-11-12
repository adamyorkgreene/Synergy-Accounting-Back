package edu.kennesaw.appdomain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kennesaw.appdomain.dto.*;
import edu.kennesaw.appdomain.entity.*;
import edu.kennesaw.appdomain.repository.*;
import edu.kennesaw.appdomain.service.utils.AccountNumberGenerator;
import edu.kennesaw.appdomain.types.AccountCategory;
import edu.kennesaw.appdomain.types.AccountSubCategory;
import edu.kennesaw.appdomain.types.AccountType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionRequestRepository transactionRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private EventLogService eventLogService;  // Added EventLogService for logging events

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AccountResponseDTO> getChartOfAccounts() {
        List<Account> accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "accountNumber"));
        return accounts.stream()
                .map(account -> new AccountResponseDTO(
                        account.getAccountName(),
                        account.getAccountNumber(),
                        account.getAccountDescription(),
                        account.getNormalSide(),
                        account.getAccountCategory(),
                        account.getAccountSubCategory(),
                        account.getInitialBalance(),
                        account.getDebitBalance(),
                        account.getCreditBalance(),
                        account.getDateAdded(),
                        userRepository.getUsernameByUserid(account.getCreator().getUserid()),
                        account.getIsActive()
                ))
                .collect(Collectors.toList());
    }

    public List<AccountResponseDTO> getChartOfAccountsWithUsername() {
        return accountRepository.getChartOfAccountsWithUsername();
    }

    public List<TransactionResponseDTO> getTransactionsByAccountNumber(Long accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountAccountNumber(accountNumber);
        return transactions.stream()
                .map(transaction -> new TransactionResponseDTO(
                        transaction.getTransactionId(),
                        new SimpleDateFormat("yyyy-MM-dd").format(transaction.getTransactionDate()),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        transaction.getTransactionType(),
                        transaction.getPr()
                ))
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> addAccount(String accountName, String accountDescription, AccountType normalSide, AccountCategory
                              accountCategory, AccountSubCategory accountSubCategory, Double initialBalance, Integer creator) {
        if (accountRepository.findByAccountName(accountName) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("This account already exists."));
        }
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber(accountCategory));
        account.setAccountName(accountName);
        account.setAccountDescription(accountDescription);
        account.setNormalSide(normalSide);
        account.setAccountCategory(accountCategory);
        account.setAccountSubCategory(accountSubCategory);
        if (initialBalance == null) {
            initialBalance = 0.0;
        }
        account.setInitialBalance(initialBalance);
        if (normalSide.equals(AccountType.DEBIT)) {
            account.setDebitBalance(initialBalance);
            account.setCreditBalance(0);
        } else {
            account.setCreditBalance(initialBalance);
            account.setDebitBalance(0);
        }
        account.setDateAdded(new Date());
        Optional<User> user = userRepository.findByUserid(creator);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Creator not found, please try again."));
        }
        account.setCreator(user.get());

        Account savedAccount = accountRepository.save(account);

        try {
            String afterState = objectMapper.writeValueAsString(account);
            eventLogService.logAccountEvent(savedAccount, "CREATE", user.get().getUserid(), null, afterState);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(savedAccount);
    }

    @Transactional
    public Transaction addTransaction(Account account, String transactionDescription, Double transactionAmount,
                                      AccountType transactionType) {

        if (!account.getIsActive()) {
            return null;
        }

        if (transactionType.equals(AccountType.DEBIT)) {
            account.setDebitBalance(account.getDebitBalance() + transactionAmount);
        } else if (transactionType.equals(AccountType.CREDIT)) {
            account.setCreditBalance(account.getCreditBalance() + transactionAmount);
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setDescription(transactionDescription);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(new Date());

        return transactionRepository.save(transaction);

    }

    @Transactional
    public TransactionRequest addTransactionRequest(Account account, String transactionDescription, Double transactionAmount,
                                      AccountType transactionType, Long pr) {

        if (!account.getIsActive()) {
            return null;
        }

        TransactionRequest transaction = new TransactionRequest();
        transaction.setAccount(account);
        transaction.setDescription(transactionDescription);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(new Date());
        transaction.setPr(pr);
        transaction.setApproved(null);

        return transactionRequestRepository.save(transaction);

    }

    @Transactional
    public ResponseEntity<?> approveJournalEntry(List<TransactionRequest> trs, String comments) {
        for (TransactionRequest tr : trs) {
            Transaction transaction = new Transaction();
            Account account = tr.getAccount();
            account = accountRepository.findById(account.getAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            Double amount = tr.getAmount();
            if (tr.getTransactionType().equals(AccountType.DEBIT)) {
                account.setDebitBalance(account.getDebitBalance() + amount);
            } else if (tr.getTransactionType().equals(AccountType.CREDIT)) {
                account.setCreditBalance(account.getCreditBalance() + amount);
            }
            accountRepository.save(account);
            transaction.setTransactionId(tr.getTransactionId());
            transaction.setAccount(account);
            transaction.setTransactionType(tr.getTransactionType());
            transaction.setTransactionDate(tr.getTransactionDate());
            transaction.setAmount(tr.getAmount());
            transaction.setDescription(tr.getDescription());
            transaction.setPr(tr.getPr());
            tr.setApproved(true);
            transactionRepository.save(transaction);
            transactionRequestRepository.save(tr);
        }
        JournalEntry je = journalEntryRepository.findByPr(trs.get(0).getPr());
        je.setComments(comments);
        je.setApproved(true);
        journalEntryRepository.save(je);
        User user = je.getUser();
        emailService.sendBasicNoReplyEmail(user.getUsername() + "@synergyaccounting.app",
                "Your journal entry has been approved!",
                user.getFirstName() + ", \n\nYour journal entry has been approved and is now reflected" +
                        " within the chart of accounts." + (comments.isEmpty() ? "" : "\n\nComments: " + comments));
        return ResponseEntity.ok().body(new MessageResponse("This journal entry has been approved."));
    }

    @Transactional
    public ResponseEntity<?> rejectJournalEntry(List<TransactionRequest> trs, String comments) {
        for (TransactionRequest tr : trs) {
            tr.setApproved(false);
            transactionRequestRepository.save(tr);
        }
        JournalEntry je = journalEntryRepository.findByPr(trs.get(0).getPr());
        je.setComments(comments);
        je.setApproved(false);
        journalEntryRepository.save(je);
        User user = je.getUser();
        emailService.sendBasicNoReplyEmail(user.getUsername() + "@synergyaccounting.app",
                "Journal Entry Response",
                user.getFirstName() + ", \n\nYour journal entry has been rejected for the following reason:\n" + comments);
        return ResponseEntity.ok().body(new MessageResponse("This journal entry has been rejected."));
    }

    public JournalEntryRequest getJournalEntry(Long pr) {
        List<TransactionRequest> trs = transactionRequestRepository
                .findAllByIsApprovedAndPr(true, pr);
        if (trs == null || trs.isEmpty()) {
            return null;
        }
        JournalEntryRequest jer = new JournalEntryRequest();
        User currentUser = journalEntryRepository.findByPr(pr).getUser();
        List<TransactionDTO> currentTransactions = new ArrayList<>();
        trs.forEach(tr -> {
            TransactionDTO transactionDTO = new TransactionDTO(
                    tr.getAccount(),
                    tr.getDescription(),
                    tr.getAmount(),
                    tr.getTransactionType()
            );
            transactionDTO.setPr(tr.getPr());
            transactionDTO.setTransactionId(tr.getTransactionId());
            transactionDTO.setTransactionDate(tr.getTransactionDate());
            currentTransactions.add(transactionDTO);
        });
        jer.setTransactions(currentTransactions.toArray(new TransactionDTO[0]));
        jer.setUser(currentUser);
        return jer;
    }

    public List<JournalEntryRequest> getJournalEntryRequests(Boolean isApproved) {
        List<JournalEntryRequest> jers = new ArrayList<>();
        List<JournalEntry> jentries = journalEntryRepository.findAllByIsApproved(isApproved);
        for (JournalEntry je : jentries) {
            JournalEntryRequest jer = new JournalEntryRequest();
            List<TransactionDTO> tdtos = new ArrayList<>();
            for (TransactionRequest tr : transactionRequestRepository.findAllByPr(je.getPr())) {
                TransactionDTO transactionDTO = new TransactionDTO(
                        tr.getAccount(),
                        tr.getDescription(),
                        tr.getAmount(),
                        tr.getTransactionType()
                );
                transactionDTO.setPr(tr.getPr());
                transactionDTO.setTransactionId(tr.getTransactionId());
                transactionDTO.setTransactionDate(tr.getTransactionDate());
                tdtos.add(transactionDTO);
            }
            jer.setTransactions(tdtos.toArray(new TransactionDTO[0]));
            jer.setApproved(null);
            jer.setUser(je.getUser());
            jer.setComments(je.getComments());
            jer.setPr(je.getPr());
            jers.add(jer);
        }
        return jers;
    }

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public List<TrialBalanceDTO> getTrialBalance(Date startDate, Date endDate) {
        logger.info("Fetching trial balance for date range: {} to {}", startDate, endDate);

        Map<String, Double> accountDebits = new HashMap<>();
        Map<String, Double> accountCredits = new HashMap<>();

        // Fetch approved journal entries within the specified date range
        List<JournalEntry> entries = journalEntryRepository.findAllByIsApprovedAndDateBetween(true, startDate, endDate);

        if (entries.isEmpty()) {
            logger.warn("No journal entries found for the specified date range.");
        } else {
            logger.info("Found {} journal entries for the specified date range.", entries.size());
        }

        // Calculate debits and credits per account
        for (JournalEntry entry : entries) {
            List<TransactionRequest> transactions = transactionRequestRepository.findAllByPr(entry.getPr());
            for (TransactionRequest transaction : transactions) {
                String accountName = transaction.getAccount().getAccountName();
                Double amount = transaction.getAmount();

                if (transaction.getTransactionType() == AccountType.DEBIT) {
                    accountDebits.put(accountName, accountDebits.getOrDefault(accountName, 0.0) + amount);
                } else {
                    accountCredits.put(accountName, accountCredits.getOrDefault(accountName, 0.0) + amount);
                }
            }
        }

        // Prepare trial balance entries
        List<TrialBalanceDTO> trialBalanceList = new ArrayList<>();
        Set<String> allAccounts = new HashSet<>(accountDebits.keySet());
        allAccounts.addAll(accountCredits.keySet());

        for (String accountName : allAccounts) {
            double debit = accountDebits.getOrDefault(accountName, 0.0);
            double credit = accountCredits.getOrDefault(accountName, 0.0);
            trialBalanceList.add(new TrialBalanceDTO(accountName, debit, credit));
        }

        logger.info("Generated trial balance with {} entries.", trialBalanceList.size());
        return trialBalanceList;
    }

    @Deprecated
    public List<JournalEntryRequest> getJournalEntryRequestsOld(Boolean isApproved) {

        List<TransactionRequest> trs = transactionRequestRepository.findAllByIsApprovedOrderByPr(isApproved);
        List<JournalEntryRequest> jers = new ArrayList<>();
        if (trs == null || trs.isEmpty()) {
            return jers;
        }

        JournalEntryRequest currentJournalEntry = new JournalEntryRequest();
        List<TransactionDTO> currentTransactions = new ArrayList<>();

        Long currentToken = trs.get(0).getPr();
        User currentUser = journalEntryRepository.findByPr(currentToken).getUser();

        for (TransactionRequest tr : trs) {

            if (!tr.getPr().equals(currentToken)) {

                currentJournalEntry.setTransactions(currentTransactions.toArray(new TransactionDTO[0]));
                currentJournalEntry.setUser(currentUser);
                JournalEntry je = journalEntryRepository.findByPr(currentToken);
                if (je != null) {
                    System.out.println("\n\n\n[COMMENTS-DEBUG] Token: " + currentToken + ", Comments: " + je.getComments());
                    currentJournalEntry.setComments(je.getComments());
                } else {
                    System.out.println("\n\n\n[COMMENTS-DEBUG] No JournalEntry found for token: " + currentToken);
                }
                jers.add(currentJournalEntry);

                currentJournalEntry = new JournalEntryRequest();
                currentTransactions = new ArrayList<>();
                currentToken = tr.getPr();
                currentUser = journalEntryRepository.findByPr(currentToken).getUser();journalEntryRepository.findByPr(currentToken).getUser();
            }

            TransactionDTO transactionDTO = new TransactionDTO(
                    tr.getAccount(),
                    tr.getDescription(),
                    tr.getAmount(),
                    tr.getTransactionType()
            );
            transactionDTO.setPr(tr.getPr());
            transactionDTO.setTransactionId(tr.getTransactionId());
            transactionDTO.setTransactionDate(tr.getTransactionDate());
            currentTransactions.add(transactionDTO);
        }

        currentJournalEntry.setTransactions(currentTransactions.toArray(new TransactionDTO[0]));
        currentJournalEntry.setUser(currentUser);
        jers.add(currentJournalEntry);

        System.out.println("[COMMENTS-DEBUG] Final JournalEntryRequest list:");
        for (JournalEntryRequest jer : jers) {
            System.out.println("Token: " + (jer.getTransactions().length > 0 ? jer.getTransactions()[0].getPr() : "No Token") +
                    ", Comments: " + jer.getComments());
        }

        return jers;
    }


    @Transactional
    public Transaction updateTransaction(TransactionDTO transactionDTO) {
        Optional<Transaction> transactionOptional = transactionRepository.findById(transactionDTO.getTransactionId());
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            Account account = transaction.getAccount();
            if (transaction.getTransactionType() == AccountType.DEBIT) {
                account.setDebitBalance(account.getDebitBalance() - transaction.getAmount());
            } else {
                account.setCreditBalance(account.getCreditBalance() - transaction.getAmount());
            }
            transaction.setDescription(transactionDTO.getTransactionDescription());
            transaction.setAmount(transactionDTO.getTransactionAmount());
            transaction.setTransactionType(transactionDTO.getTransactionType());
            if (transactionDTO.getTransactionType() == AccountType.DEBIT) {
                account.setDebitBalance(account.getDebitBalance() + transactionDTO.getTransactionAmount());
            } else {
                account.setCreditBalance(account.getCreditBalance() + transactionDTO.getTransactionAmount());
            }
            accountRepository.save(account);
            return transactionRepository.save(transaction);
        } else {
            throw new IllegalArgumentException("Transaction not found.");
        }
    }

    @Transactional
    public Account updateAccount(UpdateAccountDTO accountDTO) {
        Optional<Account> accountOptional = accountRepository.findById(accountDTO.getAccountNumber());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            try {
                String beforeState = objectMapper.writeValueAsString(account);  // Serialize state before update

                // Update account fields
                account.setAccountName(accountDTO.getAccountName());
                account.setAccountDescription(accountDTO.getAccountDescription());

                Account updatedAccount = accountRepository.save(account);

                String afterState = objectMapper.writeValueAsString(updatedAccount);
                eventLogService.logAccountEvent(updatedAccount, "UPDATE", accountOptional.get().getCreator().getUserid(), beforeState, afterState);

                return updatedAccount;

            } catch (Exception e) {
                throw new RuntimeException("Failed to log event", e);
            }
        } else {
            throw new IllegalArgumentException("Account not found");
        }
    }


    @Transactional
    public boolean deleteTransactions(TransactionResponseDTO[] transactions) {
        for (TransactionResponseDTO transactionDTO : transactions) {
            Optional<Transaction> transactionOptional = transactionRepository.findById(transactionDTO.getTransactionId());
            if (transactionOptional.isPresent()) {
                Transaction transaction = transactionOptional.get();
                Account account = transaction.getAccount();
                if (transaction.getTransactionType().equals(AccountType.DEBIT)) {
                    account.setDebitBalance(account.getDebitBalance() - transaction.getAmount());
                } else {
                    account.setCreditBalance(account.getCreditBalance() - transaction.getAmount());
                }
                accountRepository.save(account);
                transactionRequestRepository.deleteByTransactionId(transaction.getTransactionId());
                transactionRepository.delete(transaction);
            } else {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public ResponseEntity<?> deactivateAccount(AccountResponseDTO accountDTO) {
        Optional<Account> accountOptional = accountRepository.findById(accountDTO.getAccountNumber());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            List<Transaction> transactions = transactionRepository.findByAccountAccountNumber(account.getAccountNumber());
            if (!transactions.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("You may not deactivate an account with existing transactions."));
            account.setIsActive(!account.getIsActive());
            return ResponseEntity.ok(accountRepository.save(account));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("This account does not exist."));
        }
    }

    private Long generateAccountNumber(AccountCategory category) {
        Long[] range = AccountNumberGenerator.getRange(category);
        if (range == null) {
            throw new IllegalArgumentException("Invalid account category");
        }
        Long minRange = range[0];
        Long maxRange = range[1];
        Long maxAccountNumber = accountRepository.findMaxAccountNumberInRange(minRange, maxRange);
        if (maxAccountNumber == null) {
            return minRange;
        }
        if (maxAccountNumber >= maxRange) {
            throw new RuntimeException("No available account numbers in category range " + category);
        }
        return maxAccountNumber + 1;
    }

}
