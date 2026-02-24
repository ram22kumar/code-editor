package com.realtime.code_editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresenceDTO {

    private String userId;
    private String username;
    private String color;
    private Boolean online;
    private Integer cursorPosition;
}