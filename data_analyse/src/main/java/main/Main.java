package main;

import utils.Analyzer;

/**
 * 程序主入口。
 *
 * @author WilliamLi
 * @version: 1.0
 * @date 2022/6/22 1:30
 */
public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Analyzer analyzer = new Analyzer(args[0]);
        analyzer.analysis();
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - start) + "(ms)");
    }

}
