package eu.cadmus.vulnshop.web;

import eu.cadmus.vulnshop.repo.ProductRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  A05:2025 - INJECTION      and      A01:2025 - BROKEN ACCESS CONTROL (SSRF)
 * ============================================================================
 *
 * Two categories in one controller, and the SSRF placement is the change most
 * likely to catch out anyone trained on the 2021 list: SSRF was A10:2021 in its
 * own right and is now folded into A01:2025. The reasoning is that SSRF is the
 * server being made to reach a resource on the attacker's behalf, which is an
 * access-control failure rather than a category of its own.
 *
 * Injection itself FELL from #3 to #5 in the 2025 ranking - not because it got
 * rarer, but because misconfiguration and supply chain overtook it.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final JdbcTemplate jdbc;
    private final ProductRepository products;

    public ProductController(JdbcTemplate jdbc, ProductRepository products) {
        this.jdbc = jdbc;
        this.products = products;
    }

    /**
     * A05 - SQL INJECTION by string concatenation. The single most tested
     * weakness class in the OWASP dataset, and still shipping.
     *
     * The fix is one line (a bound parameter). The interesting question for
     * node N1 is why the concatenated version was written at all: usually
     * because "the sort column has to be dynamic", which is a design problem
     * that a parameter cannot solve. See docs/FIXES.md.
     */
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String category,
                                            @RequestParam(defaultValue = "name") String sort) {
        String sql = "SELECT id, name, price, category FROM product "
                   + "WHERE category = '" + category + "' ORDER BY " + sort;
        return jdbc.queryForList(sql);
    }

    /**
     * The SECURE counterpart, for the CR2 comparison. The value is bound, so it
     * cannot change the shape of the statement no matter what it contains.
     */
    @GetMapping("/search-safe")
    public List<Map<String, Object>> searchSafe(@RequestParam String category) {
        return jdbc.queryForList(
                "SELECT id, name, price, category FROM product WHERE category = ?", category);
    }

    /**
     * A01 - SERVER-SIDE REQUEST FORGERY (A10:2021, now part of A01:2025).
     *
     * The application fetches a URL chosen by the caller, from inside the trust
     * boundary. Cloud metadata services, internal admin panels and anything else
     * reachable from this host are now reachable from the internet, using this
     * server's network position as the credential.
     *
     * There is no allow-list, no scheme restriction, no DNS re-resolution check
     * and no egress control.
     */
    @GetMapping("/import-feed")
    public Map<String, Object> importFeed(@RequestParam String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.ALWAYS)   // makes filtering harder still
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // the response body is returned to the caller, so this is a full read primitive
        return Map.of("status", response.statusCode(), "body", response.body());
    }

    @GetMapping("/all")
    public Object all() {
        return products.findAll();
    }
}
