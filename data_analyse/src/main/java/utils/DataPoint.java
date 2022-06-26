package utils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author WilliamLi
 * @version: 1.0
 * @date 2022/6/22 8:27
 */
public class DataPoint {

    private double x = 0;

    private double y = 0;

    private double[] otherVal;

    public DataPoint(){
        this.otherVal = new double[0];
    }

    public DataPoint(double x, double y){
        this();
        this.x = x;
        this.y = y;
    }

    public DataPoint(double x,double y, double... otherVal){
        this(x, y);
        this.otherVal = otherVal;
    }

    public double getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public double[] getOtherVal() {
        return otherVal;
    }

    public void setOtherVal(double[] otherVal) {
        this.otherVal = otherVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPoint dataPoint = (DataPoint) o;
        return x == dataPoint.x && y == dataPoint.y && Arrays.equals(otherVal, dataPoint.otherVal);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(x, y);
        result = 31 * result + Arrays.hashCode(otherVal);
        return result;
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "x=" + x +
                ", y=" + y +
                ", otherVal=" + Arrays.toString(otherVal) +
                '}';
    }
}
