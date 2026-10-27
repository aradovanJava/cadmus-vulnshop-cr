package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.domain.OrderEntity;
import eu.cadmus.vulnshop.repo.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ============================================================================
 *  A01:2025 - BROKEN ACCESS CONTROL   (excerpt A in learning node N1)
 * ============================================================================
 *
 * A01 has ranked first in every edition of the OWASP Top 10 since 2017, and it
 * is simultaneously the category automated tooling is worst at detecting.
 * Nothing in getOrder() looks anomalous to a scanner: findById() on a path
 * variable is ordinary, correct Spring code. The tool has no way to know that
 * an ownership relation was intended.
 *
 * Note the two contrasting methods in this one class. myOrders() is written
 * correctly, by a different developer, later. That is realistic: insecure code
 * almost always sits next to secure code.
 *
 * The ticket said: "support agents must be able to open any order by id".
 * The code says: "anyone with a session can". Nobody named a role, so the
 * DESIGN flaw (A06) sits underneath the IMPLEMENTATION flaw (A01).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orders;

    public OrderController(OrderRepository orders) {
        this.orders = orders;
    }

    /**
     * A01 - BOLA / IDOR. No ownership predicate, no role check, no tenant scope.
     * Order ids are sequential from 1000, so this is an enumerable export of
     * every order in the system.
     *
     * A01 also - the JPA entity is returned directly, so the response contract is
     * whatever the schema happens to contain (including shippingAddress).
     */
    @GetMapping("/{id}")
    public OrderEntity getOrder(@PathVariable Long id) {
        return orders.findById(id).orElseThrow();
    }

    /**
     * The SECURE counterpart, kept deliberately for contrast in N1 and CR2.
     * The caller is a parameter of the query, so this method cannot be called
     * without scoping the result to somebody.
     */
    @GetMapping("/mine")
    public List<OrderEntity> myOrders(HttpSession session) {
        String caller = (String) session.getAttribute("username");
        if (caller == null) {
            throw new IllegalStateException("not authenticated");
        }
        return orders.findByCustomerUsername(caller);
    }

    /**
     * A01 - MASS ASSIGNMENT / BFLA. Status is a workflow field, and any caller
     * can move any order to any state - including marking an unpaid order SHIPPED.
     * CSRF is disabled globally (see SecurityConfig), so this is also forgeable.
     */
    @PostMapping("/{id}/status")
    public OrderEntity updateStatus(@PathVariable Long id, @RequestParam String status) {
        OrderEntity order = orders.findById(id).orElseThrow();
        order.setStatus(status);
        return orders.save(order);
    }
}
