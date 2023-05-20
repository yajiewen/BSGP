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
     * @Date 2022-08-09 11-42-00
     * @Description 根据BSG描述，删除了长宽高和箱子相同的块
    */

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

    public static ArrayList<Block> generateGuillotineBlocksNotFullySupported(Problem problem,int MaxBlocks,double MinFillRate){
        long start = System.currentTimeMillis();
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
    /**
     * @Author yajiewen
     * @Date 2022-10-02 22-40-39
     * @Description 改进之前没有在block里面添加score 现在加了直接对blockList进行排序，超级无敌优化
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitAndFbr(Space space, State state, int number){
        ArrayList<Block> viableBlockList = new ArrayList<>();

        // 计算每一个块的fbr得分
        for (int i = 0; i < state.blockList.size(); i++) {
            // 拿取一个块
            Block block = state.blockList.get(i);
            // 计算这个块的得分
            block.fbrScore = scoreFunctionTwo(space, block, state);
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
        }
        // 选出满足前minL个fbrScore最大的块,且能放入的块fbr score 等于Integer.MIN_VALUE 表示不可发放入
        for(int i = 0; i < minL; i++){
            if(state.blockList.get(i).fbrScore != Integer.MIN_VALUE){
                viableBlockList.add(state.blockList.get(i));
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

    /**
     * @Author yajiewen
     * @Date 2022-10-02 22-08-10
     * @Description 看了论文后的修改,长度，宽，高的线性组合考虑了箱子能否放入
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
                // 求所有剩余箱子的数目
                int boxNum = 0;
                for(int i = 0; i <availBox2.length; i++){
                    boxNum += availBox2[i];
                }
                // 求放入块后长宽高三个方向上剩余的长度
                int leftLength = (int) (space.spaceLength - block.blockLenght);
                int leftWidth = (int) (space.spaceWidth - block.blockWidth);
                int leftHigh = (int) (space.spaceHigh - block.blockHigh);
                // 定义剩余箱子的 长 宽 高 长度表
                int[] lengthList = new int[boxNum];
                int[] widthList = new int[boxNum];
                int[] hightList = new int[boxNum];
                // 求能放入x方向上剩余空间的每一种箱子的数目
                int[] xAvaiBox = getEachBoxNumForXyz(leftLength, space.spaceWidth, space.spaceHigh, availBox2, state);
                // 求能放入y方向上剩余空间的每一种箱子的数目
                int[] yAvaiBox = getEachBoxNumForXyz(space.spaceLength, leftWidth, space.spaceHigh, availBox2, state);
                // 求能放入z方向上剩余空间的每一种箱子的数目
                int[] zAvaiBox = getEachBoxNumForXyz(space.spaceLength, space.spaceWidth, leftHigh, availBox2, state);

                // 获取lengthList
                int index = 0;
                for(int i = 0; i < xAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < xAvaiBox[i]; j++){
                        lengthList[index] = (int) box.boxLength;
                        index++;
                    }
                }
                // 获取widthList
                index = 0;
                for(int i = 0; i < yAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < yAvaiBox[i]; j++){
                        widthList[index] = (int) box.boxWidth;
                        index++;
                    }
                }

                // 获取hightList
                index = 0;
                for(int i = 0; i < zAvaiBox.length; i++){
                    Box box = state.problem.boxList.get(i);
                    for(int j = 0; j < zAvaiBox[i]; j++){
                        hightList[index] = (int) box.boxHigh;
                        index++;
                    }
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
     * @Date 2022-10-02 22-10-40
     * @Description 用于返回能放入x，y，z方向上剩余空间的每一种箱子的数目
    */
    public static int[] getEachBoxNumForXyz(double length, double width, double hight, int[] avaiBox, State state){
        int[] returnBoxList = new int[state.availBox.length];
        for(int i = 0; i < avaiBox.length; i++){
            Box box = state.problem.boxList.get(i);
            if(box.boxLength <= length && box.boxWidth <= width && box.boxHigh <= hight){
                returnBoxList[i] = avaiBox[i];
            }
        }
        return returnBoxList;
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
}
