package com.hhplus.precourse.comment;

import com.hhplus.precourse.comment.vo.CommentVo;
import com.hhplus.precourse.common.TestFixture;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
public class CommentVoFixture implements TestFixture<CommentVo> {
    private long id = 1L;
    private long userId = 1L;
    private long postId = 1L;
    private String content = "댓글 내용";
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Override
    public CommentVo build() {
        return new CommentVo(
            id,
            userId,
            postId,
            content,
            createdAt,
            updatedAt
        );
    }
}
