package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.map.CommentMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.map.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemDto addItem(ItemCreateDto dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с таким id: " + ownerId));

        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();

        if (dto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + dto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemUpdateDto dto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден с id: " + itemId));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Доступ запрещен: Пользователь " + ownerId + " не может вносить изменения " + itemId);
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemWithBookingsDto getItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден с id: " + itemId));

        ItemWithBookingsDto.BookingShortDto lastBooking = null;
        ItemWithBookingsDto.BookingShortDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            // Получаем все APPROVED бронирования для этой вещи
            List<Booking> itemBookings = bookingRepository.findAllByItemIdAndStatusOrderByStartAsc(itemId, BookingStatus.APPROVED);

            // Ищем последнее завершенное бронирование
            Optional<Booking> lastBookingOpt = itemBookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd));

            if (lastBookingOpt.isPresent()) {
                Booking booking = lastBookingOpt.get();
                lastBooking = ItemWithBookingsDto.BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build();
            }

            // Ищем ближайшее будущее бронирование
            Optional<Booking> nextBookingOpt = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart));

            if (nextBookingOpt.isPresent()) {
                Booking booking = nextBookingOpt.get();
                nextBooking = ItemWithBookingsDto.BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build();
            }
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, commentDtos);
    }

    @Override
    public List<ItemWithBookingsDto> getItemsOfUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден с таким id: " + userId);
        }

        // Загружаем все вещи пользователя
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        // Получаем ID всех вещей
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        //Загружаем APPROVED бронирования для всех вещей пользователя за один запрос
        List<Booking> allBookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartAsc(
                itemIds, BookingStatus.APPROVED);

        //Группируем бронирования по ID вещи
        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        //Загружаем все комментарии для всех вещей за один запрос
        Map<Long, List<CommentResponseDto>> commentsByItemId = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentResponseDto, Collectors.toList())
                ));

        //Обрабатываем каждую вещь
        LocalDateTime now = LocalDateTime.now();
        List<ItemWithBookingsDto> result = new ArrayList<>();

        for (Item item : items) {
            ItemWithBookingsDto.BookingShortDto lastBooking = null;
            ItemWithBookingsDto.BookingShortDto nextBooking = null;

            List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), List.of());

            if (!itemBookings.isEmpty()) {
                // Находим последнее завершенное бронирование
                Optional<Booking> lastBookingOpt = itemBookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .max(Comparator.comparing(Booking::getEnd));

                if (lastBookingOpt.isPresent()) {
                    Booking booking = lastBookingOpt.get();
                    lastBooking = ItemWithBookingsDto.BookingShortDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .build();
                }

                // Находим ближайшее будущее бронирование
                Optional<Booking> nextBookingOpt = itemBookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .min(Comparator.comparing(Booking::getStart));

                if (nextBookingOpt.isPresent()) {
                    Booking booking = nextBookingOpt.get();
                    nextBooking = ItemWithBookingsDto.BookingShortDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .build();
                }
            }

            List<CommentResponseDto> comments = commentsByItemId.getOrDefault(item.getId(), List.of());
            result.add(ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments));
        }

        //Сортируем результат по ID вещей
        result.sort(Comparator.comparing(ItemWithBookingsDto::getId));

        return result;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto commentRequestDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, now);

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда еще не завершена");
        }

        Comment comment = CommentMapper.toComment(commentRequestDto, item, author);
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentResponseDto(savedComment);
    }
}