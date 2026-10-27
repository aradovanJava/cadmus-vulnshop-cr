package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.domain.AppUser;
import eu.cadmus.vulnshop.repo.UserRepository;
import eu.cadmus.vulnshop.service.AuditService;
import eu.cadmus.vulnshop.service.CryptoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ============================================================================
 *  A07:2025 - AUTHENTICATION FAILURES
 * ============================================================================
 *
 * Renamed in the 2025 edition from "Identification and Authentication Failures".
 * 36 CWEs. This controller demonstrates six of the classic ones:
 *
 *   1. Credentials accepted in a GET query string (logged, cached, in Referer).
 *   2. Username enumeration through distinguishable error messages.
 *   3. No lockout, no rate limit, no backoff - unlimited guessing.
 *   4. Session id never rotated after authentication (session fixation).
 *   5. Session cookie mirrored into a non-HttpOnly, non-Secure cookie.
 *   6. Failed authentication never recorded (A09).
 */
@RestController
public class AuthController {

    private final UserRepository users;
    private final CryptoService crypto;
    private final AuditService audit;

    public AuthController(UserRepository users, CryptoService crypto, AuditService audit) {
        this.users = users;
        this.crypto = crypto;
        this.audit = audit;
    }

    /**
     * A07 - credentials in the query string of a GET. They will appear in the
     * access log, the browser history, any proxy in the path and the Referer
     * header of the next request.
     */
    @GetMapping("/api/login")
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

        AppUser user = users.findByUsername(username).orElse(null);

        // A07 - USERNAME ENUMERATION. The two failure branches are distinguishable,
        //       so an attacker can confirm which accounts exist before guessing.
        // A09 - and neither branch records the failed attempt.
        if (user == null) {
            audit.loginFailed(username, request.getRemoteAddr());
            return Map.of("ok", false, "error", "No such user: " + username);
        }

        if (!crypto.hashPassword(password).equals(user.getPasswordHash())) {
            audit.loginFailed(username, request.getRemoteAddr());
            // A07 - no attempt counter, no lockout, no delay. Unlimited guesses.
            return Map.of("ok", false, "error", "Wrong password for " + username);
        }

        // A07 - SESSION FIXATION. The pre-authentication session is reused, so a
        //       session id planted before login remains valid after it.
        HttpSession session = request.getSession(true);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        // A06 - an eight hour session with no idle timeout and no re-authentication
        //       for sensitive actions.
        session.setMaxInactiveInterval(8 * 60 * 60);

        // A04 + A07 - the session token is mirrored into a cookie that is neither
        //             HttpOnly nor Secure nor SameSite, so any XSS reads it.
        Cookie legacy = new Cookie("vulnshop_token", crypto.newToken());
        legacy.setHttpOnly(false);
        legacy.setSecure(false);
        legacy.setPath("/");
        response.addCookie(legacy);

        // A09 - the password is written to the application log on success.
        audit.loginSucceeded(user.getUsername(), password);

        return Map.of("ok", true, "user", user.getUsername(), "role", user.getRole());
    }

    /**
     * A07 - registration accepts any password. No length, no complexity, no
     * breached-password check. This is a DESIGN decision (A06), not a bug.
     */
    @PostMapping("/api/register")
    public Map<String, Object> register(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String email) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(crypto.hashPassword(password));
        user.setEmail(email);
        // A01 - the client may not set this, but nothing stops a later refactor
        //       from binding it. Defaulted here rather than derived from policy.
        user.setRole("USER");
        user.setTenant("default");
        users.save(user);
        return Map.of("ok", true, "user", username);
    }

    @PostMapping("/api/logout")
    public Map<String, Object> logout(HttpSession session) {
        // A07 - the mirrored vulnshop_token cookie is never invalidated here,
        //       so it outlives the session it was issued alongside.
        session.invalidate();
        return Map.of("ok", true);
    }
}
