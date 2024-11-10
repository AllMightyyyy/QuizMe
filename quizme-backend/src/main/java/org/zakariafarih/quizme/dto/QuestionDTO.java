package org.zakariafarih.quizme.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class QuestionDTO {
    private Long id;

    @NotBlank(message = "Question content is required")
    private String content;

    @NotNull(message = "Time limit is required")
    private Long timeLimit;

    @Size(min = 2, message = "Each question must have at least two options")
    private List<OptionDTO> options = new ArrayList<>();
}
