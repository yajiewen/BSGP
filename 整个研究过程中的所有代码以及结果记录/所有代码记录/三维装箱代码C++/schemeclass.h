#include <iostream>
#include <vector>
#include "blockclass.h"
#include "spaceclass.h"
using namespace std;

// 放置类(放置块,放置空间)
class Put{
    public:
        Block block; // 放置的块
        Space space; // 放置的空间
        // 构造函数
        Put(Block block, Space space){
            this -> block = block;
            this -> space = space;
        }
};

class Scheme{
    public:
        double totalVolum; // 该方案的装填总体积
        vector<Put> putList; // put放置列表
        // 构造函数
        Scheme(){
            this -> totalVolum = 0.0;
        }
        Scheme(const Scheme &copy){
            totalVolum = copy.totalVolum;
            for(int i = 0; i < copy.putList.size(); i++){
                putList.push_back(copy.putList.at(i));
            }
        }
        // 把put 加入scheme
        void addPutToScheme(Put &put){
            totalVolum += put.block.blockVolum;
            putList.push_back(put);
        }
};