package org.example.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationCondition {
    private String authObj;
    private String field;
    private String valFrom;
    private String valTo;
    private String condition; // AND, OR
}
