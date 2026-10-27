package eu.cadmus.vulnshop.mvc;

import eu.cadmus.vulnshop.domain.OrderEntity;
import eu.cadmus.vulnshop.domain.Product;
import eu.cadmus.vulnshop.repo.OrderRepository;
import eu.cadmus.vulnshop.repo.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  MVC / Thymeleaf cart and checkout
 * ============================================================================
 *
 * Every line in this class is correct with respect to the specification it was
 * written from. That is exactly what makes the weakness here worth finding:
 * no scanner has a model of what a price means, so nothing reports it.
 *
 * Read cart/cart.html together with this class. Which values does the server
 * decide, and which does it accept?
 */
@Controller
public class CartMvcController {

    private final ProductRepository products;
    private final OrderRepository orders;

    public CartMvcController(ProductRepository products, OrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    @GetMapping("/cart")
    public String cart(@RequestParam Long productId, HttpSession session, Model model) {
        model.addAttribute("product", products.findById(productId).orElseThrow());
        model.addAttribute("user", session.getAttribute("user"));
        return "cart/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam Long productId,
                           @RequestParam String productName,
                           @RequestParam int quantity,
                           @RequestParam BigDecimal unitPrice,
                           @RequestParam String shippingAddress,
                           HttpSession session,
                           Model model) {

        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/account/login";
        }

        OrderEntity order = new OrderEntity();
        order.setCustomerUsername(username);
        order.setProductName(productName);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        order.setShippingAddress(shippingAddress);
        order.setStatus("PLACED");

        OrderEntity saved = orders.save(order);
        model.addAttribute("order", saved);
        model.addAttribute("user", username);
        return "cart/confirm";
    }
}
