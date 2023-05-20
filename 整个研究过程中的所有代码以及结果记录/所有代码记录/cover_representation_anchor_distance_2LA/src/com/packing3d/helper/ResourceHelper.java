package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.space.Space;

import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-04-25 12-06-56
 * @Description 优化代码
*/
public class ResourceHelper {
    // 资源(状态)更新(传入的spaceArrayList 是顶部空间去除后的, space 是被去除的放置空间, block是放置快,)
    public static void renewResource(ArrayList<Space> spaceArrayList, Space space, Block block, int[] availNum, Problem problem){
        // 更新箱子数目
        renewAvailBoxNum(block, availNum);
        // 求放置后的剩余空间
        // 把块放在空间的0点,根据空间0点坐标更新块的八点坐标
        block.initCoordinateAndRange(space.locationX, space.locationY, space.locationZ);
        // 求剩余空间(C2支持完全支撑)
        ArrayList<Space> newSpaceList = getLeftSpaceFullySupport(space,block,problem);
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
                coveredSpaceList.addAll(getLeftSpaceForCoveredSpace(coverSpace,coverBlock,problem));
            }
        }
        // 把得到的所有空间重新放入spaceArrayList
        spaceArrayList.addAll(coveredSpaceList);
        spaceArrayList.addAll(newSpaceList);

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

         //检查以下是否有空间包含
        for (int i = 0; i < spaceArrayList.size(); i++) {
            Space spaceA = spaceArrayList.get(i);
            for (Space spaceB : spaceArrayList) {
                if (!spaceA.equals(spaceB) && inStateCode(spaceB, spaceA) == 1) {
                    System.out.println("发生空间包含");
                    System.out.println(spaceA);
                    System.out.println(spaceB.toString());
                }
            }
        }
    }

    // 更新箱子数目
    public static void renewAvailBoxNum(Block block, int[] availNum){
        for (int i = 0; i < availNum.length; i++) {
            availNum[i] -= block.neededBoxNumOfEachType[i];
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
    public static ArrayList<Space> getLeftSpaceForCoveredSpace(Space space, Block block,Problem problem){

        ArrayList<Space> newSpaceList = new ArrayList<>();
        // 1号面形成空间
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

        // 6 号面生成的空间
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
        if(!spaceZ6.isZeroV()){
            newSpaceList.add(spaceZ6);
        }
        return newSpaceList;
    }

    // 求一个放置块 与 放置空间外的其他空间的交叠部分(交叠块:自带空间坐标,无需手动初始化)
    public static Block getCoverBlock(Space space, Block block){
        Block coverBlock;
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
        coverBlock = new Block(xCoverRange, yCoverRange, zCoverRange);
        return coverBlock;
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
