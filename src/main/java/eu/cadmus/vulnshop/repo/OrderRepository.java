package eu.cadmus.vulnshop.repo;

import eu.cadmus.vulnshop.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * The SECURE pattern, for contrast: the caller is a required parameter, so
     * this method cannot be invoked without scoping the result to somebody.
     * Compare with OrderController.getOrder().
     */
    List<OrderEntity> findByCustomerUsername(String customerUsername);
}
