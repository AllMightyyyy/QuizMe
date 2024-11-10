package org.zakariafarih.quizme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerMessage {
    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Selected option ID is required")
    private Long selectedOptionId;
}