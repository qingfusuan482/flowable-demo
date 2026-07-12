package org.example.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleAssignDTO {

    private Long userId;

    private List<Long> roleIds;
}
