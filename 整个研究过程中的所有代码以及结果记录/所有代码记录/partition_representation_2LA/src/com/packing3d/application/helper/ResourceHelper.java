package com.packing3d.application.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.space.Space;

import java.util.Stack;

public class ResourceHelper {
    // 传入出栈后的空间栈, 和出栈的空间
    public static void renewResource(Stack<Space> spaceStack, Space space, Block block, int[] availNum){
        // 更新箱子数目
        for (int i = 0; i < availNum.length; i++) {
            availNum[i] -= block.neededBoxNumOfEachType[i];
        }
        SpaceHelper.renewSpaceFullySupported(spaceStack, space, block);
//        SpaceHelper.renewSpaceNotFullySupported(spaceStack, space, block);
    }
}
