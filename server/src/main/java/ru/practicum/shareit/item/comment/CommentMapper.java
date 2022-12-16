package ru.practicum.shareit.item.comment;


import ru.practicum.shareit.user.model.User;

public class CommentMapper {

    public static Comment mapToNewComment(CommentDto commentDto, Long itemId, User author) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItemId(itemId);
        comment.setAuthor(author);
        return comment;
    }

    public static CommentOutput mapToCommentDto(Comment comment) {
        CommentOutput commentDto = new CommentOutput();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }
}
