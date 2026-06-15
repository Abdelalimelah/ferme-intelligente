package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${spring.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost}")
    private String frontendUrl;

    @Async
    public void sendTempPasswordEmail(String toEmail, String prenom, String tempPassword) {
        if (resendApiKey.isBlank()) {
            log.warn("[EmailService] RESEND_API_KEY not configured — skipping email to {}", toEmail);
            return;
        }
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
                      ⚠️ Ce mot de passe est à usage unique. Vous serez invité(e) à en définir un nouveau lors de votre première connexion.
                    </p>
                  </div>
                </div>
                """.formatted(prenom, tempPassword, frontendUrl);

        send(toEmail, "🌾 Ferme Intelligente — Votre accès temporaire", html);
    }

    @Async
    public void sendPasswordChangedEmail(String toEmail, String prenom) {
        if (resendApiKey.isBlank()) return;
        String html = """
                <div style="font-family:'Segoe UI',Arial,sans-serif;max-width:560px;margin:0 auto;background:#FBF8F3;border-radius:12px;overflow:hidden;border:1px solid #E8E0D4">
                  <div style="background:linear-gradient(135deg,#8BA888,#6B9E6A);padding:32px;text-align:center">
                    <h1 style="color:white;margin:0;font-size:24px;font-weight:700">🌾 Ferme Intelligente</h1>
                  </div>
                  <div style="padding:32px">
                    <h2 style="color:#3D2E1F;font-size:20px;margin:0 0 16px">Mot de passe modifié</h2>
                    <p style="color:#5C5047;line-height:1.6">
                      Bonjour %s, votre mot de passe a bien été modifié.
                    </p>
                  </div>
                </div>
                """.formatted(prenom);

        send(toEmail, "🔒 Ferme Intelligente — Mot de passe modifié", html);
    }

    private void send(String to, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "html", html
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.resend.com/emails",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[EmailService] Email sent to {}", to);
            } else {
                log.error("[EmailService] Resend returned {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[EmailService] Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
