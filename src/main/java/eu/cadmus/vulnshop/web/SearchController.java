package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.repo.ProductRepository;
import eu.cadmus.vulnshop.service.AuditService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ============================================================================
 *  A05:2025 - INJECTION (cross-site scripting)
 * ============================================================================
 *
 * XSS sits inside the Injection category: untrusted data injected into a
 * browser context. High frequency, lower impact than SQL injection - the 2025
 * category description makes exactly that contrast.
 *
 * The weakness here is one character: th:utext instead of th:text in
 * templates/search.html. Thymeleaf escapes by default (secure by default),
 * and this template opts out of that protection.
 */
@Controller
public class SearchController {

    private final ProductRepository products;
    private final AuditService audit;

    public SearchController(ProductRepository products, AuditService audit) {
        this.products = products;
        this.audit = audit;
    }

    /**
     * A05 - REFLECTED XSS. The raw query is placed into the model and rendered
     * unescaped by the template.
     * A09 - and it is concatenated straight into a log line (log injection).
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "") String q, Model model) {
        audit.searchPerformed(q);
        model.addAttribute("query", q);
        model.addAttribute("results", products.findAll());
        return "search";
    }
}
