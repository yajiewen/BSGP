package com.packing3d.datastructure.space;
/**
 * @Author yajiewen
 * @Date 2022-03-12 17-18-51
 * @Description
*/ 
public class Space{
    public double locationX; // 空间左后下角的X坐标
    public double locationY; // 空间左后下角的Y坐标
    public double locationZ; // 空间左后下角的Z坐标
    public double spaceLength; // 空间的长
    public double spaceWidth; // 空间的宽
    public double spaceHigh; // 空间的高

    public boolean isHasTransferSpace; // 是否含有可转移空间

    public double transferSpaceLocationX; // 可转移空间左后下角的X坐标
    public double transferSpaceLocationY; // 可转移空间左后下角Y坐标
    public double transferSpaceLocationZ; // 可转移空间左后下角Z坐标
    public double transferSpaceLength; // 可转移空间的长
    public double transferSpaceWidth; // 可转移空间的宽
    public double transferspaceHigh; // 可转移空间的高

    public Space(double locationX, double locationY, double locationZ, double spaceLength, double spaceWidth, double spaceHigh, boolean isHasTransferSpace) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.locationZ = locationZ;
        this.spaceLength = spaceLength;
        this.spaceWidth = spaceWidth;
        this.spaceHigh = spaceHigh;
        this.isHasTransferSpace = isHasTransferSpace;
    }

    public Space(double locationX, double locationY, double locationZ, double spaceLength, double spaceWidth, double spaceHigh, boolean isHasTransferSpace, double transferSpaceLocationX, double transferSpaceLocationY, double transferSpaceLocationZ, double transferSpaceLength, double transferSpaceWidth, double transferspaceHigh) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.locationZ = locationZ;
        this.spaceLength = spaceLength;
        this.spaceWidth = spaceWidth;
        this.spaceHigh = spaceHigh;
        this.isHasTransferSpace = isHasTransferSpace;
        this.transferSpaceLocationX = transferSpaceLocationX;
        this.transferSpaceLocationY = transferSpaceLocationY;
        this.transferSpaceLocationZ = transferSpaceLocationZ;
        this.transferSpaceLength = transferSpaceLength;
        this.transferSpaceWidth = transferSpaceWidth;
        this.transferspaceHigh = transferspaceHigh;
    }
    // 克隆方法(用不到)
    public Space cloneObj(){
        Space space  = new Space(this.locationX,this.locationY,this.locationZ, this.spaceLength, this.spaceWidth, this.spaceHigh,this.isHasTransferSpace,this.transferSpaceLocationX,this.transferSpaceLocationY,this.transferSpaceLocationZ,this.transferSpaceLength,this.transferSpaceWidth,this.transferspaceHigh);
        return space;
    }
    @Override
    public String toString() {
        return "Space{" +
                "locationX=" + locationX +
                ", locationY=" + locationY +
                ", locationZ=" + locationZ +
                ", spaceLength=" + spaceLength +
                ", spaceWidth=" + spaceWidth +
                ", spaceHigh=" + spaceHigh +
                ", isHasTransferSpace=" + isHasTransferSpace +
                ", transferSpaceLocationX=" + transferSpaceLocationX +
                ", transferSpaceLocationY=" + transferSpaceLocationY +
                ", transferSpaceLocationZ=" + transferSpaceLocationZ +
                ", transferSpaceLength=" + transferSpaceLength +
                ", transferSpaceWidth=" + transferSpaceWidth +
                ", transferspaceHigh=" + transferspaceHigh +
                '}';
    }
}
