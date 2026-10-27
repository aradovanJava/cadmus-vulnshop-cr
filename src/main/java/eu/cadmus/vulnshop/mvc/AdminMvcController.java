package eu.cadmus.vulnshop.mvc;

import eu.cadmus.vulnshop.domain.Product;
import eu.cadmus.vulnshop.repo.OrderRepository;
import eu.cadmus.vulnshop.repo.ProductRepository;
import eu.cadmus.vulnshop.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ============================================================================
 *  MVC / Thymeleaf administration area
 * ============================================================================
 *
 * The navigation fragment only renders the Admin link when the session role is
 * ADMIN, so the panel is invisible to ordinary users browsing the site.
 *
 * Ask a different question: what happens when somebody types the URL?
 */
@Controller
public class AdminMvcController {

    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orders;

    public AdminMvcController(UserRepository users, ProductRepository products, OrderRepository orders) {
        this.users = users;
        this.products = products;
        this.orders = orders;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("userCount", users.count());
        model.addAttribute("orderCount", orders.count());
        model.addAttribute("productCount", products.count());
        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("role", session.getAttribute("role"));
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String userList(HttpSession session, Model model) {
        model.addAttribute("accounts", users.findAll());
        model.addAttribute("user", session.getAttribute("user"));
        return "admin/users";
    }

    /**
     * Product descriptions are authored here and rendered unescaped on the
     * public product page. Two categories meet at this method: whoever can
     * reach it can persist markup that executes in every visitor's browser.
     */
    @PostMapping("/admin/products/save")
    public String saveProduct(@RequestParam Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              HttpSession session) {
        Product p = products.findById(id).orElseThrow();
        p.setName(name);
        p.setDescription(description);
        products.save(p);
        return "redirect:/admin/dashboard";
    }

    /**
     * Support staff use this to view the site as a customer while resolving a
     * ticket. Ask which checks exist, and what the session looks like
     * afterwards.
     */
    @GetMapping("/admin/impersonate")
    public String impersonate(@RequestParam String username, HttpSession session) {
        session.setAttribute("user", username);
        session.setAttribute("role", "USER");
        return "redirect:/shop";
    }
}
