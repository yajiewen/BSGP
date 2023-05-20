package com.packing3d.datastructure.problem;

import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-04-04 16-51-53
 * @Description 带 容器八点坐标的problem
*/
public class Problem {
    public double containnerLength;// 该问题中容器的长度
    public double containnerWidth;// 该问题中容器的宽度
    public double containnerHigh;// 该问题中容器的高度
    public int typeNumberOfBox;//箱子的类别数
    public ArrayList<Box> boxList;// 每种类别的箱子的信息构成的列表
    public double[][] containerEightCoordinate; // 容器的八点坐标

    public Problem(){
        boxList = new ArrayList<>();
        containerEightCoordinate = new double[8][3];
    }
    // 构造函数2
    public Problem(double containnerLength,double containnerWidth, double containnerHigh, int typeNumberOfBox){
        boxList = new ArrayList<>();
        containerEightCoordinate = new double[8][3];
        this.containnerLength = containnerLength;
        this.containnerWidth = containnerWidth;
        this.containnerHigh = containnerHigh;
        this.typeNumberOfBox = typeNumberOfBox;

        containerEightCoordinate[0][0] = 0;
        containerEightCoordinate[0][1] = 0;
        containerEightCoordinate[0][2] = 0;

        containerEightCoordinate[1][0] = containerEightCoordinate[0][0] + containnerLength;
        containerEightCoordinate[1][1] = containerEightCoordinate[0][1];
        containerEightCoordinate[1][2] = containerEightCoordinate[0][2];

        containerEightCoordinate[2][0] = containerEightCoordinate[0][0] + containnerLength;
        containerEightCoordinate[2][1] = containerEightCoordinate[0][1] + containnerWidth;
        containerEightCoordinate[2][2] = containerEightCoordinate[0][2];

        containerEightCoordinate[3][0] = containerEightCoordinate[0][0];
        containerEightCoordinate[3][1] = containerEightCoordinate[0][1] + containnerWidth;
        containerEightCoordinate[3][2] = containerEightCoordinate[0][2];
        // 剩下四点的坐标等于前面四个点的坐标的z 值加上块的高度
        for (int i = 0; i < 4; i++) {
            containerEightCoordinate[i + 4][0] = containerEightCoordinate[i][0];
            containerEightCoordinate[i + 4][1] = containerEightCoordinate[i][1];
            containerEightCoordinate[i + 4][2] = containerEightCoordinate[i][2] + containnerHigh;
        }
    }

    @Override
    public String toString() {
        return "Problem{" +
                "containnerLength=" + containnerLength +
                ", containnerWidth=" + containnerWidth +
                ", containnerHigh=" + containnerHigh +
                ", typeNumberOfBox=" + typeNumberOfBox +
                ", boxList=" + boxList +
                '}';
    }
}
