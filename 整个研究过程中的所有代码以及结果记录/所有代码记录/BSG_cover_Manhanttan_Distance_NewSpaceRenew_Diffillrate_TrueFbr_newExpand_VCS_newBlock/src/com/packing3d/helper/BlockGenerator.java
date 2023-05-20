package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.problem.Problem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

/**
 * @Author yajiewen
 * @Date 2022-10-21 09-44-51
 * @Description 为了代码可读 把之前BlockHelp 里面的块生成方法分类放到这个类里面
*/
public class BlockGenerator {
    /**
     * @Author yajiewen
     * @Date 2022-08-23 16-36-04
     * @Description 用单个箱子生成6个反向上的简单快
     */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 09-58-37
     * @Description 使用构造函数生成块增加可读性
    */
    public static ArrayList<Block> sixDirectionBlocks(Problem problem){
        ArrayList<Block> blockList = new ArrayList<>(10000);
        // 用单个箱子生成6个方项的块
        for (int p = 0; p < problem.typeNumberOfBox; p++) {
            Box box = problem.boxList.get(p);
            double boxLength = box.boxLength;
            double boxWidth = box.boxWidth;
            double boxHigh = box.boxHigh;
            int[] isVertial = {box.isXvertical, box.isYvertical, box.isZvertical};
            for (int j = 0; j < isVertial.length; j++) {
                if(isVertial[j] == 1 && j == 0){
                    boxHigh = box.boxLength;
                    boxLength = box.boxHigh;
                    boxWidth = box.boxWidth;
                }else if(isVertial[j] == 1 && j == 1) { // 宽可以竖直放置
                    boxHigh = box.boxWidth;
                    boxWidth = box.boxHigh;
                    boxLength = box.boxLength;
                }else if(isVertial[j] == 1 && j == 2){ // 初始状态
                    boxLength = box.boxLength;
                    boxWidth = box.boxWidth;
                    boxHigh = box.boxHigh;
                }
                if((isVertial[j] == 1 && j == 0) || (isVertial[j] == 1 && j == 1) || (isVertial[j] == 1 && j == 2)){
                    for(int k = 0; k < 2; k++) {
                        // 首次默认,第二次旋转长宽
                        if (k == 1) {
                            double term = boxLength;
                            boxLength = boxWidth;
                            boxWidth = term;
                        }
                        for (int i = 0; i < box.boxNumber; i++) {
                            int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
                            neededBoxNumOfEachType[p] = 1;
                            Block block = new Block(problem.typeNumberOfBox, boxLength, boxWidth, boxHigh, boxLength,
                                    boxWidth, boxLength * boxWidth * boxHigh, neededBoxNumOfEachType);
                            blockList.add(block);
                        }
                    }
                }
            }
        }
        return blockList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-02 11-27-57
     * @Description 论文A new iterative-doubling Greedy–Lookahead algorithm for the single
     * container loading problem 使用的 Simple Blocks
     */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 09-58-14
     * @Description 修复使用构造函数生成块,增加可读性
    */
    /**
     * @Author yajiewen
     * @Date 2023-02-02 20-49-38
     * @Description 原文为从小到大排序，之前排序错了
     */
    public static ArrayList<Block> simpleBlocks(Problem problem, int MaxCount){
        ArrayList<Block> returnBlockList = new ArrayList<>(10000);
        ArrayList<ArrayList<Block>> blockLists = new ArrayList<>(problem.typeNumberOfBox);

        for (int i = 0; i < problem.typeNumberOfBox; i++) {
            // 拿出一个盒子的信息
            Box box = problem.boxList.get(i);
            // 生成用于保存该类别箱子生成的块的list
            blockLists.add(new ArrayList<Block>());
            // 初始方项的长宽高
            double boxLength = box.boxLength;
            double boxWidth = box.boxWidth;
            double boxHigh = box.boxHigh;
            int[] isVertial = {box.isXvertical, box.isYvertical, box.isZvertical};
            for(int j = 0; j < isVertial.length; j++){
                if(isVertial[j] == 1 && j == 0){ //长可以竖直放
                    boxHigh = box.boxLength;
                    boxLength = box.boxHigh;
                    boxWidth = box.boxWidth;
                }else{
                    if(isVertial[j] == 1 && j == 1){ // 宽可以竖直放置
                        boxHigh = box.boxWidth;
                        boxWidth = box.boxHigh;
                        boxLength = box.boxLength;
                    }else{
                        if(isVertial[j] == 1 && j == 2){ // 初始状态
                            boxLength = box.boxLength;
                            boxWidth = box.boxWidth;
                            boxHigh = box.boxHigh;
                        }
                    }
                }
                // 满足上面情况开始
                if((isVertial[j] == 1 && j == 0) || (isVertial[j] == 1 && j == 1) || (isVertial[j] == 1 && j == 2)){
                    // 长和宽旋转
                    for(int k = 0; k < 2; k++){
                        // 首次默认,第二次旋转长宽
                        if(k == 1){
                            double term = boxLength;
                            boxLength = boxWidth;
                            boxWidth = term;
                        }
                        for(int nx = 1; nx <= box.boxNumber; nx++){
                            for(int ny = 1; ny <= box.boxNumber / nx; ny++){
                                for(int nz = 1; nz <= box.boxNumber / nx / ny; nz++){
                                    // 求以下简单块的长宽高
                                    double blockLength = boxLength * nx;
                                    double blockWidth = boxWidth * ny;
                                    double blockHigh = boxHigh * nz;
                                    if(blockLength <= problem.containnerLength && blockWidth <= problem.containnerWidth && blockHigh <= problem.containnerHigh){
                                        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
                                        neededBoxNumOfEachType[i] = nx * ny * nz;
                                        Block block = new Block(problem.typeNumberOfBox, blockLength, blockWidth, blockHigh, blockLength, blockWidth,
                                                blockLength * blockWidth * blockHigh, neededBoxNumOfEachType);
                                        blockLists.get(i).add(block);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        int totalBlocks = 0;
        for (ArrayList<Block> blockList : blockLists) {
            totalBlocks += blockList.size();
        }
        if (totalBlocks > MaxCount){
            for (ArrayList<Block> blockList : blockLists) {
                int num = Math.max( (int)Math.floor(1.0 * blockList.size() / totalBlocks * MaxCount) , 1);
                blockSortIncrease(blockList);
                for (int i = 0; i < num; i++) {
                    returnBlockList.add(blockList.get(i));
                }
            }
        }else{
            for (ArrayList<Block> blockList : blockLists) {
                returnBlockList.addAll(blockList);
            }
        }
        return returnBlockList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-02 11-50-02
     * @Description A new iterative-doubling Greedy–Lookahead algorithm for the single
     * container loading problem 使用的 Guillotine Blocks (非完全支撑)
     */
    /**
     * @Author yajiewen
     * @Date 2022-10-10 09-32-34
     * @Description 添加6个方向上的单独箱子构成的块
     */
    public static ArrayList<Block> guillotineBlocks(Problem problem, int MaxBlocks, double MinFillRate){
        long start = System.currentTimeMillis();
        ArrayList<Block> blockList = simpleBlocks(problem,MaxBlocks / 2);
        // 单个物品生成6个方项上的块
        ArrayList<Block> sixDirectionBlock = sixDirectionBlocks(problem);
        // 开始进行GC块拼接
        ArrayList<Block> pList = new ArrayList<>(blockList);
        ArrayList<Block> newBlockList = new ArrayList<>(5000);
        while(blockList.size() < MaxBlocks){
            newBlockList.clear();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    Block block;
                    //x
                    block = xCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                }
            }
            if(newBlockList.isEmpty()){
                break;
            }
            for (Block block : newBlockList) {
                if(!blockList.contains(block) && blockList.size() < MaxBlocks){
                    blockList.add(block);
                }
            }
            pList = new ArrayList<>(newBlockList);
        }

        // 将单个物品生成的6个反方向上的块加入块表,解决块丢失问题
//        for (Block block : sixDirectionBlock) {
//            if(!blockList.contains(block)){
//                blockList.add(block);
//            }
//        }

//        blockSort(blockList);
        System.out.println("Guillotine Blocks块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-15 11-31-07
     * @Description 在原来GuillotineBlocks生成的基础上,把simpleblock 生成的块添加上了左右方向的旋转
     */

    public static ArrayList<Block> generateMyGuillotineBlocksNotFullySupported(Problem problem,int MaxBlocks,double MinFillRate){
        long start = System.currentTimeMillis();
        ArrayList<Block> blockList = simpleBlocks(problem,MaxBlocks / 2);
        // 开始进行GC块拼接
        ArrayList<Block> pList = new ArrayList<>(blockList);
        // 用于保存左右旋转后的GuillotineBlocks
        ArrayList<Block> rotateBlockList = new ArrayList<>();

        while(blockList.size() < MaxBlocks){
            ArrayList<Block> newBlockList = new ArrayList<>();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    Block rotateBlock;
                    //x
                    block = xCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                        rotateBlock = block.cloneOne();
                        double term = rotateBlock.blockLenght;
                        rotateBlock.blockLenght = rotateBlock.blockWidth;
                        rotateBlock.blockWidth = term;
                        rotateBlockList.add(rotateBlock);
                    }
                    //y
                    block = yCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                        rotateBlock = block.cloneOne();
                        double term = rotateBlock.blockLenght;
                        rotateBlock.blockLenght = rotateBlock.blockWidth;
                        rotateBlock.blockWidth = term;
                        rotateBlockList.add(rotateBlock);
                    }
                    //z
                    block = zCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                        rotateBlock = block.cloneOne();
                        double term = rotateBlock.blockLenght;
                        rotateBlock.blockLenght = rotateBlock.blockWidth;
                        rotateBlock.blockWidth = term;
                        rotateBlockList.add(rotateBlock);
                    }
                }
            }
            if(newBlockList.isEmpty()){
                break;
            }
            for (Block block : newBlockList) {
                if(!blockList.contains(block) && blockList.size() < MaxBlocks){
                    blockList.add(block);
                }
            }
            pList = new ArrayList<>(newBlockList);
        }

        // 把旋转后的块的前n个加入blockList
        int n = 10000;
        for(int i = 0; i < Math.min(rotateBlockList.size(),n); i++){
            blockList.add(rotateBlockList.get(i));
        }

        // 旋转后的块按照大小排序
        blockSort(blockList);
        System.out.println("MyGuillotine Blocks块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-04-29 09-50-44
     * @Description A Tree Search Algorithm for Solving theContainer Loading Problem
     *      * general packing block : 在这片文章中提出来用于 完全支撑
     */
    public static ArrayList<Block> generalPackingBlocks(Problem problem, int MaxBlocks, double MinFillRate, double MinAreaRate){
        ArrayList<Block> blockList = sixDirectionBlocks(problem);

        // 开始拼接GP块
        ArrayList<Block> pList = new ArrayList<>(blockList);
        ArrayList<Block> newBlockList = new ArrayList<>(5000);
        while(blockList.size() < MaxBlocks){
            newBlockList.clear();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombiningGPBlock(block1,block2,MinFillRate,MinAreaRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombiningGPBlock(block1,block2,MinFillRate,MinAreaRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombiningGPBlock(block1,block2,MinFillRate,MinAreaRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                }
            }
            if(newBlockList.isEmpty()){
                break;
            }
            for (Block block : newBlockList) {
                if(!blockList.contains(block) && blockList.size() < MaxBlocks){
                    blockList.add(block);
                }
            }
            pList = new ArrayList<>(newBlockList);
        }
        // 旋转后的块按照大小排序
        blockSort(blockList);
        System.out.println("GP块生成完毕:" + blockList.size() +"块");
        return blockList;
    }



    /**
     * @Author yajiewen
     * @Date 2022-05-12 11-59-19
     * @Description 速度提升版本 generateGeneralCuttingBlocksNotFullySupported
     */
    public static ArrayList<Block> generateGeneralCuttingBlocksNotFullySupportedFaster(Problem problem, int MaxBlocks, double MinFillRate){
        long start = System.currentTimeMillis();
        ArrayList<Block> blockList = sixDirectionBlocks(problem);
        // 开始进行GC块拼接
        HashSet<Block> pList = new HashSet<>(blockList);
        while(blockList.size() < MaxBlocks){
            HashSet<Block> newBlockList = new HashSet<>();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null){
                        newBlockList.add(block);
                    }
                }
            }
            if(newBlockList.isEmpty()){
                break;
            }
            for (Block block : newBlockList) {
                if(blockList.size() < MaxBlocks){
                    blockList.add(block);
                }
            }
            pList = new HashSet<>(newBlockList);
        }
        // 旋转后的块按照大小排序
        blockSort(blockList);
        System.out.println("GC块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-04-25 10-43-49
     * @Description A Tree Search Algorithm for Solving theContainer Loading Problem
     * general cutting block : 在这片文章中提出来用于非完全支撑
     */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 10-14-15
     * @Description general cutting block (GC block for short) 按照原来论文呢修复
    */
    public static ArrayList<Block> generalCuttingBlocks(Problem problem, int MaxBlocks, double MinFillRate){
        long start = System.currentTimeMillis();
        ArrayList<Block> blockList = sixDirectionBlocks(problem);
        // 开始进行GC块拼接
        ArrayList<Block> pList = new ArrayList<>(blockList);
        ArrayList<Block> newBlockList = new ArrayList<>(5000);
        while(blockList.size() < MaxBlocks){
            newBlockList.clear();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombiningGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                }
            }
            if(newBlockList.isEmpty()){
                break;
            }
            for (Block block : newBlockList) {
                if(!blockList.contains(block) && blockList.size() < MaxBlocks){
                    blockList.add(block);
                }
            }
            pList = new ArrayList<>(newBlockList);
        }

        // 旋转后的块按照大小排序
        blockSort(blockList);
        System.out.println("GC块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockList;
    }


    /**
     * @Author yajiewen
     * @Date 2022-04-10 18-42-18
     * @Description 修复 nz < box.boxNumber 导致的快丢失问题 需要加上=号nz <= box.boxNumber
     */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 10-35-52
     * @Description 使用带复杂度的构造函数生成块
    */
    public static ArrayList<Block> simpleBlocks_Zhang(Problem problem){
        ArrayList<Block> blockList = new ArrayList<>(10000);
        for (int i = 0; i < problem.typeNumberOfBox; i++) {
            // 拿出一个盒子的信息
            Box box = problem.boxList.get(i);
            // 初始方项的长宽高
            double boxLength = box.boxLength;
            double boxWidth = box.boxWidth;
            double boxHigh = box.boxHigh;
            int[] isVertial = {box.isXvertical, box.isYvertical, box.isZvertical};
            for(int j = 0; j < isVertial.length; j++){
                if(isVertial[j] == 1 && j == 0){ //长可以竖直放
                    boxHigh = box.boxLength;
                    boxLength = box.boxHigh;
                    boxWidth = box.boxWidth;
                }else{
                    if(isVertial[j] == 1 && j == 1){ // 宽可以竖直放置
                        boxHigh = box.boxWidth;
                        boxWidth = box.boxHigh;
                        boxLength = box.boxLength;
                    }else{
                        if(isVertial[j] == 1 && j == 2){ // 初始状态
                            boxLength = box.boxLength;
                            boxWidth = box.boxWidth;
                            boxHigh = box.boxHigh;
                        }
                    }
                }
                // 满足上面情况开始
                if((isVertial[j] == 1 && j == 0) || (isVertial[j] == 1 && j == 1) || (isVertial[j] == 1 && j == 2)){
                    // 长和宽旋转
                    for(int k = 0; k < 2; k++){
                        // 首次默认,第二次旋转长宽
                        if(k == 1){
                            double term = boxLength;
                            boxLength = boxWidth;
                            boxWidth = term;
                        }
                        for(int nx = 1; nx <= box.boxNumber; nx++){
                            for(int ny = 1; ny <= box.boxNumber / nx; ny++){
                                for(int nz = 1; nz <= box.boxNumber / nx / ny; nz++){
                                    // 求以下简单块的长宽高
                                    double blockLength = boxLength * nx;
                                    double blockWidth = boxWidth * ny;
                                    double blockHigh = boxHigh * nz;
                                    if(blockLength <= problem.containnerLength && blockWidth <= problem.containnerWidth && blockHigh <= problem.containnerHigh){
                                        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
                                        neededBoxNumOfEachType[i] = nx * ny * nz;
                                        Block block = new Block(problem.typeNumberOfBox, 0, blockLength, blockWidth, blockHigh,
                                                blockLength, blockWidth, blockLength * blockWidth * blockHigh, neededBoxNumOfEachType);
                                        blockList.add(block);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return blockList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-09 10-43-07
     * @Description 复杂快生成,支持完全支撑
     */
    public static ArrayList<Block> complexBlocks_Zhang(int MaxLevel, int MaxBlocks, Problem problem, double MinFillRate, double MinAreaRate){
        ArrayList<Block> blockList = simpleBlocks_Zhang(problem);

        for (int level = 0; level < MaxLevel; level++) {
            ArrayList<Block> newBlockList = new ArrayList<>(10000);
            if(blockList.size() > MaxBlocks){
                break;
            }
            for(int i = 0; i < blockList.size(); i++){
                Block block1 = blockList.get(i);
                for (Block block2 : blockList) {
                    if (block1.complexLevel == level || block2.complexLevel == level) {
                        // 开始判断各个方项是否满足要求
                        // x
                        Block block = xCombiningComplexBlock(block1, block2, problem, MinFillRate, MinAreaRate);
                        if (block != null) {
                            newBlockList.add(block);
                        }
                        // y
                        block = yCombiningComplexBlock(block1, block2, problem, MinFillRate, MinAreaRate);
                        if (block != null) {
                            newBlockList.add(block);
                        }
                        // z
                        block = zCombiningComplexBlock(block1, block2, problem, MinFillRate, MinAreaRate);
                        if (block != null) {
                            newBlockList.add(block);
                        }

                    }
                }
            }
            // 把新block加入表
            for (Block block : newBlockList) {
                if(!blockList.contains(block)){
                    blockList.add(block);
                }
            }
        }
        // 旋转后的块按照大小排序
        blockSort(blockList);
        return blockList;
    }

    public static Block xCombiningComplexBlock(Block block1, Block block2, Problem problem, double MinFillRate, double MinAreaRate){

        // 条件1
        if(block1.viableLength != block1.blockLenght && block2.viableLength != block2.blockLenght && block1.blockHigh != block2.blockHigh){
            return null;
        }
        // 条件2
        double blockLenght = block1.blockLenght + block2.blockLenght;
        double blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        double blockHigh = block1.blockHigh;
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3,4
        double viableLength = block1.viableLength + block2.viableLength;
        double viableWidth = Math.min(block1.viableWidth, block2.viableWidth);
        double blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        double viableRate = (viableLength * viableWidth) / (blockLenght * blockWidth);
        if(viableRate < MinAreaRate){
            return null;
        }

        int complexLevel = Math.max(block1.complexLevel,block2.complexLevel) + 1;
        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }

        return new Block(block1.typeNumberOfBox, complexLevel, blockLenght, blockWidth, blockHigh,
                viableLength, viableWidth, blockVolum, neededBoxNumOfEachType);
    }
    public static Block yCombiningComplexBlock(Block block1, Block block2, Problem problem, double MinFillRate, double MinAreaRate){
        // 条件1
        if(block1.viableWidth != block1.blockWidth && block2.viableWidth != block2.blockWidth && block1.blockHigh != block2.blockHigh){
            return null;
        }
        // 条件2
        double blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        double blockWidth = block1.blockWidth + block2.blockWidth;
        double blockHigh = block1.blockHigh;
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }

        // 条件3,4
        double viableLength = Math.min(block1.viableLength, block2.viableLength);
        double viableWidth = block1.viableWidth + block2.viableWidth;
        double blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        double viableRate = (viableLength * viableWidth) / (blockLenght * blockWidth);
        if(viableRate < MinAreaRate){
            return null;
        }

        int complexLevel = Math.max(block1.complexLevel,block2.complexLevel) + 1;
        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }

        return new Block(block1.typeNumberOfBox, complexLevel, blockLenght, blockWidth, blockHigh,
                viableLength, viableWidth, blockVolum, neededBoxNumOfEachType);
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-21 10-58-10
     * @Description block2 放在block1 上面
    */
    public static Block zCombiningComplexBlock(Block block1, Block block2, Problem problem, double MinFillRate, double MinAreaRate){
        // 条件1
        if(block1.viableLength < block2.blockLenght && block1.viableWidth < block2.blockWidth){
            return null;
        }
        // 条件2
        double blockLenght = block1.blockLenght;
        double blockWidth = block1.blockWidth;
        double blockHigh = block1.blockHigh + block2.blockHigh;
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3,4
        double viableLength = block2.viableLength;
        double viableWidth = block2.viableWidth;
        double blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        double viableRate = (viableLength * viableWidth) / (blockLenght * blockWidth);
        if(viableRate < MinAreaRate){
            return null;
        }

        int complexLevel = Math.max(block1.complexLevel,block2.complexLevel) + 1;
        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }

        return new Block(block1.typeNumberOfBox, complexLevel, blockLenght, blockWidth, blockHigh,
                viableLength, viableWidth, blockVolum, neededBoxNumOfEachType);
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-46-51
     * @Description 重大优化 优化条件判断顺序加速 非完全支撑结合
     */
    public static Block xCombiningGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件1
        if(block1.blockWidth * block1.blockHigh < block2.blockWidth * block2.blockHigh){
            return null;
        }
        // 条件2
        double blockLenght = block1.blockLenght + block2.blockLenght;
        double blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        double blockHigh = Math.max(block1.blockHigh, block2.blockHigh);
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }

        // 条件4
        int[] blockNeededBoxNumOfEachType = new int[block1.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                blockNeededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }

        return new Block(block1.typeNumberOfBox,blockLenght,blockWidth,blockHigh, blockLenght, blockWidth,blockVolum,blockNeededBoxNumOfEachType);
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-46-51
     * @Description 优化条件判断顺序加速
     */
    public static Block yCombiningGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件1
        if(block1.blockLenght * block1.blockHigh < block2.blockLenght * block2.blockHigh){
            return null;
        }
        // 条件2
        double blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        double blockWidth = block1.blockWidth + block2.blockWidth;
        double blockHigh = Math.max(block1.blockHigh, block2.blockHigh);
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        // 条件4
        int[] blockNeededBoxNumOfEachType = new int[block1.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                blockNeededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        return new Block(block1.typeNumberOfBox,blockLenght,blockWidth,blockHigh, blockLenght, blockWidth,blockVolum,blockNeededBoxNumOfEachType);
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-46-51
     * @Description 优化条件判断顺序加速
     */
    public static Block zCombiningGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件1
        if(block1.blockLenght * block1.blockWidth < block2.blockLenght * block2.blockWidth){
            return null;
        }

        double blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        double blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        double blockHigh = block1.blockHigh + block2.blockHigh;
        // 条件2
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }

        // 条件3
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }

        // 条件4
        int[] blockNeededBoxNumOfEachType = new int[block1.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                blockNeededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        return new Block(block1.typeNumberOfBox,blockLenght,blockWidth,blockHigh, blockLenght, blockWidth,blockVolum,blockNeededBoxNumOfEachType);
    }

    public static Block xCombiningGPBlock(Block block1, Block block2, double MinFillRate, double MinAreaRate, Problem problem){

        // 条件1
        if(!(block1.blockHigh == block2.blockHigh &&
                block1.blockLenght == block1.viableLength &&
                block2.blockLenght == block2.viableLength &&
                block1.blockWidth >= block2.blockWidth)){
            return null;
        }

        double blockLenght = block1.blockLenght + block2.blockLenght;
        double blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        double blockHigh = block1.blockHigh;
        // 条件2
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        // 条件4
        double viableLength = block1.viableLength + block2.viableLength;
        double viableWidth = Math.min(block1.viableWidth, block2.viableWidth);
        double viableRate = (viableLength * viableWidth) / (blockLenght * blockWidth);
        if(viableRate < MinAreaRate){
            return null;
        }

        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        return new Block(block1.typeNumberOfBox, blockLenght, blockWidth, blockHigh, viableLength, viableWidth,
                blockVolum, neededBoxNumOfEachType);
    }

    public static Block yCombiningGPBlock(Block block1, Block block2, double MinFillRate, double MinAreaRate, Problem problem){

        // 条件1
        if( !(block1.blockHigh == block2.blockHigh &&
                block1.blockWidth == block1.viableWidth &&
                block2.blockWidth == block2.viableWidth &&
                block1.blockLenght >= block2.blockLenght)){
            return null;
        }

        double blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        double blockWidth = block1.blockWidth + block2.blockWidth;
        double blockHigh = block1.blockHigh;
        // 条件2
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3,4
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        // 条件4
        double viableLength = Math.min(block1.viableLength, block2.viableLength);
        double viableWidth = block1.viableWidth + block2.viableWidth;
        double viableRate = (viableLength * viableWidth) / (blockLenght * blockWidth);
        if(viableRate < MinAreaRate){
            return null;
        }

        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        return new Block(block1.typeNumberOfBox, blockLenght, blockWidth, blockHigh, viableLength, viableWidth,
                blockVolum, neededBoxNumOfEachType);
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-21 11-26-38
     * @Description block 2 放 block1 上面
    */
    public static Block zCombiningGPBlock(Block block1, Block block2, double MinFillRate, double MinAreaRate, Problem problem){


        // 条件1
        if( !(block1.viableLength >= block2.blockLenght && block1.viableWidth >= block2.blockWidth)){
            return null;
        }

        double blockLenght = block1.blockLenght;
        double blockWidth = block1.blockWidth;
        double blockHigh = block1.blockHigh + block2.blockHigh;
        // 条件2
        if(blockLenght > problem.containnerLength || blockWidth > problem.containnerWidth || blockHigh > problem.containnerHigh){
            return null;
        }
        // 条件3
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        if(fillRate < MinFillRate){
            return null;
        }
        // 条件4
        double viableLength = block2.viableLength;
        double viableWidth = block2.viableWidth;
        double viableRate = (viableLength * viableWidth) / (blockLenght * blockWidth);
        if(viableRate < MinAreaRate){
            return null;
        }

        int[] neededBoxNumOfEachType = new int[problem.typeNumberOfBox];
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        return new Block(block1.typeNumberOfBox, blockLenght, blockWidth, blockHigh, viableLength, viableWidth,
                blockVolum, neededBoxNumOfEachType);
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-21 11-04-39
     * @Description 把块生成中的块按照从大到小排序抽离出来
    */
    public static void blockSort(ArrayList<Block> blockList){
                blockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (o2.blockVolum > o1.blockVolum) {
                    return 1;
                } else {
                    if (o2.blockVolum < o1.blockVolum) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
    }

    /**
     * @Author yajiewen
     * @Date 2023-2-02 20-46-56
     * @Description 把块生成中的块按照从小到大排序抽离出来
     */
    public static void blockSortIncrease(ArrayList<Block> blockList){
        blockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (o2.blockVolum > o1.blockVolum) {
                    return -1;
                } else {
                    if (o2.blockVolum < o1.blockVolum) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        });
    }

}
