package com.example.phonehub.service;

import com.example.phonehub.dto.ChangeEmailRequest;
import com.example.phonehub.entity.EmailVerificationToken;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.EmailVerificationTokenRepository;
import com.example.phonehub.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

  @Autowired
  private EmailVerificationTokenRepository tokenRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired(required = false)
  private JavaMailSender mailSender;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  private static final int TOKEN_EXPIRY_HOURS = 24;

  @Transactional
  public void createEmailVerificationToken(ChangeEmailRequest request) {
    Integer uid = Integer.valueOf(request.getUserId());
    User user = userRepository.findById(uid)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!request.getCurrentEmail().equalsIgnoreCase(user.getEmail())) {
      throw new IllegalArgumentException("Current email does not match");
    }

    // Kiểm tra email mới đã được sử dụng chưa
    if (userRepository.existsByEmail(request.getNewEmail().trim())) {
      throw new IllegalArgumentException("Email mới đã được sử dụng bởi tài khoản khác");
    }

    String token = UUID.randomUUID().toString();
    EmailVerificationToken record = new EmailVerificationToken();
    record.setUserId(uid);
    record.setCurrentEmail(request.getCurrentEmail());
    record.setNewEmail(request.getNewEmail());
    record.setToken(token);
    record.setUsed(false);
    record.setExpiredAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
    tokenRepository.save(record);

    // Gửi email xác minh tới EMAIL HIỆN TẠI kèm thông tin tài khoản
    sendVerificationEmail(
        request.getCurrentEmail(),
        token,
        user.getUsername(),
        request.getCurrentEmail(),
        request.getNewEmail());
  }

  @Transactional
  public void verifyEmailToken(String token) {
    EmailVerificationToken record = tokenRepository.findByToken(token)
        .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

    if (record.isUsed()) {
      throw new IllegalArgumentException("Invalid or expired token");
    }
    if (record.getExpiredAt() != null && record.getExpiredAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Invalid or expired token");
    }

    User user = userRepository.findById(record.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setEmail(record.getNewEmail());
    userRepository.save(user);

    record.setUsed(true);
    tokenRepository.save(record);

    // Sau khi đổi thành công: gửi thông báo đến EMAIL HIỆN TẠI, kèm thông tin tài
    // khoản
    if (record.getCurrentEmail() != null) {
      sendConfirmChangedEmail(
          record.getCurrentEmail(),
          user.getUsername(),
          record.getCurrentEmail(),
          record.getNewEmail());
    }
    // Đồng thời gửi thông báo đến EMAIL MỚI
    if (record.getNewEmail() != null) {
      sendNotifyNewEmail(
          record.getNewEmail(),
          user.getUsername(),
          record.getCurrentEmail(),
          record.getNewEmail());
    }
  }

  public void sendVerificationEmail(String toEmail, String token, String username, String currentEmail,
      String newEmail) {
    if (mailSender == null)
      return;
    String verifyUrl = frontendUrl + "/account/verify-email-change?token=" + token;
    String html = buildVerificationEmailHtml(verifyUrl, username, currentEmail, newEmail);
    sendHtml(toEmail, "Xác minh đổi email", html);
  }

  private void sendConfirmChangedEmail(String toEmail, String username, String currentEmail, String newEmail) {
    if (mailSender == null)
      return;
    String html = buildConfirmEmailHtml(username, currentEmail, newEmail);
    sendHtml(toEmail, "Đã đổi email thành công", html);
  }

  private String buildVerificationEmailHtml(String verifyUrl, String username, String currentEmail, String newEmail) {
    return """
        <!DOCTYPE html>
          <html lang="vi">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0" />
              <title>Xác minh đổi email</title>
              <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-gradient-to-b from-orange-50 to-rose-50 font-sans">
              <div class="min-h-screen flex items-center justify-center py-10">
                <div class="bg-white shadow-xl rounded-2xl overflow-hidden w-full max-w-xl border border-orange-100">
                  <!-- Header -->
                  <div class="bg-gradient-to-r from-amber-400 via-orange-500 to-rose-500 text-center py-8">
                    <h1 class="text-white text-2xl font-semibold tracking-wide drop-shadow-sm">
                      ✉️ Xác minh đổi email
                    </h1>
                  </div>

                  <!-- Content -->
                  <div class="p-8">
                    <!-- Account Info -->
                    <div class="bg-orange-50 rounded-md p-5 mb-5 border border-orange-100">
                      <div class="mb-3">
                        <p class="text-sm text-gray-500 mb-1">Tài khoản</p>
                        <p class="text-gray-800 font-semibold text-base">%s</p>
                      </div>
                      <div class="mb-3">
                        <p class="text-sm text-gray-500 mb-1">Email hiện tại</p>
                        <p class="text-gray-700 text-base">%s</p>
                      </div>
                      <div>
                        <p class="text-sm text-gray-500 mb-1">Email mới</p>
                        <p class="text-orange-600 text-base font-semibold">%s</p>
                      </div>
                    </div>

                    <!-- Action -->
                    <p class="text-gray-600 text-sm leading-relaxed mb-4">
                      Để xác nhận bạn là chủ sở hữu tài khoản và đồng ý đổi email, vui lòng nhấn nút bên dưới:
                    </p>

                    <div class="text-center my-6">
                      <a
                        href="%s"
                        class="inline-block bg-gradient-to-r from-orange-500 to-rose-500 hover:from-orange-600 hover:to-rose-600 text-white font-semibold px-8 py-3 rounded-lg text-base shadow-md transition-all duration-200"
                      >
                        Xác minh đổi email
                      </a>
                    </div>

                    <!-- Backup link -->
                    <p class="text-xs text-gray-500">
                      Nếu nút không hoạt động, sao chép liên kết sau:
                      <span class="text-orange-600 break-words font-medium">%s</span>
                    </p>

                    <hr class="my-6 border-gray-200" />

                    <p class="text-xs text-gray-400">
                      Liên kết sẽ hết hạn sau <span class="text-rose-500 font-medium">24 giờ</span>.
                      Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.
                    </p>
                  </div>
                </div>
              </div>
            </body>
          </html>

        """
        .formatted(username, currentEmail, newEmail, verifyUrl, verifyUrl);
  }

  private String buildConfirmEmailHtml(String username, String currentEmail, String newEmail) {
    return """
                  <!DOCTYPE html>
        <html lang="vi">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Đổi email thành công</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif">
          <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="padding:40px 0;">
            <tr>
              <td align="center">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="background-color:#ffffff;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.08);overflow:hidden;">

                  <!-- Header -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#34d399 0%,#059669 100%);padding:36px 24px;text-align:center;">
                      <img src="https://cdn-icons-png.flaticon.com/512/845/845646.png" width="64" height="64" alt="Success" style="margin-bottom:12px;" />
                      <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:600;">Đổi email thành công 🎉</h1>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:32px;">
                      <p style="margin:0 0 20px;color:#374151;font-size:15px;">Xin chào, quá trình thay đổi email cho tài khoản của bạn đã được thực hiện thành công. Dưới đây là thông tin chi tiết:</p>

                      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f9fafb;border-radius:8px;padding:16px;">
                        <tr><td style="padding:8px 0;">
                          <p style="margin:0 0 4px;color:#6b7280;font-size:14px;">Tài khoản</p>
                          <p style="margin:0;color:#111827;font-size:16px;font-weight:600;">%s</p>
                        </td></tr>
                        <tr><td style="padding:8px 0;">
                          <p style="margin:0 0 4px;color:#6b7280;font-size:14px;">Email cũ</p>
                          <p style="margin:0;color:#1f2937;font-size:15px;">%s</p>
                        </td></tr>
                        <tr><td style="padding:8px 0;">
                          <p style="margin:0 0 4px;color:#6b7280;font-size:14px;">Email mới</p>
                          <p style="margin:0;color:#2563eb;font-size:15px;font-weight:600;">%s</p>
                        </td></tr>
                      </table>

                      <p style="margin:24px 0 0;color:#9ca3af;font-size:13px;text-align:center;">Đây là email tự động, vui lòng không trả lời.</p>
                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="background-color:#f3f4f6;text-align:center;padding:16px;">
                      <p style="margin:0;color:#9ca3af;font-size:12px;">© 2025 Tên công ty của bạn. Mọi quyền được bảo lưu.</p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>

                    """
        .formatted(username, currentEmail, newEmail);
  }

  private void sendNotifyNewEmail(String toEmail, String username, String oldEmail, String newEmail) {
    if (mailSender == null)
      return;
    String html = buildNotifyNewEmailHtml(username, oldEmail, newEmail);
    sendHtml(toEmail, "Tài khoản của bạn đã được thêm vào PhoneHub", html);
  }

  private String buildNotifyNewEmailHtml(String username, String oldEmail, String newEmail) {
    return """
        <!DOCTYPE html>
        <html lang=\"vi\">
        <head>
          <meta charset=\"UTF-8\">
          <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
          <title>Chào mừng đến với PhoneHub</title>
        </head>
        <body style=\"margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background-color:#f5f5f5\">
          <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color:#f5f5f5;padding:40px 0\">
            <tr>
              <td align=\"center\">
                <table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color:#ffffff;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);overflow:hidden\">
                  <tr>
                    <td style=\"background:linear-gradient(135deg,#3b82f6 0%%,#2563eb 100%%);padding:32px;text-align:center\">
                      <h1 style=\"margin:0;color:#ffffff;font-size:24px;font-weight:600\">🎉 Email đã được thêm vào PhoneHub</h1>
                    </td>
                  </tr>
                  <tr>
                    <td style=\"padding:28px 32px\">
                      <p style=\"margin:0 0 12px;color:#333;font-size:16px\">Xin chào,</p>
                      <p style=\"margin:0 0 16px;color:#666;font-size:14px;line-height:1.6\">Email này vừa được gán làm địa chỉ liên hệ cho tài khoản trên PhoneHub.</p>
                      <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color:#f9fafb;border-radius:6px;padding:16px;margin:0 0 16px\">
                        <tr><td>
                          <p style=\"margin:0 0 6px;color:#666;font-size:14px\">Tài khoản</p>
                          <p style=\"margin:0;color:#111;font-size:16px;font-weight:600\">%s</p>
                        </td></tr>
                        <tr><td style=\"padding-top:12px\">
                          <p style=\"margin:0 0 6px;color:#666;font-size:14px\">Email cũ</p>
                          <p style=\"margin:0;color:#333;font-size:15px\">%s</p>
                        </td></tr>
                        <tr><td style=\"padding-top:12px\">
                          <p style=\"margin:0 0 6px;color:#666;font-size:14px\">Email này (mới)</p>
                          <p style=\"margin:0;color:#2563eb;font-size:15px;font-weight:600\">%s</p>
                        </td></tr>
                      </table>
                      <p style=\"margin:12px 0 0;color:#999;font-size:12px\">Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ hỗ trợ ngay.</p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """
        .formatted(username, oldEmail, newEmail);
  }

  private void sendHtml(String to, String subject, String html) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
          StandardCharsets.UTF_8.name());
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(html, true);
      mailSender.send(message);
    } catch (MessagingException ignored) {
    }
  }
}
