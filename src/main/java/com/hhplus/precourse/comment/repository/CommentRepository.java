package com.hhplus.precourse.comment.repository;

import com.hhplus.precourse.comment.domain.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostId(long id, Sort sort);

    List<Comment> findAllByPostIdIn(List<Long> ids, Sort sort);
}
