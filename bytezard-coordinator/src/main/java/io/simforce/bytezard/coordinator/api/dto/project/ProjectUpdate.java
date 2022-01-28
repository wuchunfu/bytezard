package io.simforce.bytezard.coordinator.api.dto.project;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@NotNull(message = "Project cannot be null")
public class ProjectUpdate extends ProjectCreate {
    private Long id;
}
