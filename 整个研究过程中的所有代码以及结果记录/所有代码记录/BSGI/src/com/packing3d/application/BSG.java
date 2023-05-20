package com.packing3d.application;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.state.State;
import com.packing3d.filetool.FileHelper;
import com.packing3d.helper.BlockGenerator;
import com.packing3d.helper.BlockSelector;
import com.packing3d.helper.SpaceHelper;
import com.packing3d.helper.StateHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author yajiewen
 * @Date 2022-07-14 10-28-59
 * @Description
 * 根据原文,不同的问题,块的fillrate 使用不同的值: BR0-BR7 Minfillrate = 1 ; BR8-BR15 Minfillrate = 0.98
 * 块选择方法使用fbr 修复fbr
*/

/**
 * @Author yajiewen
 * @Date 2022-10-22 22-00-36
 * @Description 优化使用配置文件类每次不需要再改Main
*/

public class BSG {
    public static void main(String[] args) throws IOException {
        InitHelper.printParameter();
        if(InitHelper.RUN_WAY == Param.MULTI_THREADING){
            mutilThread(args[0], args[1], InitHelper.RUN_TIME_SECOND_LIMIT, InitHelper.TASK);
        }else if(InitHelper.RUN_WAY == Param.SINGLE_THREADING){
            singleThread(args[0], args[1],args[2]);
        }
    }

    /**
     * @Author yajiewen
     * @Date 2023-03-02 17-15-01
     * @Description 服务器单线程
    */
    public static void singleThread(String inputPath, String outputPath, String fileNum) throws IOException{
        File outputPathFolder = new File(outputPath);
        if(!outputPathFolder.exists() && !outputPathFolder.isDirectory()){
            System.out.println("创建输出文件夹");
            outputPathFolder.mkdir();
        }
        int p = Integer.parseInt(fileNum);
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
        int i = 1;
        boolean isRuoYiGou = true;
        double MinFillRate;
        if (p >= 0 && p <= 7){
            MinFillRate = 1;
            isRuoYiGou = true;
        }else{
            MinFillRate = 0.98;
            isRuoYiGou = false;
        }

        for (Problem problem : problemList) {
            State state = algorithmBSG(problem,InitHelper.RUN_TIME_SECOND_LIMIT * 1000, MinFillRate, isRuoYiGou);
            double fillRate = state.scheme.totalVolum / problem.containnerVolume;
            System.out.println("BR"+ p +" problem " + i +" : "+"填充率" + fillRate);
            i++;
            avaFillRate += fillRate;
            // 每个问题结束后gc前
            System.gc();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("BR"+ p +"平均填充率为:" + avaFillRate / 100);
        try {
            fileWriter.write("BR" + p +" : " + avaFillRate / 100 +"\n");
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

    /**
     * @Author yajiewen
     * @Date 2022-05-12 11-54-30
     * @Description 单线程
    */
    public static void singleThread() throws IOException{
        // 读取文件
        FileHelper fileHelper = new FileHelper("E:\\Standard_Datasets\\br14.txt");
        ArrayList<Problem> problems = fileHelper.getProblems();
        double MinFillRate = 0.98;
        double rate = 0;
        int i = 0;
        for(Problem problem : problems){
            State state = algorithmBSG(problem,InitHelper.RUN_TIME_SECOND_LIMIT * 1000,MinFillRate,false);
            double fillRate = state.scheme.totalVolum / problem.containnerVolume;
            System.out.println("problem " + i + "填充率为"  + fillRate);
            rate += fillRate;
            System.out.println();
            if(InitHelper.STATE_CHECK){
                StateHelper.stateCheck(state,problem);
            }
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        System.out.println("平均利用率为：" + rate);
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-12 11-54-51
     * @Description 多线程
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-20 14-30-50
     * @Description 每个问题前加gc
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-22 00-10-01
     * @Description 为了避免16个线程一起开慢，设置task 选择任务
    */
    public static void mutilThread(String inputPath, String outputPath, int timeLimit, int task){
        File outputPathFolder = new File(outputPath);
        if(!outputPathFolder.exists() && !outputPathFolder.isDirectory()){
            System.out.println("创建输出文件夹");
            outputPathFolder.mkdir();
        }

        //多线程解决
        final int[] plist1 = {0,1,2,3,4,5,6,7};
        final int[] plist2 = {8,9,10,11,12,13,14,15};
        final int[] plist3 = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        final int[] plist;
        if(task == Param.TASK0_7){
            plist = plist1;
        }else if(task == Param.TASK8_15){
            plist = plist2;
        }else{
            plist = plist3;
        }
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
                    int i = 1;
                    boolean isRuoYiGou = true;
                    double MinFillRate;
                    if (p >= 0 && p <= 7){
                        MinFillRate = 1;
                        isRuoYiGou = true;
                    }else{
                        MinFillRate = 0.98;
                        isRuoYiGou = false;
                    }
                    for (Problem problem : problemList) {
                        // 每个问题结束后gc前
                        System.gc();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        State state = algorithmBSG(problem,timeLimit * 1000, MinFillRate, isRuoYiGou);
                        double fillRate = state.scheme.totalVolum / problem.containnerVolume;
                        avaFillRate += fillRate;
                        System.out.println("BR"+ p +" problem " + i +" : "+ fillRate);
                        if(InitHelper.STATE_CHECK){
                            StateHelper.stateCheck(state,problem);
                        }
                        i++;
                    }
                    System.out.println("BR"+ p +"平均填充率为:" + avaFillRate / 100);
                    try {
                        fileWriter.write("BR" + p +" : " + avaFillRate / 100 +"\n");
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

    /**
     * @Author yajiewen
     * @Date 2022-10-04 00-04-34
     * @Description 没有线程的BSG 和带线程速度一样，说明不是线程问题
    */
    public static State algorithmBSGNoThread(Problem problem,int timeLimit,double MinFillRate){
        // 开辟一个final的对象数组,用于保存匿名内部类的里面的结果
        State[] bestState = new State[1];
        int MaxBlocks = 10000;
        ArrayList<Block> blockList = BlockGenerator.guillotineBlocks(problem,MaxBlocks,MinFillRate);
        int w = 1;
        while(!Thread.interrupted()){
            beamSearch(problem,blockList,w,bestState);
            w =(int) Math.ceil(Math.sqrt(2) * w);
            double rate = bestState[0].scheme.totalVolum / (problem.containnerHigh * problem.containnerLength * problem.containnerWidth);
            System.out.println("利用率=====>" + rate );
        }

        return bestState[0];
    }
    /**
     * @Author yajiewen
     * @Date 2022-05-09 10-39-00
     * @Description BSG 算法
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-21 11-55-10
     * @Description 修复把保存最好效果的bestState变量变为引用
    */
    public static State algorithmBSG(Problem problem,int timeLimit,double MinFillRate, boolean isRuoYiGou){
        // 开辟一个final的对象数组,用于保存匿名内部类的里面的结果
        State[] bestState = new State[1];
        int MaxBlocks = 10000;

//        ArrayList<Block> blockList = BlockGenerator.guillotineBlocks(problem,MaxBlocks,MinFillRate);
//        ArrayList<Block> blockList = BlockGenerator.generalPackingBlocks(problem,MaxBlocks,MinFillRate,0.98);
        ArrayList<Block> blockList;
        if(isRuoYiGou){
            blockList = BlockGenerator.simpleBlocks_Zhang(problem);
//            ArrayList<Block> sixDirectionBlock = BlockGenerator.sixDirectionBlocks(problem);
//            // 将单个物品生成的6个反方向上的块加入块表,解决块丢失问题
//            for (Block block : sixDirectionBlock) {
//                if(!blockList.contains(block)){
//                    blockList.add(block);
//                }
//            }
//            System.out.println(blockList.size() + "简单快生成完毕");
        }else{
            blockList = BlockGenerator.guillotineBlocks(problem,MaxBlocks,MinFillRate);
//            int num = blockList.size();
//            BlockGenerator.addRotatedBlocks(blockList,problem);
//            System.out.println("开始有" + num +"个块，添加旋转块后有" + blockList.size());
//            BlockGenerator.deleteSimilarBlock(blockList,10);
        }


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int w = 1;
                while(true){
                    beamSearch(problem,blockList,w,bestState);
                    w =(int) Math.ceil(Math.sqrt(2) * w);
                }
            }
        });
        long start = System.currentTimeMillis();
        thread.start();
        try {
            TimeUnit.MILLISECONDS.sleep(timeLimit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.stop();
        System.out.println("运行时间:" + (System.currentTimeMillis() - start) + "ms");
        // 返回结果
        return bestState[0];
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-03 09-43-50
     * @Description 将状态列表数据结构从HashSet 改为 ArrayList
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-03 10-17-01
     * @Description 优化，使得不需要每次在while循环中创建successor 和succ
    */
    public static void beamSearch(Problem problem, ArrayList<Block> blockList, int w, State[] bestState){
        // 初始化一个state
        State stateInit = new State(problem,blockList);
        // 定义一个state list
        ArrayList<State> stateList = new ArrayList<>();
        stateList.add(stateInit);

        ArrayList<State> successors = new ArrayList<>();
        ArrayList<State> succ;

        while(!stateList.isEmpty()){
            // 初始化一个子孙stateList
            successors.clear();
            for(State state : stateList){
                if(state.equals(stateInit)){ // 新论文里面没有判断这一步离谱，并且去掉后效果更好
                    succ = expand(state,w * w);
                }else{
                    succ = expand(state,w);
                }
                successors.addAll(succ);
            }

            stateList.clear();
            if(!successors.isEmpty()){
                for(State state : successors){
                    state.greedyScore = greedy(state.cloneObj(),bestState, state);
                }

                // 选出前minL个greedyScore最大的块
                // 删除相似状态(目前没看出来有什么区别)
                successors = removeSimilarStates(successors);

                int minL = Math.min(successors.size(),w);
                // 简单选择排序选出最大的minL个块,放在块表前面
                for(int i = 0; i < minL; i++){
                    int maxIndex = i;
                    for(int j = i + 1; j < successors.size(); j++){
                        if(successors.get(maxIndex).greedyScore < successors.get(j).greedyScore){
                            maxIndex = j;
                        }
                    }
                    // 保留最大的
                    stateList.add(successors.get(maxIndex));
                    // 交换
                    State stateTemp = successors.get(i);
                    successors.set(i,successors.get(maxIndex));
                    successors.set(maxIndex,stateTemp);
                }
            }
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-10 13-06-01
     * @Description greedy 返回装填体积
    */
    /**
     * @Author yajiewen
     * @Date 2023-02-16 13-28-45
     * @Description 优化代码
    */
    public static double greedy(State state ,State[] bestState,State oriState){

        while(!state.spaceArrayList.isEmpty()){
            int selectedSpaceIndex = SpaceHelper.selectSpace(state.spaceArrayList);
            ArrayList<Block> viableBlockList = BlockSelector.searchViableBlockWithNumLimitByVCS(state.spaceArrayList.get(selectedSpaceIndex), state,1);
            if(!viableBlockList.isEmpty()){
                State.renewState(state,viableBlockList.get(0),selectedSpaceIndex);
            }else{
                state.spaceArrayList.remove(selectedSpaceIndex);
            }
        }
        // 更新bestState
        if(bestState[0] == null){
            bestState[0] = state;
        }else{
            if(bestState[0].scheme.totalVolum < state.scheme.totalVolum){
                bestState[0] = state;
            }
        }
        oriState.finalavailBox = new int[oriState.availBox.length];
        System.arraycopy(state.availBox, 0, oriState.finalavailBox, 0, state.availBox.length);
        return state.scheme.totalVolum;
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-11 11-48-22
     * @Description 删除相似状态
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-03 22-02-50
     * @Description 修改使得代码更加易读 神奇的是修改后br1 64 问题从0.9528 装填率变为
    */
    public static ArrayList<State> removeSimilarStates(ArrayList<State> stateList){
        ArrayList<State> notSimilarStateList = new ArrayList<>();
        notSimilarStateList.add(stateList.get(0));
        boolean isSimilar = false;

        for (int i = 1; i < stateList.size(); i++) {
            isSimilar = false;
            // 拿出一个状态
            State stateA = stateList.get(i);
            for (int j = 0; j < notSimilarStateList.size(); j++) {
                // 拿出一个状态
                State stateB = notSimilarStateList.get(j);
                // 最终状态相似,则把当前装载体积小的换入notSimilarStateList
                if(Arrays.equals(stateA.finalavailBox, stateB.finalavailBox)){
                    if(stateB.scheme.totalVolum > stateA.scheme.totalVolum){
                        notSimilarStateList.set(j,stateA);
                    }
                    isSimilar = true;
                    break;
                }
            }
            if(!isSimilar){
                notSimilarStateList.add(stateA);
            }
        }
        return notSimilarStateList;
    }
    /**
     * @Author yajiewen
     * @Date 2022-10-03 23-29-57
     * @Description 尝试通过最终装填的体积来删除相似状态
    */
    public static ArrayList<State> removeSimilarStatesByVolume(ArrayList<State> stateList){
        ArrayList<State> afterRemoveedStateList = new ArrayList<>();
        afterRemoveedStateList.add(stateList.get(0));
        boolean isSimilar = false;

        for (int i = 1; i < stateList.size(); i++) {
            isSimilar = false;
            // 拿出一个状态
            State stateA = stateList.get(i);
            for (int j = 0; j < afterRemoveedStateList.size(); j++) {
                // 拿出一个状态
                State stateB = afterRemoveedStateList.get(j);
                // 当前状态相似,则最终装载体积大的换入afterRemoveedStateList
                if(Arrays.equals(stateA.availBox, stateB.availBox)){
                    if(stateB.greedyScore < stateA.greedyScore){
                        afterRemoveedStateList.set(j,stateA);
                    }
                    isSimilar = true;
                    break;
                }
            }
            if(!isSimilar){
                afterRemoveedStateList.add(stateA);
            }
        }
        return afterRemoveedStateList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-10 12-44-52
     * @Description expand 扩展节点算法
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-03 09-44-43
     * @Description 将状态列表数据结构从HashSet 改为 ArrayList
    */
//    public static ArrayList<State> expand(State state, int w){
//        ArrayList<State> succ = new ArrayList<>();
//        int selectedSpaceIndex = SpaceHelper.selectSpace(state.spaceArrayList);
//        ArrayList<Block> viableBlockList = BlockSelector.searchViableBlockWithNumLimitByVCS(state.spaceArrayList.get(selectedSpaceIndex), state, w);
//        for(Block block :viableBlockList){
//            State newState = State.getNewState(state,block,selectedSpaceIndex);
//            succ.add(newState);
//        }
//        return succ;
//    }

    /**
     * @Author yajiewen
     * @Date 2022-10-23 19-50-28
     * @Description 全新的expand函数，当搜索不到块的时候应该把这个空间删除，在选一个搜索，而不是返回空的后继，这样会导致有的状态没法往下搜索
    */
    public static ArrayList<State> expand(State state, int w){
        ArrayList<Block> viableBlockList = new ArrayList<>();
        int selectedSpaceIndex = 0;
        // 当没找到块，并且还有剩余空间就继续找块
        while(viableBlockList.isEmpty() && !state.spaceArrayList.isEmpty()){
            // 选出一个空间
            selectedSpaceIndex = SpaceHelper.selectSpace(state.spaceArrayList);
            // 找块
            viableBlockList = BlockSelector.searchViableBlockWithNumLimitByVCS(state.spaceArrayList.get(selectedSpaceIndex), state, w);
            // 没找到删除空间
            if(viableBlockList.isEmpty()){
                state.spaceArrayList.remove(selectedSpaceIndex);
            }else{
                break; //break 可省略，为了方便理解写着
            }
        }
        ArrayList<State> succ = new ArrayList<>();
        for(Block block :viableBlockList){
            State newState = State.getNewState(state,block,selectedSpaceIndex);
            succ.add(newState);
        }
        return succ;
    }
}
