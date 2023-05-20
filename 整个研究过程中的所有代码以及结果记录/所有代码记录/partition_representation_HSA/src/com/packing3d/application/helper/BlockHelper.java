package com.packing3d.application.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.space.Space;

import java.util.ArrayList;
import java.util.Comparator;

public class BlockHelper {
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

        block.typeNumberOfBox = block1.typeNumberOfBox;
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

        block.typeNumberOfBox = block1.typeNumberOfBox;
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

        block.typeNumberOfBox = block1.typeNumberOfBox;
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
     * @Date 2022-04-29 10-36-38
     * @Description 获取简单快不考虑方向
    */
    public static ArrayList<Block> getSimpleBlockList(Problem problem){
        ArrayList<Block> blockList = new ArrayList<>();
        for(int i = 0; i < problem.typeNumberOfBox; i++){
            for(int j = 1; j <= problem.boxList.get(i).boxNumber; j++){
                for(int k = 1; k <= problem.boxList.get(i).boxNumber / j; k++){
                    for(int l = 1; l <= problem.boxList.get(i).boxNumber / j / k; l++){
                        double blockLength = problem.boxList.get(i).boxLength * j;
                        double blockWidth = problem.boxList.get(i).boxWidth * k;
                        double blockHigh = problem.boxList.get(i).boxHigh * l;
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
                            block.neededBoxNumOfEachType[i] = j * k * l;
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
     * @Date 2022-03-15 12-14-37
     * @Description
     */
    public static ArrayList<Block> generateGeneralCuttingBlocksNotFullySupported(Problem problem, int MaxBlocks, double MinFillRate){
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

        block.typeNumberOfBox = block1.typeNumberOfBox;
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
        block.typeNumberOfBox = block1.typeNumberOfBox;
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
     * @Date 2022-04-29 10-38-07
     * @Description 获取复杂快 支持完全支撑
    */
    public static ArrayList<Block> generateComplexBlocksFullySupported(ArrayList<Block> blockList, int MaxLevel, int MaxBlocks, Problem problem, double MinFillRate, double MinAreaRate){
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

    // 查找可行块表
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
}
