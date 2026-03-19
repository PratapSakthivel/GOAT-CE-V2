package com.goatce.service;

import com.goatce.dto.AuthResponse;
import com.goatce.dto.LoginRequest;
import com.goatce.dto.RegisterRequest;
import com.goatce.dto.VerifyOtpRequest;
import com.goatce.model.User;
import com.goatce.repository.UserRepository;
import com.goatce.security.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       StringRedisTemplate redisTemplate,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public String forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email not found");
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        redisTemplate.opsForValue().set("otp:" + email, otp, 10, TimeUnit.MINUTES);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Goat CE - Password Reset OTP");
        message.setText("Your OTP: " + otp + "\nValid 10 minutes.");
        mailSender.send(message);

        return "OTP sent successfully";
    }

    public String verifyOtp(VerifyOtpRequest request) {
        String storedOtp = redisTemplate.opsForValue().get("otp:" + request.email());

        if (storedOtp == null) {
            throw new RuntimeException("OTP expired");
        }

        if (!storedOtp.equals(request.otp())) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        redisTemplate.delete("otp:" + request.email());

        return "Password reset successful";
    }
}
