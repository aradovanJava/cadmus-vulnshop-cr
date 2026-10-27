package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.domain.AppUser;
import eu.cadmus.vulnshop.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================================
 *  A01:2025 - BROKEN ACCESS CONTROL  (property level)
 * ============================================================================
 *
 * The profile update endpoint, posted to by account/profile.html.
 *
 * The template carries the whole record into the browser as hidden fields so
 * that the form can post it back "unchanged", and this handler writes back
 * whatever it receives. Nothing here validates that the fields the user was
 * not supposed to edit still hold the values that were sent to them.
 *
 * This is the third of the three access-control questions from N3:
 *
 *     1. Can this caller use this operation?      (function level)
 *     2. Can this caller reach this object?       (object level)
 *     3. Can this caller SEE and SET these fields? (property level)  <-- here
 *
 * Note that no static analysis rule fires on this method. Every line is
 * ordinary, correct Spring MVC. The weakness is that the specification never
 * said which fields a customer may change - so the implementation permitted
 * all of them.
 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserRepository users;

    public UserApiController(UserRepository users) {
        this.users = users;
    }

    @PostMapping("/update")
    public String update(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String role,
                         @RequestParam String tenant,
                         @RequestParam Integer riskScore,
                         @RequestParam String pwHash,
                         HttpSession session) {

        String caller = (String) session.getAttribute("user");
        if (caller == null) {
            return "not logged in";
        }

        AppUser u = users.findByUsername(username).orElseThrow();
        u.setEmail(email);
        u.setRole(role);
        u.setTenant(tenant);
        u.setInternalRiskScore(riskScore);
        u.setPasswordHash(pwHash);
        users.save(u);

        return "saved";
    }
}
