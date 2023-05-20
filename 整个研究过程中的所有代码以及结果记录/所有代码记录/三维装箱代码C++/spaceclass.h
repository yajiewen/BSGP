#include <iostream>
using namespace std;

class Space{
    public:
        double locationX; // 空间左后下角的X坐标
        double locationY; // 空间左后下角的Y坐标
        double locationZ; // 空间左后下角的Z坐标
        double spaceLength; // 空间的长
        double spaceWidth; // 空间的宽
        double spaceHigh; // 空间的高

        bool isHasTransferSpace; // 是否含有可转移空间

        double transferSpaceLocationX; // 可转移空间左后下角的X坐标
        double transferSpaceLocationY; // 可转移空间左后下角Y坐标
        double transferSpaceLocationZ; // 可转移空间左后下角Z坐标
        double transferSpaceLength; // 可转移空间的长
        double transferSpaceWidth; // 可转移空间的宽
        double transferspaceHigh; // 可转移空间的高
        Space(){};
        Space(const Space &copy){
            locationX = copy.locationX;
            locationY = copy.locationY;
            locationZ = copy.locationZ;
            spaceLength = copy.spaceLength;
            spaceWidth = copy.spaceWidth;
            spaceHigh = copy.spaceHigh;
            isHasTransferSpace = copy.isHasTransferSpace;
            transferSpaceLocationX = copy.transferSpaceLocationX;
            transferSpaceLocationY = copy.transferSpaceLocationY;
            transferSpaceLocationZ = copy.transferSpaceLocationZ;
            transferSpaceLength = copy.transferSpaceLength;
            transferspaceHigh = copy.transferspaceHigh;
            transferSpaceWidth = copy.transferSpaceWidth;
        }
        void showSpaceInfo(){
            cout << "======空间信息======" << endl;
            cout << "空间坐标为: (" << locationX <<", " << locationY << ", " << locationZ << ")" << endl;
            cout << "空间长宽高: (" << spaceLength <<", " << spaceWidth << ", " << spaceHigh << ")" << endl;
            if(isHasTransferSpace){
                cout << "有可转移空间" << endl;
                cout << "可转移空间坐标: (" << transferSpaceLocationX <<", " << transferSpaceLocationY << ", " << transferSpaceLocationZ << ")" << endl;
                cout << "可转移空间长宽高: (" << transferSpaceLength <<", " << transferSpaceWidth << ", " << transferspaceHigh << ")" << endl;
            }else{
                cout << "无可转移空间" << endl;
            }
        }
};