package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.space.Space;
import com.packing3d.datastructure.state.State;

import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-04-25 12-06-56
 * @Description 优化代码
 * 更新剩余空间和剩余箱子数目
 */
public class ResourceHelper {
    /**
     * @Author yajiewen
     * @Date 2022-10-21 14-02-43
     * @Description 优化:将删除被选中空间操作移入renewResource
     */
    // 资源(状态)更新(传入的spaceArrayList 是顶部空间去除后的, space 是被去除的放置空间, block是放置快,)
    public static void renewResource(State state, int selectedSpaceIndex, Block block){
        // 更新箱子数目
        renewAvailBoxNum(block, state.availBox);
        // 更新剩余空间
        renewSpaceListTwo(block,selectedSpaceIndex,state.spaceArrayList,state.problem);
        // 更新块表
        renewBlockList(state);
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-07 10-36-15
     * @Description 剩余更新箱子数目
    */
    public static void renewAvailBoxNum(Block block, int[] availNum){
        for (int i = 0; i < availNum.length; i++) {
            availNum[i] -= block.neededBoxNumOfEachType[i];
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-07 10-36-28
     * @Description 更新剩余空间列表
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 01-04-08
     * @Description 重大更新,放入快与其他空间生成的新空间放入剩余空间列表前,先判断是否包含在在某一个列表中的空间内,不包含才放入
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 14-58-18
     * @Description 重新优化剩余空间更新，减少不必要的包含判断
    */
    public static void renewSpaceList(Block block, int selectedSpaceIndex, ArrayList<Space> spaceArrayList, Problem problem){
        // 从空间列表中删除被选中的空间
        Space space = spaceArrayList.remove(selectedSpaceIndex);
        // 把块放在空间的anchor点,根据空间anchor点坐标更新块的八点坐标
        block.initCoordinateAndRangeByAnchorCorner(space);
        // 根据八点坐标得到当前被放置空间的剩余空间
        ArrayList<Space> newSpaceList = renewSpace(space,block,problem);
        // 更新 其余交叠空间
        // 1. 遍历每一个其余空间,判断是否与块有交叠
        // 定义一个列表保存每次更新后的交叠空间
        ArrayList<Space> coveredSpaceList = new ArrayList<>();

        for (int i = spaceArrayList.size() - 1; i >= 0; i--) {
            // 获取交叠快
            Block coverBlock = getCoverBlock(spaceArrayList.get(i), block);
            if(coverBlock != null){ // 有交叠则在list中删除该空间
                Space coverSpace = spaceArrayList.remove(i);
                // 更新该空间的剩余空间,并返回列表
                coveredSpaceList.addAll(renewSpace(coverSpace,coverBlock,problem));
            }
        }

//        // 先把生成的三个剩余空间放入
//        spaceArrayList.addAll(newSpaceList);
//        for (Space coverSpace : coveredSpaceList) {
//            if(!isSpaceContainToOneOfTheSpaceList(spaceArrayList, coverSpace)){
//                spaceArrayList.add(coverSpace);
//            }
//        }

        // 2
//        newSpaceList.addAll(coveredSpaceList);
//        deleteIncludingSpace(newSpaceList);
//        spaceArrayList.addAll(newSpaceList);

        // 3
//        spaceArrayList.addAll(newSpaceList);
//        spaceArrayList.addAll(coveredSpaceList);
//        deleteIncludingSpace(spaceArrayList);
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-22 21-14-11
     * @Description 新的剩余空间列表更新
    */
    public static void renewSpaceListTwo(Block block, int selectedSpaceIndex, ArrayList<Space> spaceArrayList, Problem problem){
        // 从空间列表中删除被选中的空间
        Space space = spaceArrayList.remove(selectedSpaceIndex);
        // 把块放在空间的anchor点,根据空间anchor点坐标更新块的八点坐标
        block.initCoordinateAndRangeByAnchorCorner(space);
        // 根据八点坐标得到当前被放置空间的剩余空间
        ArrayList<Space> newSpaceList = minus(space,block,problem);
        // 更新 其余交叠空间
        ArrayList<Space> coveredSpaceList = new ArrayList<>();

        for (int i = spaceArrayList.size() - 1; i >= 0; i--) {
            Space space1 = spaceArrayList.get(i);
            if(isIntersecting(space1, block)){ // 相交才能用这个减法相减
                coveredSpaceList.addAll(minus(space1, block, problem));
                // 更新后删除原空间
                spaceArrayList.remove(i);
            }
        }

        // 先把生成的三个剩余空间放入
//        spaceArrayList.addAll(newSpaceList);
//        for (Space coverSpace : coveredSpaceList) {
//            if(!isSpaceContainToOneOfTheSpaceList(spaceArrayList, coverSpace)){
//                spaceArrayList.add(coverSpace);
//            }
//        }

        // 2
        newSpaceList.addAll(coveredSpaceList);
        deleteIncludingSpace(newSpaceList);
        spaceArrayList.addAll(newSpaceList);

        // 3
//        spaceArrayList.addAll(newSpaceList);
//        spaceArrayList.addAll(coveredSpaceList);
//        deleteIncludingSpace(spaceArrayList);
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-22 20-20-11
     * @Description space - block
    */
    public static ArrayList<Space> minus(Space space, Block block, Problem problem){
        ArrayList<Space> outcomeSpaceList = new ArrayList<>();
        // 前后左右四个空间
        double spaceMinZ = space.eightCoordinate[0][2];
        double spaceMaxZ = space.eightCoordinate[6][2];
        if(block.eightCoordinate[2][0] < space.eightCoordinate[2][0]){
            double minX = block.eightCoordinate[2][0];
            double minY = space.eightCoordinate[0][1];
            double maxX = space.eightCoordinate[2][0];
            double maxY = space.eightCoordinate[2][1];
            Space space1 = new Space(minX, minY, spaceMinZ, maxX - minX, maxY - minY,spaceMaxZ - spaceMinZ, problem);
            outcomeSpaceList.add(space1);
        }

        if(block.eightCoordinate[2][1] < space.eightCoordinate[2][1]){
            double minX = space.eightCoordinate[0][0];
            double minY = block.eightCoordinate[2][1];
            double maxX = space.eightCoordinate[2][0];
            double maxY = space.eightCoordinate[2][1];
            Space space1 = new Space(minX, minY, spaceMinZ, maxX - minX, maxY - minY,spaceMaxZ - spaceMinZ, problem);
            outcomeSpaceList.add(space1);
        }

        if(space.eightCoordinate[0][0] < block.eightCoordinate[0][0]){
            double minX = space.eightCoordinate[0][0];
            double minY = space.eightCoordinate[0][1];
            double maxX = block.eightCoordinate[0][0];
            double maxY = space.eightCoordinate[2][1];
            Space space1 = new Space(minX, minY, spaceMinZ, maxX - minX, maxY - minY,spaceMaxZ - spaceMinZ, problem);
            outcomeSpaceList.add(space1);
        }

        if(space.eightCoordinate[0][1] < block.eightCoordinate[0][1]){
            double minX = space.eightCoordinate[0][0];
            double minY = space.eightCoordinate[0][1];
            double maxX = space.eightCoordinate[2][0];
            double maxY = block.eightCoordinate[0][1];
            Space space1 = new Space(minX, minY, spaceMinZ, maxX - minX, maxY - minY,spaceMaxZ - spaceMinZ, problem);
            outcomeSpaceList.add(space1);
        }
        // 开始上下两个空间
        double blockMinZ = block.eightCoordinate[0][2];
        double blockMaxZ = block.eightCoordinate[6][2];
        double minX = space.eightCoordinate[0][0];
        double minY = space.eightCoordinate[0][1];
        double maxX = space.eightCoordinate[2][0];
        double maxY = space.eightCoordinate[2][1];
        if(spaceMaxZ > blockMaxZ){
            Space space1 = new Space(minX, minY, blockMaxZ, maxX - minX, maxY - minY,spaceMaxZ - blockMaxZ, problem);
            outcomeSpaceList.add(space1);
        }
        if(blockMinZ > spaceMinZ){
            Space space1 = new Space(minX, minY, spaceMinZ, maxX - minX, maxY - minY,blockMinZ - spaceMinZ, problem);
            outcomeSpaceList.add(space1);
        }

        return outcomeSpaceList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-22 20-22-36
     * @Description 判断块和空间 是否相交
    */
    public static boolean isIntersecting(Space space, Block block){
        // 先判断是否有交集(物体和空间相交,三个方向都必须有交集)
        if(space.xRange[1] <= block.xRange[0] || block.xRange[1] <= space.xRange[0]){
            return false;
        }
        if(space.yRange[1] <= block.yRange[0] || block.yRange[1] <= space.yRange[0]){
            return false;
        }
        if(space.zRange[1] <= block.zRange[0] || block.zRange[1] <= space.zRange[0]){
            return false;
        }
        return true;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-21 01-10-09
     * @Description 判断空间是否包含在空间列表中某一空间内
    */
    public static boolean isSpaceContainToOneOfTheSpaceList(ArrayList<Space> spaceArrayList, Space space){
        for (Space space1 : spaceArrayList) {
            if(isInSpace(space, space1)){
                return true;
            }
        }
        return false;
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-07 10-44-52
     * @Description 若一个空间A包含在另一个空间B中,删除A
     * 这个方法正确
    */
    public static void deleteIncludingSpace(ArrayList<Space> spaceArrayList){
        // 把在大空间中出现的小空间删除
        for (int i = spaceArrayList.size() - 1; i >= 0; i--) {
            if(i > spaceArrayList.size() - 1){
                i = spaceArrayList.size() - 1;
            }
            Space spaceA = spaceArrayList.get(i);
            for(int j = spaceArrayList.size() - 1; j >= 0; j--){
                Space spaceB = spaceArrayList.get(j);
                // spaceA 和 spaceB 不是同一个, 并且spaceB 在 spaceA 里面 则删除spaceB
                if(spaceA != spaceB && isInSpace(spaceB,spaceA)){
                    spaceArrayList.remove(j);
                }
            }
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-13 10-36-24
     * @Description 若一个空间A包含在另一个空间B中,删除A; 加速优化版本
     * 得到的效果没有上一个效果好,该方法有逻辑问题,并不能完全删除子空间
    */
    public static void deleteIncludingSpace2(ArrayList<Space> spaceArrayList){
        ArrayList<Space> afterDeletedSpaceList = new ArrayList<>();
        for (Space spaceB : spaceArrayList) {
            boolean inSpaceA = false;
            for (Space spaceA : afterDeletedSpaceList) {
                if(isInSpace(spaceB, spaceA)){
                    inSpaceA = true;
                    break;
                }
            }
            if(!inSpaceA){
                afterDeletedSpaceList.add(spaceB);
            }
        }
        spaceArrayList.clear();
        spaceArrayList.addAll(afterDeletedSpaceList);
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-07 10-41-46
     * @Description 检测空间是否有包含
    */
    public static void detectSpatialPhaseInclusions(ArrayList<Space> spaceArrayList){
        //检查以下是否有空间包含
        for (int i = 0; i < spaceArrayList.size(); i++) {
            Space spaceA = spaceArrayList.get(i);
            for (Space spaceB : spaceArrayList) {
                if (!spaceA.equals(spaceB) && inStateCode(spaceA, spaceB) != 0) {
                    System.out.println("发生空间包含");
                    System.out.println(spaceA);
                    System.out.println(spaceB);
                }
            }
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-07 11-17-46
     * @Description 更新块表 (删除不满足剩余箱子数目的块)
    */
    public static void renewBlockList(State state){
        // 删除剩余箱子不可生成的块
        for (int i = state.blockList.size() -1; i >= 0; i--) {
            // 判断剩余箱子能否生成此块
            boolean isOk = true;
            Block block1 = state.blockList.get(i);

            for (int j = 0; j < state.availBox.length; j++) {
                if(block1.neededBoxNumOfEachType[j] > state.availBox[j]){
                    isOk  = false;
                    break;
                }
            }
            if(!isOk){
                state.blockList.remove(i);
            }
        }
    }

    // 给一个空间和一个 block 求剩余空间(完全支撑),并加入空间列表, 在放置一个块求剩余空间前,需要用放置空间的坐标初始化 block的坐标信息
    public static ArrayList<Space> getLeftSpaceFullySupport(Space space, Block block, Problem problem){

        ArrayList<Space> newSpaceList = new ArrayList<>();
        // z 的 左后下角坐标,即0点坐标  是由 block 位置决定
        Space spaceZ = new Space(
                block.eightCoordinate[4][0],
                block.eightCoordinate[4][1],
                block.eightCoordinate[4][2],
                block.viableLength,
                block.viableWidth,
                space.eightCoordinate[4][2] - block.eightCoordinate[4][2],
                problem
        );
        if(!spaceZ.isZeroV()){
            newSpaceList.add(spaceZ);
        }
        // 1号面形成空间
        double[][] x1SpaceCoordinate = new double[8][3];
        for (int i = 0; i < 8; i++) {
            if(i == 1 || i == 2 || i == 5 || i == 6){
                x1SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                x1SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                x1SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
            }else{ // 平移yz 不变,x变成block 1号角的x
                if(i == 0){
                    x1SpaceCoordinate[i][0] = block.eightCoordinate[1][0];
                    x1SpaceCoordinate[i][1] = space.eightCoordinate[1][1];
                    x1SpaceCoordinate[i][2] = space.eightCoordinate[1][2];
                }else if(i == 3){
                    x1SpaceCoordinate[i][0] = block.eightCoordinate[2][0];
                    x1SpaceCoordinate[i][1] = space.eightCoordinate[2][1];
                    x1SpaceCoordinate[i][2] = space.eightCoordinate[2][2];
                }else if(i == 4){
                    x1SpaceCoordinate[i][0] = block.eightCoordinate[5][0];
                    x1SpaceCoordinate[i][1] = space.eightCoordinate[5][1];
                    x1SpaceCoordinate[i][2] = space.eightCoordinate[5][2];
                }else {
                    x1SpaceCoordinate[i][0] = block.eightCoordinate[6][0];
                    x1SpaceCoordinate[i][1] = space.eightCoordinate[6][1];
                    x1SpaceCoordinate[i][2] = space.eightCoordinate[6][2];
                }
            }
        }
        Space spaceX1= new Space(x1SpaceCoordinate,problem);
        if(!spaceX1.isZeroV()){
            newSpaceList.add(spaceX1);
        }

        // 2 号面形成空间
        double[][] x2SpaceCoordinate = new double[8][3];
        for (int i = 0; i < 8; i++) {
            if(i == 0 || i == 3 || i == 4 || i == 7){
                x2SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                x2SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                x2SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
            }else{ // 平移yz 不变,x变成block 1号角的x
                if(i == 1){
                    x2SpaceCoordinate[i][0] = block.eightCoordinate[0][0];
                    x2SpaceCoordinate[i][1] = space.eightCoordinate[0][1];
                    x2SpaceCoordinate[i][2] = space.eightCoordinate[0][2];
                }else if(i == 2){
                    x2SpaceCoordinate[i][0] = block.eightCoordinate[3][0];
                    x2SpaceCoordinate[i][1] = space.eightCoordinate[3][1];
                    x2SpaceCoordinate[i][2] = space.eightCoordinate[3][2];
                }else if(i == 5){
                    x2SpaceCoordinate[i][0] = block.eightCoordinate[4][0];
                    x2SpaceCoordinate[i][1] = space.eightCoordinate[4][1];
                    x2SpaceCoordinate[i][2] = space.eightCoordinate[4][2];
                }else {
                    x2SpaceCoordinate[i][0] = block.eightCoordinate[7][0];
                    x2SpaceCoordinate[i][1] = space.eightCoordinate[7][1];
                    x2SpaceCoordinate[i][2] = space.eightCoordinate[7][2];
                }
            }
        }
        Space spaceX2= new Space(x2SpaceCoordinate,problem);
        if(!spaceX2.isZeroV()){
            newSpaceList.add(spaceX2);
        }

        //3 号面生成空间
        double[][] y3SpaceCoordinate = new double[8][3];
        for (int i = 0; i < 8; i++) {
            if(i == 2 || i == 3 || i == 6 || i == 7){
                y3SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                y3SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                y3SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
            }else{ // 平移yz 不变,x变成block 1号角的x
                if(i == 0){
                    y3SpaceCoordinate[i][0] = space.eightCoordinate[3][0];
                    y3SpaceCoordinate[i][1] = block.eightCoordinate[3][1];
                    y3SpaceCoordinate[i][2] = space.eightCoordinate[3][2];
                }else if(i == 1){
                    y3SpaceCoordinate[i][0] = space.eightCoordinate[2][0];
                    y3SpaceCoordinate[i][1] = block.eightCoordinate[2][1];
                    y3SpaceCoordinate[i][2] = space.eightCoordinate[2][2];
                }else if(i == 4){
                    y3SpaceCoordinate[i][0] = space.eightCoordinate[7][0];
                    y3SpaceCoordinate[i][1] = block.eightCoordinate[7][1];
                    y3SpaceCoordinate[i][2] = space.eightCoordinate[7][2];
                }else {
                    y3SpaceCoordinate[i][0] = space.eightCoordinate[6][0];
                    y3SpaceCoordinate[i][1] = block.eightCoordinate[6][1];
                    y3SpaceCoordinate[i][2] = space.eightCoordinate[6][2];
                }
            }
        }
        Space spaceY3= new Space(y3SpaceCoordinate,problem);
        if(!spaceY3.isZeroV()){
            newSpaceList.add(spaceY3);
        }

        //4 号面生成空间
        double[][] y4SpaceCoordinate = new double[8][3];
        for (int i = 0; i < 8; i++) {
            if(i == 0 || i == 1 || i == 4 || i == 5){
                y4SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                y4SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                y4SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
            }else{ // 平移yz 不变,x变成block 1号角的x
                if(i == 2){
                    y4SpaceCoordinate[i][0] = space.eightCoordinate[1][0];
                    y4SpaceCoordinate[i][1] = block.eightCoordinate[1][1];
                    y4SpaceCoordinate[i][2] = space.eightCoordinate[1][2];
                }else if(i == 3){
                    y4SpaceCoordinate[i][0] = space.eightCoordinate[0][0];
                    y4SpaceCoordinate[i][1] = block.eightCoordinate[0][1];
                    y4SpaceCoordinate[i][2] = space.eightCoordinate[0][2];
                }else if(i == 6){
                    y4SpaceCoordinate[i][0] = space.eightCoordinate[5][0];
                    y4SpaceCoordinate[i][1] = block.eightCoordinate[5][1];
                    y4SpaceCoordinate[i][2] = space.eightCoordinate[5][2];
                }else {
                    y4SpaceCoordinate[i][0] = space.eightCoordinate[4][0];
                    y4SpaceCoordinate[i][1] = block.eightCoordinate[4][1];
                    y4SpaceCoordinate[i][2] = space.eightCoordinate[4][2];
                }
            }
        }
        Space spaceY4= new Space(y4SpaceCoordinate,problem);
        if(!spaceY4.isZeroV()){
            newSpaceList.add(spaceY4);
        }

        // 5 号面生成的空间
        double[][] z5SpaceCoordinate = new double[8][3];
        for (int i = 0; i < 8; i++) {
            if(i == 0 || i == 1 || i == 2 || i == 3){
                z5SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                z5SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                z5SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
            }else{ // 平移yz 不变,x变成block 1号角的x
                z5SpaceCoordinate[i][0] = space.eightCoordinate[i - 4][0];
                z5SpaceCoordinate[i][1] = space.eightCoordinate[i - 4][1];
                z5SpaceCoordinate[i][2] = block.eightCoordinate[i - 4][2];
            }
        }
        Space spaceZ5= new Space(z5SpaceCoordinate,problem);
        if(!spaceZ5.isZeroV()){
            newSpaceList.add(spaceZ5);
        }
        return newSpaceList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-04-05 07-53-53
     * @Description 用交叠块 更新所有交叠空间
     */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 01-22-30
     * @Description 重大跟新,空间体积不为0才创建对象
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 01-48-12
     * @Description 再一次重大修复,先判断 两个面是否重合,重合则说明该方向生成体积为0
    */
    public static ArrayList<Space> renewSpace(Space space, Block block, Problem problem){

        ArrayList<Space> newSpaceList = new ArrayList<>();
        // 1号面形成空间
        // 判断面是否重合(空间1,2,5,6号点的x 不能等于 块1,2,5,6 号点的x)
        if(space.eightCoordinate[1][0] != block.eightCoordinate[1][0]){
            // 求x 方向的 空间的八点坐标
            double[][] x1SpaceCoordinate = new double[8][3];
            for (int i = 0; i < 8; i++) {
                if(i == 1 || i == 2 || i == 5 || i == 6){
                    x1SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                    x1SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                    x1SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
                }else{ // 平移yz 不变,x变成block 1号角的x
                    if(i == 0){
                        x1SpaceCoordinate[i][0] = block.eightCoordinate[1][0];
                        x1SpaceCoordinate[i][1] = space.eightCoordinate[1][1];
                        x1SpaceCoordinate[i][2] = space.eightCoordinate[1][2];
                    }else if(i == 3){
                        x1SpaceCoordinate[i][0] = block.eightCoordinate[2][0];
                        x1SpaceCoordinate[i][1] = space.eightCoordinate[2][1];
                        x1SpaceCoordinate[i][2] = space.eightCoordinate[2][2];
                    }else if(i == 4){
                        x1SpaceCoordinate[i][0] = block.eightCoordinate[5][0];
                        x1SpaceCoordinate[i][1] = space.eightCoordinate[5][1];
                        x1SpaceCoordinate[i][2] = space.eightCoordinate[5][2];
                    }else {
                        x1SpaceCoordinate[i][0] = block.eightCoordinate[6][0];
                        x1SpaceCoordinate[i][1] = space.eightCoordinate[6][1];
                        x1SpaceCoordinate[i][2] = space.eightCoordinate[6][2];
                    }
                }
            }
            Space spaceX1= new Space(x1SpaceCoordinate,problem);
            newSpaceList.add(spaceX1);
        }

        // 2 号面形成空间
        // 判断面是否重合(空间0,3,4,7号点的x 不能等于 块0,3,4,7 号点的x)
        if(space.eightCoordinate[0][0] != block.eightCoordinate[0][0]){
            double[][] x2SpaceCoordinate = new double[8][3];
            for (int i = 0; i < 8; i++) {
                if(i == 0 || i == 3 || i == 4 || i == 7){
                    x2SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                    x2SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                    x2SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
                }else{ // 平移yz 不变,x变成block 1号角的x
                    if(i == 1){
                        x2SpaceCoordinate[i][0] = block.eightCoordinate[0][0];
                        x2SpaceCoordinate[i][1] = space.eightCoordinate[0][1];
                        x2SpaceCoordinate[i][2] = space.eightCoordinate[0][2];
                    }else if(i == 2){
                        x2SpaceCoordinate[i][0] = block.eightCoordinate[3][0];
                        x2SpaceCoordinate[i][1] = space.eightCoordinate[3][1];
                        x2SpaceCoordinate[i][2] = space.eightCoordinate[3][2];
                    }else if(i == 5){
                        x2SpaceCoordinate[i][0] = block.eightCoordinate[4][0];
                        x2SpaceCoordinate[i][1] = space.eightCoordinate[4][1];
                        x2SpaceCoordinate[i][2] = space.eightCoordinate[4][2];
                    }else {
                        x2SpaceCoordinate[i][0] = block.eightCoordinate[7][0];
                        x2SpaceCoordinate[i][1] = space.eightCoordinate[7][1];
                        x2SpaceCoordinate[i][2] = space.eightCoordinate[7][2];
                    }
                }
            }
            Space spaceX2= new Space(x2SpaceCoordinate,problem);
            newSpaceList.add(spaceX2);
        }


        //3 号面生成空间
        // 判断面是否重合(空间2,3,6,7号点的y 不能等于 块2,3,6,7 号点的y)
        if(space.eightCoordinate[2][1] != block.eightCoordinate[2][1]){
            double[][] y3SpaceCoordinate = new double[8][3];
            for (int i = 0; i < 8; i++) {
                if(i == 2 || i == 3 || i == 6 || i == 7){
                    y3SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                    y3SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                    y3SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
                }else{ // 平移yz 不变,x变成block 1号角的x
                    if(i == 0){
                        y3SpaceCoordinate[i][0] = space.eightCoordinate[3][0];
                        y3SpaceCoordinate[i][1] = block.eightCoordinate[3][1];
                        y3SpaceCoordinate[i][2] = space.eightCoordinate[3][2];
                    }else if(i == 1){
                        y3SpaceCoordinate[i][0] = space.eightCoordinate[2][0];
                        y3SpaceCoordinate[i][1] = block.eightCoordinate[2][1];
                        y3SpaceCoordinate[i][2] = space.eightCoordinate[2][2];
                    }else if(i == 4){
                        y3SpaceCoordinate[i][0] = space.eightCoordinate[7][0];
                        y3SpaceCoordinate[i][1] = block.eightCoordinate[7][1];
                        y3SpaceCoordinate[i][2] = space.eightCoordinate[7][2];
                    }else {
                        y3SpaceCoordinate[i][0] = space.eightCoordinate[6][0];
                        y3SpaceCoordinate[i][1] = block.eightCoordinate[6][1];
                        y3SpaceCoordinate[i][2] = space.eightCoordinate[6][2];
                    }
                }
            }
            Space spaceY3= new Space(y3SpaceCoordinate,problem);
            newSpaceList.add(spaceY3);
        }

        //4 号面生成空间
        // 判断面是否重合(空间0,1,4,5号点的y 不能等于 块0,1,4,5 号点的y)
        if(space.eightCoordinate[0][1] != block.eightCoordinate[0][1]){
            double[][] y4SpaceCoordinate = new double[8][3];
            for (int i = 0; i < 8; i++) {
                if(i == 0 || i == 1 || i == 4 || i == 5){
                    y4SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                    y4SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                    y4SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
                }else{ // 平移yz 不变,x变成block 1号角的x
                    if(i == 2){
                        y4SpaceCoordinate[i][0] = space.eightCoordinate[1][0];
                        y4SpaceCoordinate[i][1] = block.eightCoordinate[1][1];
                        y4SpaceCoordinate[i][2] = space.eightCoordinate[1][2];
                    }else if(i == 3){
                        y4SpaceCoordinate[i][0] = space.eightCoordinate[0][0];
                        y4SpaceCoordinate[i][1] = block.eightCoordinate[0][1];
                        y4SpaceCoordinate[i][2] = space.eightCoordinate[0][2];
                    }else if(i == 6){
                        y4SpaceCoordinate[i][0] = space.eightCoordinate[5][0];
                        y4SpaceCoordinate[i][1] = block.eightCoordinate[5][1];
                        y4SpaceCoordinate[i][2] = space.eightCoordinate[5][2];
                    }else {
                        y4SpaceCoordinate[i][0] = space.eightCoordinate[4][0];
                        y4SpaceCoordinate[i][1] = block.eightCoordinate[4][1];
                        y4SpaceCoordinate[i][2] = space.eightCoordinate[4][2];
                    }
                }
            }
            Space spaceY4= new Space(y4SpaceCoordinate,problem);
            newSpaceList.add(spaceY4);
        }

        // 5 号面生成的空间
        // 判断面是否重合(空间0,1,2,3号点的z 不能等于 块0,1,2,3 号点的z)
        if(space.eightCoordinate[0][2] != block.eightCoordinate[0][2]){
            double[][] z5SpaceCoordinate = new double[8][3];
            for (int i = 0; i < 8; i++) {
                if(i == 0 || i == 1 || i == 2 || i == 3){
                    z5SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                    z5SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                    z5SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
                }else{ // 平移yz 不变,x变成block 1号角的x
                    z5SpaceCoordinate[i][0] = space.eightCoordinate[i - 4][0];
                    z5SpaceCoordinate[i][1] = space.eightCoordinate[i - 4][1];
                    z5SpaceCoordinate[i][2] = block.eightCoordinate[i - 4][2];
                }
            }
            Space spaceZ5= new Space(z5SpaceCoordinate,problem);
            newSpaceList.add(spaceZ5);
        }

        // 6 号面生成的空间
        // 判断面是否重合(空间4,5,6,7号点的z 不能等于 块4,5,6,7号点的z)
        if(space.eightCoordinate[6][2] != block.eightCoordinate[6][2]){
            double[][] z6SpaceCoordinate = new double[8][3];
            for (int i = 0; i < 8; i++) {
                if(i == 4 || i == 5 || i == 6 || i == 7){
                    z6SpaceCoordinate[i][0] = space.eightCoordinate[i][0];
                    z6SpaceCoordinate[i][1] = space.eightCoordinate[i][1];
                    z6SpaceCoordinate[i][2] = space.eightCoordinate[i][2];
                }else{ // 平移yz 不变,x变成block 1号角的x
                    z6SpaceCoordinate[i][0] = space.eightCoordinate[i + 4][0];
                    z6SpaceCoordinate[i][1] = space.eightCoordinate[i + 4][1];
                    z6SpaceCoordinate[i][2] = block.eightCoordinate[i + 4][2];
                }
            }
            Space spaceZ6= new Space(z6SpaceCoordinate,problem);
            newSpaceList.add(spaceZ6);
        }

        return newSpaceList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-21 01-24-00
     * @Description 通过八点坐标判断空间大小是否为0
    */
    public static boolean isSpaceVolumZero(double[][] eightCoordinate){
        return eightCoordinate[1][0] == eightCoordinate[0][0] || eightCoordinate[3][1] == eightCoordinate[0][1] ||
                eightCoordinate[4][2] == eightCoordinate[0][2];
    }

    // 求一个放置块 与 放置空间外的其他空间的交叠部分(交叠块:自带空间坐标,无需手动初始化)
    public static Block getCoverBlock(Space space, Block block){
        // 先判断是否有交集(物体和空间相交,三个方向都必须有交集)
        double[] xCoverRange = new double[2];
        double[] yCoverRange = new double[2];
        double[] zCoverRange = new double[2];
        if(space.xRange[1] < block.xRange[0] || block.xRange[1] < space.xRange[0]){
            return null;
        }else{
            xCoverRange[0] = Math.max(space.xRange[0], block.xRange[0]);
            xCoverRange[1] = Math.min(space.xRange[1], block.xRange[1]);
        }

        if(space.yRange[1] < block.yRange[0] || block.yRange[1] < space.yRange[0]){
            return null;
        }else{
            yCoverRange[0] = Math.max(space.yRange[0], block.yRange[0]);
            yCoverRange[1] = Math.min(space.yRange[1], block.yRange[1]);
        }

        if(space.zRange[1] < block.zRange[0] || block.zRange[1] < space.zRange[0]){
            return null;
        }else{
            zCoverRange[0] = Math.max(space.zRange[0], block.zRange[0]);
            zCoverRange[1] = Math.min(space.zRange[1], block.zRange[1]);
        }
        // 排除体积为0的情况
        if(xCoverRange[0] == xCoverRange[1] || yCoverRange[0] == yCoverRange[1] || zCoverRange[0] == zCoverRange[1]){
            return null;
        }

        // 使用范围 生成一个coverBlock
        return new Block(xCoverRange, yCoverRange, zCoverRange);
    }

    /*A 在B 内返回1
     * B 在A 内返回2
     * 不相互属于返回0*/
    public static int inStateCode(Space spaceA, Space spaceB){
        if(isInSpace(spaceA,spaceB)){
            return 1;
        }
        if(isInSpace(spaceB,spaceA)){
            return 2;
        }
        return 0;
    }
    // 判断空间A 是否包含在空间B内
    public static boolean isInSpace(Space spaceA, Space spaceB){
        return spaceA.xRange[0] >= spaceB.xRange[0] && spaceA.xRange[1] <= spaceB.xRange[1] &&
                spaceA.yRange[0] >= spaceB.yRange[0] && spaceA.yRange[1] <= spaceB.yRange[1] &&
                spaceA.zRange[0] >= spaceB.zRange[0] && spaceA.zRange[1] <= spaceB.zRange[1];
    }
}
