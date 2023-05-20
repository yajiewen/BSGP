#include <iostream>
using namespace std;

class Ps{
    public:
        int *psList; // 存放每一阶段选择的块的下标
        int psLength; // 存放ps当前阶段长度
        Ps(int maxSeq){
            psList = new int[maxSeq];
            psLength = 0;
            for(int i = 0; i < maxSeq; i++){
                psList[i] = 0;
            }
        }
};


class Ps2{
    public:
        int *psList; // 存放每一阶段选择的块的下标
        int psLength; // 存放ps当前阶段长度
        Ps2(){
            psList = new int[100000];
            psLength = 0;
            for(int i = 0; i < 100000; i++){
                psList[i] = 0;
            }
        }
        Ps2(const Ps2 &copy){
            psLength = copy.psLength;
            psList = new int[100000];
            for(int i = 0; i < 100000; i++){
                psList[i] = copy.psList[i];
            }
        }
        ~Ps2(){
            delete psList;
        }
};