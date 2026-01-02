package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.id = :bookingId AND (b.booker.id = :userId OR i.owner.id = :userId)")
    Optional<Booking> findByIdAndBookerIdOrItemOwnerId(Long bookingId, Long userId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC")
    List<Booking> findLastBookingForItem(Long itemId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingForItem(Long itemId, LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsByBookerIdAndItemIdAndEndBefore(Long userId, Long itemId, LocalDateTime now);

    //методы букинга
    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    // Методы для владельца
    List<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    // Получение всех бронирований по itemId
    List<Booking> findByItemId(Long itemId);

    // Получить все APPROVED бронирования для списка вещей
    List<Booking> findAllByItemIdInAndStatusOrderByStartAsc(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status);

    // Получить все APPROVED бронирования для одной вещи
    List<Booking> findAllByItemIdAndStatusOrderByStartAsc(
            @Param("itemId") Long itemId,
            @Param("status") BookingStatus status);


}
