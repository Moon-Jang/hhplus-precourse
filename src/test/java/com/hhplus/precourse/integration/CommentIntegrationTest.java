package com.hhplus.precourse.integration;

import com.hhplus.precourse.comment.controller.CreateCommentController;
import com.hhplus.precourse.comment.controller.UpdateCommentController;
import com.hhplus.precourse.comment.domain.CommentFixture;
import com.hhplus.precourse.comment.repository.CommentRepository;
import com.hhplus.precourse.common.IntegrationTest;
import com.hhplus.precourse.common.component.JwtTokenManager;
import com.hhplus.precourse.common.support.utils.JsonUtils;
import com.hhplus.precourse.post.domain.Post;
import com.hhplus.precourse.post.domain.PostFixture;
import com.hhplus.precourse.post.repository.PostRepository;
import com.hhplus.precourse.user.domain.User;
import com.hhplus.precourse.user.domain.UserFixture;
import com.hhplus.precourse.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentIntegrationTest extends IntegrationTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenManager jwtTokenManager;
    private User savedUser;
    private Post savedPost;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        var user = new UserFixture().build();
        this.savedUser = userRepository.save(user);
        var post = new PostFixture().setUserId(savedUser.id()).build();
        this.savedPost = postRepository.save(post);
        this.jwtToken = jwtTokenManager.issue(savedUser.id(), savedUser.name());
    }

    @DisplayName("댓글 생성")
    @Test
    void createComment() throws Exception {
        var request = new CreateCommentController.Request(
            savedPost.id(),
            savedUser.id(),
            "댓글 내용"
        );

        mockMvc.perform(
                post("/comments")
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(JsonUtils.stringify(request))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.id").isNumber())
            .andExpect(jsonPath("$.data.postId").value(savedPost.id()))
            .andExpect(jsonPath("$.data.userId").value(savedUser.id()))
            .andExpect(jsonPath("$.data.content").value("댓글 내용"));
    }

    @DisplayName("댓글 수정")
    @Test
    void updateComment() throws Exception {
        var comment = new CommentFixture()
            .setPostId(savedPost.id())
            .setUserId(savedUser.id())
            .build();
        var savedComment = commentRepository.save(comment);
        var request = new UpdateCommentController.Request(
            "수정된 댓글 내용"
        );

        mockMvc.perform(
                put("/comments/{id}", savedComment.id())
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(JsonUtils.stringify(request))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.id").value(savedComment.id()))
            .andExpect(jsonPath("$.data.postId").value(savedComment.postId()))
            .andExpect(jsonPath("$.data.userId").value(savedComment.userId()))
            .andExpect(jsonPath("$.data.content").value(request.content()));
    }

    @DisplayName("댓글 삭제")
    @Test
    void deleteComment() throws Exception {
        var comment = new CommentFixture()
            .setPostId(savedPost.id())
            .setUserId(savedUser.id())
            .build();
        var savedComment = commentRepository.save(comment);

        mockMvc.perform(
                delete("/comments/{id}", savedComment.id())
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + jwtToken)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").doesNotExist());
    }
    
    @DisplayName("댓글 수정시 작성자가 아닐 경우 실패")
    @Test
    void updateCommentByWriter() throws Exception {
        var comment = new CommentFixture()
            .setPostId(savedPost.id())
            .setUserId(savedUser.id())
            .build();
        var savedComment = commentRepository.save(comment);
        var request = new UpdateCommentController.Request(
            "수정된 댓글 내용"
        );
        var otherUser = new UserFixture().setId(savedUser.id() + 1).setName(savedUser.name() + "2").build();
        var savedOtherUser = userRepository.save(otherUser);
        var otherJwtToken = jwtTokenManager.issue(savedOtherUser.id(), savedOtherUser.name());

        mockMvc.perform(
                put("/comments/{id}", savedComment.id())
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + otherJwtToken)
                    .content(JsonUtils.stringify(request))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("작성자만 수정할 수 있습니다."));
    }

    @DisplayName("댓글 삭제시 작성자가 아닐 경우 실패")
    @Test
    void deleteCommentByWriter() throws Exception {
        var comment = new CommentFixture()
            .setPostId(savedPost.id())
            .setUserId(savedUser.id())
            .build();
        var savedComment = commentRepository.save(comment);
        var otherUser = new UserFixture().setId(savedUser.id() + 1).setName(savedUser.name() + "2").build();
        var savedOtherUser = userRepository.save(otherUser);
        var otherJwtToken = jwtTokenManager.issue(savedOtherUser.id(), savedOtherUser.name());

        mockMvc.perform(
                delete("/comments/{id}", savedComment.id())
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + otherJwtToken)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("작성자만 삭제할 수 있습니다."));
    }
}
