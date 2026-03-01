package com.example.check.define;
import lombok.Data;
@Data
public class FieldRule {
    private String name;
    private boolean required;
    private String type;
    private String min;
    private String max;
    private String length;
}

