package com.recognition;

import lombok.Data;

@Data
public class FigurePoint {
    private int x;
    private int y;

    public FigurePoint(){
        this(0, 0);
    }
    public FigurePoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setXY(int x, int y){
        setX(x);
        setY(y);
    }
    public void setX(int x){
        this.x = x;
    }
    public void setY(int y){
        this.x = y;
    }

    @Override
    public String toString() {
        return String.format("x = %d, y = %d", x, y);
    }
}
