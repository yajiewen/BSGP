package com.packing3d.application;

/**
 * @Author yajiewen
 * @Date 2022-10-21 13-53-45
 * @Description ArrayList 容量初始化非常重要
*/
/**
 * @Author yajiewen
 * @Date 2022-10-22 22-01-55
 * @Description 增加这配置文件方便运行
*/

/**
 * @Author yajiewen
 * @Date 2022-10-23 17-04-51
 * @Description 改代码该配置现在是巅峰
*/
public class InitHelper {
    public static String TITLE  = "BIBSGP SUPPORT 150S"; // 本次运行开头输出内容
    public static int RUN_TIME_SECOND_LIMIT = 150; // 一次搜索的运行时间
//    public static int RUN_WAY = Param.MULTI_THREADING; //
    public static int RUN_WAY = Param.SINGLE_THREADING; //

//    public static int TASK = Param.TASK0_15; // 多线程的时候跑那几个数据集
    public static int TASK = Param.TASK8_15;
//    public static int TASK = Param.TASK0_7;

    public static boolean STATE_CHECK = false; // 是否开启状态检测

    // 定义ArrayList 对于空间和块的初始化大小 使得ArrayList 更快
    public static int INIT_CAPACITY_BLOCKLIST = 10002;
    public static int INIT_CAPACITY_SPACELIST = 1000;
    public static int INIT_CAPACITY_PUTLIST = 60;

    public static void printParameter(){
        System.out.println(TITLE);
        System.out.println("***运行时间：" + RUN_TIME_SECOND_LIMIT + "秒");

        if(RUN_WAY == Param.SINGLE_THREADING){
            System.out.println("***单线程运行");
        }
        if(RUN_WAY == Param.MULTI_THREADING){
            System.out.println("***多线程运行");
            if(TASK == Param.TASK0_7){
                System.out.println("***TASK 0_7");
            }else if(TASK == Param.TASK8_15){
                System.out.println("***TASK 8_15");
            }else if(TASK == Param.TASK0_15){
                System.out.println("***TASK 1_15");
            }
        }
        if(STATE_CHECK){
            System.out.println("***开启状态检测");
        }
        System.out.println();
    }
}
