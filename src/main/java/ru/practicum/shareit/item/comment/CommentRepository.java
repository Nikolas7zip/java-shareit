package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(" select new ru.practicum.shareit.item.comment.CommentOutput(c.id, c.text, c.author.name, c.created) " +
            "from Comment c " +
            "where c.itemId=?1")
    List<CommentOutput> findItemComments(Long itemId);
}
