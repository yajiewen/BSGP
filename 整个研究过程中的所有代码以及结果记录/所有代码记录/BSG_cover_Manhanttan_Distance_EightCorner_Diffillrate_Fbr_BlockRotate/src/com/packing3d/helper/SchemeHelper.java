package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.scheme.Put;
import com.packing3d.datastructure.scheme.Scheme;
import com.packing3d.datastructure.state.State;

public class SchemeHelper {
    /**
     * @Author yajiewen
     * @Date 2022-07-23 13-21-40
     * @Description
     * 检测方面:
     * 1.所有放入块的箱子数目是否超过总的可用的箱子数目
     * 2.每一个放置是否满足块可以放入在空间内
     * 3.所有块不能有交叠
     * 4.所有空间不能有包含关系
     * 5.所有放入的块的箱子数目加上剩余箱子数目是否等于总的箱子数
     */
    public static void schemeDetecte(Scheme scheme, Problem problem, State state){
        //1
        int[] puttedBoxNum = new int[problem.typeNumberOfBox];
        for (Put put : scheme.putList) {
            //2
            if(put.block.blockLenght > put.space.spaceLength || put.block.blockHigh > put.space.spaceHigh || put.block.blockWidth > put.space.spaceWidth){
                System.out.println("!!!!!!块无法放置到空间内");
            }
            for (int i = 0; i < problem.typeNumberOfBox; i++) {
                puttedBoxNum[i] += put.block.neededBoxNumOfEachType[i];
            }
        }
        for (int i = 0; i < problem.typeNumberOfBox; i++) {
            if(puttedBoxNum[i] > problem.boxList.get(i).boxNumber){
                System.out.println("!!!!!!放置的箱子数目超过可用的箱子数目");
                break;
            }

            if(puttedBoxNum[i] + state.availBox[i] != problem.boxList.get(i).boxNumber){
                System.out.println("放置的箱子数+剩余箱子数不等于总的箱子数");
            }
        }



        //3 根据原来的块,新生成两个,来解决地址空间是一个的问题
        for (int i = 0; i < scheme.putList.size(); i++) {
            Put putA = scheme.putList.get(i);
            Block blockA = putA.block;
            blockA.initCoordinateAndRangeByAnchorCorner(putA.space);

            for (int j = i + 1; j < scheme.putList.size(); j++) {
                Put putB = scheme.putList.get(j);
                Block blockB = putB.block;
                blockB.initCoordinateAndRangeByAnchorCorner(putB.space);
                boolean isIntersection = isBlockIntersection(blockA,blockB);
                if (isIntersection){
                    System.out.println(putA.space.toString());
                    System.out.println(putB.space.toString());
                    System.out.println(blockA);
                    System.out.println(blockB);
                    System.out.println("!!!!!!块有交叠");
                }
            }
        }

    }

    // 判断块之间是否交叠
    public static boolean isBlockIntersection(Block blockA, Block blockB){
        double[] ixRange = new double[2];
        double[] iyRange = new double[2];
        double[] izRange = new double[2];
        // x
        if(blockA.xRange[1] < blockB.xRange[0] || blockB.xRange[1] < blockA.xRange[0]){
            return false;
        }else {
            ixRange[0] = Math.max(blockA.xRange[0], blockB.xRange[0]);
            ixRange[1] = Math.min(blockA.xRange[1], blockB.xRange[1]);
        }
        // y
        if(blockA.yRange[1] < blockB.yRange[0] || blockB.yRange[1] < blockA.yRange[0]){
            return false;
        }else {
            iyRange[0] = Math.max(blockA.yRange[0], blockB.yRange[0]);
            iyRange[1] = Math.min(blockA.yRange[1], blockB.yRange[1]);
        }
        // z
        if(blockA.zRange[1] < blockB.zRange[0] || blockB.zRange[1] < blockA.zRange[0]){
            return false;
        }else {
            izRange[0] = Math.max(blockA.zRange[0], blockB.zRange[0]);
            izRange[1] = Math.min(blockA.zRange[1], blockB.zRange[1]);
        }
        // 排除体积为0的情况
        if(ixRange[0] == ixRange[1] || iyRange[0] == iyRange[1] || izRange[0] == izRange[1]){
            return false;
        }
        if(blockA == blockB){
            System.out.println("地址相同");
        }
        return true;
    }
}
