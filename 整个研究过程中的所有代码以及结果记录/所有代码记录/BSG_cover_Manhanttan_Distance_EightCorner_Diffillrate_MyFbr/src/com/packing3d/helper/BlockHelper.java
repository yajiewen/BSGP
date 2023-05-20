package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.space.Space;
import com.packing3d.datastructure.state.State;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
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
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-10 09-32-34
     * @Description 添加6个方向上的单独箱子构成的块
    */
    public static ArrayList<Block> generateGuillotineBlocksNotFullySupported(Problem problem,int MaxBlocks,double MinFillRate){
        long start = System.currentTimeMillis();
        ArrayList<Block> blockList = generateSimpleBlocks(problem,MaxBlocks / 2);
        // 单个物品生成6个方项上的块
        ArrayList<Block> sixDirectionBlock = generateSixDirectionBlock(problem);

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

//        blockList.sort(new Comparator<Block>() {
//            @Override
//            public int compare(Block o1, Block o2) {
//                if (o2.blockVolum > o1.blockVolum) {
//                    return 1;
//                } else {
//                    if (o2.blockVolum < o1.blockVolum) {
//                        return -1;
//                    } else {
//                        return 0;
//                    }
//                }
//            }
//        });
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
     * @Description 按照原论文修复 The six elements to block-building approaches for the single container loading problem
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
    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-46-51
     * @Description 重大优化 优化条件判断顺序加速 非完全支撑结合
    */
    public static Block xCombineGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件1
        if(block1.blockWidth * block1.blockHigh < block2.blockWidth * block2.blockHigh){
            return null;
        }

        double blockLenght = block1.blockLenght + block2.blockLenght;
        double blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        double blockHigh = Math.max(block1.blockHigh, block2.blockHigh);
        // 条件2
        if(blockLenght > problem.containnerLength && blockWidth > problem.containnerWidth && blockHigh > problem.containnerHigh){
            return null;
        }

        double viableLength = blockLenght;
        double viableWidth = blockWidth;
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        // 条件3
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
        Block block = new Block(block1.typeNumberOfBox,blockLenght,blockWidth,blockHigh,viableLength,viableWidth,blockVolum,blockNeededBoxNumOfEachType);
        return block;
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-46-51
     * @Description 优化条件判断顺序加速
     */
    public static Block yCombineGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件1
        if(block1.blockLenght * block1.blockHigh < block2.blockLenght * block2.blockHigh){
            return null;
        }

        double blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        double blockWidth = block1.blockWidth + block2.blockWidth;
        double blockHigh = Math.max(block1.blockHigh, block2.blockHigh);
        // 条件2
        if(blockLenght > problem.containnerLength && blockWidth > problem.containnerWidth && blockHigh > problem.containnerHigh){
            return null;
        }

        double viableLength = blockLenght;
        double viableWidth = blockWidth;
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        // 条件3
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
        Block block = new Block(block1.typeNumberOfBox,blockLenght,blockWidth,blockHigh,viableLength,viableWidth,blockVolum,blockNeededBoxNumOfEachType);
        return block;
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-46-51
     * @Description 优化条件判断顺序加速
     */
    public static Block zCombineGCBlock(Block block1, Block block2, double MinFillRate, Problem problem){
        // 条件1
        if(block1.blockLenght * block1.blockWidth < block2.blockLenght * block2.blockWidth){
            return null;
        }

        double blockLenght = Math.max(block1.blockLenght, block2.blockLenght);
        double blockWidth = Math.max(block1.blockWidth, block2.blockWidth);
        double blockHigh = block1.blockHigh + block2.blockHigh;
        // 条件2
        if(blockLenght > problem.containnerLength && blockWidth > problem.containnerWidth && blockHigh > problem.containnerHigh){
            return null;
        }

        double viableLength = blockLenght;
        double viableWidth = blockWidth;
        double blockVolum = block1.blockVolum + block2.blockVolum;
        double fillRate = blockVolum / (blockLenght * blockWidth * blockHigh);
        // 条件3
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

        Block block = new Block(block1.typeNumberOfBox,blockLenght,blockWidth,blockHigh,viableLength,viableWidth,blockVolum,blockNeededBoxNumOfEachType);
        return block;
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

    /**
     * @Author yajiewen
     * @Date 2022-10-04 11-11-27
     * @Description 我自己的fbr 根据能放入剩余三个方向上的箱子来求
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-05 10-02-26
     * @Description 把fbrscore改为double
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitAndMyFbr(Space space, State state, int number){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        double start = System.currentTimeMillis();
        for (int i = 0; i < state.blockList.size(); i++) {
            // 拿取一个块
            Block block = state.blockList.get(i);
            block.fbrScore = scoreFunctionThree(space, block, state);

        }
//        System.out.println("myFBrTime:" + (System.currentTimeMillis() - start));
        int minL = Math.min(number, state.blockList.size());
        // 简单选择排序选出最大的minL个块,放在块表前面
        for(int i = 0; i < minL; i++){
            int maxIndex = i;
            for(int j = i + 1; j < state.blockList.size(); j++){
                if(state.blockList.get(maxIndex).fbrScore < state.blockList.get(j).fbrScore){
                    maxIndex = j;
                }
            }
            // 交换
            Block blockTemp = state.blockList.get(i);
            state.blockList.set(i,state.blockList.get(maxIndex));
            state.blockList.set(maxIndex,blockTemp);
            // 放入fbr分数最大的主义minvalue表示不能放入空间
            if(state.blockList.get(i).fbrScore != Integer.MIN_VALUE){
                viableBlockList.add(state.blockList.get(i));
            }
        }
//        System.out.println("fbr块搜索耗时：" + (System.currentTimeMillis() - start) +"ms");
        return viableBlockList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-05 09-49-20
     * @Description 优化maxLength，maxWidth 和maxHigh的求法，优化为一个循环
    */
    public static int scoreFunctionThree(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
            // 求放入这个箱子后 剩余的箱子数目
            int[] availBox2 = new int[state.availBox.length];
            for(int i = 0; i < state.availBox.length; i++){
                availBox2[i] = state.availBox[i] - block.neededBoxNumOfEachType[i];
            }

            // 求三个方向上剩余的长度
            double leftLength = space.spaceLength - block.blockLenght;
            double leftWidth = space.spaceWidth - block.blockWidth;
            double leftHigh = space.spaceHigh - block.blockHigh;

            ArrayList<Double> maxList = getMaxLWH(leftLength,leftWidth,leftHigh,space.spaceLength, space.spaceWidth, space.spaceHigh, state,availBox2);
            double maxLength = maxList.get(0);
            double maxWidth = maxList.get(1);
            double maxHigh = maxList.get(2);
            int vLoss =(int) ( (space.spaceLength * space.spaceWidth * space.spaceHigh) - (block.blockLenght + maxLength) * (block.blockWidth + maxWidth) * (block.blockHigh + maxHigh) );
            int score =(int) (block.blockLenght * block.blockWidth * block.blockHigh) - vLoss;

            return score;
        }else{
            return Integer.MIN_VALUE;
        }
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-06 09-44-40
     * @Description 求能放入x方向的箱子的最大长度，能放入y方向的箱子的最大长度，能放入z方向的箱子的最大长度
    */
    public static ArrayList<Double> getMaxLWH(double leftLength,double leftWidth,double leftHigh, double spaceLength, double spaceWidth, double spaceHigh, State state, int[] avaiBox){
        ArrayList<Double> returnList = new ArrayList<>();
        double maxLength = 0;
        double maxWidth = 0;
        double maxHigh = 0;
        // 便利每一个箱子
        for (int i = 0; i < avaiBox.length; i++) {
            if(avaiBox[i] > 0 ){
                // 判断箱子是否可已放入空间
                Box box = state.problem.boxList.get(i);
                // 求maxLength
                if(box.boxLength <= leftLength && box.boxWidth <= spaceWidth && box.boxHigh <= spaceHigh){
                    if(box.boxLength > maxLength){
                        maxLength = box.boxLength;
                    }
                }
                // 求maxWidth
                if(box.boxLength <= spaceLength && box.boxWidth <= leftWidth && box.boxHigh <= spaceHigh){
                    if(box.boxWidth > maxWidth){
                        maxWidth = box.boxWidth;
                    }
                }
                // 求maxHigh
                if(box.boxLength <= spaceLength && box.boxWidth <= spaceWidth && box.boxHigh <= leftHigh){
                    if(box.boxHigh > maxHigh){
                        maxHigh = box.boxHigh;
                    }
                }
            }
        }
        returnList.add(maxLength);
        returnList.add(maxWidth);
        returnList.add(maxHigh);
        return returnList;
    }

    // 根据f(b,r) 查找可行快表
    /**
     * @Author yajiewen
     * @Date 2022-10-02 22-40-39
     * @Description 改进之前没有在block里面添加score 现在加了直接对blockList进行排序，超级无敌优化
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-05 09-30-49
     * @Description fbr 只求前面300个块的fbr
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitAndFbr(Space space, State state, int number){
        double start = System.currentTimeMillis();
        ArrayList<Block> viableBlockList = new ArrayList<>();

        // 计算每一个块的fbr得分
//        System.out.println("需要就计算fbr的块数目" + state.blockList.size());
        for (int i = 0; i < state.blockList.size(); i++) {
            // 拿取一个块
            Block block = state.blockList.get(i);
            // 计算这个块的得分
            // 计算前300个
            if( true ){
                block.fbrScore = scoreFunctionFive(space, block, state);
            }else{
                block.fbrScore = Integer.MIN_VALUE;
            }

        }
//        System.out.println("所有KPA耗时；" + (System.currentTimeMillis() - start) + "ms");

        int minL = Math.min(number, state.blockList.size());
        // 简单选择排序选出最大的minL个块,放在块表前面
        for(int i = 0; i < minL; i++){
            int maxIndex = i;
            for(int j = i + 1; j < state.blockList.size(); j++){
                if(state.blockList.get(maxIndex).fbrScore < state.blockList.get(j).fbrScore){
                    maxIndex = j;
                }
            }
            // 交换
            Block blockTemp = state.blockList.get(i);
            state.blockList.set(i,state.blockList.get(maxIndex));
            state.blockList.set(maxIndex,blockTemp);
            if(state.blockList.get(i).fbrScore != Integer.MIN_VALUE){
                viableBlockList.add(state.blockList.get(i));
            }
        }

//        System.out.println("fbr块搜索耗时：" + (System.currentTimeMillis() - start) +"ms");
        return viableBlockList;
    }


    /**
     * @Author yajiewen
     * @Date 2022-10-19 20-44-11
     * @Description 今日彻底放弃这个 公式太菜
    */
    public static int scoreFunction(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
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
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 21-23-58
     * @Description 全新的fbr 重大突破 hhhhhhhhhhhh
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitByFbr(Space space, State state, int number){
        double start = System.currentTimeMillis();
        ArrayList<Block> viableBlockList = new ArrayList<>();

        // 每次选出一个空间，咱么就要更新三个方向的vloss 向量
        generateVlossVector(state, space);

        for (int i = 0; i < state.blockList.size(); i++) {
            // 拿取一个块
            Block block = state.blockList.get(i);
            // 计算这个块的得分
            block.fbrScore = scoreFunctionByLossVector(space, block, state);
        }

        int minL = Math.min(number, state.blockList.size());
        // 简单选择排序选出最大的minL个块,放在块表前面
        for(int i = 0; i < minL; i++){
            int maxIndex = i;
            for(int j = i + 1; j < state.blockList.size(); j++){
                if(state.blockList.get(maxIndex).fbrScore < state.blockList.get(j).fbrScore){
                    maxIndex = j;
                }
            }
            // 交换
            Block blockTemp = state.blockList.get(i);
            state.blockList.set(i,state.blockList.get(maxIndex));
            state.blockList.set(maxIndex,blockTemp);
            if(state.blockList.get(i).fbrScore != Integer.MIN_VALUE){
                viableBlockList.add(state.blockList.get(i));
            }
        }

        return viableBlockList;
    }

    public static int scoreFunctionByLossVector(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
            int lMax = state.xVlossVector.get((int) (space.spaceLength - block.blockLenght) );
            int wMax = state.yVlossVector.get((int) (space.spaceWidth - block.blockWidth) );
            int hMax = state.zVlossVector.get((int) (space.spaceHigh - block.blockHigh) );
            int vLoss = (int) ( space.spaceLength * space.spaceWidth * space.spaceHigh - (block.blockLenght + lMax) * (block.blockWidth + wMax) * (block.blockHigh + hMax));
            int vWaste = (int) (block.blockLenght * block.blockWidth * block.blockHigh - block.blockVolum);
            int score = (int) block.blockVolum - vLoss;
            return score;
        }else{
            return Integer.MIN_VALUE;
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 21-01-40
     * @Description 重大发现，计算vLossVector VCS: A new heuristic function for selecting boxes in the single 老天保佑
     */
    public static void generateVlossVector(State state, Space space){
        state.xVlossVector = new ArrayList<>();
        state.yVlossVector = new ArrayList<>();
        state.zVlossVector = new ArrayList<>();

        // 定义剩余箱子的 长 宽 高 长度表
        ArrayList<Integer> lengthList = new ArrayList<>();
        ArrayList<Integer> widthList = new ArrayList<>();
        ArrayList<Integer> hightList = new ArrayList<>();

        Box box;
        for(int j = 0; j < state.availBox.length; j++){
            if(state.availBox[j] > 0){
                box = state.problem.boxList.get(j);
                if(box.boxLength <= space.spaceLength){
                    lengthList.add((int) box.boxLength);
                }
                if(box.boxWidth <= space.spaceWidth){
                    widthList.add((int) box.boxWidth);
                }
                if(box.boxHigh <= space.spaceHigh){
                    hightList.add((int) box.boxHigh);
                }
            }
        }
        // 计算x方向上的vloss 向量 不需要取等号， 剩余长度为0的最大线性组合也要放进去，因为箱子长度不能为0所以不需要取等号
        for(int i = 0; i < (int) space.spaceLength; i++){
            if(i <= 10){
                state.xVlossVector.add(0);
            }else{
                state.xVlossVector.add(KPAForArrayList(i,lengthList));
            }
        }
        // 计算y方向上的vloss 向量
        for(int i = 0; i < (int) space.spaceWidth; i++){
            if(i <= 10){
                state.yVlossVector.add(0);
            }else{
                state.yVlossVector.add(KPAForArrayList(i,widthList));
            }
        }
        // 计算z方向上的vloss 向量
        for(int i = 0; i < (int) space.spaceHigh; i++){
            if(i <= 10){
                state.zVlossVector.add(0);
            }else{
                state.zVlossVector.add(KPAForArrayList(i,hightList));
            }
        }

    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 20-28-24
     * @Description 17 年VCS: A new heuristic function for selecting boxes in the single
     * container loading problem 里面对fbr的描述 ,原文里面没有vwaste，这里加了效果不好， 是不是每一个箱子的长宽高都放入list ：放入不放入效果差不多，但是放入减去vwaste效果变差，不放入减去效果好
    */
    public static int scoreFunctionFive(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
            // 开始计算分数
            // 1.求放入这个箱子后 剩余的箱子数目
            int[] availBox2 = new int[state.availBox.length];
            for(int i = 0; i < state.availBox.length; i++){
                availBox2[i] = state.availBox[i] - block.neededBoxNumOfEachType[i];
            }

            // 定义剩余箱子的 长 宽 高 长度表
            ArrayList<Integer> lengthList = new ArrayList<>();
            ArrayList<Integer> widthList = new ArrayList<>();
            ArrayList<Integer> hightList = new ArrayList<>();

            double leftLength = space.spaceLength - block.blockLenght;
            double leftWidth = space.spaceWidth - block.blockWidth;
            double leftHigh = space.spaceHigh - block.blockHigh;

            for(int i = 0; i < availBox2.length; i++){
                if (availBox2[i] > 0){
                    Box box = state.problem.boxList.get(i);
                    //for(int j = 0; j < availBox2[i]; j++){
                        if(box.boxLength < leftLength){
                            lengthList.add((int) box.boxLength);
                        }
                        if(box.boxWidth < leftWidth){
                            widthList.add((int) box.boxWidth);
                        }
                        if(box.boxHigh < leftHigh) {
                            hightList.add((int) box.boxHigh);
                        }
                    //}
                }
            }

            int maxLength = KPAForArrayList((int)leftLength, lengthList);
            int maxWidth = KPAForArrayList((int)leftWidth, widthList);
            int maxHigh = KPAForArrayList((int)leftHigh, hightList);

            int vLoss =(int) ( (space.spaceLength * space.spaceWidth * space.spaceHigh) - (block.blockLenght + maxLength) * (block.blockWidth + maxWidth) * (block.blockHigh + maxHigh) );
            int vWaste = (int) (block.blockLenght * block.blockWidth * block.blockHigh - block.blockVolum);
            int score = (int) block.blockVolum - vLoss - vWaste;
            return score;
        }else{
            return Integer.MIN_VALUE;
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-02 22-08-10
     * @Description 看了论文后的修改,长度，宽，高的线性组合考虑了箱子能否放入，求能放入x方向剩余空间的所有箱子的长度的线性组合，y，z方向也是一样
    */
    public static int scoreFunctionTwo(Space space, Block block, State state){
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

                // 求放入块后长宽高三个方向上剩余的长度
                int leftLength = (int) (space.spaceLength - block.blockLenght);
                int leftWidth = (int) (space.spaceWidth - block.blockWidth);
                int leftHigh = (int) (space.spaceHigh - block.blockHigh);

                // 求能放入x方向上剩余空间的每一种箱子的数目
                int[] xAvaiBox = getEachBoxNumForXyz(leftLength, space.spaceWidth, space.spaceHigh, availBox2, state);
                // 求能放入y方向上剩余空间的每一种箱子的数目
                int[] yAvaiBox = getEachBoxNumForXyz(space.spaceLength, leftWidth, space.spaceHigh, availBox2, state);
                // 求能放入z方向上剩余空间的每一种箱子的数目
                int[] zAvaiBox = getEachBoxNumForXyz(space.spaceLength, space.spaceWidth, leftHigh, availBox2, state);

                // 获取lengthListArrayList
                ArrayList<Integer> lengthArray = new ArrayList<>();
                for(int i = 0; i < xAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < xAvaiBox[i]; j++){
                        lengthArray.add((int) box.boxLength);
                    }
                }
                // 获取widthListArrayList
                ArrayList<Integer> widthArray = new ArrayList<>();
                for(int i = 0; i < yAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < yAvaiBox[i]; j++){
                        widthArray.add((int) box.boxWidth);
                    }
                }

                // 获取hightListArrayList
                ArrayList<Integer> highArray = new ArrayList<>();
                for(int i = 0; i < zAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < zAvaiBox[i]; j++){
                        highArray.add((int) box.boxHigh);
                    }
                }
                // 定义剩余箱子的 长 宽 高 长度表
                int[] lengthList = new int[lengthArray.size()];
                for (int i = 0; i < lengthArray.size(); i++) {
                    lengthList[i] = lengthArray.get(i);
                }
                int[] widthList = new int[widthArray.size()];
                for (int i = 0; i < widthArray.size(); i++) {
                    widthList[i] = widthArray.get(i);
                }
                int[] hightList = new int[highArray.size()];
                for (int i = 0; i < highArray.size(); i++) {
                    hightList[i] = highArray.get(i);
                }

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
    /**
     * @Author yajiewen
     * @Date 2022-10-09 11-12-28
     * @Description 对scoreFunctionTwo的优化(但是每个类别的箱子长宽高只放了一次)
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-19 00-21-40
     * @Description 根据论文A new iterative-doubling Greedy–Lookahead algorithm for the single
     * container loading problem添加 vwast变量 同时修复score 计算
     * s f(r,b) = V + a (Vloss + Vwaste)
     * where V is the total volume of the boxes
     * in the block, Vloss is the wasted space volume after the block b is
     * placed into r, Vwaste is the wasted block volume in block b
    */
    public static int scoreFunctionTwoFaster(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
                // 开始计算分数
                // 1.求放入这个箱子后 剩余的箱子数目
                int[] availBox2 = new int[state.availBox.length];
                for(int i = 0; i < state.availBox.length; i++){
                    availBox2[i] = state.availBox[i] - block.neededBoxNumOfEachType[i];
                }

                // 求放入块后长宽高三个方向上剩余的长度
                int leftLength = (int) (space.spaceLength - block.blockLenght);
                int leftWidth = (int) (space.spaceWidth - block.blockWidth);
                int leftHigh = (int) (space.spaceHigh - block.blockHigh);

                ArrayList<Integer> lengthArray = new ArrayList<>();
                ArrayList<Integer> widthArray = new ArrayList<>();
                ArrayList<Integer> highArray = new ArrayList<>();

                for(int i = 0; i < availBox2.length; i++){
                    if(availBox2[i] > 0 ){
                        // 判断箱子是否可已放入空间
                        Box box = state.problem.boxList.get(i);
                        // 求lengthArray
                        if(box.boxLength <= leftLength && box.boxWidth <= space.spaceWidth && box.boxHigh <= space.spaceHigh){
                            lengthArray.add((int) box.boxLength);
//                            for(int j = 0; j < availBox2[i]; j++){
//                                lengthArray.add((int) box.boxLength);
//                            }
                        }
                        // 求widthArray
                        if(box.boxLength <= space.spaceLength && box.boxWidth <= leftWidth && box.boxHigh <= space.spaceHigh){
                            widthArray.add((int) box.boxWidth);
                        }
                        // 求highArray
                        if(box.boxLength <= space.spaceLength && box.boxWidth <= space.spaceWidth && box.boxHigh <= leftHigh){
                            highArray.add((int) box.boxHigh);
                        }
                    }
                }


                // 定义剩余箱子的 长 宽 高 长度表
                int[] lengthList = new int[lengthArray.size()];
                for (int i = 0; i < lengthArray.size(); i++) {
                    lengthList[i] = lengthArray.get(i);
                }
                int[] widthList = new int[widthArray.size()];
                for (int i = 0; i < widthArray.size(); i++) {
                    widthList[i] = widthArray.get(i);
                }
                int[] hightList = new int[highArray.size()];
                for (int i = 0; i < highArray.size(); i++) {
                    hightList[i] = highArray.get(i);
                }

                int maxLength = KPA(leftLength, lengthList);
                int maxWidth = KPA(leftWidth, widthList);
                int maxHigh = KPA(leftHigh, hightList);

                int vLoss =(int) ( (space.spaceLength * space.spaceWidth * space.spaceHigh) - (block.blockLenght + maxLength) * (block.blockWidth + maxWidth) * (block.blockHigh + maxHigh) );
                int vWaste = (int) (block.blockLenght * block.blockWidth * block.blockHigh - block.blockVolum);
                int score = (int) (block.blockVolum) - vLoss - vWaste;
                return score;
            }else{
                return Integer.MIN_VALUE;
            }
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-05 10-41-08
     * @Description 按照G2LA说法三个方向上的空间的浪费程度单独计算，即求能放入x方向上的箱子的长的线性组合，宽的线性组合，高的线性组合，y，z方向上一样
    */
    public static int scoreFunctionTwoG2LA(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
                // 开始计算分数
                // 1.求放入这个箱子后 剩余的箱子数目
                int[] availBox2 = new int[state.availBox.length];
                for(int i = 0; i < state.availBox.length; i++){
                    availBox2[i] = state.availBox[i] - block.neededBoxNumOfEachType[i];
                }

                // 求放入块后长宽高三个方向上剩余的长度
                int leftLength = (int) (space.spaceLength - block.blockLenght);
                int leftWidth = (int) (space.spaceWidth - block.blockWidth);
                int leftHigh = (int) (space.spaceHigh - block.blockHigh);

                // 求能放入x方向上剩余空间的每一种箱子的数目
                int[] xAvaiBox = getEachBoxNumForXyz(leftLength, space.spaceWidth, space.spaceHigh, availBox2, state);
                // 求能放入y方向上剩余空间的每一种箱子的数目
                int[] yAvaiBox = getEachBoxNumForXyz(space.spaceLength, leftWidth, space.spaceHigh, availBox2, state);
                // 求能放入z方向上剩余空间的每一种箱子的数目
                int[] zAvaiBox = getEachBoxNumForXyz(space.spaceLength, space.spaceWidth, leftHigh, availBox2, state);

                // 获取X方向 箱子length，width，high ListArrayList
                ArrayList<Integer> xLengthArray = new ArrayList<>();
                ArrayList<Integer> xWidthArray = new ArrayList<>();
                ArrayList<Integer> xHighArray = new ArrayList<>();
                for(int i = 0; i < xAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < xAvaiBox[i]; j++){
                        xLengthArray.add((int) box.boxLength);
                        xWidthArray.add((int) box.boxWidth);
                        xHighArray.add((int) box.boxHigh);
                    }
                }
                int xVloss = getVloss(leftLength, (int)space.spaceWidth, (int)space.spaceHigh,xLengthArray,xWidthArray,xHighArray);

                // 获取Y方向 箱子length，width，high ListArrayList
                ArrayList<Integer> yLengthArray = new ArrayList<>();
                ArrayList<Integer> yWidthArray = new ArrayList<>();
                ArrayList<Integer> yHighArray = new ArrayList<>();
                for(int i = 0; i < yAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < yAvaiBox[i]; j++){
                        yLengthArray.add((int) box.boxLength);
                        yWidthArray.add((int) box.boxWidth);
                        yHighArray.add((int) box.boxHigh);
                    }
                }
                int yVloss = getVloss((int)space.spaceLength, leftWidth, (int)space.spaceHigh,yLengthArray,yWidthArray,yHighArray);

                // 获取Y方向 箱子length，width，high ListArrayList
                ArrayList<Integer> zLengthArray = new ArrayList<>();
                ArrayList<Integer> zWidthArray = new ArrayList<>();
                ArrayList<Integer> zHighArray = new ArrayList<>();
                for(int i = 0; i < zAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < zAvaiBox[i]; j++){
                        zLengthArray.add((int) box.boxLength);
                        zWidthArray.add((int) box.boxWidth);
                        zHighArray.add((int) box.boxHigh);
                    }
                }
                int zVloss = getVloss((int)space.spaceLength, (int) space.spaceWidth,leftHigh,zLengthArray,zWidthArray,zHighArray);


                int vLoss = xVloss + yVloss + zVloss;
                int score = (int) (block.blockLenght * block.blockWidth * block.blockHigh) - vLoss;
                return score;
            }else{
                return Integer.MIN_VALUE;
            }
    }

    public static int getVloss(int spaceLength, int spaceWidth, int spaceHigh, ArrayList<Integer> lengthArray, ArrayList<Integer> widthArray, ArrayList<Integer> highArray){
        // 定义剩余箱子的 长 宽 高 长度表
        int[] lengthList = new int[lengthArray.size()];
        for (int i = 0; i < lengthArray.size(); i++) {
            lengthList[i] = lengthArray.get(i);
        }
        int[] widthList = new int[widthArray.size()];
        for (int i = 0; i < widthArray.size(); i++) {
            widthList[i] = widthArray.get(i);
        }
        int[] hightList = new int[highArray.size()];
        for (int i = 0; i < highArray.size(); i++) {
            hightList[i] = highArray.get(i);
        }

        int maxLength = KPA(spaceLength, lengthList);
        int maxWidth = KPA(spaceWidth, widthList);
        int maxHigh = KPA(spaceHigh, hightList);

        int vloss = spaceLength * spaceWidth * spaceHigh - maxLength * maxWidth * maxHigh;
        return vloss;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-02 22-10-40
     * @Description 用于返回能放入x，y，z方向上剩余空间的每一种箱子的数目
    */
    public static int[] getEachBoxNumForXyz(double length, double width, double hight, int[] avaiBox, State state){
        int[] returnBoxList = new int[state.availBox.length];
        for(int i = 0; i < avaiBox.length; i++){
            if(avaiBox[i] != 0){
                Box box = state.problem.boxList.get(i);
                if(box.boxLength <= length && box.boxWidth <= width && box.boxHigh <= hight){
                    returnBoxList[i] = avaiBox[i];
                }
            }
        }
        return returnBoxList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 17-35-18
     * @Description 根据论文A new iterative-doubling Greedy–Lookahead algorithm for the single
     *container loading problem添加 vwast变量 以及进行优化, 以及将 生成的三个空间从之前的cover 变为 partition
     */
    public static int scoreFunctionTwoG2LANew(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
            // 开始计算分数
            // 1.求放入这个箱子后 剩余的箱子数目
            int[] availBox2 = new int[state.availBox.length];
            for(int i = 0; i < state.availBox.length; i++){
                availBox2[i] = state.availBox[i] - block.neededBoxNumOfEachType[i];
            }

            // 求放入块后长宽高三个方向上剩余的长度
            double leftLength = space.spaceLength - block.blockLenght;
            double leftWidth = space.spaceWidth - block.blockWidth;
            double leftHigh = space.spaceHigh - block.blockHigh;

            // 获取能放入x方向上的箱子的长宽高列表
            double spaceLength = leftLength;
            double spaceWidth = space.spaceWidth;
            double spaceHigh = space.spaceHigh;
            ArrayList<ArrayList<Integer>> xLWHarrayList = getLWHarrayListTwo(state, availBox2, spaceLength, spaceWidth, spaceHigh);
            int xVloss = getVlossNew(xLWHarrayList, (int)spaceLength, (int)spaceWidth, (int)spaceHigh);

            // 获取能放入y方向上的箱子的长宽高列表
            spaceLength = block.blockLenght;
            spaceWidth = leftWidth;
            spaceHigh = space.spaceHigh;
            ArrayList<ArrayList<Integer>> yLWHarrayList = getLWHarrayListTwo(state, availBox2, spaceLength, spaceWidth, spaceHigh);
            int yVloss = getVlossNew(yLWHarrayList, (int)spaceLength, (int)spaceWidth, (int)spaceHigh);

            // 获取能放入z方向上的箱子的长宽高列表
            spaceLength = block.blockLenght;
            spaceWidth = block.blockWidth;
            spaceHigh = leftHigh;
            ArrayList<ArrayList<Integer>> zLWHarrayList = getLWHarrayListTwo(state, availBox2, spaceLength, spaceWidth, spaceHigh);
            int zVloss = getVlossNew(zLWHarrayList, (int)spaceLength, (int)spaceWidth, (int)spaceHigh);


            int vLoss = xVloss + yVloss + zVloss;
            int vWaste = (int) (block.blockLenght * block.blockWidth * block.blockHigh - block.blockVolum);
            int score = (int) block.blockVolum - vLoss - vWaste;
            return score;
        }else{
            return Integer.MIN_VALUE;
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 19-00-44
     * @Description 获取vloss
    */
    public static int getVlossNew(ArrayList<ArrayList<Integer>> LWHarrayList, int spaceLength, int spaceWidth, int spaceHigh){

        int maxLength = KPAForArrayList(spaceLength, LWHarrayList.get(0));
        int maxWidth = KPAForArrayList(spaceWidth, LWHarrayList.get(1));
        int maxHigh = KPAForArrayList(spaceHigh, LWHarrayList.get(2));
        int vLoss = spaceLength * spaceWidth * spaceHigh - maxLength * maxWidth * maxHigh;
        return vLoss;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 19-23-39
     * @Description 获取能放入空间内的箱子的长宽高列表,空间里面放入箱子的时候不考虑旋转
    */
    public static ArrayList<ArrayList<Integer>> getLWHarrayListTwo(State state, int[] availBox, double spaceLength, double spaceWidth, double spaceHigh){
        ArrayList<ArrayList<Integer>> lwhArrayList = new ArrayList<>();
        // 0 lengList, 1 widthList, 2 highList
        for(int i = 0; i < 3; i++){
            lwhArrayList.add(new ArrayList<Integer>());
        }

        // 开始遍历每一个箱子
        Box box;
        for (int i = 0; i < availBox.length; i++) {
            if(availBox[i] > 0){
                box = state.problem.boxList.get(i);
                if(canBePutIn(box.boxLength,box.boxWidth,box.boxHigh,spaceLength,spaceWidth,spaceHigh)){
                    lwhArrayList.get(0).add((int)box.boxLength);
                    lwhArrayList.get(1).add((int)box.boxWidth);
                    lwhArrayList.get(2).add((int)box.boxHigh);
                }
            }
        }
        return lwhArrayList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-19 17-45-21
     * @Description 获取能放入空间内的箱子的长宽高列表,空间里面放入箱子的时候考虑旋转
    */
    public static ArrayList<ArrayList<Integer>> getLWHarrayList(State state, int[] availBox, double spaceLength, double spaceWidth, double spaceHigh){
        ArrayList<ArrayList<Integer>> lwhArrayList = new ArrayList<>();
        // 0 lengList, 1 widthList, 2 highList
        for(int i = 0; i < 3; i++){
            lwhArrayList.add(new ArrayList<Integer>());
        }

        // 开始遍历每一个箱子
        Box box;
        for (int i = 0; i < availBox.length; i++) {
            if(availBox[i] > 0){
                box = state.problem.boxList.get(i);

                if(box.isXvertical == 1){ // 长做高
                    // 判断能否放入(两种情况，左右旋转)
                    if(canBePutIn(box.boxHigh,box.boxWidth,box.boxLength,spaceLength,spaceWidth,spaceHigh)){
                        lwhArrayList.get(0).add((int)box.boxHigh);
                        lwhArrayList.get(1).add((int)box.boxWidth);
                        lwhArrayList.get(2).add((int)box.boxLength);
                    }
                    if(canBePutIn(box.boxWidth,box.boxHigh,box.boxLength,spaceLength,spaceWidth,spaceHigh)){
                        lwhArrayList.get(0).add((int)box.boxWidth);
                        lwhArrayList.get(1).add((int)box.boxHigh);
                        lwhArrayList.get(2).add((int)box.boxLength);
                    }
                }
                if(box.isYvertical == 1){ // 宽做高
                    if(canBePutIn(box.boxLength,box.boxHigh,box.boxWidth,spaceLength,spaceWidth,spaceHigh)){
                        lwhArrayList.get(0).add((int)box.boxLength);
                        lwhArrayList.get(1).add((int)box.boxHigh);
                        lwhArrayList.get(2).add((int)box.boxWidth);
                    }
                    if(canBePutIn(box.boxHigh,box.boxLength,box.boxWidth,spaceLength,spaceWidth,spaceHigh)){
                        lwhArrayList.get(0).add((int)box.boxHigh);
                        lwhArrayList.get(1).add((int)box.boxLength);
                        lwhArrayList.get(2).add((int)box.boxWidth);
                    }
                }
                if(box.isZvertical == 1){
                    if(canBePutIn(box.boxLength,box.boxWidth,box.boxHigh,spaceLength,spaceWidth,spaceHigh)){
                        lwhArrayList.get(0).add((int)box.boxLength);
                        lwhArrayList.get(1).add((int)box.boxWidth);
                        lwhArrayList.get(2).add((int)box.boxHigh);
                    }
                    if(canBePutIn(box.boxWidth,box.boxLength,box.boxHigh,spaceLength,spaceWidth,spaceHigh)){
                        lwhArrayList.get(0).add((int)box.boxWidth);
                        lwhArrayList.get(1).add((int)box.boxLength);
                        lwhArrayList.get(2).add((int)box.boxHigh);
                    }
                }
            }
        }
        return lwhArrayList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-19 18-16-49
     * @Description 判断一个箱子能不能被放入空间
    */
    public static boolean canBePutIn(double length, double width, double high, double spaceLength, double spaceWidth, double spaceHigh){
        return length <= spaceLength && width <= spaceWidth && high <= spaceHigh;
    }

    public static int KPA(int totalLength, int[] lengthList){
        double start  = System.currentTimeMillis();
        int[] outComeList = new int[totalLength + 1]; // 用于存放中间过程

        for(int i = 0; i < lengthList.length; i++){
            for(int j = totalLength; j >= lengthList[i]; j--){
                outComeList[j] = Math.max(outComeList[j], outComeList[ j - lengthList[i] ] + lengthList[i]);
            }
        }
//        System.out.println("kpa耗时" + (System.currentTimeMillis() - start) +"ms");
        return outComeList[totalLength];
    }

    /**
     * @Author yajiewen
     * @Date ] 17-37-15
     * @Description KPA动态规划的arrayList版本 为了 免去arrayList 转化为数组这一步骤
    */
    public static int KPAForArrayList(int totalLength, ArrayList<Integer> lengthList){
        double start  = System.currentTimeMillis();
        int[] outComeList = new int[totalLength + 1]; // 用于存放中间过程

        for(int i = 0; i < lengthList.size(); i++){
            for(int j = totalLength; j >= lengthList.get(i); j--){
                outComeList[j] = Math.max(outComeList[j], outComeList[ j - lengthList.get(i) ] + lengthList.get(i));
            }
        }
//        System.out.println("kpa耗时" + (System.currentTimeMillis() - start) +"ms");
        return outComeList[totalLength];
    }
}
