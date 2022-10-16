package com.recognition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Window {
    private FigurePoint startPoint;
    private FigurePoint endPoint;

    public Window() {
        this(null, null);
    }
}
