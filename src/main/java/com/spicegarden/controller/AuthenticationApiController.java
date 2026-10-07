package com.spicegarden.controller;

import com.spicegarden.domain.CustomerAccount;
import com.spicegarden.service.CustomerAccountService;
import com.spicegarden.service.dto.LoginRequest;
import com.spicegarden.service.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthenticationApiController {
    private final CustomerAccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthenticationApiController(CustomerAccountService accountService,
                                       AuthenticationManager authenticationManager,
                                       SessionAuthenticationStrategy sessionAuthenticationStrategy,
                                       SecurityContextRepository securityContextRepository,
                                       CsrfTokenRepository csrfTokenRepository) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrfToken(CsrfToken csrfToken) {
        return csrfToken;
    }

    @GetMapping("/auth/me")
    public AccountResponse currentAccount(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
            || !authentication.isAuthenticated()) {
            return AccountResponse.guest();
        }
        CustomerAccount account = accountService.findByEmail(authentication.getName());
        return AccountResponse.from(account);
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(@Valid @RequestBody RegisterRequest request,
                                   HttpServletRequest servletRequest, HttpServletResponse response) {
        accountService.register(request.fullName(), request.email(), request.password());
        return authenticate(request.email(), request.password(), servletRequest, response);
    }

    @PostMapping("/auth/login")
    public AccountResponse login(@Valid @RequestBody LoginRequest request,
                                 HttpServletRequest servletRequest, HttpServletResponse response) {
        return authenticate(request.email(), request.password(), servletRequest, response);
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        csrfTokenRepository.saveToken(null, request, response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidRequest(IllegalArgumentException exception) {
        return new ApiError(exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError invalidCredentials(AuthenticationException exception) {
        return new ApiError("Email or password is incorrect");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidFields(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Please check the submitted fields");
        return new ApiError(message);
    }

    private AccountResponse authenticate(String email, String password,
                                         HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        if (!authentication.isAuthenticated()) {
            throw new AuthenticationServiceException("Authentication failed");
        }

        request.getSession(true);
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return AccountResponse.from(accountService.findByEmail(authentication.getName()));
    }

    public record AccountResponse(boolean authenticated, String fullName, String email) {
        private static AccountResponse guest() {
            return new AccountResponse(false, null, null);
        }

        private static AccountResponse from(CustomerAccount account) {
            return new AccountResponse(true, account.getFullName(), account.getEmail());
        }
    }

    public record ApiError(String message) {
    }
}