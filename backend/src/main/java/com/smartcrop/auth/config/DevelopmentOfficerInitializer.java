package com.smartcrop.auth.config;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Component
@Profile("dev")
public class DevelopmentOfficerInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevelopmentOfficerInitializer.class);
    private static final String EMAIL = "OFFICER_TEST_EMAIL";
    private static final String PASSWORD = "OFFICER_TEST_PASSWORD";
    private static final String NAME = "OFFICER_TEST_NAME";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Supplier<Map<String, String>> environmentSupplier;

    @Autowired
    public DevelopmentOfficerInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, System::getenv);
    }

    DevelopmentOfficerInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Supplier<Map<String, String>> environmentSupplier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environmentSupplier = environmentSupplier;
    }

    @Override
    public void run(String... args) {
        Map<String, String> environment = environmentSupplier.get();
        String email = value(environment, EMAIL);
        String password = environment.get(PASSWORD);
        String name = value(environment, NAME);

        if (email == null || password == null || password.isBlank() || name == null) {
            logger.info("Development officer test account was not created: {} must be set and non-blank.",
                    missingVariable(email, password, name));
            return;
        }

        if (userRepository.existsByEmail(email)) {
            logger.info("Development officer test account already exists for email {}. No changes made.", email);
            return;
        }

        User user = new User(
                null,
                name,
                email,
                null,
                passwordEncoder.encode(password),
                Role.OFFICER,
                null,
                null);
        userRepository.save(user);
        logger.info("Development officer test account created for email {}.", email);
    }

    private String value(Map<String, String> environment, String variable) {
        String value = environment.get(variable);
        return value == null || value.isBlank()
                ? null
                : variable.equals(EMAIL) ? value.trim().toLowerCase(Locale.ROOT) : value.trim();
    }

    private String missingVariable(String email, String password, String name) {
        if (email == null) {
            return EMAIL;
        }
        if (password == null || password.isBlank()) {
            return PASSWORD;
        }
        return NAME;
    }
}
