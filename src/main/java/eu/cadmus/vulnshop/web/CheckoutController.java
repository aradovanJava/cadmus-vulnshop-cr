package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.domain.OrderEntity;
import eu.cadmus.vulnshop.domain.Product;
import eu.cadmus.vulnshop.repo.OrderRepository;
import eu.cadmus.vulnshop.repo.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ============================================================================
 *  A06:2025 - INSECURE DESIGN (business logic)
 * ============================================================================
 *
 * Business logic weaknesses are the purest form of design flaw: the code does
 * exactly what it was asked to do, and what it was asked to do is wrong.
 *
 * No scanner will report this file. There is no dangerous API call, no
 * concatenated query and no unsafe deserialisation. The arithmetic is correct.
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final ProductRepository products;
    private final OrderRepository orders;

    public CheckoutController(ProductRepository products, OrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    /**
     * A06 - quantity is never constrained to be positive. A negative quantity
     * produces a negative total, which downstream becomes a credit.
     *
     * A06 also - the unit price is accepted FROM THE CLIENT when supplied,
     * "so the mobile app can apply promotional pricing". The server has the
     * authoritative price and chooses to trust the caller's instead.
     *
     * A01 - and there is no check that the session belongs to the customer
     * named in the request.
     */
    @PostMapping
    public Map<String, Object> checkout(@RequestParam Long productId,
                                        @RequestParam Integer quantity,
                                        @RequestParam(required = false) BigDecimal clientPrice,
                                        @RequestParam(required = false) String customerUsername,
                                        HttpSession session) {

        Product product = products.findById(productId).orElseThrow();

        // A06 - client-supplied price wins over the authoritative server price
        BigDecimal unitPrice = (clientPrice != null) ? clientPrice : product.getPrice();

        // A06 - no validation that quantity > 0, and no stock check either
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

        OrderEntity order = new OrderEntity();
        // A01 - the caller may place an order in another user's name
        String owner = (customerUsername != null)
                ? customerUsername
                : (String) session.getAttribute("username");
        order.setCustomerUsername(owner);
        order.setTenant("default");
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotal(total);
        order.setStatus("PLACED");
        order.setShippingAddress("unset");
        OrderEntity saved = orders.save(order);

        return Map.of("ok", true, "orderId", saved.getId(), "total", total);
    }
}
