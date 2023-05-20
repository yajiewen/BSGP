#include <iostream>
using namespace std;
class BoxTypeInfo{
    public:
        double boxLength; // 箱子的长
        double boxWidht; // 箱子的宽
        double boxHigh; // 箱子的高度
        int boxType; // 箱子的类别
        int boxNumber; // 该类别的箱子的数目
        int isXvertical; // 长是否可竖直放置
        int isYvertical; // 宽是否可竖直放置
        int isZvertical; // 高是否可竖直放置
        BoxTypeInfo(){
            
        }
        BoxTypeInfo(const BoxTypeInfo &copy){
            boxLength = copy.boxLength;
            boxWidht = copy.boxWidht;
            boxHigh = copy.boxHigh;
            boxType = copy.boxType;
            boxNumber = copy.boxNumber;
            isXvertical = copy.isXvertical;
            isYvertical = copy.isYvertical;
            isZvertical = copy.isZvertical;
        }
        void showBoxTypeInfo(){
            cout << "------箱子类别信息------" << endl;
            cout << "箱子的类别是:" << boxType << endl;
            cout << "该类别箱子数目:" << boxNumber << endl;
            cout << "箱子的长度:" << boxLength << endl;
            cout << "长度方项是否可垂直摆放:" << isXvertical << endl;
            cout << "箱子的宽度:" << boxWidht << endl;
            cout << "宽度方项是否可以垂直摆放:" << isYvertical << endl;
            cout << "箱子的高度是:" << boxHigh << endl;
            cout << "高度方项是否可以垂直摆放:" << isZvertical << endl;
        }
};