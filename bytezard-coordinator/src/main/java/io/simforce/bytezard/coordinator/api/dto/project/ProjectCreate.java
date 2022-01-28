package io.simforce.bytezard.coordinator.api.dto.project;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@NotNull(message = "Project info cannot be null")
public class ProjectCreate {

    @NotBlank(message = "Project name cannot be empty")
    private String name;

}
