package ru.practicum.shareit.server.item.map;

import ru.practicum.shareit.server.item.dto.CommentRequestDto;
import ru.practicum.shareit.server.item.dto.CommentResponseDto;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

public class CommentMapper {

    public static Comment toComment(CommentRequestDto dto, Item item, User author) {
        if (dto == null || item == null || author == null) {
            return null;
        }

        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .build();
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .created(comment.getCreated())
                .build();
    }
}