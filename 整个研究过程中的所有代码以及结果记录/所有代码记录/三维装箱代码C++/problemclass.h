#include "boxclass.h"
#include <iostream>
using namespace std;
class Problem{
    public:
        double containerLength; // 该问题中容器的长度
        double containerWidth;  // 该问题中容器的宽度
        double containerHigh;   // 该问题中容器的高度
        int typeNumberOfBox; //箱子的类别数
        BoxTypeInfo * boxTypeInfoList; // 每种类别的箱子的信息构成的列表

        void showProblemInfo(){
            cout << "======该问题信息======" << endl;
            cout << "容器的长度:" << containerLength << endl;
            cout << "容器的宽度:" << containerWidth << endl;
            cout << "容器的高度:" << containerHigh << endl;
            cout << "箱子类别数:" << typeNumberOfBox << endl;
            for(int i = 0; i < typeNumberOfBox; i++){
                boxTypeInfoList[i].showBoxTypeInfo();
            }
        }
};
