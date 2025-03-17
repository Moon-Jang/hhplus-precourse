package com.hhplus.precourse.post.service;

import com.hhplus.precourse.common.exception.BadRequestException;
import com.hhplus.precourse.common.exception.NotFoundException;
import com.hhplus.precourse.post.domain.PostFixture;
import com.hhplus.precourse.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.hhplus.precourse.common.support.ApplicationStatus.POST_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DeletePostServiceTest {
    @InjectMocks
    private DeletePostService service;
    @Mock
    private PostRepository postRepository;

    @Test
    void success() {
        // given
        var post = new PostFixture().build();
        given(postRepository.findById(post.id()))
            .willReturn(Optional.of(post));

        // when
        var throwable = catchThrowable(() -> service.delete(post.id(), post.userId()));

        // then
        assertThat(throwable).isNull();
    }

    @Test
    void notFound() {
        // given
        given(postRepository.findById(any()))
            .willReturn(Optional.empty());

        // when
        var throwable = catchThrowable(() -> service.delete(1L, 1L));

        // then
        assertThat(throwable).isInstanceOf(NotFoundException.class)
            .hasMessage(POST_NOT_FOUND.message());
    }

    @Test
    void unauthorized() {
        // given
        var post = new PostFixture().build();
        given(postRepository.findById(post.id()))
            .willReturn(Optional.of(post));

        // when
        var throwable = catchThrowable(() -> service.delete(post.id(), 2L));

        // then
        assertThat(throwable).isInstanceOf(BadRequestException.class)
            .hasMessage("작성자만 삭제할 수 있습니다.");
    }
}