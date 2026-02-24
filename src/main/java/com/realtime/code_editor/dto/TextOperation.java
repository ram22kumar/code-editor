package com.realtime.code_editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextOperation {

    private String type;
    private Integer position;  // Changed from int to Integer
    private String text;
    private Integer length;    // Changed from int to Integer
    private Long version;      // Changed from long to Long
}