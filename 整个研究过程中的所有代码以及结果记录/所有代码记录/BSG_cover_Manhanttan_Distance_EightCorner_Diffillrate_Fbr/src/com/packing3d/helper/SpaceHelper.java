package com.packing3d.helper;

import com.packing3d.datastructure.space.Space;

import java.util.ArrayList;

public class SpaceHelper {

    // 根据Manhantan anchor distance 选择空间 返回被选中空间的下标
    public static int selectSpace(ArrayList<Space> spaceArrayList){
        Space selectedSpace = spaceArrayList.get(0);
        int selectedSpaceIndex = 0;

        for(int i = 1; i < spaceArrayList.size(); i++){
            Space space = spaceArrayList.get(i);
            // 比较anchor distance
            if(selectedSpace.anchorDistance > space.anchorDistance){
                selectedSpace = space;
                selectedSpaceIndex = i;
            }else if(selectedSpace.anchorDistance == space.anchorDistance){ // 距离相同保留体积大的
                if(selectedSpace.spaceLength * selectedSpace.spaceWidth * selectedSpace.spaceHigh <
                        space.spaceLength * space.spaceWidth * space.spaceHigh){
                    selectedSpace = space;
                    selectedSpaceIndex = i;
                }
            }
        }
        return selectedSpaceIndex;
    }
}
