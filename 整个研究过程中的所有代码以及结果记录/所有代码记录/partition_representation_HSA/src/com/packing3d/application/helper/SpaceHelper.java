package com.packing3d.application.helper;

import com.packing3d.datastructure.block.Block;
import com.packing3d.datastructure.space.Space;

import java.util.Stack;

public class SpaceHelper {
    /**
     * @Author yajiewen
     * @Date 2022-04-30 10-53-10
     * @Description 空间更新非完全支撑
     */
    public static void renewSpaceNotFullySupported(Stack<Space> spaceStack, Space space, Block block){
        Space spaceZ = new Space(
                space.locationX,
                space.locationY,
                space.locationZ + block.blockHigh,
                block.blockLenght,
                block.blockWidth,
                space.spaceHigh - block.blockHigh,
                false
        );
        double mx = space.spaceLength - block.blockLenght;
        double my = space.spaceWidth - block.blockWidth;
        if(mx > my){
            Space spaceX = new Space(
                    space.locationX + block.blockLenght,
                    space.locationY,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    space.spaceWidth,
                    space.spaceHigh,
                    true,
                    space.locationX + block.blockLenght,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh
            );
            Space spaceY = new Space(
                    space.locationX,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    block.blockLenght,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh,
                    false
            );
            spaceStack.push(spaceZ);
            spaceStack.push(spaceY);
            spaceStack.push(spaceX);
        }else{
            Space spaceX = new Space(
                    space.locationX + block.blockLenght,
                    space.locationY,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    block.blockWidth,
                    space.spaceHigh,
                    false);
            Space spaceY = new Space(
                    space.locationX,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    space.spaceLength,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh,
                    true,
                    space.locationX + block.blockLenght,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh
            );
            spaceStack.push(spaceZ);
            spaceStack.push(spaceX);
            spaceStack.push(spaceY);
        }
    }
    /**
     * @Author yajiewen
     * @Date 2022-04-30 10-51-07
     * @Description 空间更新支持完全支撑
     */
    public static void renewSpaceFullySupported(Stack<Space> spaceStack, Space space, Block block){

        Space spaceZ = new Space(
                space.locationX,
                space.locationY,
                space.locationZ + block.blockHigh,
                block.viableLength,
                block.viableWidth,
                space.spaceHigh - block.blockHigh,
                false
        );
        double mx = space.spaceLength - block.blockLenght;
        double my = space.spaceWidth - block.blockWidth;
        if(mx > my){
            Space spaceX = new Space(
                    space.locationX + block.blockLenght,
                    space.locationY,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    space.spaceWidth,
                    space.spaceHigh,
                    true,
                    space.locationX + block.blockLenght,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh
            );
            Space spaceY = new Space(
                    space.locationX,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    block.blockLenght,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh,
                    false
            );
            spaceStack.push(spaceZ);
            spaceStack.push(spaceY);
            spaceStack.push(spaceX);
        }else{
            Space spaceX = new Space(
                    space.locationX + block.blockLenght,
                    space.locationY,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    block.blockWidth,
                    space.spaceHigh,
                    false);
            Space spaceY = new Space(
                    space.locationX,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    space.spaceLength,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh,
                    true,
                    space.locationX + block.blockLenght,
                    space.locationY + block.blockWidth,
                    space.locationZ,
                    space.spaceLength - block.blockLenght,
                    space.spaceWidth - block.blockWidth,
                    space.spaceHigh
            );
            spaceStack.push(spaceZ);
            spaceStack.push(spaceX);
            spaceStack.push(spaceY);
        }
    }
    /**
     * @Author yajiewen
     * @Date 2022-04-30 10-56-05
     * @Description 空间转移
    */
    public static void transferSpace(Stack<Space> spaceStack, Space space){
        Space space1 = spaceStack.peek();
        //x
        if(space1.locationX + space1.spaceLength == space.transferSpaceLocationX
                && space1.locationY == space.transferSpaceLocationY
                && space1.locationZ == space.transferSpaceLocationZ
                && space1.spaceWidth == space.transferSpaceWidth
                && space1.spaceHigh == space.transferspaceHigh
        ){
            space1.spaceLength += space.transferSpaceLength;
        }else{
            // y
            if(space1.locationY + space1.spaceWidth == space.transferSpaceLocationY
                    && space1.locationX == space.transferSpaceLocationX
                    && space1.locationZ == space.transferSpaceLocationZ
                    && space1.spaceLength == space.transferSpaceLength
                    && space1.spaceHigh == space.transferspaceHigh){
                space1.spaceWidth += space.transferSpaceWidth;
            }
        }
    }
}
