package utils;

import com.william.util.FileHelper;
import org.apache.commons.math3.distribution.TDistribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 此类用于处理实验数据。<br/>
 * 进行置信区间计算的时候使用到了apache 的commons-maths-3.6.1.jar作为外部依赖。
 * 读取文本数据使用到了FileHelper.jar作为外部依赖。
 *
 * @author WilliamLi
 * @version: 1.0
 * @date 2022/6/22 1:34
 * @see DataPoint
 * @see FileHelper
 */
public class Analyzer {

    private FileHelper fileHelper = null;

    private double x_mean = 0;

    private double y_mean = 0;

    private DataPoint median = null;

    private DataPoint first_q = null;

    private DataPoint thrid_q = null;

    private double IQRX = 0;

    private double IQRY = 0;

    private double x_min = Double.MAX_VALUE;

    private double x_max = Double.MIN_VALUE;

    private double y_min = Double.MAX_VALUE;

    private double y_max = Double.MIN_VALUE;

    private double x_sd_squire = 0;

    private double y_sd_squire = 0;

    private double x_sd = 0;

    private double y_sd = 0;

    private double r = 0;

    private double a = 0;

    private double b = 0;

    double test_statistic = 0;

    double SE = 0;

    double upperBound = 0;

    double lowerBound = 0;

    private double[][] residuals = null;

    /**
     * 有参数构造方法，传入原始数据文件位置。
     *
     * @param path 原始数据文件位置
     */
    public Analyzer(String path) {
        fileHelper = new FileHelper(path);
    }

    /**
     * 数据处理方法。
     */
    public void analysis() {
        try {
            //读取所需所有数据
            List<String> dataList = fileHelper.bufferFileList();

            String[][] stringAllData = new String[dataList.size() - 1][11];
            for (int i = 1; i < dataList.size(); i++) {
                stringAllData[i - 1] = dataList.get(i).split("\t");
            }

            List<DataPoint> dataPointList = new ArrayList<>(stringAllData.length);
            List<DataPoint> residualList = new ArrayList<>(stringAllData.length);
            long[][] neededData = new long[stringAllData.length][5];

            int index = 0;
            for (int i = 0; i < stringAllData.length; i++) {
                for (int j = 0; j < 5; j++) {
                    neededData[i][j] = Long.parseLong(stringAllData[i][j + 6]);
                }
                dataPointList.add(new DataPoint(neededData[i][1], neededData[i][4], index));
                index++;
            }

            //开始计算，为计算残差和移除outlier做准备
            calculateNums(dataPointList);

            //开始计算残差
            calculateResidual(dataPointList, residualList);

            //移除outlier
            calculatorOutlier(residualList);
            removeOutlier(dataPointList, residualList);

            //再次计算
            calculateNums(dataPointList);

            //计算置信区间
            confidentInterval(dataPointList);

            //打印结果
            printValues(dataPointList);
            printDataList(dataPointList);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            dispose();
        }
    }

    /**
     * 基础数据分析。
     *
     * @param dataPointList 实验结果列表
     */
    private void calculateNums(List<DataPoint> dataPointList) {

        x_max = Double.MIN_VALUE;
        x_min = Double.MAX_VALUE;
        y_max = Double.MIN_VALUE;
        y_min = Double.MAX_VALUE;
        long x_sum = 0;
        long y_sum = 0;
        double x_sd_temp = 0;
        double y_sd_temp = 0;

        //获取最大最小值。
        for (DataPoint point : dataPointList) {
            x_sum += point.getX();
            y_sum += point.getY();
            if (point.getX() < x_min)
                x_min = point.getX();
            if (point.getX() > x_max)
                x_max = point.getX();
            if (point.getY() < y_min)
                y_min = point.getY();
            if (point.getY() > y_max)
                y_max = point.getY();
        }

        //计算均值。
        x_mean = (double) x_sum / dataPointList.size();
        y_mean = (double) y_sum / dataPointList.size();

        //计算标准差。
        for (DataPoint point : dataPointList) {
            x_sd_temp += Math.pow(point.getX() - x_mean, 2);
            y_sd_temp += Math.pow(point.getY() - y_mean, 2);
        }
        x_sd_squire = x_sd_temp / (dataPointList.size() - 1);
        y_sd_squire = y_sd_temp / (dataPointList.size() - 1);
        x_sd = Math.sqrt(x_sd_squire);
        y_sd = Math.sqrt(y_sd_squire);

        //计算线性回归相关系数 r。
        double temp_sum = 0;
        for (DataPoint point : dataPointList) {
            temp_sum += ((point.getX() - x_mean) / x_sd) * ((point.getY() - y_mean) / x_sd);
        }
        r = temp_sum / (dataPointList.size() - 1);

        //计算回归线斜率b以及截距a。
        b = r * (y_sd / x_sd);
        a = y_mean - b * x_mean;
    }

    /**
     * 计算残差。
     *
     * @param dataPointList 实验结果列表
     * @param residualList  残差列表
     */
    private void calculateResidual(List<DataPoint> dataPointList, List<DataPoint> residualList) {

        residuals = new double[dataPointList.size()][2];

        int count = 0;
        for (DataPoint point : dataPointList) {
            double y_hat = a + b * point.getX();
            double residual = point.getY() - y_hat;
            residuals[count][0] = point.getX();
            residuals[count][1] = residual;
            residualList.add(new DataPoint(residuals[count][0], residuals[count][1], point.getOtherVal()[0]));
            count++;
        }
    }

    /**
     * 计算Outlier。
     *
     * @param dataPointList 实验结果列表
     */
    private void calculatorOutlier(List<DataPoint> dataPointList) {
        int length = dataPointList.size();
        boolean isOdd = (length % 2) != 0;                          //计算数据量是否为奇数，后续需要分类讨论。
        double[] xs = new double[dataPointList.size()];
        double[] ys = new double[dataPointList.size()];
        for (int i = 0; i < length; i++) {
            xs[i] = dataPointList.get(i).getX();
            ys[i] = dataPointList.get(i).getY();
        }

        Arrays.sort(xs);
        Arrays.sort(ys);

        if (isOdd) {
            int medianIndex = length / 2 + 1;
            median = new DataPoint(xs[medianIndex], ys[medianIndex]);
        } else {
            median = new DataPoint(
                    (xs[length / 2] + xs[length / 2 + 1]) / 2,
                    (ys[length / 2] + ys[length / 2 + 1]) / 2
            );
        }
        first_q = new DataPoint(Tools.percentile(xs, 0.25), Tools.percentile(ys, 0.25));               //定义Q1
        thrid_q = new DataPoint(Tools.percentile(xs, 0.75), Tools.percentile(ys, 0.75));               //定义Q3
        IQRX = thrid_q.getX() - first_q.getX();                                                             //X轴IQR
        IQRY = thrid_q.getY() - first_q.getY();                                                             //Y轴IQR
    }

    /**
     * 移除Outlier。<br/>
     * 依据1.5*IQR rule判断outlier并移除。
     *
     * @param dataPointList 实验结果列表
     * @param residualList  残差列表
     */
    private void removeOutlier(List<DataPoint> dataPointList, List<DataPoint> residualList) {

        for (int i = 0; i < residualList.size(); ) {
            DataPoint point = residualList.get(i);
            if ((point.getY() < (first_q.getY() - IQRY)) || (point.getY() > (thrid_q.getY() + IQRY))) {
                residualList.remove(i);
                dataPointList.remove(i);
            } else {
                i++;
            }
        }
    }

    /**
     * 计算置信区间。
     *
     * @param dataPointList 实验结果列表
     */
    private void confidentInterval(List<DataPoint> dataPointList) {
        //引用commons-math3的T分布。
        TDistribution distribution = new TDistribution(dataPointList.size() - 2);

        test_statistic = distribution.inverseCumulativeProbability(0.975);

        double tempS = 0;
        for (DataPoint point : dataPointList) {
            tempS += Math.pow(point.getY() - (a + b * point.getX()), 2);
        }
        double s = Math.sqrt(tempS / (dataPointList.size() - 2));
        SE = s / (x_sd * Math.sqrt(dataPointList.size() - 1));
        upperBound = b + test_statistic * SE;
        lowerBound = b - test_statistic * SE;
    }

    /**
     * 打印实验数据列表。
     *
     * @param list 实验数据列表
     */
    private void printDataList(List<DataPoint> list) {
        System.out.println("start print----------------------------------------");
        System.out.println("{");
        for (DataPoint point : list) {
            System.out.println("\t{" + point.getX() + "\t" + point.getY() + "}");
        }
        System.out.println("}");
        System.out.println("end print------------------------------------------");
    }

    /**
     * 打印分析结果。
     *
     * @param dataPointList 数据列表
     */
    private void printValues(List<DataPoint> dataPointList) {
        System.out.println("After remove outlier: ");
        System.out.println("-----------------------------------------------------");
        System.out.println("Data size: " + dataPointList.size());
        System.out.println("-----------------------------------------------------");
        System.out.println("x mean: " + x_mean);
        System.out.println("y mean: " + y_mean);
        System.out.println("x range: " + x_min + "~" + x_max);
        System.out.println("y range: " + y_min + "~" + y_max);
        System.out.println("x standard deviation: " + x_sd);
        System.out.println("y standard deviation: " + y_sd);
        System.out.println("-----------------------------------------------------");
        System.out.println("r: " + r);
        System.out.println("r-squire: " + Math.pow(r, 2));
        System.out.println("a: " + a);
        System.out.println("b: " + b);
        System.out.println("y^ = " + a + " + " + b + "x");
        System.out.println("-----------------------------------------------------");
        System.out.println("t*: " + test_statistic);
        System.out.println("SE: " + SE);
        System.out.println("Confident interval: ( " + lowerBound + ", " + upperBound + " )");
        System.out.println("-----------------------------------------------------");
    }

    /**
     * 释放资源。
     */
    private void dispose() {
        if (fileHelper != null)
            fileHelper = null;
    }

}
