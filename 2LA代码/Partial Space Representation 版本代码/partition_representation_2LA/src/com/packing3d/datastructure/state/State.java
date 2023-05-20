package com.packing3d.datastructure.state;

import com.packing3d.application.helper.ResourceHelper;
import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.problem.Problem;
import com.packing3d.datastructure.scheme.Put;
import com.packing3d.datastructure.scheme.Scheme;
import com.packing3d.datastructure.space.Space;

import java.util.ArrayList;
import java.util.Stack;

/**
 * @Author yajiewen
 * @Date 2022-03-19 11-35-09
 * @Description 描述每一个阶段的状态
*/
public class State {
    // 剩余空间栈
    public Stack<Space> spaceStack;
    // 剩余块列表
    public ArrayList<Block> blockList;
    // 剩余箱子数目
    public int[] availBox;
    // 当前装填方案
    public Scheme scheme;
    // 父亲state (用于反向查找)
    public State fatherState;
    
    // 无参构造函数
    public State(){
        this.spaceStack = new Stack<>();
        this.blockList = new ArrayList<>();
    }
    // 初始化状态的构造函数(需要传problem 用于初始化)
    public State(Problem problem,ArrayList<Block> blockList){
        this.spaceStack = new Stack<>();
        this.blockList = new ArrayList<>();
        this.availBox = new int[problem.typeNumberOfBox];
        this.scheme = new Scheme();

        // 初始化剩余空间栈
        spaceStack.push(new Space(0,0,0, problem.containnerLength, problem.containnerWidth, problem.containnerHigh, false));
        // 初始化块表,不能用引用,因为后面要删除
        for (Block block : blockList) {
            this.blockList.add(block);
        }
        // 初始化剩余箱子数目
        for (int i = 0; i < problem.boxList.size(); i++) {
            this.availBox[i] = problem.boxList.get(i).boxNumber;
        }
        // 初始化father,初始时的state没有父亲节点
        this.fatherState = null;
    }

    // 获取新的状态(即状态更新)
    public static State getNewState(State stateOld,Block block){
        State newState = stateOld.cloneObj();
        // 栈顶空间出栈
        Space space = newState.spaceStack.pop();
        // 更新newState
        newState.scheme.addPut(new Put(block,space));
        // 更新资源相关
        ResourceHelper.renewResource(newState.spaceStack, space, block, newState.availBox);
        // 块列表更新
        newState.blockList.remove(block);
        // 删除剩余箱子不可生成的块
        for (int i = newState.blockList.size() -1; i >= 0; i--) {
            // 判断剩余箱子能否生成此块
            boolean isOk = true;
            Block block1 = newState.blockList.get(i);

            for (int j = 0; j < newState.availBox.length; j++) {
                if(block1.neededBoxNumOfEachType[j] > newState.availBox[j]){
                    isOk  = false;
                    break;
                }
            }
            if(isOk == false){
                newState.blockList.remove(i);
            }
        }
        return newState;
    }
    
    // 用于克隆state ,搜索阶段需要克隆
    public State cloneObj(){
        State state = new State();
        // 复制空间栈内容
        for (Space space : this.spaceStack) {
            state.spaceStack.push(space);
        }
        // 复制块表内容
        for (Block block : this.blockList) {
            state.blockList.add(block);
        }
        // 复制剩余箱子数目
        state.availBox = new int[this.availBox.length];
        for (int i = 0; i < this.availBox.length; i++) {
            state.availBox[i] = this.availBox[i];
        }
        // 复制放置方案
        state.scheme = this.scheme.cloneObj();
        // 更新父亲节点
        state.fatherState = this;
        return state;
    }
}
