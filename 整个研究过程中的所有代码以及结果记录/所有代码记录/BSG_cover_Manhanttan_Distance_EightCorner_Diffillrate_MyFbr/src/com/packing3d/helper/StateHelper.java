package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.state.State;

import java.util.ArrayList;
import java.util.Arrays;

public class StateHelper {
    /**
     * @Author yajiewen
     * @Date 2022-07-23 10-20-13
     * @Description 检测整个state
    */
    public static void stateDetecte(State state, Problem problem){
        boxsDetecte(state, problem);
        SchemeHelper.schemeDetecte(state.scheme, problem, state);
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-17 12-44-16
     * @Description 检测state 中的 剩余箱子的所有可能生成的单独的块 能否在剩余块中找到
    */
    public static void boxsDetecte(State state, Problem problem){
        // 求所有剩余箱子可能生成的单独的块
        ArrayList<Block> blockList = new ArrayList<>();
        for(int i = 0; i < problem.typeNumberOfBox; i++){
            if(state.availBox[i] != 0){
                // 拿出该类别的箱子
                Box box = problem.boxList.get(i);
                // 生成各个方向上的块
                double blockLength = box.boxLength;
                double blockWidth = box.boxWidth;
                double blockHigh = box.boxHigh;

                int[] isVertial = {box.isXvertical, box.isYvertical, box.isZvertical};
                for(int j = 0; j < 3; j++){
                    if(isVertial[j] == 1 && j == 0){
                        blockLength = box.boxHigh;
                        blockWidth = box.boxWidth;
                        blockHigh = box.boxLength;
                    }else if(isVertial[j] == 1 && j == 1){
                        blockLength = box.boxLength;
                        blockWidth = box.boxHigh;
                        blockHigh = box.boxWidth;
                    }else if(isVertial[j] == 1 && j ==2){
                        blockLength = box.boxLength;
                        blockWidth = box.boxWidth;
                        blockHigh = box.boxHigh;
                    }

                    if((isVertial[j] == 1 && j == 0) || (isVertial[j] == 1 && j == 1) || (isVertial[j] == 1 && j == 2)){
                        for(int k = 0; k < state.availBox[i]; k++){
                            Block block = new Block(problem.typeNumberOfBox);
                            block.blockLenght = blockLength;
                            block.blockWidth = blockWidth;
                            block.blockHigh = blockHigh;
                            block.neededBoxNumOfEachType[i] = 1;
                            blockList.add(block);
                        }
                    }
                }
            }
        }
        // 看块在剩余块中能否找到
        for(Block block : blockList){
            boolean isFound = false;
            for(Block block1 : state.blockList){
                if(block.blockLenght == block1.blockLenght && block.blockWidth == block1.blockWidth && block.blockHigh == block1.blockHigh && Arrays.equals(block.neededBoxNumOfEachType, block1.neededBoxNumOfEachType)){
                    isFound = true;
                    break;
                }
            }
            if(!isFound){
                System.out.println("有块丢失!!!");
            }
        }
    }
}
