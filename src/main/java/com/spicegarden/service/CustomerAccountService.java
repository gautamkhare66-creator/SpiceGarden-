package com.spicegarden.service;

import com.spicegarden.domain.CustomerAccount;
import com.spicegarden.repository.CustomerAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Transactional
public class CustomerAccountService {
    private final CustomerAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAccountService(CustomerAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public CustomerAccount register(String fullName, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be no longer than 72 bytes");
        }

        CustomerAccount account = new CustomerAccount();
        account.setFullName(fullName.trim());
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(password));
        return repository.save(account);
    }

    @Transactional(readOnly = true)
    public CustomerAccount findByEmail(String email) {
        return repository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}