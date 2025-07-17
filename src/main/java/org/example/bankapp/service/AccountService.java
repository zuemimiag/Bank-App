package org.example.bankapp.service;

import org.example.bankapp.model.Account;
import org.example.bankapp.model.Trasaction;
import org.example.bankapp.repository.AccountRepository;
import org.example.bankapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;

    public Account findAccountByUsername(String username){
        return accountRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("Account not found!"));
    }

    public Account registerAccount(String username,String  password) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException(("Username already exists!"));
        }
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public void deposit(Account account,BigDecimal amount){
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Trasaction trasaction = new Trasaction(
                amount,
                "Deposit",
                LocalDateTime.now(),
                account
        );
        transactionRepository.save(trasaction);
    }

    public void withdraw (Account account,BigDecimal amount){
        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException(("Insufficient balance!"));
        }
        account.setBalance(account.getBalance().subtract(amount));
         accountRepository.save(account);

         Trasaction trasaction= new Trasaction(
                 amount,
                 "Withdraw",
                 LocalDateTime.now(),
                 account
         );
         transactionRepository.save(trasaction);
    }

    public List<Trasaction> getTransactionHistory(Account account){
        return transactionRepository.findByAccountId(account.getId());
    }

    public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException{
        Account account = findAccountByUsername(username);
        if(account == null){
            throw new UsernameNotFoundException("Username or Password not found");
        }
        return new Account(
                account.getUsername(),
                account.getPassword(),
                account.getBalance(),
                account.getTransactions(),
                authorities()
        );
    }
    public Collection<? extends GrantedAuthority> authorities(){
        return Arrays.asList(new SimpleGrantedAuthority("User"));
    }

    public void transferAmount(Account fromAccount, String toUsername, BigDecimal amount){
        if(fromAccount.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insuficients funds");
        }
        Account toAccount = accountRepository.findByUsername(toUsername)
                .orElseThrow(()-> new RuntimeException("Recipient account not found!"));

        //Subtract
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        //Add
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        //Create transaction records
        Trasaction debitTransaction  =  new Trasaction(
                amount,
                "Transfer Out  to" + toAccount.getUsername(),
                LocalDateTime.now(),
                fromAccount
        );
        transactionRepository.save(debitTransaction);

        Trasaction creditTransaction  =  new Trasaction(
              amount,
              "Transfer In  to" + fromAccount.getUsername(),
              LocalDateTime.now(),
              toAccount
        );
        transactionRepository.save(creditTransaction);

        

    }
}
