package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.space.Space;
import com.packing3d.datastructure.state.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class BlockHelper {

    /**
     * @Author yajiewen
     * @Date 2022-08-23 16-36-04
     * @Description 用单个箱子生成6个反向上的简单快
    */

    public static ArrayList<Block> generateSixDirectionBlock(Problem problem){
        ArrayList<Block> blockList = new ArrayList<>();
        // 用单个箱子生成6个方项的GC块
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
                            Block block = new Block(problem.typeNumberOfBox);
                            block.typeNumberOfBox = problem.typeNumberOfBox;
                            block.blockLenght = boxLength;
                            block.blockWidth = boxWidth;
                            block.blockHigh = boxHigh;
                            block.viableLength = boxLength;
                            block.viableWidth = boxWidth;
                            block.blockVolum = boxLength * boxWidth * boxHigh;
                            block.neededBoxNumOfEachType[p] = 1;
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

    public static ArrayList<Block> generateSimpleBlocks(Problem problem,int MaxCount){
        ArrayList<Block> returnBlockList = new ArrayList<>();
        ArrayList<ArrayList<Block>> blockLists = new ArrayList<>();

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
                                        Block block = new Block(problem.typeNumberOfBox);
                                        block.complexLevel = 0;
                                        block.typeNumberOfBox = problem.typeNumberOfBox;
                                        block.blockLenght = blockLength;
                                        block.blockWidth = blockWidth;
                                        block.blockHigh = blockHigh;
                                        block.viableLength = blockLength;
                                        block.viableWidth = blockWidth;
                                        block.blockVolum = blockLength * blockWidth * blockHigh;
                                        block.neededBoxNumOfEachType[i] = nx * ny * nz;
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
                for (int i = 0; i < num; i++) {
                    returnBlockList.add(blockList.get(i));
                }
            }
        }else{
            for (ArrayList<Block> blockList : blockLists) {
                returnBlockList.addAll(blockList);
            }
        }
//        for (ArrayList<Block> blockList : blockLists) {
//            returnBlockList.addAll(blockList);
//        }
        return returnBlockList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-06-02 11-50-02
     * @Description A new iterative-doubling Greedy–Lookahead algorithm for the single
     * container loading problem 使用的 Guillotine Blocks (非完全支撑)
     * 8-23 更新,增加了单个箱子生成的块
    */

    public static ArrayList<Block> generateGuillotineBlocksNotFullySupported(Problem problem,int MaxBlocks,double MinFillRate){
        long start = System.currentTimeMillis();
        // 单个物品生成6个方项上的块
        ArrayList<Block> sixDirectionBlock = generateSixDirectionBlock(problem);
        // 生成简单块
        ArrayList<Block> blockList = generateSimpleBlocks(problem,MaxBlocks / 2);
        // 开始进行GC块拼接
        ArrayList<Block> pList = new ArrayList<>(blockList);

        while(blockList.size() < MaxBlocks){
            ArrayList<Block> newBlockList = new ArrayList<>();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombineGCBlock(block1,block2,MinFillRate,problem);
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
        for (Block block : sixDirectionBlock) {
            if(!blockList.contains(block)){
                blockList.add(block);
            }
        }

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
        ArrayList<Block> blockList = generateSimpleBlocks(problem,MaxBlocks / 2);
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
                    block = xCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                        rotateBlock = block.cloneObj();
                        double term = rotateBlock.blockLenght;
                        rotateBlock.blockLenght = rotateBlock.blockWidth;
                        rotateBlock.blockWidth = term;
                        rotateBlockList.add(rotateBlock);
                    }
                    //y
                    block = yCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                        rotateBlock = block.cloneObj();
                        double term = rotateBlock.blockLenght;
                        rotateBlock.blockLenght = rotateBlock.blockWidth;
                        rotateBlock.blockWidth = term;
                        rotateBlockList.add(rotateBlock);
                    }
                    //z
                    block = zCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                        rotateBlock = block.cloneObj();
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

        // 旋转后的块按照大小排序
        rotateBlockList.sort(new Comparator<Block>() {
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
        // 把旋转后的块的前n个加入blockList
        int n = 10000;
        for(int i = 0; i < Math.min(rotateBlockList.size(),n); i++){
            blockList.add(rotateBlockList.get(i));
        }

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
        System.out.println("MyGuillotine Blocks块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-04-29 09-50-44
     * @Description GP块生成方法(Bortfield 长论文)
     */
    public static ArrayList<Block> generateGeneralPackingBlocksFullySupported(Problem problem, int MaxBlocks, double MinFillRate, double MinAreaRate){
        ArrayList<Block> blockList = new ArrayList<>();
        // 用单个箱子生成6个方项的GC块
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
                            Block block = new Block(problem.typeNumberOfBox);
                            block.typeNumberOfBox = problem.typeNumberOfBox;
                            block.blockLenght = boxLength;
                            block.blockWidth = boxWidth;
                            block.blockHigh = boxHigh;
                            block.blockVolum = boxLength * boxWidth * boxHigh;
                            block.neededBoxNumOfEachType[p] = 1;
                            blockList.add(block);
                        }
                    }
                }
            }
        }
        // 开始拼接GP块
        ArrayList<Block> pList = new ArrayList<>(blockList);
        while(blockList.size() < MaxBlocks){
            ArrayList<Block> newBlockList = new ArrayList<>();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombineGPBlock(block1,block2,MinFillRate,MinAreaRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombineGPBlock(block1,block2,MinFillRate,MinAreaRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombineGPBlock(block1,block2,MinFillRate,MinAreaRate,problem);
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
        System.out.println("GP块生成完毕:" + blockList.size() +"块");
        return blockList;
    }

    public static Block xCombineGPBlock(Block block1, Block block2, double MinFillRate,double MinAreaRate, Problem problem){

        Block block = new Block(block1.typeNumberOfBox);
        // 条件1
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        // 条件2
        if(!(block1.blockHigh == block2.blockHigh &&
                block1.blockLenght == block1.viableLength &&
                block2.blockLenght == block2.viableLength &&
                block1.blockWidth >= block2.blockWidth)){
            return null;
        }

        block.blockLenght = block1.blockLenght + block2.blockLenght;
        block.blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        block.blockHigh = block1.blockHigh;
        block.viableLength = block1.viableLength + block2.viableLength;
        block.viableWidth = Math.min(block1.viableWidth, block2.viableWidth);
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        double viableRate = (block.viableLength * block.viableWidth) / (block.blockLenght * block.blockWidth);
        // 条件4
        if(fillRate < MinFillRate || viableRate < MinAreaRate){
            return null;
        }
        // 条件5
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            return block;
        }
        return null;
    }

    public static Block yCombineGPBlock(Block block1, Block block2, double MinFillRate,double MinAreaRate, Problem problem){

        Block block = new Block(block1.typeNumberOfBox);
        // 条件1
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        // 条件2
        if(!(block1.blockHigh == block2.blockHigh &&
                block1.blockWidth == block1.viableWidth &&
                block2.blockWidth == block2.viableWidth &&
                block1.blockLenght >= block2.blockLenght)){
            return null;
        }

        block.blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        block.blockWidth = block1.blockWidth + block2.blockWidth;
        block.blockHigh = block1.blockHigh;
        block.viableLength = Math.min(block1.viableLength, block2.viableLength);
        block.viableWidth = block1.viableWidth + block2.viableWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        double viableRate = (block.viableLength * block.viableWidth) / (block.blockLenght * block.blockWidth);
        // 条件4
        if(fillRate < MinFillRate || viableRate < MinAreaRate){
            return null;
        }
        // 条件5
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            return block;
        }
        return null;
    }

    public static Block zCombineGPBlock(Block block1, Block block2, double MinFillRate,double MinAreaRate, Problem problem){

        Block block = new Block(block1.typeNumberOfBox);
        // 条件1
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        // 条件2
        if(!(block1.viableLength >= block2.blockLenght && block1.viableWidth >= block2.blockWidth)){
            return null;
        }

        block.blockLenght = block1.blockLenght;
        block.blockWidth = block1.blockWidth;
        block.blockHigh = block1.blockHigh + block2.blockHigh;
        block.viableLength = block2.viableLength;
        block.viableWidth = block2.viableWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        double viableRate = (block.viableLength * block.viableWidth) / (block.blockLenght * block.blockWidth);
        // 条件4
        if(fillRate < MinFillRate || viableRate < MinAreaRate){
            return null;
        }
        // 条件5
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            return block;
        }
        return null;
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-12 11-59-19
     * @Description 速度提升版本 generateGeneralCuttingBlocksNotFullySupported
    */
    public static ArrayList<Block> generateGeneralCuttingBlocksNotFullySupportedFaster(Problem problem, int MaxBlocks, double MinFillRate){
        long start = System.currentTimeMillis();
        HashSet<Block> blockList = new HashSet<>();
        // 用单个箱子生成6个方项的GC块
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
                            Block block = new Block(problem.typeNumberOfBox);
                            block.typeNumberOfBox = problem.typeNumberOfBox;
                            block.blockLenght = boxLength;
                            block.blockWidth = boxWidth;
                            block.blockHigh = boxHigh;
                            block.blockVolum = boxLength * boxWidth * boxHigh;
                            block.neededBoxNumOfEachType[p] = 1;
                            blockList.add(block);
                        }
                    }
                }
            }
        }
        // 开始进行GC块拼接
        HashSet<Block> pList = new HashSet<>(blockList);
        while(blockList.size() < MaxBlocks){
            HashSet<Block> newBlockList = new HashSet<>();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombineGCBlock(block1,block2,MinFillRate,problem);
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
        ArrayList<Block> blockArrayList = new ArrayList<>(blockList);
        blockArrayList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (o2.blockVolum > o1.blockVolum) {
                    return 1;
                } else if(o2.blockVolum < o1.blockVolum){
                        return -1;
                } else {
                        return 0;
                       }
            }
        });
        System.out.println("GC块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockArrayList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-04-25 10-43-49
     * @Description 按照原论文修复
     */
    public static ArrayList<Block> generateGeneralCuttingBlocksNotFullySupported(Problem problem, int MaxBlocks, double MinFillRate){
        long start = System.currentTimeMillis();
        ArrayList<Block> blockList = new ArrayList<>();
        // 用单个箱子生成6个方项的GC块
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
                            Block block = new Block(problem.typeNumberOfBox);
                            block.typeNumberOfBox = problem.typeNumberOfBox;
                            block.blockLenght = boxLength;
                            block.blockWidth = boxWidth;
                            block.blockHigh = boxHigh;
                            block.blockVolum = boxLength * boxWidth * boxHigh;
                            block.neededBoxNumOfEachType[p] = 1;
                            blockList.add(block);
                        }
                    }
                }
            }
        }
        // 开始进行GC块拼接
        ArrayList<Block> pList = new ArrayList<>(blockList);
        while(blockList.size() < MaxBlocks){
            ArrayList<Block> newBlockList = new ArrayList<>();
            for (Block block1 : pList) {
                for (Block block2 : blockList) {
                    // 获取三个方项的拼接块
                    Block block;
                    //x
                    block = xCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //y
                    block = yCombineGCBlock(block1,block2,MinFillRate,problem);
                    if(block != null && !newBlockList.contains(block)){
                        newBlockList.add(block);
                    }
                    //z
                    block = zCombineGCBlock(block1,block2,MinFillRate,problem);
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
        System.out.println("GC块生成完毕:" + blockList.size() +"块");
        System.out.println("生成时间" + (System.currentTimeMillis() - start) + "ms");
        return blockList;
    }

    public static Block xCombineGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件3
        if(block1.blockWidth * block1.blockHigh < block2.blockWidth * block2.blockHigh){
            return null;
        }

        Block block = new Block(block1.typeNumberOfBox);
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null; // 条件1
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }

        block.blockLenght = block1.blockLenght + block2.blockLenght;
        block.blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        block.blockHigh = Math.max(block1.blockHigh, block2.blockHigh);
        block.viableLength = block.blockLenght;
        block.viableWidth = block.blockWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        if(fillRate < MinFillRate){ // 条件4
            return null;
        }
        // 条件2
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            return block;
        }
        return null;
    }

    public static Block yCombineGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件3
        if(block1.blockLenght * block1.blockHigh < block2.blockLenght * block2.blockHigh){
            return null;
        }

        Block block = new Block(block1.typeNumberOfBox);
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null; // 条件1
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }

        block.blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        block.blockWidth = block1.blockWidth + block2.blockWidth;
        block.blockHigh = Math.max(block1.blockHigh, block2.blockHigh);
        block.viableLength = block.blockLenght;
        block.viableWidth = block.blockWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        if(fillRate < MinFillRate){
            return null; // 条件4
        }
        // 条件2
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            return block;
        }
        return null;
    }

    public static Block zCombineGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件3
        if(block1.blockLenght * block1.blockWidth < block2.blockLenght * block2.blockWidth){
            return null;
        }
        Block block = new Block(block1.typeNumberOfBox);
        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null; // 条件1
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        block.typeNumberOfBox = block1.typeNumberOfBox;
        block.blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        block.blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        block.blockHigh = block1.blockHigh + block2.blockHigh;
        block.viableLength = block.blockLenght;
        block.viableWidth = block.blockWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        if(fillRate < MinFillRate){
            return null; // 条件4
        }
        // 条件2
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            return block;
        }
        return null;
    }

    /**
     * @Author yajiewen
     * @Date 2022-04-10 18-42-18
     * @Description 修复 nz < box.boxNumber 导致的快丢失问题 需要加上=号nz <= box.boxNumber
     */
    public static ArrayList<Block> getSimpleBlockListWithDirection(Problem problem){
        ArrayList<Block> blockList = new ArrayList<>();
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
                                        Block block = new Block(problem.typeNumberOfBox);
                                        block.complexLevel = 0;
                                        block.typeNumberOfBox = problem.typeNumberOfBox;
                                        block.blockLenght = blockLength;
                                        block.blockWidth = blockWidth;
                                        block.blockHigh = blockHigh;
                                        block.viableLength = blockLength;
                                        block.viableWidth = blockWidth;
                                        block.blockVolum = blockLength * blockWidth * blockHigh;
                                        block.neededBoxNumOfEachType[i] = nx * ny * nz;
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
     * @Description 一步生成复杂快
    */
    public static ArrayList<Block> generateComplexBlocksFullySupported(int MaxLevel, int MaxBlocks, Problem problem, double MinFillRate, double MinAreaRate){
        ArrayList<Block> blockList = getSimpleBlockListWithDirection(problem);
        for (int level = 0; level < MaxLevel; level++) {
            ArrayList<Block> newBlockList = new ArrayList<>();
            if(blockList.size() > MaxBlocks){
                break;
            }
            for(int i = 0; i < blockList.size(); i++){
                Block block1 = blockList.get(i);
                for(int j = 0; j < blockList.size(); j++){
                    Block block2 = blockList.get(j);
                    if(block1.complexLevel == level || block2.complexLevel == level){
                        // 开始判断各个方项是否满足要求
                        // x
                        Block block = xCombineComplexBlock(block1,block2,problem,MinFillRate,MinAreaRate);
                        if(block != null){
                            newBlockList.add(block);
                        }
                        // y
                        block = yCombineComplexBlock(block1,block2,problem,MinFillRate,MinAreaRate);
                        if(block != null){
                            newBlockList.add(block);
                        }
                        // z
                        block = zCombineComplexBlock(block1,block2,problem,MinFillRate,MinAreaRate);
                        if(block != null){
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
        return blockList;
    }

    public static Block xCombineComplexBlock(Block block1, Block block2, Problem problem, double MinFillRate, double MinAreaRate){
        Block block = new Block(block1.typeNumberOfBox);

        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        block.complexLevel = Math.max(block1.complexLevel,block2.complexLevel) + 1;
        block.blockLenght = block1.blockLenght + block2.blockLenght;
        block.blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        block.blockHigh = block1.blockHigh;
        block.viableLength = block1.viableLength + block2.viableLength;
        block.viableWidth = Math.min(block1.viableWidth, block2.viableWidth);
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        double viableRate = (block.viableLength * block.viableWidth) / (block.blockLenght * block.blockWidth);
        // 判断生成的c是否正确
        // 判断是否超出容器大小
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            //判断填充率和可行域
            if(fillRate >= MinFillRate && viableRate >= MinAreaRate){
                // 判断是否满足可行放置矩阵条件
                if(block1.viableLength == block1.blockLenght && block2.viableLength == block2.blockLenght && block1.blockHigh == block2.blockHigh){
                    return block;
                }
            }
        }
        return null;
    }
    public static Block yCombineComplexBlock(Block block1, Block block2, Problem problem, double MinFillRate, double MinAreaRate){
        Block block = new Block(block1.typeNumberOfBox);

        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        block.complexLevel = Math.max(block1.complexLevel,block2.complexLevel) + 1;
        block.blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        block.blockWidth = block1.blockWidth + block2.blockWidth;
        block.blockHigh = block1.blockHigh;
        block.viableLength = Math.min(block1.viableLength, block2.viableLength);
        block.viableWidth = block1.viableWidth + block2.viableWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        double viableRate = (block.viableLength * block.viableWidth) / (block.blockLenght * block.blockWidth);
        // 判断生成的c是否正确
        // 判断是否超出容器大小
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            //判断填充率和可行域
            if(fillRate >= MinFillRate && viableRate >= MinAreaRate){
                // 判断是否满足可行放置矩阵条件
                if(block1.viableWidth == block1.blockWidth && block2.viableWidth == block2.blockWidth && block1.blockHigh == block2.blockHigh){
                    return block;
                }
            }
        }
        return null;
    }
    public static Block zCombineComplexBlock(Block block1, Block block2, Problem problem, double MinFillRate, double MinAreaRate){
        Block block = new Block(block1.typeNumberOfBox);

        for(int i = 0; i < block1.typeNumberOfBox; i++){
            if(block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i] > problem.boxList.get(i).boxNumber){
                return null;
            }else{
                block.neededBoxNumOfEachType[i] = block1.neededBoxNumOfEachType[i] + block2.neededBoxNumOfEachType[i];
            }
        }
        block.complexLevel = Math.max(block1.complexLevel,block2.complexLevel) + 1;
        block.blockLenght = block1.blockLenght;
        block.blockWidth = block1.blockWidth;
        block.blockHigh = block1.blockHigh + block2.blockHigh;
        block.viableLength = block2.viableLength;
        block.viableWidth = block2.viableWidth;
        block.blockVolum = block1.blockVolum + block2.blockVolum;

        double fillRate = block.blockVolum / (block.blockLenght * block.blockWidth * block.blockHigh);
        double viableRate = (block.viableLength * block.viableWidth) / (block.blockLenght * block.blockWidth);
        // 判断生成的c是否正确
        // 判断是否超出容器大小
        if(block.blockLenght <= problem.containnerLength && block.blockWidth <= problem.containnerWidth && block.blockHigh <= problem.containnerHigh){
            //判断填充率和可行域
            if(fillRate >= MinFillRate && viableRate >= MinAreaRate){
                // 判断是否满足可行放置矩阵条件
                if(block1.viableLength >= block2.blockLenght && block1.viableWidth >= block2.blockWidth){
                    return block;
                }
            }
        }
        return null;
    }


    // 查找可行块表(所有)
    public static ArrayList<Block> searchViableBlock(Space space, ArrayList<Block> blockList, int [] availNum){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        for (Block block : blockList) {
            boolean isOk = true;
            if(block.blockLenght <= space.spaceLength &&
                    block.blockWidth <= space.spaceWidth &&
                    block.blockHigh <= space.spaceHigh){
                for(int i = 0; i < availNum.length; i++){
                    if(block.neededBoxNumOfEachType[i] > availNum[i]){
                        isOk = false;
                        break;
                    }
                }
                if(isOk){
                    viableBlockList.add(block);
                }
            }
        }
        return viableBlockList;
    }

    // 查找可行块表(限定数目)
    public static ArrayList<Block> searchViableBlockWithNumLimit(Space space, ArrayList<Block> blockList, int [] availNum, int number){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        for (Block block : blockList) {
            boolean isOk = true;
            if(block.blockLenght <= space.spaceLength &&
                    block.blockWidth <= space.spaceWidth &&
                    block.blockHigh <= space.spaceHigh){
                for(int i = 0; i < availNum.length; i++){
                    if(block.neededBoxNumOfEachType[i] > availNum[i]){
                        isOk = false;
                        break;
                    }
                }
                if(isOk){
                    viableBlockList.add(block);
                }
            }
            if(viableBlockList.size() >= number){
                break;
            }
        }
        return viableBlockList;
    }

    // 根据f(b,r) 查找可行快表
    public static ArrayList<Block> searchViableBlockWithNumLimitAndFbr(Space space, State state, int number){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        HashMap<Integer,String> map = new HashMap<>();

        for (int i = 0; i < state.blockList.size(); i++) {
            // 拿取一个块
            Block block = state.blockList.get(i);
            // 计算这个块的得分
            int score = scoreFunction(space, block, state);
            // 将得分和 对应块的在blockList 中的下标加入map
            if(score != Integer.MIN_VALUE){
                if(map.containsKey(score)){
                    map.put(score, map.get(score) + " " + i);
                }else{
                    map.put(score, String.valueOf(i));
                }
            }
        }
        // 对所有块的score 排序
        ArrayList<Integer> scoreList = new ArrayList<>(map.keySet());
        scoreList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });

        // 选出满足 数量要求的块
        int minL = Math.min(number, scoreList.size());
        for(int i = 0; i < minL; i++){
            String[] indexList = map.get(scoreList.get(i)).split(" ");
            boolean isFull = false;
            for (int i1 = 0; i1 < indexList.length; i1++) {
                if(viableBlockList.size() < minL){
                    viableBlockList.add(state.blockList.get(Integer.parseInt(indexList[i1])));
                }else{
                    isFull = true;
                    break;
                }
            }
            if(isFull){
                break;
            }
        }
        return viableBlockList;
    }

    public static int scoreFunction(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
            // 判断剩余箱子数目是否能满足这个块
            boolean isOk = true;
            for(int j = 0; j < state.availBox.length; j++){
                if(block.neededBoxNumOfEachType[j] > state.availBox[j]){
                    isOk = false;
                    break;
                }
            }
            // 如果剩余箱子数目满足这个块
            if(isOk){
                // 开始计算分数
                // 1.求放入这个箱子后 剩余的箱子数目
                int[] availBox2 = new int[state.availBox.length];
                for(int i = 0; i < state.availBox.length; i++){
                    availBox2[i] = state.availBox[i] - block.neededBoxNumOfEachType[i];
                }
                // 求所有剩余箱子的数目
                int boxNum = 0;
                for(int i = 0; i <availBox2.length; i++){
                    boxNum += availBox2[i];
                }
                // 定义剩余箱子的 长 宽 高 长度表
                int[] lengthList = new int[boxNum];
                int[] widthList = new int[boxNum];
                int[] hightList = new int[boxNum];
                int index = 0;
                for(int i = 0; i < availBox2.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < availBox2[i]; j++){
                        lengthList[index] = (int) box.boxLength;
                        widthList[index] = (int) box.boxWidth;
                        hightList[index] = (int) box.boxHigh;
                        index++;
                    }
                }
                int leftLength = (int) (space.spaceLength - block.blockLenght);
                int leftWidth = (int) (space.spaceWidth - block.blockWidth);
                int leftHigh = (int) (space.spaceHigh - block.blockHigh);

                int maxLength = KPA(leftLength, lengthList);
                int maxWidth = KPA(leftWidth, widthList);
                int maxHigh = KPA(leftHigh, hightList);

                int vLoss =(int) ( (space.spaceLength * space.spaceWidth * space.spaceHigh) - (block.blockLenght + maxLength) * (block.blockWidth + maxWidth) * (block.blockHigh + maxHigh) );
                int score = (int) (block.blockLenght * block.blockWidth * block.blockHigh) - vLoss;
                return score;
            }else{
                return Integer.MIN_VALUE;
            }
        }else{
            return Integer.MIN_VALUE;
        }
    }

    public static int KPA(int totalLength, int[] lengthList){
        int[] outComeList = new int[totalLength + 1]; // 用于存放中间过程

        for(int i = 0; i < lengthList.length; i++){
            for(int j = totalLength; j >= lengthList[i]; j--){
                outComeList[j] = Math.max(outComeList[j], outComeList[ j - lengthList[i] ] + lengthList[i]);
            }
        }
        return outComeList[totalLength];
    }

    /***************************************************************************************************
     * @Author yajiewen
     * @Date 2022-09-03 12-15-04
     * @Description 开始自己的快选择函数
    */
    public static ArrayList<Block> myBlockSearchFunction(State state, Space space, int num){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        // 先找出所有可行的块(即满足长宽高箱子数量要求)
        viableBlockList = searchViableBlock(space, state.blockList, state.availBox);
        // 根据评分函数计算所有可行块的评分
        for (Block block : viableBlockList) {
            generBlockScore(block,space,state);
        }
        // 根据分数从大到小排序
        viableBlockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if(o2.blockScore > o1.blockScore){
                    return 1;
                }else{
                    if(o2.blockScore < o1.blockScore){
                        return -1;
                    }else{
                        return 0;
                    }
                }
            }
        });
        // 依次选出num个块返回
        ArrayList<Block> returnBlockList = new ArrayList<>();
        int minNum = Math.min(num, viableBlockList.size());
        for (int i = 0; i < minNum; i++) {
            returnBlockList.add(viableBlockList.get(i));
        }
        return returnBlockList;
    }
    // 把各种scoreFunction 结合得到块分数
    public static void generBlockScore(Block block, Space space, State state){
        // 定义权重
        double q1 = 1,q2 = -0.8;
        block.blockScore =q1 * myscoreFunction1(block,space) - q2 * myscoreFunction2(block, state.spaceArrayList);
    }
    // 根据块三边长度与空间的比例得到分数
    /*
    这个好处在于当某一边太短时候，那一边计算出的分数为负数
    (2blockL- spaceL)/spaceL + (2blockW - spaceW)/spaceW + (2blockH -spaceH) /spaceH
     */
    public static double myscoreFunction1(Block block, Space space){
        double score = 0.0;
        score = (block.blockLenght / space.spaceLength) + (block.blockWidth / space.spaceWidth) + (block.blockHigh / space.spaceHigh)
                - ((space.spaceLength - block.blockLenght) / space.spaceLength) - ((space.spaceWidth - block.blockWidth) / space.spaceWidth)
                -((space.spaceHigh - block.blockHigh) / space.spaceHigh);
        return score;
    }
    /*
    根据块和其他空间的交叠情况定义分数，选择交叠数最少的块
     */

    public static double myscoreFunction2(Block block, ArrayList<Space> spaceArrayList){
        double returnNum = 0.0;
        for (Space space : spaceArrayList) {
            Block coverBlock = ResourceHelper.getCoverBlock(space,block);
            if(coverBlock != null){
                returnNum++;
            }
        }
        return returnNum - 1; // 把当被放置的空间除外
    }
}
