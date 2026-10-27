package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.domain.AppUser;
import eu.cadmus.vulnshop.repo.UserRepository;
import eu.cadmus.vulnshop.service.CryptoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ============================================================================
 *  A06:2025 - INSECURE DESIGN
 * ============================================================================
 *
 * This is the excerpt to use when someone asks what a design flaw actually
 * looks like. Read the code carefully: every line is correct. The comparison
 * works, the lookup works, the update works. There is no bug here.
 *
 * The flaw is the SPECIFICATION. Somebody decided that a four digit code, with
 * no expiry and no attempt limit, was an acceptable proof of identity. A code
 * review looking for defects finds nothing, because there is no defect - and
 * that is precisely why A06 exists as its own category.
 *
 * A06 slid from #4 to #6 in the 2025 edition. The OWASP commentary attributes
 * that to genuine industry improvement in threat modelling, which is worth
 * saying out loud: this is the one category that moved because things got better.
 */
@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final UserRepository users;
    private final CryptoService crypto;

    public PasswordResetController(UserRepository users, CryptoService crypto) {
        this.users = users;
        this.crypto = crypto;
    }

    /**
     * A06 - 10,000 possible codes, generated from java.util.Random (A04),
     * with no expiry. The response also confirms whether the account exists (A07).
     */
    @PostMapping("/request-reset")
    public Map<String, Object> requestReset(@RequestParam String username) {
        AppUser user = users.findByUsername(username).orElse(null);
        if (user == null) {
            return Map.of("ok", false, "error", "unknown account");
        }
        user.setResetCode(crypto.newResetCode());
        users.save(user);
        // in the training build the code is returned directly instead of emailed
        return Map.of("ok", true, "codeSentTo", user.getEmail(), "code", user.getResetCode());
    }

    /**
     * A06 - NO ATTEMPT LIMIT. The comparison itself is implemented correctly;
     * the design permits unlimited guesses against a 4-digit secret. There is
     * also no rate limit, no backoff, no CAPTCHA and no notification to the
     * account owner that a reset was attempted (A09).
     *
     * A10 - the code is not cleared after a failed attempt or after success,
     *       so it remains valid indefinitely.
     */
    @PostMapping("/confirm-reset")
    public Map<String, Object> confirmReset(@RequestParam String username,
                                            @RequestParam String code,
                                            @RequestParam String newPassword) {
        AppUser user = users.findByUsername(username).orElseThrow();
        if (!code.equals(user.getResetCode())) {
            return Map.of("ok", false, "error", "invalid code");
        }
        user.setPasswordHash(crypto.hashPassword(newPassword));
        users.save(user);
        return Map.of("ok", true);
    }
}
