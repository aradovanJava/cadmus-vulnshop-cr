package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.domain.AppUser;
import eu.cadmus.vulnshop.repo.UserRepository;
import eu.cadmus.vulnshop.service.AuditService;
import eu.cadmus.vulnshop.service.AuthorizationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A01:2025 - BROKEN ACCESS CONTROL (function level - BFLA)
 * ============================================================================
 *
 * The 2025 category description explicitly calls out BOLA and BFLA - object
 * level and function level authorisation failures - as the most exploited
 * patterns in API-heavy applications.
 *
 * Nothing here is protected by the security filter chain, because SecurityConfig
 * permits every request. What protection exists is ad hoc, inconsistent, and in
 * one case delegated to a method that fails open (A10).
 */
@RestController
@RequestMapping("/api/admin")   // moved under /api in CR1: the MVC tier owns /admin
public class AdminController {

    private final UserRepository users;
    private final AuthorizationService authz;
    private final AuditService audit;

    public AdminController(UserRepository users, AuthorizationService authz, AuditService audit) {
        this.users = users;
        this.authz = authz;
        this.audit = audit;
    }

    /**
     * A01 - completely unauthenticated. Returns every user entity, including the
     * MD5 password hashes, the reset codes and the internal risk score, because
     * the entity is serialised directly.
     */
    @GetMapping("/users")
    public List<AppUser> allUsers() {
        return users.findAll();
    }

    /**
     * A01 + A10 - "protected", but by a check that returns true on any exception
     * and that decides administrator status from a username prefix.
     * Authorisation by string matching is not authorisation.
     */
    @PostMapping("/promote")
    public Map<String, Object> promote(@RequestParam String targetUser, HttpSession session) {
        String caller = (String) session.getAttribute("username");
        audit.privilegedActionAttempted(caller, "promote:" + targetUser);

        if (!authz.isAdmin(caller)) {
            return Map.of("ok", false, "error", "forbidden");
        }
        AppUser user = users.findByUsername(targetUser).orElseThrow();
        user.setRole("ADMIN");
        users.save(user);
        return Map.of("ok", true, "user", targetUser, "role", "ADMIN");
    }

    /**
     * A01 - authorisation by URL prefix only. Anything that reaches this handler
     * by a path the matcher did not anticipate (encoding, trailing slash, a
     * proxy rewrite) is unprotected. Deny-by-default at the filter chain would
     * make this class of bypass impossible.
     */
    @PostMapping("/delete-user")
    public Map<String, Object> deleteUser(@RequestParam Long id) {
        users.deleteById(id);
        return Map.of("ok", true, "deleted", id);
    }
}
