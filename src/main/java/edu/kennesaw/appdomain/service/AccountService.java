package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.AccountResponseDTO;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.TransactionDTO;
import edu.kennesaw.appdomain.dto.TransactionResponseDTO;
import edu.kennesaw.appdomain.entity.Account;
import edu.kennesaw.appdomain.entity.Transaction;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.AccountRepository;
import edu.kennesaw.appdomain.repository.TransactionRepository;
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
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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
    public Transaction addTransaction(Long accountNumber, String transactionDescription, Double transactionAmount,
                                      AccountType transactionType) {

        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

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
    public boolean deactivateAccounts(AccountResponseDTO[] accountsToDelete) {
        for (AccountResponseDTO accountDTO : accountsToDelete) {
            Optional<Account> accountOptional = accountRepository.findById(accountDTO.getAccountNumber());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                List<Transaction> transactions = transactionRepository.findByAccountAccountNumber(account.getAccountNumber());
                for (Transaction transaction : transactions) {
                    if (transaction.getTransactionType().equals(AccountType.DEBIT)) {
                        account.setDebitBalance(account.getDebitBalance() - transaction.getAmount());
                    } else {
                        account.setCreditBalance(account.getCreditBalance() - transaction.getAmount());
                    }
                    transactionRepository.delete(transaction);
                }
                account.setIsActive(false);
            } else {
                return false;
            }
        }
        return true;
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
