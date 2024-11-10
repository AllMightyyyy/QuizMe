package org.zakariafarih.quizme.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class QuestionDTO {
    private Long id;

    @NotBlank(message = "Question content is required")
    private String content;

    @NotNull(message = "Time limit is required")
    @Min(value = 10, message = "Time limit must be at least 10 seconds")
    private Long timeLimit;

    @Size(min = 2, message = "Each question must have at least two options")
    @Valid
    private Set<OptionDTO> options;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Set<OptionDTO> getOptions() {
        return options;
    }

    public void setOptions(Set<OptionDTO> options) {
        this.options = options;
    }
}