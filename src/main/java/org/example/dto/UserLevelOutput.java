package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLevelOutput {

    private String riskId;

    private String description;

    private String userId;

    private String roleName;

    private String profile;

}
