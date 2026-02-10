package org.example.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionPermission {
    private String functionId;
    private String action;
    private List<AuthorizationCondition> conditions;
}