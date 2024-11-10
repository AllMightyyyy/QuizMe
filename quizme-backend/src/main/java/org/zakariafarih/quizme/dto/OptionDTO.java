package org.zakariafarih.quizme.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OptionDTO {
    private Long id;

    @NotBlank(message = "Option text is required")
    private String text;

    private boolean isCorrect;
}

