package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    Optional<Item> findByIdAndOwnerId(Long id, Long ownerId);

    @Query(" SELECT i FROM Item i " +
            "WHERE i.available = true AND " +
            "(UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%') ) " +
            "   OR UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%')) )")
    Page<Item> findAvailableToRentByText(String text, Pageable pageable);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findAllByRequestIdIn(Collection<Long> requests);
}
