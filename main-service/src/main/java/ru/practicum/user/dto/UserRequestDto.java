package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "пустой Email")
    @Email(message = "отсутствует символ @")
    @Size(min = 6, max = 254, message = "Поле email должно содержать от 6 до 254 символом")
    private String email;

    @NotBlank(message = "пустой name")
    @Size(min = 2, max = 250, message = "Поле name должно содержать от 2 до 250 символом")
    private String name;
}
