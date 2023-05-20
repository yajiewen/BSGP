package com.packing3d.application;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.scheme.Put;
import com.packing3d.datastructure.scheme.Scheme;
import com.packing3d.datastructure.space.Space;
import com.packing3d.filetool.FileHelper;
import com.packing3d.helper.BlockHelper;
import com.packing3d.helper.ResourceHelper;
import com.packing3d.helper.SchemeHelper;
import com.packing3d.helper.SpaceHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * @Author yajiewen
 * @Date 2022-05-03 12-04-31
 * @Description
 *  * (K1) how to represent free space in the container;  cover representation
 *  * (K2) how to generate a list of blocks (of boxes);
 *  * (K3) how to select a free space; 选择anchor distance 最小的空间,最小的有多个,则取体积大的
 *  * (K4) how to select a block; 选择体积最大的块
 *  * (K5) how to place the selected block into the selected space and update the list of free space; 块放在0点 ,使用八点坐标更新发求剩余空间,同时支持完全支撑
 *  * (K6) what is the overarching search strategy;    HSA 模拟退火
*/
public class HSA2app {
    public static void main(String[] args) throws IOException {
//        oneThread();
        mutilThread(args[0],args[1]);
    }

    // 单线程
    public static void oneThread() throws IOException {
        double MinFillRate = 0.98; //复合块种要达到的最小填充量
        double MinAreaRate = 0.96; // 复合块顶部最小可行矩阵面积占符合块顶部面积比例
        int MaxLevel = 5;          // 复合块的最大复杂度
        int MaxBlocks = 10000;     // 生成复合块的最大块数
        int MaxSeq = 200;          // ps 的长度
        int MaxSelect = 47;        // 每个阶段的块选择数最大为

        double Ts = 1.0;         //初始温度
        double Tf = 0.01;        // 结束温度
        double Dt = 0.98;        // 降温系数
        int LengthOfMkF = 200; // 马尔可夫链长度
        boolean isLiner = true;     // 选择线性降温还是非线性降温
        FileHelper fileUtils = new FileHelper("B:\\Standard_Datasets\\br1.txt");
        ArrayList<Problem> problemList = fileUtils.getProblems(); // 获取文件中的所有问题

        double avaFillRate = 0;
        int i = 1;
        for (Problem problem : problemList) {
            ArrayList<Block> gPblockList = BlockHelper.generateGeneralCuttingBlocksNotFullySupported(problem,MaxBlocks,MinFillRate); //Gp块生成
            Scheme scheme = algorithmSA(gPblockList,problem,new Ps(MaxSeq),Ts,Tf,Dt,LengthOfMkF,isLiner,MaxSeq,MaxSelect);
            double fillRate = scheme.totalVolum / (problem.containnerLength * problem.containnerWidth * problem.containnerHigh);
            avaFillRate += fillRate;
            System.out.println("problem " + i + "=====>"+ fillRate);
            i++;
            SchemeHelper.schemeDetecte(scheme,problem);
        }
        System.out.println("平均填充率为:" + avaFillRate / 100);
    }

    public static void mutilThread(String inputPath, String outputPath){
        double MinFillRate = 0.98; //复合块种要达到的最小填充量
        double MinAreaRate = 0.96; // 复合块顶部最小可行矩阵面积占符合块顶部面积比例
        int MaxLevel = 5;          // 复合块的最大复杂度
        int MaxBlocks = 10000;     // 生成复合块的最大块数
        int MaxSeq = 200;          // ps 的长度
        int MaxSelect = 47;        // 每个阶段的块选择数最大为

        double Ts = 1.0;         //初始温度
        double Tf = 0.01;        // 结束温度
        double Dt = 0.98;        // 降温系数
        int LengthOfMkF = 200; // 马尔可夫链长度
        boolean isLiner = true;     // 选择线性降温还是非线性降温

        File outputPathFolder = new File(outputPath);
        if(!outputPathFolder.exists() && !outputPathFolder.isDirectory()){
            System.out.println("创建输出文件夹");
            outputPathFolder.mkdir();
        }

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

                    FileHelper fileUtils = new FileHelper(inputPath + File.separator +"br"+ p +".txt");
                    ArrayList<Problem> problemList = null;
                    try {
                        problemList = fileUtils.getProblems();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    double avaFillRate = 0;
                    int i = 1;
                    for (Problem problem : problemList) {
//                        ArrayList<Block> simpleBlockList = BlockHelper.getSimpleBlockListWithDirection(problem);
//                        ArrayList<Block> blockList = BlockHelper.generateComplexBlocksFullySupported(simpleBlockList,MaxLevel,MaxBlocks,problem,MinFillRate,MinAreaRate);
                        ArrayList<Block> blockList = BlockHelper.generateGeneralCuttingBlocksNotFullySupported(problem,MaxBlocks,MinFillRate);
                        Scheme scheme = algorithmSA(blockList,problem,new Ps(MaxSeq),Ts,Tf,Dt,LengthOfMkF,isLiner,MaxSeq,MaxSelect);
                        double fillRate = scheme.totalVolum / (problem.containnerLength * problem.containnerWidth * problem.containnerHigh);
                        avaFillRate += fillRate;
                        System.out.println("br"+ p +":==problem " + i + "===>"+"填充率为:"+ fillRate);
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


    // 混合启发式算法
    public static Scheme algorithmSA(ArrayList<Block> blockList, Problem problem, Ps ps,
                                     double Ts,double Tf, double Dt,int LengthOfMkF, boolean isLiner,int MaxSeq,int MaxSelect){
        Scheme scheme = getSchemeByHeuristic(blockList,problem,ps);
        Scheme bestScheme = scheme;
        Ps ps2 = new Ps(MaxSeq);
        double T = Ts;
        Random random = new Random();
        while(T >= Tf){
            for (int i = 0; i < LengthOfMkF; i++) {
                int k = random.nextInt( ps.psLength - 0 + 1) + 0;
                ps.psCopyto(ps2);
                ps2.psList[k] = random.nextInt(MaxSelect - 0 + 1) + 0;
                Scheme newScheme = getSchemeByHeuristic(blockList,problem,ps2);
                if(bestScheme.totalVolum < newScheme.totalVolum){
                    bestScheme = newScheme;
                }
                if(newScheme.totalVolum > scheme.totalVolum){
                    scheme = newScheme;
                    ps2.psCopyto(ps);
                }else{
                    if(random.nextDouble() < Math.exp((newScheme.totalVolum - scheme.totalVolum) / T)){
                        scheme = newScheme;
                        ps2.psCopyto(ps);
                    }
                }
            }
            if(isLiner){
                T *= Dt;
            }else{
                T = (1 - T * Dt) * T;
            }
        }
        return bestScheme;
    }
    // 基础启发式算法
    public static Scheme getSchemeByHeuristic(ArrayList<Block> blockList, Problem problem, Ps ps){
        // 记录每个类型的箱子的可用数目
        int[] availNum = new int[problem.typeNumberOfBox];
        for(int i = 0; i < problem.typeNumberOfBox; i++){
            availNum[i] = problem.boxList.get(i).boxNumber;
        }
        ArrayList<Space> spaceArrayList = new ArrayList<>();
        Space space = new Space(0,0,0,problem.containnerLength, problem.containnerWidth, problem.containnerHigh,problem);
        spaceArrayList.add(space);
        // 创建一个方案
        Scheme scheme = new Scheme();
        int index = 0; // 表示第几次放置
        while(!spaceArrayList.isEmpty()){
            int selectedSpaceIndex = SpaceHelper.selectSpace(spaceArrayList); // 根据anchor corner选择一个空间
            space = spaceArrayList.remove(selectedSpaceIndex);
            ArrayList<Block> viableBlockList = BlockHelper.searchViableBlock(space,blockList,availNum);
            if(!viableBlockList.isEmpty()){
                int selectedIndex = ps.psList[index++];
                if(selectedIndex >= viableBlockList.size()){
                    selectedIndex %= viableBlockList.size();
                }
                Block selectedBlock = viableBlockList.get(selectedIndex);
                scheme.addPut(new Put(selectedBlock,space));
                // 更新资源(箱子,和空间)
                ResourceHelper.renewResource(spaceArrayList,space,selectedBlock,availNum,problem);
            }
        }
        ps.psLength = index;
        return scheme;
    }
}


