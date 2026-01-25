package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.map.CommentMapper;
import ru.practicum.shareit.item.map.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

            List<Booking> itemBookings = bookingRepository.findAllByItemIdAndStatusOrderByStartAsc(itemId, BookingStatus.APPROVED);

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

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> allBookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartAsc(
                itemIds, BookingStatus.APPROVED);

        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        Map<Long, List<CommentResponseDto>> commentsByItemId = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentResponseDto, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();
        List<ItemWithBookingsDto> result = new ArrayList<>();

        for (Item item : items) {
            ItemWithBookingsDto.BookingShortDto lastBooking = null;
            ItemWithBookingsDto.BookingShortDto nextBooking = null;

            List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), List.of());

            if (!itemBookings.isEmpty()) {
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
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto requestDto) {
        log.info("Adding comment to item {} by user {}", itemId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasCompletedBooking = bookingRepository.existsCompletedBooking(
                itemId,
                userId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );

        if (!hasCompletedBooking) {
            throw new UnavailableItemException(
                    "Комментарий можно оставить только после завершения бронирования"
            );
        }

        Comment comment = Comment.builder()
                .text(requestDto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentMapper.toCommentResponseDto(saved);
    }
}