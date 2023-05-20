package com.packing3d.datastructure.state;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.scheme.Put;
import com.packing3d.datastructure.scheme.Scheme;
import com.packing3d.datastructure.space.Space;
import com.packing3d.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Author yajiewen
 * @Date 2022-04-04 13-19-57
 * @Description 每一阶段的状态, 更新空间时候同时生成anchor corner and distance
*/
public class State {
    // 剩余空间栈
    public ArrayList<Space> spaceArrayList;
    // 剩余块列表
    public ArrayList<Block> blockList;
    // 剩余箱子数目
    public int[] availBox;
    // 当前装填方案
    public Scheme scheme;
    // 父亲state (用于反向查找)
    public State fatherState;
    // 带上proble 方便使用
    public Problem problem;
    // fbr 每次生成新状态需要求 vLoss向量用于选择块
    public ArrayList<Integer> xVlossVector;
    public ArrayList<Integer> yVlossVector;
    public ArrayList<Integer> zVlossVector;
    // greedy score
    public double greedyScore;
    // greed 后剩余箱子数,用于删除相似状态
    public int[] finalavailBox;

    
    // 无参构造函数
    public State(){
        this.spaceArrayList = new ArrayList<>();
        this.blockList = new ArrayList<>();
        this.scheme = new Scheme();
    }
    // 初始化状态的构造函数(需要传problem 用于初始化)
    public State(Problem problem,ArrayList<Block> blockList){
        double start = System.currentTimeMillis();
        this.spaceArrayList = new ArrayList<>();
        this.blockList = new ArrayList<>();
        this.availBox = new int[problem.typeNumberOfBox];
        this.scheme = new Scheme();

        // 初始化剩余空间栈
        spaceArrayList.add(new Space(0,0,0, problem.containnerLength, problem.containnerWidth, problem.containnerHigh, problem));
        // 初始化块表,不能用引用,因为后面要删除
        this.blockList.addAll(blockList);
        // 初始化剩余箱子数目
        for (int i = 0; i < problem.boxList.size(); i++) {
            this.availBox[i] = problem.boxList.get(i).boxNumber;
        }
        // 初始化father,初始时的state没有父亲节点
        this.fatherState = null;
        // 初始化proble
        this.problem = problem;
//        System.out.println("生成初始状态耗时：" + (System.currentTimeMillis() - start));
    }

    // 在原状态上更新
    public static void renewState(State state,Block block,int selectedSpaceIndex){
        // 从空间列表中删除被选中的空间
        Space space = state.spaceArrayList.remove(selectedSpaceIndex);
        // 更新newState
        state.scheme.addPut(new Put(block,space));
        // 更新资源相关
        ResourceHelper.renewResource(state, space, block);

    }

    // 获取新的状态(在复制的上面更新)
    public static State getNewState(State stateOld,Block block,int selectedSpaceIndex){
//        double start = System.currentTimeMillis();
        State newState = stateOld.cloneObj();
        // 从空间列表中删除被选中的空间
        Space space = newState.spaceArrayList.remove(selectedSpaceIndex);
        // 更新newState
        newState.scheme.addPut(new Put(block,space));
        // 更新资源相关
        ResourceHelper.renewResource(newState, space, block);
//        System.out.println("状态更新耗时：" + (System.currentTimeMillis() - start) + "ms");
        return newState;
    }
    
    // 用于克隆state ,搜索阶段需要克隆
    public State cloneObj(){
        State state = new State();
        // 复制空间栈内容
        state.spaceArrayList.addAll(this.spaceArrayList);
        // 复制块表内容
        state.blockList.addAll(this.blockList);
        // 复制剩余箱子数目
        state.availBox = new int[this.availBox.length];
        System.arraycopy(this.availBox, 0, state.availBox, 0, this.availBox.length);
        // 复制放置方案
        state.scheme = this.scheme.cloneObj();
        // 更新父亲节点
        state.fatherState = this;
        // 复制problem
        state.problem = this.problem;
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return spaceArrayList.equals(state.spaceArrayList) && blockList.equals(state.blockList) && Arrays.equals(availBox, state.availBox);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(spaceArrayList, blockList);
        result = 31 * result + Arrays.hashCode(availBox);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "scheme=" + scheme +
                '}';
    }
}
