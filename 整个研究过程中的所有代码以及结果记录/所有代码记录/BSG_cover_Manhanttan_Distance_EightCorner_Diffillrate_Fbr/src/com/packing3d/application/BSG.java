package com.packing3d.application;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.state.State;
import com.packing3d.filetool.FileHelper;
import com.packing3d.helper.BlockHelper;
import com.packing3d.helper.SpaceHelper;
import com.packing3d.helper.StateHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @Author yajiewen
 * @Date 2022-07-14 10-28-59
 * @Description
 * 根据原文,不同的问题,块的fillrate 使用不同的值: BR0-BR7 Minfillrate = 1 ; BR8-BR15 Minfillrate = 0.98
 * 块选择方法使用fbr 修复fbr
*/
public class BSG {
    public static void main(String[] args) throws IOException {
        oneThread();
//        mutilThread(args[0],args[1]);
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-12 11-54-30
     * @Description 单线程
    */
    public static void oneThread() throws IOException {
        // 读取文件
        FileHelper fileHelper = new FileHelper("B:\\Standard_Datasets\\br1.txt");
        ArrayList<Problem> problems = fileHelper.getProblems();
//        for(Problem problem : problems){
//            State state = algorithmBSG(problem,30 * 1000);
//            double rate = state.scheme.totalVolum / (problem.containnerHigh * problem.containnerLength * problem.containnerWidth);
//            System.out.println("利用率=====>" + rate );
//        }
        Problem problem = problems.get(64);
        double MinFillRate = 0.98;
        State state = algorithmBSG(problem,30 * 1000,MinFillRate);
        double rate = state.scheme.totalVolum / (problem.containnerHigh * problem.containnerLength * problem.containnerWidth);
        System.out.println("利用率=====>" + rate );
        StateHelper.stateDetecte(state,problem);
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-12 11-54-51
     * @Description 多线程
    */
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
                    int i = 1;
                    double MinFillRate;
                    if (p >= 0 && p <= 7){
                        MinFillRate = 1;
                    }else{
                        MinFillRate = 0.98;
                    }
                    for (Problem problem : problemList) {
                        State state = algorithmBSG(problem,500 * 1000,MinFillRate);
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

    /**
     * @Author yajiewen
     * @Date 2022-05-09 10-39-00
     * @Description BSG 算法
    */
    public static State algorithmBSG(Problem problem,int timeLimit,double MinFillRate){
        // 开辟一个final的对象数组,用于保存匿名内部类的里面的结果
        State[] bestState = new State[1];
        int MaxBlocks = 10000;
//        ArrayList<Block> blockList = BlockHelper.generateGeneralCuttingBlocksNotFullySupported(problem,MaxBlocks,MinFillRate);
        ArrayList<Block> blockList = BlockHelper.generateGuillotineBlocksNotFullySupported(problem,MaxBlocks,MinFillRate);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // 开始未来任务
        FutureTask<?> futureTask = new FutureTask<>(new Runnable() {
            @Override
            public void run() {
                int w = 1;
                while(!Thread.interrupted()){
                    beamSearch(problem,blockList,w,bestState);
                    w =(int) Math.ceil(Math.sqrt(2) * w);
                }
            }
        },null);

        // 开始执行任务
        executorService.execute(futureTask);
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start <= timeLimit);
//        try {
//            Thread.sleep(timeLimit);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        // 时间到结束
        futureTask.cancel(true);
        executorService.shutdown();
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
                if(state.equals(stateInit)){
                    succ = expand(state,w * w);
                }else{
                    succ = expand(state,w);
                }
                successors.addAll(succ);
            }

            stateList.clear();
            if(!successors.isEmpty()){
                for(State state : successors){
                    state.greedyScore = greedy(state.cloneObj(), bestState, state);
                }

                // 选出前minL个greedyScore最大的块
                System.out.println("sortList = " +successors.size() + "w=" + w);
                // 删除相似状态(目前没看出来有什么区别)
                successors = removeSimilarStates(successors);
                successors.sort(new Comparator<State>() {
                    @Override
                    public int compare(State o1, State o2) {
                        if(o2.greedyScore > o1.greedyScore){
                            return 1;
                        }else if(o2.greedyScore < o1.greedyScore){
                            return -1;
                        }else{
                            return 0;
                        }
                    }
                });
                int len = Math.min(successors.size(),w);
                for(int i = 0; i < len; i++){
                    stateList.add(successors.get(i));
                }
            }
        }
//        SchemeHelper.schemeDetecte(bestState[0].scheme, bestState[0].problem);
    }

    /**
     * @Author yajiewen
     * @Date 2022-05-10 13-06-01
     * @Description greedy 返回装填体积
    */
    public static double greedy(State state,final State[] bestState,State oriState){
        while(!state.spaceArrayList.isEmpty()){
            int selectedSpaceIndex = SpaceHelper.selectSpace(state.spaceArrayList);
            ArrayList<Block> viableBlockList = BlockHelper.searchViableBlockWithNumLimitAndFbr(state.spaceArrayList.get(selectedSpaceIndex), state,1);
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
//        double rate = state.scheme.totalVolum / (state.problem.containnerHigh * state.problem.containnerLength * state.problem.containnerWidth);
//        System.out.println("利用率greedy=====>" + rate );
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
                // 状态相似,则把当前装载体积小的换入deletedStateList
                if(Arrays.equals(stateA.finalavailBox, stateB.finalavailBox)){
                    if(stateB.scheme.totalVolum > stateA.scheme.totalVolum){
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
    public static ArrayList<State> expand(State state, int w){
        ArrayList<State> succ = new ArrayList<>();
        int selectedSpaceIndex = SpaceHelper.selectSpace(state.spaceArrayList);
//        ArrayList<Block> viableBlockList = BlockHelper.searchViableBlockWithNumLimit(state.spaceArrayList.get(selectedSpaceIndex),state.blockList,state.availBox,w);
        ArrayList<Block> viableBlockList = BlockHelper.searchViableBlockWithNumLimitAndFbr(state.spaceArrayList.get(selectedSpaceIndex), state, w);
        for(Block block :viableBlockList){
            State newState = State.getNewState(state,block,selectedSpaceIndex);
            succ.add(newState);
        }
        return succ;
    }
}
