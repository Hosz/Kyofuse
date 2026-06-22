package com.hokyozu.kyofuse.comments.controller;

import com.hokyozu.kyofuse.comments.service.CommentService;
import com.hokyozu.kyofuse.infrastructure.security.SecurityConfig;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtAuthConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, JwtAuthConverter.class})
@TestPropertySource(properties =
        "security.jwt.secret=kyofuse-local-development-secret-key-change-me-please-123456789")
class CommentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void postCommentRejectsRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/comments/post/{postId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"content\"}"))
                .andExpect(status().isUnauthorized());
    }
}
