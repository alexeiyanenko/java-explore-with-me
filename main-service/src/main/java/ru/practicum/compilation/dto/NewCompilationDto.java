package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
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
public class NewCompilationDto {
    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "пустой title")
    @Size(min = 1, max = 50)
    private String title;

    @Builder.Default
    private Set<Long> events = new HashSet<>();

}
