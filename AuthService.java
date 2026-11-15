package com.mindsmart.service;

import com.mindsmart.dto.AuthRequest;
import com.mindsmart.dto.AuthResponse;
import com.mindsmart.dto.RegisterRequest;
import com.mindsmart.entity.CompanyProfile;
import com.mindsmart.entity.Role;
import com.mindsmart.entity.StudentProfile;
import com.mindsmart.entity.User;
import com.mindsmart.repository.CompanyProfileRepository;
import com.mindsmart.repository.StudentProfileRepository;
import com.mindsmart.repository.UserRepository;
import com.mindsmart.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       CompanyProfileRepository companyProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       EmailService emailService,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_STUDENT;

        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        user = userRepository.save(user);

        Long profileId = null;
        boolean verified = false;

        if (role == Role.ROLE_STUDENT) {
            StudentProfile profile = new StudentProfile(user);
            if (request.getCollege() != null) profile.setCollege(request.getCollege());
            if (request.getDegree() != null) profile.setDegree(request.getDegree());
            if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
            if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
            if (request.getSkills() != null) profile.setSkills(new HashSet<>(request.getSkills()));
            profile = studentProfileRepository.save(profile);
            profileId = profile.getId();
        } else if (role == Role.ROLE_COMPANY) {
            String cName = request.getCompanyName() != null ? request.getCompanyName() : user.getName();
            CompanyProfile profile = new CompanyProfile(user, cName);
            profile.setRecruiterName(user.getName());
            profile.setWebsite(request.getWebsite());
            profile.setIndustry(request.getIndustry());
            profile.setLocation(request.getLocation());
            profile.setDescription(request.getDescription());
            profile = companyProfileRepository.save(profile);
            profileId = profile.getId();
        }

        // Send registration welcome email
        emailService.sendRegistrationConfirmation(user.getEmail(), user.getName(), role.name());

        // In-app welcome notification
        notificationService.notify(
                user,
                "Welcome to MindSmart!",
                "Your account has been created. Start exploring smart internship opportunities tailored to your skillset.",
                "SUCCESS"
        );

        String token = tokenProvider.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                profileId,
                verified,
                "Registration successful! Welcome to MindSmart."
        );
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is suspended. Please contact MindSmart administration.");
        }

        String token = tokenProvider.generateToken(authentication);

        Long profileId = null;
        boolean verified = false;

        if (user.getRole() == Role.ROLE_STUDENT) {
            var prof = studentProfileRepository.findByUserId(user.getId());
            if (prof.isPresent()) profileId = prof.get().getId();
        } else if (user.getRole() == Role.ROLE_COMPANY) {
            var comp = companyProfileRepository.findByUserId(user.getId());
            if (comp.isPresent()) {
                profileId = comp.get().getId();
                verified = comp.get().isVerified();
            }
        }

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                profileId,
                verified,
                "Login successful!"
        );
    }
}
