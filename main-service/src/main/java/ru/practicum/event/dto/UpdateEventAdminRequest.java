package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.enums.EventStateAdminAction;
import ru.practicum.event.validation.EventDate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000, message = "поле annotation должно содержать от 20 до 2000 символом")
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000, message = "поле description должно содержать от 20 до 2000 символом")
    private String description;
    @EventDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    LocationDto location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventStateAdminAction stateAction;

    @Size(min = 3, max = 120, message = "поле title должно содержать от 3 до 120 символом")
    private String title;
}
