package com.example.backend.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Gmail SMTP로 비밀번호 재설정 인증코드를 실제로 발송함 (disastermonitor 프로젝트의 EmailService를 참고해 이식).
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code, String purposeLabel) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[냉장고 앱] " + purposeLabel + " 인증코드");
        message.setText(
                "요청하신 인증코드는 다음과 같습니다.\n\n" +
                code + "\n\n" +
                "이 코드는 발급 후 10분간 유효합니다.\n" +
                "본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다."
        );
        mailSender.send(message);
    }
}
