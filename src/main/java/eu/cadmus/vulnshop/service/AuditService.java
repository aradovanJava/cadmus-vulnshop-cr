package eu.cadmus.vulnshop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 *  A09:2025 - SECURITY LOGGING AND ALERTING FAILURES
 * ============================================================================
 *
 * Note the 2025 rename: "alerting", not "monitoring". The authors' argument is
 * that excellent logging with no alerting has minimal value for identifying an
 * incident. This class is a compact demonstration of all four failure modes:
 *
 *   1. Security-relevant events that are never logged at all.
 *   2. Sensitive data written INTO the log when something is logged.
 *   3. Unsanitised user input concatenated into a log line (log injection -
 *      an attacker can forge log entries with a newline).
 *   4. Nothing, anywhere, raises an alert.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    /**
     * A09 - failed authentication is the single most important event to record,
     * and here it is deliberately not recorded. Brute force is invisible.
     */
    public void loginFailed(String username, String sourceIp) {
        // intentionally empty - "the log was too noisy"
    }

    /**
     * A09 - when something IS logged, the credential goes into the log with it,
     * moving the secret into a system with weaker access controls than the
     * database it came from.
     */
    public void loginSucceeded(String username, String password) {
        log.info("login ok user=" + username + " pw=" + password);
    }

    /**
     * A09 - LOG INJECTION. The value is concatenated straight into the line, so
     * a newline in the input lets an attacker write arbitrary forged log records
     * and destroy the integrity of the audit trail during an investigation.
     */
    public void searchPerformed(String rawQuery) {
        log.info("search query=" + rawQuery);
    }

    /**
     * A09 - a genuinely alertable event is emitted at DEBUG and no alert rule
     * exists. Detection depends on somebody happening to look.
     */
    public void privilegedActionAttempted(String username, String action) {
        log.debug("privileged action user={} action={}", username, action);
    }
}
