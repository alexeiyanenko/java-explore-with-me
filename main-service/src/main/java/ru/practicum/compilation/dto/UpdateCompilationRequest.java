package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCompilationRequest {
    @Builder.Default
    private Boolean pinned = false;

    @Size(min = 1, max = 50)
    private String title;

    @Builder.Default
    private Set<Long> events = new HashSet<>();
}
