package eu.cadmus.vulnshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CADMUS-CR-14 · VulnShop
 *
 * An intentionally vulnerable Spring Boot application used as the training
 * target for the "Secure Coding Java" module. Every weakness in this code
 * base is deliberate and documented in VULNERABILITY_MAP.md.
 *
 * THIS APPLICATION IS NOT SAFE TO RUN OUTSIDE AN ISOLATED SANDBOX.
 * See SAFETY.md.
 */
@SpringBootApplication
public class VulnShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnShopApplication.class, args);
    }

    @Component
    static class StartupWarning {
        @EventListener(ApplicationReadyEvent.class)
        public void warn() {
            System.out.println("""

                ==========================================================================
                  CADMUS-CR-14  VulnShop  ·  INTENTIONALLY VULNERABLE TRAINING APPLICATION
                --------------------------------------------------------------------------
                  This application contains deliberate security defects covering the
                  OWASP Top 10:2025. It is a target for SAST/DAST/SCA exercises.

                  * Run it ONLY inside the Cyber Range sandbox or an isolated VM.
                  * It binds to 127.0.0.1 by default. Do not change that on a shared network.
                  * Never load real data, real credentials or real secrets into it.
                  * Never expose it to the internet, and never deploy it anywhere.

                  http://127.0.0.1:8080/
                ==========================================================================
                """);
        }
    }
}
