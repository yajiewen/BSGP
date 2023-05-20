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

    public Block(int typeNum){
        neededBoxNumOfEachType = new int[typeNum];
        typeNumberOfBox = typeNum;
    }

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
                "complexLevel=" + complexLevel +
                ", neededBoxNumOfEachType=" + Arrays.toString(neededBoxNumOfEachType) +
                ", typeNumberOfBox=" + typeNumberOfBox +
                ", blockLenght=" + blockLenght +
                ", blockWidth=" + blockWidth +
                ", blockHigh=" + blockHigh +
                ", viableLength=" + viableLength +
                ", viableWidth=" + viableWidth +
                ", blockVolum=" + blockVolum +
                '}';
    }
}
