package ru.practicum.shareit.server.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
}