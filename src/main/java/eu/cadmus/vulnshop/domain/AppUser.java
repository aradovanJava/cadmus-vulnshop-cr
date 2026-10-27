package eu.cadmus.vulnshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A01/A04: this entity is returned directly from REST endpoints, so the API
 * contract is whatever the persistence model happens to contain today.
 * Adding a column becomes an information disclosure.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    /** A04:2025 - stored as an unsalted MD5 digest. See CryptoService. */
    private String passwordHash;

    private String email;

    /** A01 - role is present but never actually enforced anywhere. */
    private String role;

    /** A01 - multi-tenant field that no query ever filters on. */
    private String tenant;

    /** A04 - internal field that leaks because the entity is serialised directly. */
    private Integer internalRiskScore;

    /** A06 - a 4-digit reset code with no expiry and no attempt counter. */
    private String resetCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public Integer getInternalRiskScore() { return internalRiskScore; }
    public void setInternalRiskScore(Integer internalRiskScore) { this.internalRiskScore = internalRiskScore; }
    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
}
