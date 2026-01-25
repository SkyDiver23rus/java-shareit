package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createAndGetBooking_success() {

        User owner = userRepository.save(new User(null, "Владелец", "owner@test.com"));
        User booker = userRepository.save(new User(null, "Бронировщик", "booker@test.com"));


        Item item = itemRepository.save(new Item(null, "Дрель", "Мощная дрель", true, owner, null));


        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto requestDto = new BookingRequestDto(item.getId(), start, end);


        BookingResponseDto saved = bookingService.create(requestDto, booker.getId());


        BookingResponseDto found = bookingService.getById(saved.getId(), booker.getId());


        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getItem().getId()).isEqualTo(item.getId());
        assertThat(found.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(found.getStart()).isEqualTo(start);
        assertThat(found.getEnd()).isEqualTo(end);
        assertThat(found.getStatus()).isEqualTo(ru.practicum.shareit.booking.model.BookingStatus.WAITING);
    }

    @Test
    void approveBooking_success() {

        User owner = userRepository.save(new User(null, "Владелец", "owner@test.com"));
        User booker = userRepository.save(new User(null, "Бронировщик", "booker@test.com"));


        Item item = itemRepository.save(new Item(null, "Дрель", "Мощная дрель", true, owner, null));


        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto requestDto = new BookingRequestDto(item.getId(), start, end);


        BookingResponseDto booking = bookingService.create(requestDto, booker.getId());


        BookingResponseDto approved = bookingService.approve(booking.getId(), owner.getId(), true);


        assertThat(approved).isNotNull();
        assertThat(approved.getId()).isEqualTo(booking.getId());
        assertThat(approved.getStatus()).isEqualTo(ru.practicum.shareit.booking.model.BookingStatus.APPROVED);
    }

    @Test
    void getAllByBooker_success() {

        User owner = userRepository.save(new User(null, "Владелец", "owner@test.com"));
        User booker = userRepository.save(new User(null, "Бронировщик", "booker@test.com"));


        Item item = itemRepository.save(new Item(null, "Дрель", "Мощная дрель", true, owner, null));


        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto requestDto = new BookingRequestDto(item.getId(), start, end);


        bookingService.create(requestDto, booker.getId());


        var bookings = bookingService.getAllByBooker(booker.getId(),
                ru.practicum.shareit.booking.model.BookingState.ALL, 0, 10);


        assertThat(bookings).isNotEmpty();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.getFirst().getItem().getName()).isEqualTo("Дрель");
        assertThat(bookings.getFirst().getBooker().getName()).isEqualTo("Бронировщик");
    }

    @Test
    void getAllByOwner_success() {

        User owner = userRepository.save(new User(null, "Владелец", "owner@test.com"));
        User booker = userRepository.save(new User(null, "Бронировщик", "booker@test.com"));


        Item item = itemRepository.save(new Item(null, "Дрель", "Мощная дрель", true, owner, null));


        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);
        BookingRequestDto requestDto = new BookingRequestDto(item.getId(), start, end);


        bookingService.create(requestDto, booker.getId());


        var bookings = bookingService.getAllByOwner(owner.getId(),
                ru.practicum.shareit.booking.model.BookingState.ALL, 0, 10);


        assertThat(bookings).isNotEmpty();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.getFirst().getItem().getName()).isEqualTo("Дрель");
        assertThat(bookings.getFirst().getItem().getName()).isEqualTo("Дрель");
        assertThat(bookings.getFirst().getItem().getId()).isEqualTo(item.getId());
    }
}