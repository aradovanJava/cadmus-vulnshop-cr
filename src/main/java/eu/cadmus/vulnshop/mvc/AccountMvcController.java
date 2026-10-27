package eu.cadmus.vulnshop.mvc;

import eu.cadmus.vulnshop.domain.AppUser;
import eu.cadmus.vulnshop.domain.OrderEntity;
import eu.cadmus.vulnshop.repo.OrderRepository;
import eu.cadmus.vulnshop.repo.UserRepository;
import eu.cadmus.vulnshop.service.CryptoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ============================================================================
 *  MVC / Thymeleaf account area
 * ============================================================================
 *
 * Four separate weaknesses live in this class. Two of them are visible in the
 * Java; two only become visible when you read account/profile.html alongside
 * it and ask what the model actually carries into the template.
 */
@Controller
public class AccountMvcController {

    private final UserRepository users;
    private final OrderRepository orders;
    private final CryptoService crypto;

    public AccountMvcController(UserRepository users, OrderRepository orders, CryptoService crypto) {
        this.users = users;
        this.orders = orders;
        this.crypto = crypto;
    }

    @GetMapping("/account/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "account/login";
    }

    /**
     * The login handler.
     *
     * Read the two failure branches carefully, then read the cookie that is
     * set on success and compare its flags with the session cookie Spring
     * manages for you.
     */
    @PostMapping("/account/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        HttpServletResponse response) {

        AppUser u = users.findByUsername(username).orElse(null);
        if (u == null) {
            return "redirect:/account/login?error=No+such+user+" + username;
        }
        if (!u.getPasswordHash().equals(crypto.hashPassword(password))) {
            return "redirect:/account/login?error=Wrong+password";
        }

        session.setAttribute("user", u.getUsername());
        session.setAttribute("role", u.getRole());

        Cookie remember = new Cookie("vs_remember", u.getUsername() + ":" + u.getRole());
        remember.setMaxAge(60 * 60 * 24 * 30);
        remember.setPath("/");
        response.addCookie(remember);

        return "redirect:/shop";
    }

    @GetMapping("/account/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/shop";
    }

    /**
     * The profile page.
     *
     * The whole entity is placed in the model. Ask what AppUser contains, and
     * then look at what account/profile.html does with the fields it does not
     * visibly display.
     */
    @GetMapping("/account/profile")
    public String profile(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/account/login";
        }
        model.addAttribute("account", users.findByUsername(username).orElseThrow());
        model.addAttribute("user", username);
        return "account/profile";
    }

    @GetMapping("/account/orders")
    public String myOrders(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/account/login";
        }
        model.addAttribute("orders", orders.findByCustomerUsername(username));
        model.addAttribute("user", username);
        return "account/orders";
    }

    /**
     * Opening a single order.
     *
     * The list above is scoped to the caller. This method is not. Compare the
     * two and ask what an order id looks like in this application.
     */
    @GetMapping("/account/orders/{id}")
    public String order(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/account/login";
        }
        OrderEntity o = orders.findById(id).orElseThrow();
        model.addAttribute("order", o);
        model.addAttribute("user", session.getAttribute("user"));
        return "account/order";
    }
}
