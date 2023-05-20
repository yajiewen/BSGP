package com.packing3d.application;

import com.packing3d.datastructure.state.State;
import com.packing3d.filetool.FileHelper;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.scheme.Put;
import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.space.Space;
import com.packing3d.helper.BlockHelper;
import com.packing3d.helper.ResourceHelper;
import com.packing3d.helper.StateHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-04-04 14-03-37
 * @Description
 * (K1) how to represent free space in the container;  cover representation
 * (K2) how to generate a list of blocks (of boxes);    gbblock
 * (K3) how to select a free space; 选择栈顶空间
 * (K4) how to select a block; 选择体积最大的块
 * (K5) how to place the selected block into the selected space and update the list of free space; 块放在空间0点 ,使用八点坐标更新发求剩余空间,同时支持完全支撑
 * (K6) what is the overarching search strategy;    两步向前
 */

/*
* 强异构:
* 方案1:w从25 开始,每次加2
* 方案2:w从5开始,小于46每次加2,大于每次加10(约到后面需要遍历更多的情况多以到了后面加的更多) 速度效果不错
*
* 弱异构:
* 方案1:从60开始,每次减去2*/

public class LA2 {
    public static void main(String[] args) throws IOException {
//        oneThread();
        mutilThread(args[0],args[1]);
    }

    // 单线程
    public static void oneThread() throws IOException {
        // 读取文件
        FileHelper fileHelper = new FileHelper("B:\\Standard_Datasets\\br15.txt");
        ArrayList<Problem> problems = fileHelper.getProblems();
        // 拿出一个问题进行测试
        Problem problem = problems.get(0);
        // 生成Gp块
        int MaxBlocks = 10000;
        double MinFillRate = 0.98; //复合块种要达到的最小填充量
        double MinAreRate = 0.96; // 可行域填充面积
        // 设置问题是什么异构
        Boolean isStrong = true;
        ArrayList<Block> gPBlockList = BlockHelper.generateGeneralCuttingBlocksNotFullySupported(problem,MaxBlocks,MinFillRate);
        State state = algorithm2LA(problem,gPBlockList,isStrong);
        System.out.println("======>最终结果:" + state.scheme.totalVolum / (problem.containnerLength * problem.containnerWidth * problem.containnerHigh));
        StateHelper.stateDetecte(state,problem);
    }
    //2LA算法
    public static State algorithm2LA(Problem problem, ArrayList<Block> blockList, Boolean isStrong){
        State state = new State(problem,blockList);

        // 保存中间过程的 最好 complete 状态的列表
        ArrayList<State> bestCompleteStateList = new ArrayList<>();
        int depth = 2;// 向前深度
        // 设置搜索宽度和向前深度
        int w; // 搜索宽度-----------------------------------------------------------
        if(isStrong){
            w = 25;
        }else{
            w = 60;
        }

        // 获取迭代次数(等于block)的数目
        while(!state.spaceArrayList.isEmpty()){
            // 查找可行快表(限制数目)
            ArrayList<Block> viableBlockList = BlockHelper.searchViableBlockWithNumLimit(state.spaceArrayList.get(state.spaceArrayList.size() - 1), state.blockList, state.availBox, w);
            if(!viableBlockList.isEmpty()){
                // 开始2步搜索
                // 保存每次搜索的complete state
                ArrayList<State> completeStateList = new ArrayList<>();

                // 开始2步查找
                searchState(w,depth,state,completeStateList);
                // 找到最好的completeState
                State bestCompleteState = findBestCompleteState(completeStateList);

                if(bestCompleteState != null){
                    bestCompleteStateList.add(bestCompleteState);
//                    System.out.println("当前bestComplete:" + bestCompleteState.scheme.totalVolum / (problem.containnerLength * problem.containnerWidth * problem.containnerHigh));
                    //利用father指针 找到当前状态的下一步
                    state = bestCompleteState.fatherState.fatherState;
                }
//                w = (int) Math.ceil(Math.sqrt(2.0) * w);

                if(isStrong){ // 强异构w更新
                    if(w < 46){
                        w += 2;
                    }else{
                        w += 10;
                    }
                }else{ //若异构w更新
                    if(w >= 40){ // 防止w变为0的情况
                        w -= 2;
                    }
                }
            }else{
                Space space = state.spaceArrayList.remove(state.spaceArrayList.size() - 1); // 拿出不可用空间
            }
        }
        return state;
    }

    // 找出这次搜索中最好的complete state
    public static State findBestCompleteState(ArrayList<State> completeStateList){
//        System.out.println("当前complete个数"+ completeStateList.size());
        if(completeStateList.isEmpty()){
            return null;
        }
        State bestCompleteState = completeStateList.get(0);

        for (int i = 1; i < completeStateList.size(); i++) {
            if(completeStateList.get(i).scheme.totalVolum > bestCompleteState.scheme.totalVolum){
                bestCompleteState = completeStateList.get(i);
            }
        }
        return bestCompleteState;
    }

    // 2步搜索
    public static void searchState(int w, int depth, State state, ArrayList<State> completeStateList){
        if(depth != 0){
            // 查找可行快表(限制数目)
            if(!state.spaceArrayList.isEmpty()){
                ArrayList<Block> viableBlockList = BlockHelper.searchViableBlockWithNumLimit(state.spaceArrayList.get(state.spaceArrayList.size() - 1), state.blockList, state.availBox, w);
                while(viableBlockList.isEmpty() && !state.spaceArrayList.isEmpty()){
                    Space space = state.spaceArrayList.remove(state.spaceArrayList.size() - 1);
                    // 对空间转移后的状态再次找可行块
                    if(!state.spaceArrayList.isEmpty()){
                        viableBlockList = BlockHelper.searchViableBlockWithNumLimit(state.spaceArrayList.get(state.spaceArrayList.size() - 1), state.blockList, state.availBox, w);
                    }
                }
                if(!state.spaceArrayList.isEmpty() && !viableBlockList.isEmpty()){
                    int minW = Math.min(w,viableBlockList.size());
                    for (int i = 0; i < minW; i++) {
                        // 放入一个块 生成新的状态
                        State newState = State.getNewState(state,viableBlockList.get(i));
                        searchState(w,depth-1, newState, completeStateList);
                    }
                }else{
                    State newState = state.cloneObj();
                    searchState(w,depth-1, newState, completeStateList);
                }
            }else{
                State newState = state.cloneObj();
                searchState(w,depth-1, newState, completeStateList);
            }
        }else{
            //深度为0 说明已经走了两步 求complete state
            State completeState = state.cloneObj();
            greedyForCompleteState(completeState);
            // 把complete 状态加入相应列表
            completeStateList.add(completeState);
        }
    }

    // 贪心求完全状态
    public static void greedyForCompleteState(State completeState){
        while(!completeState.spaceArrayList.isEmpty()){
            // 查找可行快表(限制数目为1)
            ArrayList<Block> viableBlockList = BlockHelper.searchViableBlockWithNumLimit(completeState.spaceArrayList.get(completeState.spaceArrayList.size() - 1), completeState.blockList, completeState.availBox,1);
            if(!viableBlockList.isEmpty()){
                // 更新state
                Space space = completeState.spaceArrayList.remove(completeState.spaceArrayList.size() - 1);
                Block block = viableBlockList.get(0);

                completeState.scheme.addPut(new Put(block,space)); // 更新scheme
                ResourceHelper.renewResource(completeState.spaceArrayList, space, block, completeState.availBox); // 更新spaceArrayList ,和可用剩余箱子数
                completeState.blockList.remove(block); // 更新剩余块表
                // 删除剩余箱子不可生成的块
                for (int i = 0; i < completeState.blockList.size(); i++) {
                    // 判断剩余箱子能否生成此块
                    boolean isOk = true;
                    Block block1 = completeState.blockList.get(i);

                    for (int j = 0; j < completeState.availBox.length; j++) {
                        if(block1.neededBoxNumOfEachType[j] > completeState.availBox[j]){
                            isOk  = false;
                            break;
                        }
                    }
                    if(isOk == false){
                        completeState.blockList.remove(i);
                    }
                }
            }else{
                Space space = completeState.spaceArrayList.remove(completeState.spaceArrayList.size() - 1);
            }
        }
    }

    public static void mutilThread(String inputPath, String outputPath){

        File outputPathFolder = new File(outputPath);
        if(!outputPathFolder.exists() && !outputPathFolder.isDirectory()){
            System.out.println("创建输出文件夹");
            outputPathFolder.mkdir();
        }

        //多线程解决
        final int[] plist = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        for(final int p : plist ){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileWriter fileWriter = null;
                    try {
                        fileWriter = new FileWriter(outputPath + File.separator +"BR"+ p +"outcome.txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FileHelper fileHelper = new FileHelper(inputPath + File.separator +"br"+ p +".txt");
                    ArrayList<Problem> problemList = null;
                    try {
                        problemList = fileHelper.getProblems();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    double avaFillRate = 0;
                    int MaxBlocks = 10000;
                    double MinFillRate = 0.98; //复合块种要达到的最小填充量
                    double MinAreRate = 0.96;
                    int MaxLevel = 5;
                    // 设置问题是什么异构
                    Boolean isStrong = false;
                    if(p >= 8){
                        isStrong = true;
                    }

                    int i = 1;
                    for (Problem problem : problemList) {
//                        ArrayList<Block> blockList = BlockHelper.generateGeneralCuttingBlocksNotFullySupported(problem,MaxBlocks,MinFillRate);
                        ArrayList<Block> simpleBlockList = BlockHelper.getSimpleBlockListWithDirection(problem);
                        ArrayList<Block> blockList = BlockHelper.generateComplexBlocksFullySupported(simpleBlockList,MaxLevel,MaxBlocks,problem,MinFillRate,MinAreRate);
                        State state = algorithm2LA(problem,blockList,isStrong);
                        double fillRate = state.scheme.totalVolum / (problem.containnerLength * problem.containnerWidth * problem.containnerHigh);
                        avaFillRate += fillRate;
                        System.out.println("br"+ p +":==problem " + i + "===>"+"填充率为:"+ fillRate);
                        StateHelper.stateDetecte(state,problem);
                        i++;
                    }
                    System.out.println("<=====br"+ p +"平均填充率为:" + avaFillRate / 100);
                    try {
                        fileWriter.write("br" + p +" :" + avaFillRate / 100 +"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fileWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
}


