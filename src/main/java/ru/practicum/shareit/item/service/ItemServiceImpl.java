package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        // Если указан requestId, связываем вещь с запросом
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

        // Получаем информацию о бронированиях только для владельца
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, 1);

            // Получаем последнее завершенное бронирование
            List<Booking> lastBookings = bookingRepository.findLastBookingForItem(itemId, now, pageable);
            if (!lastBookings.isEmpty()) {
                Booking booking = lastBookings.get(0);
                lastBooking = ItemWithBookingsDto.BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build();
            }

            // Получаем ближайшее будущее бронирование
            List<Booking> nextBookings = bookingRepository.findNextBookingForItem(itemId, now, pageable);
            if (!nextBookings.isEmpty()) {
                Booking booking = nextBookings.get(0);
                nextBooking = ItemWithBookingsDto.BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build();
            }
        }

        // Получаем комментарии
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

        List<Item> items = itemRepository.findByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 1);

        // Получаем последние бронирования для всех вещей
        Map<Long, Booking> lastBookingsMap = itemIds.stream()
                .flatMap(itemId -> bookingRepository.findLastBookingForItem(itemId, now, pageable).stream())
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking,
                        (first, second) -> first
                ));

        // Получаем ближайшие бронирования для всех вещей
        Map<Long, Booking> nextBookingsMap = itemIds.stream()
                .flatMap(itemId -> bookingRepository.findNextBookingForItem(itemId, now, pageable).stream())
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking,
                        (first, second) -> first
                ));

        // Получаем комментарии для всех вещей
        Map<Long, List<CommentResponseDto>> commentsMap = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentResponseDto, Collectors.toList())
                ));

        // Формируем результат
        List<ItemWithBookingsDto> result = new ArrayList<>();
        for (Item item : items) {
            ItemWithBookingsDto.BookingShortDto lastBooking = null;
            ItemWithBookingsDto.BookingShortDto nextBooking = null;

            if (lastBookingsMap.containsKey(item.getId())) {
                Booking booking = lastBookingsMap.get(item.getId());
                lastBooking = ItemWithBookingsDto.BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build();
            }

            if (nextBookingsMap.containsKey(item.getId())) {
                Booking booking = nextBookingsMap.get(item.getId());
                nextBooking = ItemWithBookingsDto.BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build();
            }

            List<CommentResponseDto> comments = commentsMap.getOrDefault(item.getId(), List.of());

            result.add(ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments));
        }

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
        // Проверяем существование пользователя и вещи
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        // Проверяем, что пользователь брал вещь в аренду и аренда завершена
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, now);

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда еще не завершена");
        }

        // Создаем комментарий
        Comment comment = CommentMapper.toComment(commentRequestDto, item, author);
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentResponseDto(savedComment);
    }
}