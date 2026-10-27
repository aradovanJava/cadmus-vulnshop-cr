package eu.cadmus.vulnshop.mvc;

import eu.cadmus.vulnshop.domain.Product;
import eu.cadmus.vulnshop.repo.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ============================================================================
 *  MVC / Thymeleaf storefront
 * ============================================================================
 *
 * The public shop. Two weaknesses are injected here, and neither is in the
 * Java file - both are in the templates this controller renders. That is the
 * point of including an MVC tier at all: a reviewer who only reads controllers
 * will miss them entirely, and a SAST rule profile configured for Java only
 * will not report them either.
 *
 * Look at shop/product.html before deciding this class is clean.
 */
@Controller
public class ShopMvcController {

    private final ProductRepository products;

    public ShopMvcController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping({"/", "/shop"})
    public String index(@RequestParam(required = false) String category, Model model, HttpSession session) {
        List<Product> all = products.findAll();
        if (category != null && !category.isBlank()) {
            all = all.stream().filter(p -> category.equals(p.getCategory())).toList();
        }
        model.addAttribute("products", all);
        model.addAttribute("category", category);
        model.addAttribute("user", session.getAttribute("user"));
        return "shop/index";
    }

    /**
     * The product description is authored through the admin panel and stored
     * in the database, then rendered with th:utext in the template. Stored
     * cross-site scripting: the payload is persisted once and delivered to
     * every visitor, including administrators.
     */
    @GetMapping("/shop/product/{id}")
    public String product(@PathVariable Long id, Model model, HttpSession session) {
        Product p = products.findById(id).orElseThrow();
        model.addAttribute("product", p);
        model.addAttribute("user", session.getAttribute("user"));
        return "shop/product";
    }
}
