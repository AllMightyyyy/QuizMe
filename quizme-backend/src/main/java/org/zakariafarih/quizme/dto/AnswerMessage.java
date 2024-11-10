package org.zakariafarih.quizme.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerMessage {
    private String username;
    private Long questionId;
    private Long selectedOptionId;
}