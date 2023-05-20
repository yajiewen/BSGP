package com.packing3d.helper;

import com.packing3d.datastructure.space.Space;

import java.util.ArrayList;

public class SpaceHelper {

    // 根据anchor distance 选择空间 返回被选中空间的下标
    public static int selectSpace(ArrayList<Space> spaceArrayList){
        Space selectedSpace = spaceArrayList.get(0);
        int selectedSpaceIndex = 0;

        for(int i = 1; i < spaceArrayList.size(); i++){
            Space space = spaceArrayList.get(i);
            // 比较anchor distance
            int equalNum = 0;
            for(int j = 0; j < 3; j++){
                if(selectedSpace.anchorDistance[j] > space.anchorDistance[j]){
                    selectedSpace = space;
                    selectedSpaceIndex = i;
                    break;
                }else if(selectedSpace.anchorDistance[j] < space.anchorDistance[j]){
                    break;
                }else{
                    equalNum++;
                }
            }
            // 如果equalNum == 3 说明 这两个空间的anchor distance 相同,此时选择体积大的
            if(equalNum == 3){
                if((selectedSpace.spaceLength * selectedSpace.spaceWidth * selectedSpace.spaceHigh) < (space.spaceLength * space.spaceWidth * space.spaceHigh)){
                    selectedSpace = space;
                    selectedSpaceIndex = i;
                }
            }
        }
        return selectedSpaceIndex;
    }
}
