package com.packing3d.datastructure.scheme;

import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-03-12 17-18-47
 * @Description
*/ 
public class Scheme {
    public double totalVolum; // 该方案可以有的总体积
    public ArrayList<Put> putList;

    public Scheme(){
        totalVolum = 0;
        putList = new ArrayList<Put>();
    }

    public void addPut(Put put){
        totalVolum += put.block.boxVolum;
        putList.add(put);
    }

    // 克隆对象
    public Scheme cloneObj(){
        Scheme scheme = new Scheme();
        scheme.totalVolum = this.totalVolum;
        for (Put put : this.putList) {
            scheme.putList.add(put); // 不需要克隆put
        }
        return scheme;
    }

    @Override
    public String toString() {
        return "Scheme{" +
                "totalVolum=" + totalVolum +
                ", putList=" + putList +
                '}';
    }
}
