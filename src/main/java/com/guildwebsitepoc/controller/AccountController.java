package com.guildwebsitepoc.controller;

import com.guildwebsitepoc.model.Account;
import com.guildwebsitepoc.model.JwtUser;
import com.guildwebsitepoc.security.JwtGenerator;
import com.guildwebsitepoc.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base.url}" + "/AccountsService")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    JwtGenerator jwtGenerator;

    // GET all accounts
    @GetMapping("/accounts")
    public List<Account> findAll() {
        return accountService.findAll();
    }

    // GET an account by accountId
    @GetMapping("/accounts/{accountId}")
    public Account getAccount(@PathVariable int accountId) {
        Account account = accountService.findById(accountId);

        if (account == null) {
            throw new RuntimeException("Account id not found - " + accountId);
        }
        return account;
    }

    // POST a new account
    @PostMapping("/register")
    public Account addAccount(@RequestBody Account account) {
        // if they pass an id, then set it to 0
        // This will insure a new account is created instead of update due to: "currentSession.saveOrUpdate()"
        account.setAccountId(0);
        // Create new account in database
        accountService.save(account);

        return account;
    }

    // Post login with existing account
    @PostMapping("/login")
    public String loginAccount(@RequestBody JwtUser jwtUser) {
        Account expectedAccount = accountService.findByUsername(jwtUser.getUsername());
        boolean passwordMatch = accountService.verifyPassword(jwtUser.getPassword(),
                                                              expectedAccount.getPasswordSalt(),
                                                              expectedAccount.getPasswordHash());
        if (!passwordMatch) {
            throw new RuntimeException("Password does not match");
        }

        return jwtGenerator.generate(jwtUser);
    }

    // PUT an existing account's information
    @PutMapping("/accounts/{accountId}")
    public Account updateAccount(@PathVariable int accountId,
                                 @RequestBody Account account) {
        Account initialAccount = accountService.findById(accountId);
        if (initialAccount.getAccountId() != account.getAccountId()) {
            throw new RuntimeException("AccountId does not match the desired account for updating");
        }
        accountService.save(account);

        return account;
    }

    // DELETE an account by accountId
    @DeleteMapping("/accounts/{accountId}")
    public String deleteAccount(@PathVariable int accountId) {
        // Grab the account
        Account account = accountService.findById(accountId);
        // throw exception if account is null
        if (account == null) {
            throw new RuntimeException("Account id not found - " + accountId);
        }

        accountService.deleteById(accountId);

        return "Deleted account id - " + accountId;
    }

}
