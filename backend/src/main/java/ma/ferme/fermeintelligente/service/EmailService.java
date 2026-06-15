package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@ferme-intelligente.ma}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Sends the temporary password to a newly created user.
     * Sent asynchronously so it doesn't block the HTTP response.
     */
    @Async
    public void sendTempPasswordEmail(String toEmail, String prenom, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🌾 Ferme Intelligente — Votre accès temporaire");

            String html = """
                    <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:560px;margin:0 auto;background:#FBF8F3;border-radius:12px;overflow:hidden;border:1px solid #E8E0D4">
                      <div style="background:linear-gradient(135deg,#8BA888,#6B9E6A);padding:32px;text-align:center">
                        <h1 style="color:white;margin:0;font-size:24px;font-weight:700">🌾 Ferme Intelligente</h1>
                        <p style="color:rgba(255,255,255,0.85);margin:8px 0 0">Plateforme de gestion agricole intelligente</p>
                      </div>
                      <div style="padding:32px">
                        <h2 style="color:#3D2E1F;font-size:20px;margin:0 0 16px">Bienvenue, %s !</h2>
                        <p style="color:#5C5047;line-height:1.6;margin:0 0 24px">
                          Votre compte a été créé. Utilisez le mot de passe temporaire ci-dessous
                          pour vous connecter, puis changez-le immédiatement.
                        </p>
                        <div style="background:white;border:1px solid #E8E0D4;border-radius:10px;padding:20px;text-align:center;margin:0 0 24px">
                          <p style="color:#8B7355;font-size:12px;margin:0 0 8px;text-transform:uppercase;letter-spacing:1px">Mot de passe temporaire</p>
                          <code style="font-size:28px;font-weight:700;color:#3D2E1F;letter-spacing:6px;font-family:monospace">%s</code>
                        </div>
                        <a href="%s/login" style="display:block;background:#8BA888;color:white;text-decoration:none;text-align:center;padding:14px 24px;border-radius:8px;font-weight:600;font-size:15px;margin:0 0 24px">
                          Se connecter →
                        </a>
                        <p style="color:#8B7355;font-size:13px;line-height:1.5;margin:0;border-top:1px solid #E8E0D4;padding-top:16px">
                          ⚠️ Ce mot de passe est à usage unique. Vous serez invité(e) à en définir un nouveau lors de votre première connexion.<br>
                          Si vous n'êtes pas à l'origine de cette demande, ignorez cet e-mail.
                        </p>
                      </div>
                    </div>
                    """.formatted(prenom, tempPassword, frontendUrl);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Temp password email sent to {}", toEmail);

        } catch (MessagingException e) {
            // Non-fatal: log but don't crash the user-creation flow
            log.error("Failed to send temp password email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Sends a password-changed confirmation email.
     */
    @Async
    public void sendPasswordChangedEmail(String toEmail, String prenom) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔒 Ferme Intelligente — Mot de passe modifié");

            String html = """
                    <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:560px;margin:0 auto;background:#FBF8F3;border-radius:12px;overflow:hidden;border:1px solid #E8E0D4">
                      <div style="background:linear-gradient(135deg,#8BA888,#6B9E6A);padding:32px;text-align:center">
                        <h1 style="color:white;margin:0;font-size:24px;font-weight:700">🌾 Ferme Intelligente</h1>
                      </div>
                      <div style="padding:32px">
                        <h2 style="color:#3D2E1F;font-size:20px;margin:0 0 16px">Mot de passe modifié</h2>
                        <p style="color:#5C5047;line-height:1.6;margin:0 0 16px">
                          Bonjour %s, votre mot de passe a bien été modifié.
                        </p>
                        <p style="color:#8B7355;font-size:13px">
                          Si vous n'êtes pas à l'origine de ce changement, contactez votre administrateur immédiatement.
                        </p>
                      </div>
                    </div>
                    """.formatted(prenom);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Failed to send password-changed email to {}: {}", toEmail, e.getMessage());
        }
    }
}
