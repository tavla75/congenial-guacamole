package org.example.resturang_kassa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantOrderRepository
    extends JpaRepository<RestaurantOrder, Long>{
}
