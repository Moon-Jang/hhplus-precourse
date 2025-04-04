package com.hhplus.precourse.post.controller;

import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.hhplus.precourse.common.ControllerTestContext;
import com.hhplus.precourse.post.service.UpdatePostService;
import com.hhplus.precourse.post.vo.PostVo;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;

import java.time.LocalDateTime;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.hhplus.precourse.common.ApiDocumentUtils.*;
import static com.hhplus.precourse.common.TestUserDetailsConfig.USER_DETAILS_BEAN_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

class UpdatePostControllerTest extends ControllerTestContext {
    private static final String TAG = Tags.POST.tagName();
    private static final String DESCRIPTION = Tags.POST.descriptionWith("수정");

    @MockBean
    private UpdatePostService service;

    @WithUserDetails(userDetailsServiceBeanName = USER_DETAILS_BEAN_NAME)
    @Test
    void success() {
        BDDMockito.given(service.update(any()))
            .willReturn(new PostVo(
                1L,
                1L,
                "작성자명",
                "제목",
                "내용",
                LocalDateTime.now(),
                LocalDateTime.now()
            ));

        var body = new CreatePostController.Request(
            "수정할 작성자명",
            "수정할 제목",
            "수정할 내용"
        );

        given()
            .header(authorizationHeader())
            .body(body)
            .when()
            .put("/posts/{id}", "1")
            .then()
            .log().all()
            .apply(
                document(
                    identifier(),
                    new ResourceSnippetParametersBuilder()
                        .tag(TAG)
                        .description(DESCRIPTION),
                    preprocessRequest(),
                    preprocessResponse(),
                    requestHeaderWithAuthorization(),
                    requestFields(
                        fieldWithPath("author").type(STRING).description("수정할 작성자명"),
                        fieldWithPath("title").type(STRING).description("수정할 제목"),
                        fieldWithPath("content").type(STRING).description("수정할 내용")
                    ),
                    responseFields(
                        fieldsWithBasic(
                            fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                            fieldWithPath("data.id").type(NUMBER).description("게시글 ID"),
                            fieldWithPath("data.title").type(STRING).description("제목"),
                            fieldWithPath("data.author").type(STRING).description("작성자명"),
                            fieldWithPath("data.content").type(STRING).description("내용"),
                            fieldWithPath("data.createdAt").type(STRING).description("생성일시"),
                            fieldWithPath("data.updatedAt").type(STRING).description("수정일시")
                        )
                    )
                )
            )
            .status(HttpStatus.OK);
    }
}