package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDto {
    @NotBlank(message = "Пустой name")
    @Size(min = 1, max = 50, message = "Поле name должно содержать от 1 до 50 символов")
    private String name;
}
