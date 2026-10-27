package eu.cadmus.vulnshop.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

/**
 * ============================================================================
 *  A08:2025 - SOFTWARE OR DATA INTEGRITY FAILURES
 * ============================================================================
 *
 * A08 stays at #8 in 2025 and is described as integrity failures at a LOWER
 * level than A03 Software Supply Chain Failures: the failure to maintain trust
 * boundaries and verify the integrity of software, code and data artifacts.
 *
 * Three demonstrations, one theme - trusting the integrity of something that
 * crossed a boundary:
 *
 *   1. Java native deserialisation of caller-supplied bytes.
 *   2. A "plugin" descriptor fetched over plain HTTP with no signature check.
 *   3. External partner data bound straight onto internal domain state.
 *
 * NOTE FOR INSTRUCTORS: item 1 combined with the intentionally outdated
 * commons-collections 3.2.1 dependency in pom.xml is a genuine remote code
 * execution path. That combination is the point of the exercise - it is how
 * participants learn that a "dormant" dependency and a deserialisation sink are
 * only dangerous together - but it is also why this application must never
 * leave the sandbox. See SAFETY.md.
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {

    /**
     * A08 - readObject() on attacker-controlled bytes. ObjectInputStream
     * instantiates whatever classes the stream names, before any application
     * code inspects the result. There is no filter (JEP 290 /
     * ObjectInputFilter is not configured) and no allow-list.
     *
     * Remediation for CR2: do not deserialise untrusted input at all. Use a
     * data format that does not carry types (JSON to a DTO), and if native
     * serialisation is unavoidable, install a strict ObjectInputFilter.
     */
    @PostMapping("/legacy-cart")
    public Map<String, Object> importLegacyCart(@RequestBody String base64Payload) throws Exception {
        byte[] raw = Base64.getDecoder().decode(base64Payload);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(raw))) {
            Object restored = in.readObject();
            return Map.of("ok", true, "type", restored.getClass().getName());
        }
    }

    /**
     * A08 - an update descriptor is fetched over cleartext HTTP from a location
     * the caller supplies, and applied with no signature verification, no
     * checksum and no provenance check. Whoever controls the network path
     * controls the application's configuration.
     */
    @PostMapping("/plugin")
    public Map<String, Object> loadPlugin(@RequestParam String descriptorUrl) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(descriptorUrl)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // no signature, no checksum, no pinned certificate, no allow-listed origin
        return Map.of("ok", true, "applied", true, "descriptor", response.body());
    }
}
