package org.zakariafarih.quizme.dto;

import java.util.Set;

public class QuestionDTO {
    private Long id;
    private String content;
    private Long timeLimit;
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