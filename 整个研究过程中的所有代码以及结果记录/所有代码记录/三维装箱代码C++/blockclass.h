#include <iostream>
using namespace std;

// 简单块
// class SimpleBlock{
//     public:
//         int boxType; // 构成该简单块的箱子类别
//         int boxNum; // 构成该简单块的箱子的数目
//         int boxNumOfX; // x方项上该箱子的数目
//         int boxNumOfY; // y方项上该箱子的数目
//         int boxNumOfZ; // z方项上该箱子的数目
//         double blockLength; // 块的长度
//         double blockWidth; // 块的宽度
//         double blockHigh; // 块的高度
//         double blockVolum; // 块的体积

//         void showSimpleBlockInfo(){
//             cout << "构成该简单块的箱子的类别:" << boxType << endl;
//             cout << "构成该简单块的箱子的数目:" << boxNum << endl;
//             cout << "x方项上该箱子的数目:" << boxNumOfX << endl;
//             cout << "y方项上该箱子的数目:" << boxNumOfY << endl;
//             cout << "z方项上该箱子的数目:" << boxNumOfZ << endl;
//             cout << "块的长度:" << blockLength << endl;
//             cout << "块的宽度:" << blockWidth << endl;
//             cout << "块的高度:" << blockHigh << endl;
//             cout << "块的体积:" << blockVolum << endl;
//         }
// };

// 复杂快
class Block{
    public:
        int complexLevel; // 复杂程度
        int *neededBoxNumOfEachType; // 该复杂快所需要的各种类别箱子的数目(下标比表示类别)
        int typeNumberOfBox; // 用于遍历
        double blockLenght; // 复杂块的长度
        double blockWidth; // 复杂块的宽度
        double blockHigh; // 复杂快的高度
        double viableLength; // 可行域的长度
        double viableWidth; // 可行域的宽度
        double blockVolum; // 复杂快的体积(指块种有效装填的体积)
        Block(){
            
        }
        // 定义拷贝构造函数
        Block(const Block &copy){
            complexLevel = copy.complexLevel;
            typeNumberOfBox = copy.typeNumberOfBox;
            neededBoxNumOfEachType = new int[typeNumberOfBox];
            for(int i = 0; i < copy.typeNumberOfBox; i++){
                neededBoxNumOfEachType[i] = copy.neededBoxNumOfEachType[i];
            }
            blockLenght = copy.blockLenght;
            blockHigh = copy.blockHigh;
            blockWidth = copy.blockWidth;
            viableLength = copy.viableLength;
            viableWidth = copy.viableWidth;
            blockVolum = copy.blockVolum;
        }

        void showBlockInfo(){
            cout << "======块的信息======" << endl;
            cout << "该块的复杂程度:" << complexLevel << endl;
            cout << "块的长度:" << blockLenght << endl;
            cout << "块的宽度:" << blockWidth << endl;
            cout << "块的高度:" << blockHigh << endl;
            cout << "块的体积:" << blockVolum << endl;
            cout << "可行域的长度:" << viableLength << endl;
            cout << "可行域的宽度:" << viableWidth << endl;
            for(int i = 0; i < typeNumberOfBox; i++){
                
                if(neededBoxNumOfEachType[i] != 0){
                    cout << "需要类别为" << i + 1 << "的箱子 " << neededBoxNumOfEachType[i] << " 个" << endl;
                }
            }
            cout << "end" << endl;
        }
};
