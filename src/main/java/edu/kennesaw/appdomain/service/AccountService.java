package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.*;
import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.entity.Transaction;
import edu.kennesaw.appdomain.entity.TransactionRequest;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.AccountRepository;
import edu.kennesaw.appdomain.repository.TransactionRepository;
import edu.kennesaw.appdomain.repository.TransactionRequestRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.service.utils.AccountNumberGenerator;
import edu.kennesaw.appdomain.types.AccountCategory;
import edu.kennesaw.appdomain.types.AccountSubCategory;
import edu.kennesaw.appdomain.types.AccountType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
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
                        transaction.getTransactionType()
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
        return ResponseEntity.ok(accountRepository.save(account));
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
                                      AccountType transactionType, String token, User user) {

        if (!account.getIsActive()) {
            return null;
        }

        TransactionRequest transaction = new TransactionRequest();
        transaction.setAccount(account);
        transaction.setDescription(transactionDescription);
        transaction.setAmount(transactionAmount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(new Date());
        transaction.setToken(token);
        transaction.setApproved(null);
        transaction.setUser(user);

        return transactionRequestRepository.save(transaction);

    }

    @Transactional
    public ResponseEntity<?> approveJournalEntry(List<TransactionRequest> trs) {
        for (TransactionRequest tr : trs) {
            Transaction transaction = new Transaction();
            transaction.setAccount(tr.getAccount());
            transaction.setTransactionType(tr.getTransactionType());
            transaction.setTransactionDate(tr.getTransactionDate());
            transaction.setAmount(tr.getAmount());
            transaction.setDescription(tr.getDescription());
            tr.setApproved(true);
            transactionRepository.save(transaction);
            transactionRequestRepository.save(tr);
        }
        User user = trs.get(0).getUser();
        emailService.sendBasicNoReplyEmail(user.getUsername() + "@synergyaccounting.app",
                "Your journal entry has been approved!",
                user.getFirstName() + ", \n\nYour journal entry has been approved and is now reflected" +
                        " within the chart of accounts.");
        return ResponseEntity.ok().body(new MessageResponse("This journal entry has been approved."));
    }

    @Transactional
    public ResponseEntity<?> rejectJournalEntry(List<TransactionRequest> trs) {
        for (TransactionRequest tr : trs) {
            tr.setApproved(false);
            transactionRequestRepository.save(tr);
        }
        return ResponseEntity.ok().body(new MessageResponse("This journal entry has been rejected."));
    }

    public List<JournalEntryRequest> getJournalEntryRequests(Boolean isApproved) {

        List<TransactionRequest> trs = transactionRequestRepository.findAllByIsApprovedOrderByToken(isApproved);
        List<JournalEntryRequest> jers = new ArrayList<>();
        if (trs == null || trs.isEmpty()) {
            return jers;
        }

        JournalEntryRequest currentJournalEntry = new JournalEntryRequest();
        List<TransactionDTO> currentTransactions = new ArrayList<>();

        String currentToken = trs.get(0).getToken();
        User currentUser = trs.get(0).getUser();

        for (TransactionRequest tr : trs) {

            if (!tr.getToken().equals(currentToken)) {

                currentJournalEntry.setTransactions(currentTransactions.toArray(new TransactionDTO[0]));
                currentJournalEntry.setUser(currentUser);
                jers.add(currentJournalEntry);

                currentJournalEntry = new JournalEntryRequest();
                currentTransactions = new ArrayList<>();
                currentToken = tr.getToken();
                currentUser = tr.getUser();
            }

            TransactionDTO transactionDTO = new TransactionDTO(
                    tr.getAccount(),
                    tr.getDescription(),
                    tr.getAmount(),
                    tr.getTransactionType()
            );
            transactionDTO.setTransactionId(tr.getTransactionId());
            transactionDTO.setTransactionDate(tr.getTransactionDate());
            currentTransactions.add(transactionDTO);
        }

        currentJournalEntry.setTransactions(currentTransactions.toArray(new TransactionDTO[0]));
        currentJournalEntry.setUser(currentUser);
        jers.add(currentJournalEntry);

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
            account.setAccountName(accountDTO.getAccountName());
            account.setAccountDescription(accountDTO.getAccountDescription());
            return accountRepository.save(account);
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
