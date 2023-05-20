package com.packing3d.datastructure.block;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author yajiewen
 * @Date 2022-03-12 17-18-50
 * @Description
 */
public class Block {
    public int complexLevel; // 复杂程度
    public int[] neededBoxNumOfEachType; // 该复杂快所需要的各种类别箱子的数目(下标比表示类别)
    public int typeNumberOfBox; // 用于遍历
    public double blockLenght; // 复杂块的长度
    public double blockWidth; // 复杂块的宽度
    public double blockHigh; // 复杂快的高度
    public double viableLength; // 可行域的长度
    public double viableWidth; // 可行域的宽度
    public double blockVolum; // 复杂快的体积(指块种有效装填的体积)

    // 定义一个二维数组来保存八点坐标
    public double[][] eightCoordinate;
    // 定义保存x范围的数组
    public double[] xRange;
    // 定义保存y范围的数组
    public double[] yRange;
    // 定义保存z范围的数组
    public double[] zRange;


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

    // 定义一个方法以块左后下叫坐标(初步为0,0,0,)为基准,自动初始化八点坐标和范围(该方法在前面9个变量赋值完成后调用)
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

    // 克隆方法(用不到)
    public Block cloneObj(){
        Block block = new Block(this.typeNumberOfBox);
        block.complexLevel = this.complexLevel;
        block.typeNumberOfBox = this.typeNumberOfBox;
        block.blockLenght = this.blockLenght;
        block.blockWidth = this.blockWidth;
        block.blockHigh = this.blockHigh;
        block.viableLength = this.viableLength;
        block.viableWidth = this.viableWidth;
        block.blockVolum = this.blockVolum;
        for (int i = 0; i < this.neededBoxNumOfEachType.length; i++) {
            block.neededBoxNumOfEachType[i] = this.neededBoxNumOfEachType[i];
        }
        return block;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return typeNumberOfBox == block.typeNumberOfBox && Double.compare(block.blockLenght, blockLenght) == 0 && Double.compare(block.blockWidth, blockWidth) == 0 && Double.compare(block.blockHigh, blockHigh) == 0 && Double.compare(block.viableLength, viableLength) == 0 && Double.compare(block.viableWidth, viableWidth) == 0 && Double.compare(block.blockVolum, blockVolum) == 0 && Arrays.equals(neededBoxNumOfEachType, block.neededBoxNumOfEachType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(typeNumberOfBox, blockLenght, blockWidth, blockHigh, viableLength, viableWidth, blockVolum);
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
