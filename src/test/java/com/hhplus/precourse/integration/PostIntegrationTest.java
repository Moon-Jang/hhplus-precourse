package com.hhplus.precourse.integration;

import com.hhplus.precourse.comment.domain.Comment;
import com.hhplus.precourse.comment.domain.CommentFixture;
import com.hhplus.precourse.comment.repository.CommentRepository;
import com.hhplus.precourse.common.IntegrationTest;
import com.hhplus.precourse.common.component.JwtTokenManager;
import com.hhplus.precourse.common.support.utils.JsonUtils;
import com.hhplus.precourse.post.controller.CreatePostController;
import com.hhplus.precourse.post.controller.UpdatePostController;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostIntegrationTest extends IntegrationTest {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private JwtTokenManager jwtTokenManager;
    private String jwtToken;
    private User savedUser;
    private Post savedPost;
    private List<Comment> savedComments;

    @BeforeEach
    void setUp() {
        var user = new UserFixture().build();
        this.savedUser = userRepository.save(user);
        var post = new PostFixture().setUserId(savedUser.id()).build();
        this.savedPost = postRepository.save(post);
        var comments = List.of(
            new CommentFixture().setId(1L).setUserId(savedUser.id()).setPostId(savedPost.id()).setContent("댓글1").setCreatedAt(LocalDateTime.now().minusHours(1)).build(),
            new CommentFixture().setId(2L).setUserId(savedUser.id()).setPostId(savedPost.id()).setContent("댓글2").setCreatedAt(LocalDateTime.now()).build()
        );
        commentRepository.saveAll(comments);
        this.savedComments = comments;
        this.jwtToken = jwtTokenManager.issue(savedUser.id(), savedUser.name());
    }

    @DisplayName("게시글 목록 조회")
    @Test
    void getPostList() throws Exception {
        mockMvc.perform(get("/posts"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(savedPost.id()))
            .andExpect(jsonPath("$.data[0].title").value(savedPost.title()))
            .andExpect(jsonPath("$.data[0].author").value(savedPost.author()))
            .andExpect(jsonPath("$.data[0].content").value(savedPost.content()))
            .andExpect(jsonPath("$.data[0].createdAt").exists())
            .andExpect(jsonPath("$.data[0].updatedAt").exists())
            .andExpect(jsonPath("$.data[0].comments").isArray())
            .andExpect(jsonPath("$.data[0].comments[0].id").isNumber())
            .andExpect(jsonPath("$.data[0].comments[0].userId").value(savedComments.get(1).userId()))
            .andExpect(jsonPath("$.data[0].comments[0].postId").value(savedComments.get(1).postId()))
            .andExpect(jsonPath("$.data[0].comments[0].content").value(savedComments.get(1).content()))
            .andExpect(jsonPath("$.data[0].comments[0].createdAt").exists())
            .andExpect(jsonPath("$.data[0].comments[0].updatedAt").exists());
    }

    @DisplayName("게시글 상세 조회")
    @Test
    void getPost() throws Exception {
        mockMvc.perform(get("/posts/{id}", savedPost.id()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.id").value(savedPost.id()))
            .andExpect(jsonPath("$.data.title").value(savedPost.title()))
            .andExpect(jsonPath("$.data.author").value(savedPost.author()))
            .andExpect(jsonPath("$.data.content").value(savedPost.content()))
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.updatedAt").exists())
            .andExpect(jsonPath("$.data.comments").isArray())
            .andExpect(jsonPath("$.data.comments[0].id").isNumber())
            .andExpect(jsonPath("$.data.comments[0].userId").value(savedComments.get(1).userId()))
            .andExpect(jsonPath("$.data.comments[0].postId").value(savedComments.get(1).postId()))
            .andExpect(jsonPath("$.data.comments[0].content").value(savedComments.get(1).content()))
            .andExpect(jsonPath("$.data.comments[0].createdAt").exists())
            .andExpect(jsonPath("$.data.comments[0].updatedAt").exists());
    }

    @DisplayName("게시글 생성")
    @Test
    void createPost() throws Exception {
        var request = new CreatePostController.Request(
            "testAuthor",
            "testTitle",
            "testContent"
        );

        mockMvc.perform(
                post("/posts")
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(JsonUtils.stringify(request))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.id").isNumber())
            .andExpect(jsonPath("$.data.title").value(request.title()))
            .andExpect(jsonPath("$.data.author").value(request.author()))
            .andExpect(jsonPath("$.data.content").value(request.content()))
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @DisplayName("게시글 수정")
    @Test
    void updatePost() throws Exception {
        var request = new UpdatePostController.Request(
            "changedAuthor",
            "changedTitle",
            "changedContent"
        );

        mockMvc.perform(
                put("/posts/{id}", savedPost.id())
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .content(JsonUtils.stringify(request))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isMap())
            .andExpect(jsonPath("$.data.id").isNumber())
            .andExpect(jsonPath("$.data.title").value(request.title()))
            .andExpect(jsonPath("$.data.author").value(request.author()))
            .andExpect(jsonPath("$.data.content").value(request.content()))
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @DisplayName("게시글 수정시 작성자가 아닐 경우 실패")
    @Test
    void updatePostByWriter() throws Exception {
        var otherUser = new UserFixture().setId(savedUser.id() + 1).setName(savedUser.name() + "2").build();
        var savedOtherUser = userRepository.save(otherUser);
        var otherJwtToken = jwtTokenManager.issue(savedOtherUser.id(), savedOtherUser.name());
        var request = new UpdatePostController.Request(
            "changedAuthor",
            "changedTitle",
            "changedContent"
        );

        mockMvc.perform(
                put("/posts/{id}", savedPost.id())
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + otherJwtToken)
                    .content(JsonUtils.stringify(request))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("작성자만 수정할 수 있습니다."));
    }

    @DisplayName("게시글 삭제")
    @Test
    void deletePost() throws Exception {
        mockMvc.perform(
                delete("/posts/{id}", savedPost.id())
                    .header("Authorization", "Bearer " + jwtToken)
            )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @DisplayName("게시글 삭제시 작성자가 아닐 경우 실패")
    @Test
    void deletePostByWriter() throws Exception {
        var otherUser = new UserFixture().setId(savedUser.id() + 1).setName(savedUser.name() + "2").build();
        var savedOtherUser = userRepository.save(otherUser);
        var otherJwtToken = jwtTokenManager.issue(savedOtherUser.id(), savedOtherUser.name());

        mockMvc.perform(
                delete("/posts/{id}", savedPost.id())
                    .header("Authorization", "Bearer " + otherJwtToken)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("작성자만 삭제할 수 있습니다."));
    }
}
