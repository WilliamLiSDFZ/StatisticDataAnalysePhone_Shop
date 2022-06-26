package utils;

import java.util.Arrays;

/**
 * @author WilliamLi
 * @version: 1.0
 * @date 2022/6/25 22:22
 */
public class Tools {

    /**
     * 计算数组中百分位数。
     *
     * @param data 数组
     * @param p    百分位， 范围0~1
     * @return 第p百分位数
     */
    public static double percentile(double[] data, double p) {
        int n = data.length;
        Arrays.sort(data);
        double px = p * (n - 1);
        int i = (int) java.lang.Math.floor(px);
        double g = px - i;
        if (g == 0) {
            return data[i];
        } else {
            return (1 - g) * data[i] + g * data[i + 1];
        }
    }
}
