package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.scheme.Put;
import com.packing3d.datastructure.space.Space;
import com.packing3d.datastructure.state.State;

import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-11-05 14-12-23
 * @Description 方法太多进行了 全面整理， 把效果好的放前面好看
*/
public class BlockSelector {

    /**
     * @Author yajiewen
     * @Date 2022-10-22 09-51-36
     * @Description 更新，使得更快
     * 返回能放入空间的块的列表
    */
    public static ArrayList<Block> searchViableBlock(Space space, ArrayList<Block> blockList){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        for (Block block : blockList) {
            if(block.blockLenght <= space.spaceLength &&
                    block.blockWidth <= space.spaceWidth &&
                    block.blockHigh <= space.spaceHigh){
                viableBlockList.add(block);
            }
        }
        return viableBlockList;
    }

    /**
     * @Author yajiewen
     * @Date 2023-03-11 19-11-14
     * @Description 获取可以放入空间中的块包括旋转后的
    */

    public static ArrayList<Block> getViableBlock(Space space, ArrayList<Block> blockList){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        for (Block block : blockList) {
            if(block.blockLenght <= space.spaceLength &&
                    block.blockWidth <= space.spaceWidth &&
                    block.blockHigh <= space.spaceHigh){
                viableBlockList.add(block.cloneOne());
            }
            if(block.blockLenght <= space.spaceWidth &&
                    block.blockWidth <= space.spaceLength &&
                    block.blockHigh <= space.spaceHigh){
                viableBlockList.add(block.getRotatedBlock());
            }
        }
        return viableBlockList;
    }


    /**
     * @Author yajiewen
     * @Date 2022-10-22 09-49-39
     * @Description 优化fbr，因为放不进去的块不需要计算fbr
     */
    /**
     * @Author yajiewen
     * @Date 2023-03-11 19-17-04
     * @Description 考虑块旋转 取 块的两种放置中分数大的那一个
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitByVCS(Space space, State state, int number){
        ArrayList<Block> viableBlockList = getViableBlock(space, state.blockList);
        if(!viableBlockList.isEmpty()){
            // 更新获取MaxUsefulVector 向量
            generateMaxUsefulVector(state, space);
            for (Block block : viableBlockList) {
                block.fbrScore = vcsScore(1, 2, 0.2, 0.03, block, space, state);
            }

            ArrayList<Block> returnList = new ArrayList<>();
            int minLen = Math.min(number, viableBlockList.size());
            // 简单选择排序选出最大的minL个块,放在块表前面
            for(int i = 0; i < minLen; i++){
                int maxIndex = i;
                for(int j = i + 1; j < viableBlockList.size(); j++){
                    if(viableBlockList.get(maxIndex).fbrScore < viableBlockList.get(j).fbrScore){
                        maxIndex = j;
                    }
                }
                returnList.add(viableBlockList.get(maxIndex));
                // 交换
                Block blockTemp = viableBlockList.get(i);
                viableBlockList.set(i, viableBlockList.get(maxIndex));
                viableBlockList.set(maxIndex,blockTemp);
            }
            return returnList;
        }else{
            return viableBlockList;
        }
    }

    /**
     * @Author yajiewen
     * @Date 2023-02-16 14-18-48
     * @Description 为了做实验 添加个fbr
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitByFbr(Space space, State state, int number){
        ArrayList<Block> viableBlockList = searchViableBlock(space, state.blockList);
        if(!viableBlockList.isEmpty()){
            // 更新获取MaxUsefulVector 向量
            generateMaxUsefulVector(state, space);
            for (Block block : viableBlockList) {
                block.fbrScore = fbrScore(block, space, state);
            }

            ArrayList<Block> returnList = new ArrayList<>();
            int minLen = Math.min(number, viableBlockList.size());
            // 简单选择排序选出最大的minL个块,放在块表前面
            for(int i = 0; i < minLen; i++){
                int maxIndex = i;
                for(int j = i + 1; j < viableBlockList.size(); j++){
                    if(viableBlockList.get(maxIndex).fbrScore < viableBlockList.get(j).fbrScore){
                        maxIndex = j;
                    }
                }
                returnList.add(viableBlockList.get(maxIndex));
                // 交换
                Block blockTemp = viableBlockList.get(i);
                viableBlockList.set(i, viableBlockList.get(maxIndex));
                viableBlockList.set(maxIndex,blockTemp);
            }
            return returnList;
        }else{
            return viableBlockList;
        }
    }



    /**
     * @Author yajiewen
     * @Date 2022-11-05 13-57-49
     * @Description 求vloss 单独写一个函数
     */
    public static double getVlossByMaxUsefulVector(Space space, Block block, State state){
        double vloss;
        int lMax = state.xUsefulVector[(int) (space.spaceLength - block.blockLenght) ];
        int wMax = state.yUsefulVector[(int) (space.spaceWidth - block.blockWidth) ];
        int hMax = state.zUsefulVector[(int) (space.spaceHigh - block.blockHigh) ];
        vloss = space.spaceVolum - (block.blockLenght + lMax) * (block.blockWidth + wMax) * (block.blockHigh + hMax);
        return vloss;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 21-01-40
     * @Description 重大发现，计算vLossVector VCS: A new heuristic function for selecting boxes in the single 老天保佑
     */
    /**
     * @Author yajiewen
     * @Date 2022-11-05 14-28-40
     * @Description 更换名字
    */
    public static void generateMaxUsefulVector(State state, Space space){

        // 定义剩余箱子的 长 宽 高 长度表
        ArrayList<Integer> lengthList = new ArrayList<>();
        ArrayList<Integer> widthList = new ArrayList<>();
        ArrayList<Integer> hightList = new ArrayList<>();

        Box box;
        for(int i = 0; i < state.availBox.length; i++){
            if(state.availBox[i] > 0){
                box = state.problem.boxList.get(i);
                if(box.boxLength <= space.spaceLength){
                    lengthList.add((int) box.boxLength);
//                    for(int j = 0; j < state.availBox[i]; j++){
//                        lengthList.add((int) box.boxLength);
//                    }
                }
                if(box.boxWidth <= space.spaceWidth){
                    widthList.add((int) box.boxWidth);
//                    for(int j = 0; j < state.availBox[i]; j++){
//                        widthList.add((int) box.boxWidth);
//                    }
                }
                if(box.boxHigh <= space.spaceHigh){
                    hightList.add((int) box.boxHigh);
//                    for(int j = 0; j < state.availBox[i]; j++){
//                        hightList.add((int) box.boxHigh);
//                    }
                }
            }
        }
        // 计算x方向上的vloss 向量 不需要取等号， 剩余长度为0的最大线性组合也要放进去，因为箱子长度不能为0所以不需要取等号
        state.xUsefulVector = BlockSelector.KPAForArrayList((int)space.spaceLength,lengthList);
        state.yUsefulVector = BlockSelector.KPAForArrayList((int)space.spaceWidth,widthList);
        state.zUsefulVector = BlockSelector.KPAForArrayList((int)space.spaceHigh,hightList);
    }



    /**
     * @Author yajiewen
     * @Date 2022-11-05 11-36-31
     * @Description 计算两个几乎相接触（距离小于p l）的块A,B 的A与B接触的面积
    */
    /**
     * @Author yajiewen
     * @Date 2022-11-05 22-53-57
     * @Description 重大修复原作者写得不对我们的正确
    */
    /**
     * @Author yajiewen
     * @Date 2022-11-05 23-47-15
     * @Description 加上等号，去除交集为一个点的情况，效果直接爆炸提升
    */
//    public static double surface_in_contact(Block blockA,Block blockB,double p){
//        double s = 0;
//        double xMin,xMax,yMin,yMax,zMin,zMax,length = 0,width = 0,high = 0;
//        boolean isX = false,isY = false, isZ = false;
//        // 判断x方向是否有交集
//        if(!(blockA.xRange[1] <= blockB.xRange[0] || blockB.xRange[1] <= blockA.xRange[0])){
//            xMin = Math.max(blockA.xRange[0], blockB.xRange[0]);
//            xMax = Math.min(blockA.xRange[1], blockB.xRange[1]);
//            length = xMax - xMin;
//            isX = true;
//        }
//        // 判断y方向是否有交集
//        if(!(blockA.yRange[1] <= blockB.yRange[0] || blockB.yRange[1] <= blockA.yRange[0])){
//            yMin = Math.max(blockA.yRange[0], blockB.yRange[0]);
//            yMax = Math.min(blockA.yRange[1], blockB.yRange[1]);
//            width = yMax - yMin;
//            isY = true;
//        }
//        // 判断z方向是否有交集
//        if(!(blockA.zRange[1] <= blockB.zRange[0] || blockB.zRange[1] <= blockA.zRange[0])){
//            zMin = Math.max(blockA.zRange[0], blockB.zRange[0]);
//            zMax = Math.min(blockA.zRange[1], blockB.zRange[1]);
//            high = zMax - zMin;
//            isZ = true;
//        }
//        if(isX && isY){
//            s = length * width;
//        }else if(isX && isZ){
//            s = length * high;
//        }else if(isY && isZ){ // 这句话要加 会出现 三个方向都无交集
//            s = width * high;
//        }
//
//        // 作者论文中描述感觉不对
////        if(blockA.xRange[1] + p * blockA.blockLenght >= blockB.xRange[0] ||
////                blockB.xRange[1] + p * blockA.blockLenght >= blockA.xRange[0]){
////            s = (yMax - yMin) * (zMax - zMin);
////        }else if(blockA.yRange[1] + p * blockA.blockWidth >= blockB.yRange[0] ||
////                blockB.yRange[1] + p * blockA.blockWidth >= blockA.yRange[0]){
////            s = (xMax - xMin) * (zMax - zMin);
////        }else if(blockA.zRange[1] + p * blockA.blockHigh >= blockB.zRange[0] ||
////                blockB.zRange[1] + p * blockA.blockHigh >= blockA.zRange[0]){
////            s = (xMax - xMin) * (yMax - yMin);
////        }
//
//        return s;
//    }

    /**
     * @Author yajiewen
     * @Date 2023-02-17 19-47-34
     * @Description 改进的求接触面积
    */
    public static double surface_in_contact(Block blockA,Block blockB,double p){
        double s = 0;
        double xMin,xMax,yMin,yMax,zMin,zMax,length = 0,width = 0,high = 0;
        // 判断x方向是否有交集
            xMin = Math.max(blockA.xRange[0], blockB.xRange[0]);
            xMax = Math.min(blockA.xRange[1], blockB.xRange[1]);
            length = xMax - xMin;

        // 判断y方向是否有交集
            yMin = Math.max(blockA.yRange[0], blockB.yRange[0]);
            yMax = Math.min(blockA.yRange[1], blockB.yRange[1]);
            width = yMax - yMin;

        // 判断z方向是否有交集

            zMin = Math.max(blockA.zRange[0], blockB.zRange[0]);
            zMax = Math.min(blockA.zRange[1], blockB.zRange[1]);
            high = zMax - zMin;

        if(length > 0 && width > 0){
            s = length * width;
        }else if(length > 0 && high > 0){
            s = length * high;
        }else if(width > 0 && high > 0){ // 这句话要加 会出现 三个方向都无交集
            s = width * high;
        }
        return s;
    }
    /**
     * @Author yajiewen
     * @Date 2022-11-05 13-03-05
     * @Description 求容器中与块A几乎相接触的块
    */
    /**
     * @Author yajiewen
     * @Date 2023-02-16 12-54-36
     * @Description 优化
    */
    public static ArrayList<Block> get_adjacent_blocks(Block blockA, State state, double p){
        double lp = blockA.blockLenght * p;
        double wp = blockA.blockWidth * p;
        double hp = blockA.blockHigh * p;
        ArrayList<Block> adjacentBlockList = new ArrayList<>();
        for (Put put : state.scheme.putList) {
            Block blockB = put.block;
            if(isContact(blockA, blockB, lp, wp, hp)){
                adjacentBlockList.add(blockB);
            }
        }
        return adjacentBlockList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-11-05 15-08-20
     * @Description 判断块，A与B 是否几乎接触, 也就是三个方向都要有交集
    */
    /**
     * @Author yajiewen
     * @Date 2022-11-06 00-00-04
     * @Description 修复 加p 的时候 范围两端都要加 加了以后效果直接起飞 效果直接干到95牛逼啦
     * 原文只在一边加
    */
    /**
     * @Author yajiewen
     * @Date 2023-02-16 12-54-44
     * @Description 优化
    */
    public static boolean isContact(Block blockA, Block blockB, double lp, double wp, double hp){

        if(blockA.xRange[1] + lp < blockB.xRange[0] || blockB.xRange[1] < blockA.xRange[0] - lp){
            return false;
        }

        if(blockA.yRange[1] + wp < blockB.yRange[0] || blockB.yRange[1] < blockA.yRange[0] - wp){
            return false;
        }

        if(blockA.zRange[1] + hp < blockB.zRange[0] || blockB.zRange[1] < blockA.zRange[0] - hp){
            return false;
        }
        return true;
    }



    /**
     * @Author yajiewen
     * @Date 2022-11-05 13-18-51
     * @Description 计算块与容器接触的面积（几乎相接触的面积）
    */
    /**
     * @Author yajiewen
     * @Date 2022-11-05 23-51-21
     * @Description 优化减少重复计算
    */
    public static double surface_in_contact_with_the_container(Block blockA, State state, double p){
        double s = 0;
        // 左面
        double pl = p * blockA.blockLenght;
        if(blockA.xRange[0] <= pl){
            s += blockA.blockWidth * blockA.blockHigh;
        }
        // 右面
        if(blockA.xRange[1] >= state.problem.containnerLength - pl){
            s += blockA.blockWidth * blockA.blockHigh;
        }
        // 后面
        double pw = p * blockA.blockWidth;
        if(blockA.yRange[0] <= pw){
            s += blockA.blockLenght * blockA.blockHigh;
        }
        // 前面
        if(blockA.yRange[1] >= state.problem.containnerWidth - pw){
            s += blockA.blockLenght * blockA.blockHigh;
        }
        // 下面
        double pz = p * blockA.blockHigh;
        if(blockA.zRange[0] <= pz){
            s += blockA.blockLenght * blockA.blockWidth;
        }
        // 上面
        if(blockA.zRange[1] >= state.problem.containnerHigh - pz){
            s += blockA.blockLenght * blockA.blockWidth;
        }
        return s;
    }
    /**
     * @Author yajiewen
     * @Date 2022-11-05 13-48-15
     * @Description 获取CS分数
    */
    public static double getCS(double p, State state, Block block){
        double s = 0;
        ArrayList<Block> adjacentBlockList = get_adjacent_blocks(block, state, p);
        for (Block blockB : adjacentBlockList) {
            s += surface_in_contact(block, blockB, p);
        }
        s += surface_in_contact_with_the_container(block, state, p);

        return s/ block.superficialArea;
    }

    /**
     * @Author yajiewen
     * @Date 2022-11-05 13-45-34
     * @Description 求VCS分数
    */
    public static double vcsScore(double a, double b, double r, double p, Block block, Space space, State state){
        // 计算分数前一定要先将块假装放入空间中，更新块的八点坐标和范围
        block.initCoordinateAndRangeByAnchorCorner(space);
        double vb = block.blockVolum;
        double cs = getCS(p,state,block);
        double lbr = getVlossByMaxUsefulVector(space, block , state) / space.spaceVolum;

        return vb * Math.pow(cs, a) * Math.pow((1 - lbr), b) * Math.pow(block.boxNum, -1 * r);
    }

    /**
     * @Author yajiewen
     * @Date 2023-02-16 14-19-20
     * @Description fbr 分数
    */
    public static double fbrScore( Block block, Space space, State state){
        // 计算分数前一定要先将块假装放入空间中，更新块的八点坐标和范围
        double vLoss =getVlossByMaxUsefulVector(space, block , state);
        double vWaste = block.blockVolum - block.boxVolum;
        return block.boxVolum - vLoss - vWaste;
    }


    /**
     * @Author yajiewen
     * @Date 2022-10-19 18-16-49
     * @Description 判断一个箱子能不能被放入空间
    */
    public static boolean canBePutIn(double length, double width, double high, double spaceLength, double spaceWidth, double spaceHigh){
        return length <= spaceLength && width <= spaceWidth && high <= spaceHigh;
    }


    /**
     * @Author yajiewen
     * @Date 2022-10-20 00-41-38
     * @Description 重大改进 直接返回最后的结果数组 就是 每一张线性组合可能
    */
    public static int[] KPAForArrayList(int totalLength, ArrayList<Integer> lengthList){
        int[] outComeList = new int[totalLength + 1]; // 用于存放中间过程

        for(int i = 0; i < lengthList.size(); i++){
            for(int j = totalLength; j >= lengthList.get(i); j--){
                outComeList[j] = Math.max(outComeList[j], outComeList[ j - lengthList.get(i) ] + lengthList.get(i));
            }
        }
        return outComeList;
    }
}
