package eu.cadmus.vulnshop.repo;

import eu.cadmus.vulnshop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
