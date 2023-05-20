package com.packing3d.helper;

import com.packing3d.datastructure.space.Space;

import java.util.ArrayList;

public class SpaceHelper {

    /**
     * @Author yajiewen
     * @Date 2022-10-14 15-55-54
     * @Description
     * 修改：// 根据Manhantan anchor distance 选择空间 返回被选中空间的下标
     * 论文添加了三层A new iterative-doubling Greedy–Lookahead algorithm for the single
     * container loading problem
    */
    /**
     * @Author yajiewen
     * @Date 2022-10-17 22-58-56
     * @Description
     * 优化从if里面提出体积德计算到if外面,把变量定义提出for之外
    */
    public static int selectSpace(ArrayList<Space> spaceArrayList){
        Space selectedSpace = spaceArrayList.get(0);
        int selectedSpaceIndex = 0;
        double spaceV;
        double selectedSpaceV;
        ArrayList<Double> spaceLexOrder;
        ArrayList<Double> selectedSpaceLexOrder;

        for(int i = 1; i < spaceArrayList.size(); i++){
            Space space = spaceArrayList.get(i);
            // 计算spaceV 和 selectedSpaceV
            spaceV = space.spaceLength * space.spaceWidth * space.spaceHigh;
            selectedSpaceV = selectedSpace.spaceLength * selectedSpace.spaceWidth * selectedSpace.spaceHigh;
            // 比较anchor distance
            if(selectedSpace.anchorDistance > space.anchorDistance){ // 选择距离最小的
                selectedSpace = space;
                selectedSpaceIndex = i;
            }else if(selectedSpace.anchorDistance == space.anchorDistance){ // 距离相同保留体积大的
                if(selectedSpaceV < spaceV) {
                    selectedSpace = space;
                    selectedSpaceIndex = i;
                }
//                }else if(selectedSpaceV == spaceV){ // 体积相同保留字典顺序小的
//                        spaceLexOrder = getLexOrderThree(space);
//                        selectedSpaceLexOrder = getLexOrderThree(selectedSpace);
//                        if(lexOrderCompare(spaceLexOrder,selectedSpaceLexOrder)){
//                            selectedSpace = space;
//                            selectedSpaceIndex = i;
//                        }
//                }
            }
        }
        return selectedSpaceIndex;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-14 16-08-26
     * @Description 求 lexicographical order of (y1, z1, y2, z2, x1, x2),
     * 这个是根据 0 号点为x1 y1 z1 6 号为x2 y2 z2
    */
    public static ArrayList<Double> getLexOrder(Space space){
//        System.out.println("getLexOrder");
        ArrayList<Double> returnList = new ArrayList<>();
        returnList.add(space.eightCoordinate[0][1]); // y1
        returnList.add(space.eightCoordinate[0][2]); // z1

        returnList.add(space.eightCoordinate[6][1]); // y2
        returnList.add(space.eightCoordinate[6][2]); // z2

        returnList.add(space.eightCoordinate[0][0]); // x1
        returnList.add(space.eightCoordinate[6][0]); // x2
        return returnList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-14 16-08-26
     * @Description 求 lexicographical order of (y1, z1, y2, z2, x1, x2),
     * 这个是根据 Manhanttan 距离来确定x1 y1 z1 x2 y2 z2
     */
    public static ArrayList<Double> getLexOrderTwo(Space space){
//        System.out.println("getLexOrderTwo");
        ArrayList<Double> returnList = new ArrayList<>();

        int minDistanceIndex = 0;
        int maxDistanceIndex = 0;

        double[] distance = new double[8];
        for (int i = 0; i < distance.length; i++) {
            distance[i] = space.eightCoordinate[i][0] + space.eightCoordinate[i][1] + space.eightCoordinate[i][2];
        }

        for (int i = 1; i < distance.length; i++) {
            if(distance[i] < distance[minDistanceIndex]){
                minDistanceIndex = i;
            }
            if(distance[i] > distance[maxDistanceIndex]){
                maxDistanceIndex = i;
            }
        }

        returnList.add(space.eightCoordinate[minDistanceIndex][1]); // y1
        returnList.add(space.eightCoordinate[minDistanceIndex][2]); // z1

        returnList.add(space.eightCoordinate[maxDistanceIndex][1]); // y2
        returnList.add(space.eightCoordinate[maxDistanceIndex][2]); // z2

        returnList.add(space.eightCoordinate[minDistanceIndex][0]); // x1
        returnList.add(space.eightCoordinate[maxDistanceIndex][0]); // x2
        return returnList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-14 16-08-26
     * @Description 求 lexicographical order of (y1, z1, y2, z2, x1, x2),
     * 这个是根据 欧几里得 距离来确定x1 y1 z1 x2 y2 z2
     */
    public static ArrayList<Double> getLexOrderThree(Space space){
//        System.out.println("getLexOrderThree");
        ArrayList<Double> returnList = new ArrayList<>();

        int minDistanceIndex = 0;
        int maxDistanceIndex = 0;

        double[] distance = new double[8];
        for (int i = 0; i < distance.length; i++) {
            distance[i] = space.eightCoordinate[i][0] * space.eightCoordinate[i][0] +
                    space.eightCoordinate[i][1] * space.eightCoordinate[i][1] + space.eightCoordinate[i][2] * space.eightCoordinate[i][2];
        }

        for (int i = 1; i < distance.length; i++) {
            if(distance[i] < distance[minDistanceIndex]){
                minDistanceIndex = i;
            }
            if(distance[i] > distance[maxDistanceIndex]){
                maxDistanceIndex = i;
            }
        }

        returnList.add(space.eightCoordinate[minDistanceIndex][1]); // y1
        returnList.add(space.eightCoordinate[minDistanceIndex][2]); // z1

        returnList.add(space.eightCoordinate[maxDistanceIndex][1]); // y2
        returnList.add(space.eightCoordinate[maxDistanceIndex][2]); // z2

        returnList.add(space.eightCoordinate[minDistanceIndex][0]); // x1
        returnList.add(space.eightCoordinate[maxDistanceIndex][0]); // x2
        return returnList;
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-14 16-29-43
     * @Description 对lexicographical order 进行大小比较 ,A < B 返回true，否则false
    */
    public static boolean lexOrderCompare(ArrayList<Double> lexOrderA, ArrayList<Double> lexOrderB){

        double a,b;
        for (int i = 0; i < lexOrderA.size(); i++) {
            a = lexOrderA.get(i);
            b = lexOrderB.get(i);
            if(a < b){
                return true;
            }
        }
        return false;
    }

}
