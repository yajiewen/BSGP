package com.packing3d.datastructure.block;

import com.packing3d.datastructure.space.Space;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author yajiewen
 * @Date 2022-04-04 18-07-52
 * @Description 添加根据anchor corner 更新八点坐标,更新equals 和hashcode
*/
/**
 * @Author yajiewen
 * @Date 2022-11-05 10-18-03
 * @Description 添加boxNum变量，便于VCS评价函数使用
*/
public class Block {
    public int complexLevel; // 复杂程度
    public int[] neededBoxNumOfEachType; // 该复杂快所需要的各种类别箱子的数目(下标比表示类别)
    public int boxNum; // 块所包含的箱子数
    public int typeNumberOfBox; // 用于遍历
    public double blockLenght; // 复杂块的长度
    public double blockWidth; // 复杂块的宽度
    public double blockHigh; // 复杂快的高度
    public double viableLength; // 可行域的长度
    public double viableWidth; // 可行域的宽度
    public double boxVolum; // 块种箱子的体积
    public double blockVolum; // 块的体积

    public double superficialArea; // 块表面积
    public double fbrScore; // 用于存储块经过fbr得到的分数

    // 定义一个二维数组来保存八点坐标
    public double[][] eightCoordinate;
    // 定义保存x范围的数组
    public double[] xRange;
    // 定义保存y范围的数组
    public double[] yRange;
    // 定义保存z范围的数组
    public double[] zRange;

    /**
     * @Author yajiewen
     * @Date 2022-10-19 22-57-17
     * @Description 添加方便的构造函数(不带复杂度)
    */
    /**
     * @Author yajiewen
     * @Date 2022-11-05 10-20-27
     * @Description 添加计算块中箱子数目的方法
    */
    /**
     * @Author yajiewen
     * @Date 2023-02-17 20-23-59
     * @Description 添加块体积,和面积
    */
    public Block(int typeNumberOfBox, double blockLenght, double blockWidth, double blockHigh, double viableLength, double viableWidth, double boxVolum, int[] neededBoxNumOfEachType){
        // 在构造函数里面分配空间
        // 坐标空间
        eightCoordinate = new double[8][3];
        // x范围空间
        xRange = new double[2];
        // y范围空间
        yRange = new double[2];
        // z范围空间
        zRange = new double[2];
        // 开始为8个变量赋值
        this.typeNumberOfBox = typeNumberOfBox;
        this.blockLenght = blockLenght;
        this.blockWidth = blockWidth;
        this.blockHigh = blockHigh;
        this.viableLength = viableLength;
        this.viableWidth = viableWidth;
        this.boxVolum = boxVolum;
        this.blockVolum = blockLenght * blockWidth * blockHigh;
        this.superficialArea = 2 * (blockLenght * blockWidth + blockLenght * blockHigh + blockWidth * blockHigh);
        this.neededBoxNumOfEachType = neededBoxNumOfEachType;
        getBoxNum(); // 获取箱子数
    }

    /**
     * @Author yajiewen
     * @Date 2022-11-05 10-19-04
     * @Description 计算块所包含的箱子数
    */
    public void getBoxNum(){
        this.boxNum = 0;
        for (int i = 0; i < this.neededBoxNumOfEachType.length; i++) {
            this.boxNum += this.neededBoxNumOfEachType[i];
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-10-21 10-34-51
     * @Description 添加方便的构造函数(带复杂度)
    */
    public Block(int typeNumberOfBox, int complexLevel, double blockLenght, double blockWidth, double blockHigh, double viableLength, double viableWidth, double boxVolum, int[] neededBoxNumOfEachType){
        // 在构造函数里面分配空间
        // 坐标空间
        eightCoordinate = new double[8][3];
        // x范围空间
        xRange = new double[2];
        // y范围空间
        yRange = new double[2];
        // z范围空间
        zRange = new double[2];
        // 开始为8个变量赋值
        this.typeNumberOfBox = typeNumberOfBox;
        this.complexLevel = complexLevel;
        this.blockLenght = blockLenght;
        this.blockWidth = blockWidth;
        this.blockHigh = blockHigh;
        this.viableLength = viableLength;
        this.viableWidth = viableWidth;
        this.boxVolum = boxVolum;
        this.blockVolum = blockLenght * blockWidth * blockHigh;
        this.superficialArea = 2 * (blockLenght * blockWidth + blockLenght * blockHigh + blockWidth * blockHigh);
        this.neededBoxNumOfEachType = neededBoxNumOfEachType;
        getBoxNum(); // 获取箱子数
    }

    public Block(int typeNum){
        neededBoxNumOfEachType = new int[typeNum];
        typeNumberOfBox = typeNum;
        // 在构造函数里面分配空间
        // 坐标空间
        eightCoordinate = new double[8][3];
        // x范围空间
        xRange = new double[2];
        // y范围空间
        yRange = new double[2];
        // z范围空间
        zRange = new double[2];
    }

    // 使用范围生成coverBlock 的构造函数(coverBlock 在更新交叠空间时候使用, 只需用到八点坐标,其他信息用不到)
    public Block(double[] xRange, double[] yRange, double[] zRange){
        // 坐标空间
        eightCoordinate = new double[8][3];
        // x范围空间
        this.xRange = new double[2];
        // y范围空间
        this.yRange = new double[2];
        // z范围空间
        this.zRange = new double[2];

        blockLenght = xRange[1] - xRange[0];
        blockWidth = yRange[1] - yRange[0];
        blockHigh = zRange[1] - zRange[0];
        // 注意 viableLenght 和viableLength都要赋值,因为后面 更新空间时候 如z 方向空间需要用否则 z方向空间就是长宽为0 导致空间浪费
        viableLength = blockLenght;
        viableWidth = blockWidth;
        initCoordinateAndRange(xRange[0], yRange[0], zRange[0]);
    }

    // 根据anchor corner 初始化块的八点坐标
    public void initCoordinateAndRangeByAnchorCorner(Space space){
        if(space.anchorCorner == 0){
            initCoordinateAndRange(space.locationX, space.locationY, space.locationZ);
        }else if(space.anchorCorner == 1){
            // 根据1点坐标 求出块0 点处的坐标
            double oX = space.eightCoordinate[1][0] - blockLenght;
            double oY = space.eightCoordinate[1][1];
            double oZ = space.eightCoordinate[1][2];
            initCoordinateAndRange(oX,oY,oZ);
        }else if(space.anchorCorner == 2){
            double oX = space.eightCoordinate[2][0] - blockLenght;
            double oY = space.eightCoordinate[2][1] - blockWidth;
            double oZ = space.eightCoordinate[2][2];
            initCoordinateAndRange(oX,oY,oZ);
        }else if(space.anchorCorner == 3){
            double oX = space.eightCoordinate[3][0];
            double oY = space.eightCoordinate[3][1] - blockWidth;
            double oZ = space.eightCoordinate[3][2];
            initCoordinateAndRange(oX,oY,oZ);
        }else if(space.anchorCorner == 4){
            double oX = space.eightCoordinate[4][0];
            double oY = space.eightCoordinate[4][1];
            double oZ = space.eightCoordinate[4][2] - blockHigh;
            initCoordinateAndRange(oX,oY,oZ);
        }else if(space.anchorCorner == 5){
            double oX = space.eightCoordinate[5][0] - blockLenght;
            double oY = space.eightCoordinate[5][1];
            double oZ = space.eightCoordinate[5][2] - blockHigh;
            initCoordinateAndRange(oX,oY,oZ);
        }else if(space.anchorCorner == 6){
            double oX = space.eightCoordinate[6][0] - blockLenght;
            double oY = space.eightCoordinate[6][1] - blockWidth;
            double oZ = space.eightCoordinate[6][2] - blockHigh;
            initCoordinateAndRange(oX,oY,oZ);
        }else if(space.anchorCorner == 7){
            double oX = space.eightCoordinate[7][0];
            double oY = space.eightCoordinate[7][1] - blockWidth;
            double oZ = space.eightCoordinate[7][2] - blockHigh;
            initCoordinateAndRange(oX,oY,oZ);
        }
    }

    // 以0点坐标为基准,自动初始化八点坐标和范围(该方法在前面9个变量赋值完成后调用)
    public void initCoordinateAndRange(double oX, double oY, double oZ){
        // 获取8点坐标
        eightCoordinate[0][0] = oX;
        eightCoordinate[0][1] = oY;
        eightCoordinate[0][2] = oZ;

        eightCoordinate[1][0] = eightCoordinate[0][0] + blockLenght;
        eightCoordinate[1][1] = eightCoordinate[0][1];
        eightCoordinate[1][2] = eightCoordinate[0][2];

        eightCoordinate[2][0] = eightCoordinate[0][0] + blockLenght;
        eightCoordinate[2][1] = eightCoordinate[0][1] + blockWidth;
        eightCoordinate[2][2] = eightCoordinate[0][2];

        eightCoordinate[3][0] = eightCoordinate[0][0];
        eightCoordinate[3][1] = eightCoordinate[0][1] + blockWidth;
        eightCoordinate[3][2] = eightCoordinate[0][2];
        // 剩下四点的坐标等于前面四个点的坐标的z 值加上块的高度
        for (int i = 0; i < 4; i++) {
            eightCoordinate[i + 4][0] = eightCoordinate[i][0];
            eightCoordinate[i + 4][1] = eightCoordinate[i][1];
            eightCoordinate[i + 4][2] = eightCoordinate[i][2] + blockHigh;
        }
        // 获取x,y,z 范围
        xRange[0] = eightCoordinate[0][0];
        xRange[1] = eightCoordinate[1][0];

        yRange[0] = eightCoordinate[0][1];
        yRange[1] = eightCoordinate[3][1];

        zRange[0] = eightCoordinate[0][2];
        zRange[1] = eightCoordinate[4][2];
    }

    // 检测快是否合法
    public void blockDetect(){
        if(blockWidth < viableWidth || blockLenght < viableLength){
            System.out.println("块出错");
        }
    }

    /**
     * @Author yajiewen
     * @Date 2022-11-05 10-26-19
     * @Description 修改克隆函数，优化比必要步骤，添加克隆坐标
    */
    public Block cloneOne(){

        Block block = new Block(this.typeNumberOfBox,this.complexLevel,this.blockLenght,this.blockWidth,this.blockHigh,
                this.viableLength,this.viableWidth,this.boxVolum,this.neededBoxNumOfEachType);
        // 开始复制坐标范围
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 3; j++){
                block.eightCoordinate[i][j] = this.eightCoordinate[i][j];
            }
        }
        block.xRange[0] = this.eightCoordinate[0][0];
        block.xRange[1] = this.eightCoordinate[1][0];

        block.yRange[0] = this.eightCoordinate[0][1];
        block.yRange[1] = this.eightCoordinate[3][1];

        block.zRange[0] = this.eightCoordinate[0][2];
        block.zRange[1] = this.eightCoordinate[4][2];
        return block;
    }

    /**
     * @Author yajiewen
     * @Date 2023-02-21 11-59-28
     * @Description 获取旋转后的块
    */
    public Block getRotatedBlock(){
        Block block = new Block(this.typeNumberOfBox,this.complexLevel,this.blockWidth,this.blockLenght,this.blockHigh,
                this.viableLength,this.viableWidth,this.boxVolum,this.neededBoxNumOfEachType);
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Double.compare(block.blockLenght, blockLenght) == 0 && Double.compare(block.blockWidth, blockWidth) == 0 && Double.compare(block.blockHigh, blockHigh) == 0 && Arrays.equals(neededBoxNumOfEachType, block.neededBoxNumOfEachType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(blockLenght, blockWidth, blockHigh);
        result = 31 * result + Arrays.hashCode(neededBoxNumOfEachType);
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "xRange=" + Arrays.toString(xRange) +
                ", yRange=" + Arrays.toString(yRange) +
                ", zRange=" + Arrays.toString(zRange) +
                '}';
    }
}
