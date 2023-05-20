package com.packing3d.datastructure.space;

import com.packing3d.datastructure.problem.Problem;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author yajiewen
 * @Date 2022-05-14 10-51-45
 * @Description 支持Manhanttan distance
*/
public class Space{
    public double locationX; // 空间左后下角的X坐标
    public double locationY; // 空间左后下角的Y坐标
    public double locationZ; // 空间左后下角的Z坐标
    public double spaceLength; // 空间的长
    public double spaceWidth; // 空间的宽
    public double spaceHigh; // 空间的高

    public double spaceVolum; // 空间体积

    // 定义一个二维数组来保存八点坐标
    public double[][] eightCoordinate;
    // 定义保存x范围的数组
    public double[] xRange;
    // 定义保存y范围的数组
    public double[] yRange;
    // 定义保存z范围的数组
    public double[] zRange;

    // 定义anchorDistance
    public double anchorDistance;
    // 定义anchor corner
    public int anchorCorner;

    // 使用0点坐标 和空间的长宽高 生成带 anchordistanse 和 corner 的空间
    public Space(double locationX, double locationY, double locationZ, double spaceLength, double spaceWidth, double spaceHigh, Problem problem) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.locationZ = locationZ;
        this.spaceLength = spaceLength;
        this.spaceWidth = spaceWidth;
        this.spaceHigh = spaceHigh;
        this.spaceVolum = spaceLength * spaceWidth * spaceHigh;
        // 在构造函数里面分配空间
        // 坐标空间
        eightCoordinate = new double[8][3];
        // x范围空间
        xRange = new double[2];
        // y范围空间
        yRange = new double[2];
        // z范围空间
        zRange = new double[2];

        // 获取8点坐标
        eightCoordinate[0][0] = locationX;
        eightCoordinate[0][1] = locationY;
        eightCoordinate[0][2] = locationZ;

        eightCoordinate[1][0] = eightCoordinate[0][0] + spaceLength;
        eightCoordinate[1][1] = eightCoordinate[0][1];
        eightCoordinate[1][2] = eightCoordinate[0][2];

        eightCoordinate[2][0] = eightCoordinate[0][0] + spaceLength;
        eightCoordinate[2][1] = eightCoordinate[0][1] + spaceWidth;
        eightCoordinate[2][2] = eightCoordinate[0][2];

        eightCoordinate[3][0] = eightCoordinate[0][0];
        eightCoordinate[3][1] = eightCoordinate[0][1] + spaceWidth;
        eightCoordinate[3][2] = eightCoordinate[0][2];
        // 剩下四点的坐标等于前面四个点的坐标的z 值加上块的高度
        for (int i = 0; i < 4; i++) {
            eightCoordinate[i + 4][0] = eightCoordinate[i][0];
            eightCoordinate[i + 4][1] = eightCoordinate[i][1];
            eightCoordinate[i + 4][2] = eightCoordinate[i][2] + spaceHigh;
        }
        // 获取x,y,z 范围
        xRange[0] = eightCoordinate[0][0];
        xRange[1] = eightCoordinate[1][0];
        yRange[0] = eightCoordinate[0][1];
        yRange[1] = eightCoordinate[3][1];
        zRange[0] = eightCoordinate[0][2];
        zRange[1] = eightCoordinate[4][2];

        getManhanttanDistanceAndAnchorCorner(problem);

    }

    // 通过八点坐标生成空间 生成呆 anchordistanse 和 corner 的空间2
    public Space(double[][] eightCoordinate,Problem problem){
        // 在构造函数里面分配空间
        // x范围空间
        xRange = new double[2];
        // y范围空间
        yRange = new double[2];
        // z范围空间
        zRange = new double[2];
        this.locationX = eightCoordinate[0][0];
        this.locationY = eightCoordinate[0][1];
        this.locationZ = eightCoordinate[0][2];

        this.spaceLength = eightCoordinate[1][0] - eightCoordinate[0][0];
        this.spaceWidth = eightCoordinate[3][1] - eightCoordinate[0][1];
        this.spaceHigh = eightCoordinate[4][2] - eightCoordinate[0][2];
        this.spaceVolum = this.spaceLength * this.spaceWidth * this.spaceHigh;
        // 获取x,y,z 范围
        xRange[0] = eightCoordinate[0][0];
        xRange[1] = eightCoordinate[1][0];
        yRange[0] = eightCoordinate[0][1];
        yRange[1] = eightCoordinate[3][1];
        zRange[0] = eightCoordinate[0][2];
        zRange[1] = eightCoordinate[4][2];

        this.eightCoordinate = eightCoordinate;

        getManhanttanDistanceAndAnchorCorner(problem);

    }

    public void getManhanttanDistanceAndAnchorCorner(Problem problem){
        // 定义八点距离(非完全支撑用八点距离) 完全支撑只能下面四个点
        double[] eightCornerDistance = new double[8];
        // 计算八点距离(这里只求0123 这四个点,因为完全支撑不考虑悬浮放置)
        for (int i = 0; i < 8; i++) {
            eightCornerDistance[i] = Math.abs(problem.containerEightCoordinate[i][0] - this.eightCoordinate[i][0]) +
                                        Math.abs(problem.containerEightCoordinate[i][1] - this.eightCoordinate[i][1]) +
                                        Math.abs(problem.containerEightCoordinate[i][2] - this.eightCoordinate[i][2]);
        }
        // 得到Anchor distance 和 anchor corner
        this.anchorCorner = 0;
        for (int i = 1; i < 8; i++) {
            if(eightCornerDistance[this.anchorCorner] > eightCornerDistance[i]){
                this.anchorCorner = i;
            }
        }
        this.anchorDistance = eightCornerDistance[this.anchorCorner];
    }

    // 判断通过八点坐标生成的空间的体积是否为0
    public boolean isZeroV(){
        if(spaceHigh == 0 || spaceWidth == 0 || spaceLength == 0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Space space = (Space) o;
        return Double.compare(space.locationX, locationX) == 0 && Double.compare(space.locationY, locationY) == 0 && Double.compare(space.locationZ, locationZ) == 0 && Double.compare(space.spaceLength, spaceLength) == 0 && Double.compare(space.spaceWidth, spaceWidth) == 0 && Double.compare(space.spaceHigh, spaceHigh) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationX, locationY, locationZ, spaceLength, spaceWidth, spaceHigh);
    }

    @Override
    public String toString() {
        return "Space{" +
                "locationX=" + locationX +
                ", locationY=" + locationY +
                ", locationZ=" + locationZ +
                ", xRange=" + Arrays.toString(xRange) +
                ", yRange=" + Arrays.toString(yRange) +
                ", zRange=" + Arrays.toString(zRange) +
                '}';
    }
}
