package com.synchrony.inclusion.service;

import com.synchrony.inclusion.domain.Applicant;
import com.synchrony.inclusion.domain.UserAccount;
import com.synchrony.inclusion.domain.enums.BankedStatus;
import com.synchrony.inclusion.domain.enums.Role;
import com.synchrony.inclusion.dto.AuthResponse;
import com.synchrony.inclusion.dto.LoginRequest;
import com.synchrony.inclusion.dto.MeResponse;
import com.synchrony.inclusion.dto.RegisterRequest;
import com.synchrony.inclusion.exception.ResourceNotFoundException;
import com.synchrony.inclusion.repository.ApplicantRepository;
import com.synchrony.inclusion.repository.UserAccountRepository;
import com.synchrony.inclusion.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Registration, authentication and profile lookup.
 */
@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final ApplicantRepository applicantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(UserAccountRepository userRepository, ApplicantRepository applicantRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager, AuditService auditService) {
        this.userRepository = userRepository;
        this.applicantRepository = applicantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("Username is already taken.");
        }

        UserAccount account = new UserAccount();
        account.setUsername(request.username());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setFullName(request.fullName());
        account.setRole(Role.APPLICANT);
        account = userRepository.save(account);

        Applicant applicant = new Applicant();
        applicant.setUserId(account.getId());
        applicant.setDisplayName(request.displayName() != null ? request.displayName() : request.fullName());
        applicant.setAnonymizedRef(generateRef());
        applicant.setSegment(request.segment());
        applicant.setBankedStatus(request.bankedStatus() != null ? request.bankedStatus() : BankedStatus.THIN_FILE);
        applicant.setProtectedClass(request.protectedClass());
        applicant = applicantRepository.save(applicant);

        auditService.record("Applicant", applicant.getId(), "REGISTER", account.getUsername(),
                "Applicant profile created with reference " + applicant.getAnonymizedRef());

        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return new AuthResponse(token, account.getUsername(), account.getRole().name(),
                account.getFullName(), jwtService.getExpirationMinutes());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserAccount account = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        auditService.record("UserAccount", account.getId(), "LOGIN", account.getUsername(),
                "Successful authentication.");

        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return new AuthResponse(token, account.getUsername(), account.getRole().name(),
                account.getFullName(), jwtService.getExpirationMinutes());
    }

    public MeResponse me(String username) {
        UserAccount account = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        Long applicantId = applicantRepository.findByUserId(account.getId())
                .map(Applicant::getId)
                .orElse(null);
        return new MeResponse(account.getUsername(), account.getRole().name(),
                account.getFullName(), applicantId);
    }

    private String generateRef() {
        return "SYF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
