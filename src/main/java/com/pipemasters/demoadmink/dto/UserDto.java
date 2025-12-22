package com.pipemasters.demoadmink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для пользователя")
public class UserDto {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "John Doe")
    private String name;

    @Schema(description = "Email пользователя", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Возраст пользователя", example = "25")
    private Integer age;
}
