package com.packing3d.datastructure.problem;

import java.util.ArrayList;
/**
 * @Author yajiewen
 * @Date 2022-03-12 17-18-45
 * @Description
*/ 
public class Problem {
    public double containnerLength;// 该问题中容器的长度
    public double containnerWidth;// 该问题中容器的宽度
    public double containnerHigh;// 该问题中容器的高度
    public int typeNumberOfBox;//箱子的类别数
    public ArrayList<Box> boxList;// 每种类别的箱子的信息构成的列表

    public Problem(){
        boxList = new ArrayList<Box>();
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
