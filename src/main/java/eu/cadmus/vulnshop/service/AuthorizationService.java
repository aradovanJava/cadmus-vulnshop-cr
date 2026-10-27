package eu.cadmus.vulnshop.service;

import eu.cadmus.vulnshop.repo.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 *  A10:2025 - MISHANDLING OF EXCEPTIONAL CONDITIONS   (excerpt D in node N1)
 * ============================================================================
 *
 * The history of this method matters more than the code:
 *
 *   - It was written after a real incident, when a downstream lookup timed out
 *     and customers were locked out of their own orders.
 *   - It was reviewed and approved by two engineers.
 *   - It resolved the incident. It has never been reverted.
 *
 * Nobody set out to build an authorisation bypass. Availability was chosen over
 * authorisation inside a catch block, and that trade-off was never stated,
 * recorded or approved by anyone who would have objected.
 *
 * The attack is simply: cause an exception. Any exception.
 */
@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final OrderRepository orders;

    public AuthorizationService(OrderRepository orders) {
        this.orders = orders;
    }

    public boolean canAccessOrder(String username, Long orderId) {
        try {
            var order = orders.findById(orderId).orElseThrow();
            return order.getCustomerUsername() != null
                    && order.getCustomerUsername().equals(username);
        } catch (Exception e) {
            // A10 - catch (Exception) captures a timeout, a missing row, a null and a
            //       bug alike, and converts every one of them into "yes".
            // A09 - logged at WARN and nothing alerts on it, so the bypass is silent.
            log.warn("acl check failed, allowing: " + username);
            return true;   // don't block the user on a glitch
        }
    }

    /**
     * A10 - the same anti-pattern in its most compact form. A "defensive"
     * default that defaults to permit.
     */
    public boolean isAdmin(String username) {
        try {
            return username != null && username.startsWith("admin");
        } catch (Exception e) {
            return true;
        }
    }
}
