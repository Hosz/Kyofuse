package com.hokyozu.kyofuse.comments.finder;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.repository.CommentRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentFinderTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentFinder commentFinder;

    @Test
    void findByIdReturnsCommentWhenItExists() {
        UUID commentId = UUID.randomUUID();
        Comment comment = Comment.builder().id(commentId).build();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        Comment result = commentFinder.findById(commentId);

        assertThat(result).isSameAs(comment);
        verify(commentRepository).findById(commentId);
    }

    @Test
    void findByIdThrowsBadRequestWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentFinder.findById(commentId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Comment not found for ID: " + commentId);
    }
}
