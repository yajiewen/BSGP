package com.packing3d.application;
/**
 * @Author yajiewen
 * @Date 2022-03-12 17-18-42
 * @Description
*/ 
public class Ps {
    public int[] psList;
    public int psLength;
    public Ps(int maxSeq){
        psList = new int[maxSeq];
        psLength = 0;
    }

    public void psCopyto(Ps copy){
        copy.psLength = psLength;
        for (int i = 0; i < copy.psList.length; i++) {
            copy.psList[i] = psList[i];
        }
    }
}
