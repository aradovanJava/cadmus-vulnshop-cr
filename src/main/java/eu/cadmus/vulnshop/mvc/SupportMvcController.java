package eu.cadmus.vulnshop.mvc;

import eu.cadmus.vulnshop.service.AuditService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * ============================================================================
 *  MVC / Thymeleaf support desk
 * ============================================================================
 *
 * Three weaknesses, in three different categories, in forty lines. One is
 * reported by static analysis at high severity, one is reported at low
 * severity, and one is not reported at all.
 *
 * Deciding which is which is the exercise.
 */
@Controller
public class SupportMvcController {

    private static final Path ATTACHMENTS = Path.of("attachments");

    private final AuditService audit;

    public SupportMvcController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping("/support/ticket")
    public String ticketForm(HttpSession session, Model model) {
        model.addAttribute("user", session.getAttribute("user"));
        return "support/ticket";
    }

    /**
     * The confirmation page echoes the subject back to the reporter so they
     * can check it was received correctly. Look at how support/ticket.html
     * renders the "submitted" attribute.
     *
     * The audit line is a second, separate finding.
     */
    @PostMapping("/support/ticket")
    public String submitTicket(@RequestParam String subject,
                               @RequestParam String body,
                               HttpSession session,
                               Model model) {

        audit.searchPerformed(subject);

        model.addAttribute("submitted", subject);
        model.addAttribute("user", session.getAttribute("user"));
        return "support/ticket";
    }

    /**
     * Customers attach screenshots to tickets; support staff download them
     * here. The file name arrives from a link on the ticket page.
     */
    @GetMapping("/support/attachment")
    public void attachment(@RequestParam String file,
                           HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        try (InputStream in = new FileInputStream(ATTACHMENTS.resolve(file).toString())) {
            in.transferTo(response.getOutputStream());
        }
    }
}
