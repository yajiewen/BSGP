package problem;
/**
 * @Author yajiewen
 * @Date 2022-03-09 12-10-30
 * @Description
*/ 
public class Box {
    public int boxType; // 箱子的类别
    public int boxNumber; // 该类别的箱子的数目
    public double boxLength; // 箱子的长
    public double boxWidth; // 箱子的宽
    public double boxHigh; // 箱子的高度
    public int isXvertical; // 长是否可竖直放置
    public int isYvertical; // 宽是否可竖直放置
    public int isZvertical; // 高是否可竖直放置

    @Override
    public String toString() {
        return "Box{" +
                "boxType=" + boxType +
                ", boxNumber=" + boxNumber +
                ", boxLength=" + boxLength +
                ", boxWidth=" + boxWidth +
                ", boxHigh=" + boxHigh +
                ", isXvertical=" + isXvertical +
                ", isYvertical=" + isYvertical +
                ", isZvertical=" + isZvertical +
                '}';
    }
}
