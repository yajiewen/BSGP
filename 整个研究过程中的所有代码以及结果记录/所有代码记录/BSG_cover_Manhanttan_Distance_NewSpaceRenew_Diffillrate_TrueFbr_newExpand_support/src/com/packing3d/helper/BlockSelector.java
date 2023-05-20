package com.packing3d.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Box;
import com.packing3d.datastructure.space.Space;
import com.packing3d.datastructure.state.State;

import java.util.ArrayList;

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

    // 查找可行块表(限定数目)
    public static ArrayList<Block> searchViableBlockWithNumLimit(Space space, ArrayList<Block> blockList, int numLimit){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        for (Block block : blockList) {
            if(block.blockLenght <= space.spaceLength &&
                    block.blockWidth <= space.spaceWidth &&
                    block.blockHigh <= space.spaceHigh){
                if(viableBlockList.size() < numLimit){
                    viableBlockList.add(block);
                }else{
                    break;
                }
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
//        double start = System.currentTimeMillis();
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
//        double start = System.currentTimeMillis();
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
//        double start = System.currentTimeMillis();
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

    /**
     * @Author yajiewen
     * @Date 2022-10-22 09-49-39
     * @Description 优化fbr，因为放不进去的块不需要计算fbr
    */
    public static ArrayList<Block> searchViableBlockWithNumLimitByFbrFaster(Space space, State state, int number){
        ArrayList<Block> viableBlockList = searchViableBlock(space, state.blockList);
        if(!viableBlockList.isEmpty()){
            // 更新获取vloss 向量
            generateVlossVector(state, space);
            for (Block block : viableBlockList) {
                block.fbrScore = scoreFunctionByLossVector(space, block, state);
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



    public static int scoreFunctionByLossVector(Space space, Block block, State state){
        if(block.blockLenght <= space.spaceLength &&
                block.blockWidth <= space.spaceWidth &&
                block.blockHigh <= space.spaceHigh){
            int lMax = state.xVlossVector[(int) (space.spaceLength - block.blockLenght) ];
            int wMax = state.yVlossVector[(int) (space.spaceWidth - block.blockWidth) ];
            int hMax = state.zVlossVector[(int) (space.spaceHigh - block.blockHigh) ];
            int vLoss = (int) ( space.spaceLength * space.spaceWidth * space.spaceHigh - (block.blockLenght + lMax) * (block.blockWidth + wMax) * (block.blockHigh + hMax));
            int vWaste = (int) (block.blockLenght * block.blockWidth * block.blockHigh - block.blockVolum);
            int score = (int) block.blockVolum - vLoss - vWaste;
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
        state.xVlossVector = BlockSelector.KPAForArrayListTwo((int)space.spaceLength,lengthList);
        state.yVlossVector = BlockSelector.KPAForArrayListTwo((int)space.spaceWidth,widthList);
        state.zVlossVector = BlockSelector.KPAForArrayListTwo((int)space.spaceHigh,hightList);
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
//        double start  = System.currentTimeMillis();
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
//        double start  = System.currentTimeMillis();
        int[] outComeList = new int[totalLength + 1]; // 用于存放中间过程

        for(int i = 0; i < lengthList.size(); i++){
            for(int j = totalLength; j >= lengthList.get(i); j--){
                outComeList[j] = Math.max(outComeList[j], outComeList[ j - lengthList.get(i) ] + lengthList.get(i));
            }
        }
//        System.out.println("kpa耗时" + (System.currentTimeMillis() - start) +"ms");
        return outComeList[totalLength];
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-20 00-41-38
     * @Description 重大改进 直接返回最后的结果数组 就是 每一张线性组合可能
    */
    public static int[] KPAForArrayListTwo(int totalLength, ArrayList<Integer> lengthList){
//        double start  = System.currentTimeMillis();
        int[] outComeList = new int[totalLength + 1]; // 用于存放中间过程

        for(int i = 0; i < lengthList.size(); i++){
            for(int j = totalLength; j >= lengthList.get(i); j--){
                outComeList[j] = Math.max(outComeList[j], outComeList[ j - lengthList.get(i) ] + lengthList.get(i));
            }
        }
//        System.out.println("kpa耗时" + (System.currentTimeMillis() - start) +"ms");
        return outComeList;
    }
}
