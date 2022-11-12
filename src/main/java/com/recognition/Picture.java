package com.recognition;

import lombok.Data;

import java.util.List;

@Data
public class Picture {
    private List<Figure> figures;
    private String filePath;
}
