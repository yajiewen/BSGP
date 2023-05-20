#include <iostream>
#include <vector>
#include <algorithm>
#include <stack>
#include <cmath>
#include <random>
#include <ctime>
// #include "spaceclass.h"
#include "filetool.h"
// #include "blockclass.h"
#include "schemeclass.h"
#include "psclass.h"

using namespace std;
// 函数的声明
vector<Block> getSimpleBlockList(Problem *problem);                                         // 生成简单块列表
vector<Block> getComplexBlockList(Problem *problem);                                        // 生成复杂块列表
Block *getXdirectionComplexBlock(Block *a, Block *b, Problem *problem);                     // 生成X方项复杂块
Block *getYdirectionComplexBlock(Block *a, Block *b, Problem *problem);                     // 生成Y方项复杂块
Block *getZdirectionComplexBlock(Block *a, Block *b, Problem *problem);                     // 生成Z方项复杂块
void delDuplicatedBlock(vector<Block> &blockList);                                          //删除块列表种重复的块
int isBlockEqual(Block *a, Block *b);                                                       // 比较两个块是否相同
bool cmpRule(Block &a, Block &b);                                                           // vector 的排序规则
vector<Block> searchViableBlock(Space &space, vector<Block> &blockList, int *avalNumOfBox); // 查找可以放入空间的可行块表
void reNewSpaceStack(stack<Space> &spaceStack, Space &space, Block &block);                 // 求剩余空间并更新空间栈
void spaceTransfer(stack<Space> &spaceStack);                                               // 空间转移算法
Scheme getSchemeByHeuristic(vector<Block> &blockList, Problem *problem, Ps *ps);            // 通过启发式算法获取方案
Scheme algorithmSA(vector<Block> &blockList, Problem *problem, Ps *ps);                     // 混合模拟启发式算法
void psCopyAtoB(Ps *a, Ps *b);                                                              // ps 的赋值操作
void judge(Block &block, Problem *problem);
int maxV(int a, int b);
double maxV(double a, double b);
int minV(int a, int b);
double minV(double a, double b);

// 全局变量
double MinFillRate = 0.98; //复合块种要达到的最小填充量
double MinAreaRate = 0.96; // 复合块顶部最小可行矩阵面积占符合块顶部面积比例
int MaxLevel = 5;          // 复合块的最大复杂度
int MaxBlocks = 10000;     // 生成复合块的最大块数
int MaxSeq = 200;          // ps 的长度
int MaxSelect = 47;        // 每个阶段的块选择数最大为

double Ts = 1.0;         //初始温度
double Tf = 0.01;        // 结束温度
double Dt = 0.98;        // 降温系数
double LengthOfMkF = 10; // 马尔可夫链长度
bool isLiner = true;     // 选择线性降温还是非线性降温

int main()
{
    FileReader fileReader("C:\\userfile\\论文\\装箱问题\\三维装箱\\经典标准数据集\\七个数据集以及说明文档\\br1.txt");
    Problem *problemList = fileReader.getProblems();
    // problemList[0].showProblemInfo(); // 输出问题
    // vector<Block> simpleBlockList  = getSimpleBlockList(&problemList[0]); // 简单块表输出
    // for(int i = 0; i < simpleBlockList.size(); i++){
    //     simpleBlockList.at(i).showBlockInfo();
    // }
    // getchar();
    // vector<Block> complexBlockList = getComplexBlockList(&problemList[0]);
    // sort(complexBlockList.begin(), complexBlockList.end(), cmpRule);
    // for(int i = 0; i < complexBlockList.size(); i++){
    //     complexBlockList.at(i).showBlockInfo();
    // }
    // getchar();
    //遍历每一个问题
    double totalFillRate = 0.0; // 定义总的装填效率
    for (int i = 0; i < 2; i++)
    {
        // 生成复杂快表
        vector<Block> complexBlockList = getComplexBlockList(&problemList[i]);
        // 对复杂快表进行按体积从大到小排序
        sort(complexBlockList.begin(), complexBlockList.end(), cmpRule);
        // 生成ps
        Ps *ps = new Ps(MaxSeq);
        //开始启发式算法
        //scheme = getSchemeByHeuristic(complexBlockList,&problemList[i],ps);
        // 混合模拟启发式
        Scheme scheme = algorithmSA(complexBlockList, &problemList[i], ps);
        cout << "problem " << i << " 最佳装填率:" << scheme.totalVolum / (problemList[i].containerHigh * problemList[i].containerLength * problemList[i].containerWidth) << endl;
        totalFillRate += scheme.totalVolum / (problemList[i].containerHigh * problemList[i].containerLength * problemList[i].containerWidth);
    }
    cout << "br 所有问题平均装填率为:" << totalFillRate / 100 << endl;
    getchar();
    return 0;
}
// 生产简单块列表
vector<Block> getSimpleBlockList(Problem *problem)
{
    // 先生成一个vector
    vector<Block> simpleBlockList;
    // 遍历问题种的每一种箱子
    for (int i = 0; i < problem->typeNumberOfBox; i++)
    {
        // 遍历这种箱子的每一个
        for (int j = 1; j <= problem->boxTypeInfoList[i].boxNumber; j++)
        {
            for (int k = 1; k <= problem->boxTypeInfoList[i].boxNumber / j; k++)
            {
                for (int l = 1; l <= problem->boxTypeInfoList[i].boxNumber / j / k; l++)
                {
                    // 开始生成简单块
                    if (problem->boxTypeInfoList[i].boxLength * j <= problem->containerLength && problem->boxTypeInfoList[i].boxWidht * k <= problem->containerWidth && problem->boxTypeInfoList[i].boxHigh * l <= problem->containerHigh)
                    {
                        // 创建一个简单块
                        Block simpleBlock;
                        simpleBlock.complexLevel = 0;
                        simpleBlock.neededBoxNumOfEachType = new int[problem->typeNumberOfBox];
                        for (int m = 0; m < problem->typeNumberOfBox; m++)
                        {
                            if (m == i)
                            {
                                simpleBlock.neededBoxNumOfEachType[m] = j * k * l; // 该类别所需的箱子数目
                            }
                            else
                            {
                                simpleBlock.neededBoxNumOfEachType[m] = 0;
                            }
                        }
                        simpleBlock.blockLenght = problem->boxTypeInfoList[i].boxLength * j;
                        simpleBlock.blockWidth = problem->boxTypeInfoList[i].boxWidht * k;
                        simpleBlock.blockHigh = problem->boxTypeInfoList[i].boxHigh * l;
                        simpleBlock.viableLength = simpleBlock.blockLenght;
                        simpleBlock.viableWidth = simpleBlock.blockWidth;
                        simpleBlock.blockVolum = simpleBlock.blockLenght * simpleBlock.blockWidth * simpleBlock.blockHigh;
                        simpleBlock.typeNumberOfBox = problem->typeNumberOfBox;
                        //judge(simpleBlock,problem);
                        // 把该简单快加入简单块表
                        simpleBlockList.push_back(simpleBlock);
                        delete simpleBlock.neededBoxNumOfEachType;
                    }
                }
            }
        }
    }
    return simpleBlockList;
}

//生产复杂快列表
vector<Block> getComplexBlockList(Problem *problem)
{
    // 生成简单块列表
    vector<Block> simpleBlockList = getSimpleBlockList(problem);
    // 开始生成复杂块表
    for (int level = 0; level < MaxLevel; level++)
    {
        // 创建一个新复合块表
        vector<Block> newBlockList;
        if (simpleBlockList.size() >= MaxBlocks)
        {
            break;
        }
        for (int i = 0; i < simpleBlockList.size(); i++)
        {
            if (simpleBlockList.size() >= MaxBlocks)
            {
                break;
            }
            Block *a = &simpleBlockList.at(i);
            for (int j = i + 1; j < simpleBlockList.size(); j++)
            {
                if (simpleBlockList.size() >= MaxBlocks)
                {
                    break;
                }
                Block *b = &simpleBlockList.at(j);
                if (a->complexLevel < level && b->complexLevel < level)
                {
                    // 开始判断各个方项是否满足组合要求
                    // x方项
                    Block *c = getXdirectionComplexBlock(a, b, problem);
                    if (c != nullptr)
                    {
                        newBlockList.push_back(*c);
                        delete c->neededBoxNumOfEachType;
                        delete c;
                    }
                    // y方向
                    c = getYdirectionComplexBlock(a, b, problem);
                    if (c != nullptr)
                    {
                        newBlockList.push_back(*c);
                        delete c->neededBoxNumOfEachType;
                        delete c;
                    }
                    // z方向
                    c = getZdirectionComplexBlock(a, b, problem);
                    if (c != nullptr)
                    {
                        newBlockList.push_back(*c);
                        delete c->neededBoxNumOfEachType;
                        delete c;
                    }
                }
            }
        }
        simpleBlockList.insert(simpleBlockList.end(), newBlockList.begin(), newBlockList.end());
        delDuplicatedBlock(simpleBlockList); // 每次level结束删除重复块
    }
    return simpleBlockList;
}

// 在x方项上生成新复杂快
Block *getXdirectionComplexBlock(Block *a, Block *b, Problem *problem)
{
    Block *c = nullptr;
    // 先计算复合块的各种参数 然后再判断是否能生成
    int compoundComplexLevel = maxV(a->complexLevel, b->complexLevel) + 1;
    int *compoundNeededBoxNumOfEachType = new int[problem->typeNumberOfBox]; // 该复杂快所需要的各种类别箱子的数目(下标比表示类别)
    int compoundTypeNumberOfBox = problem->typeNumberOfBox;                  // 用于遍历
    // 计算所需的各种类别的箱子数目 是否超过总箱子数目
    int isChaoguo = 0;
    for (int i = 0; i < compoundTypeNumberOfBox; i++)
    {
        if (a->neededBoxNumOfEachType[i] + b->neededBoxNumOfEachType[i] > problem->boxTypeInfoList[i].boxNumber)
        {
            isChaoguo = 1;
            break;
        }
        else
        {
            compoundNeededBoxNumOfEachType[i] = a->neededBoxNumOfEachType[i] + b->neededBoxNumOfEachType[i];
        }
    }
    if (isChaoguo == 1)
    {
        delete compoundNeededBoxNumOfEachType;
        return c;
    }
    double compoundBlockLenght = a->blockLenght + b->blockLenght;      // 复杂块的长度
    double compoundBlockWidth = maxV(a->blockWidth, b->blockWidth);    // 复杂块的宽度
    double compoundBlockHigh = a->blockHigh;                           // 复杂快的高度
    double compoundViableLength = a->viableLength + b->viableLength;   // 可行域的长度
    double compoundViableWidth = minV(a->viableWidth, b->viableWidth); // 可行域的宽度
    double compoundBlockVolum = a->blockVolum + b->blockVolum;         // 复杂快的体积

    double fillRate = compoundBlockVolum / (compoundBlockLenght * compoundBlockHigh * compoundBlockWidth);
    double viableRate = (compoundViableLength * compoundViableWidth) / (compoundBlockLenght * compoundBlockWidth);

    // 判断复合块是否超出容器大小
    if (compoundBlockLenght <= problem->containerLength && compoundBlockWidth <= problem->containerWidth && compoundBlockHigh <= problem->containerHigh)
    {
        // 判断填充率和最小可行域是否满足要求是否满足要求
        if (fillRate >= MinFillRate && viableRate >= MinAreaRate)
        {
            // 判断是否满足可行放置矩阵条件
            if (a->viableLength == a->blockLenght && b->viableLength == b->blockLenght && a->blockHigh == b->blockHigh)
            {
                // 创建复合块c
                c = new Block();
                // 填充c的相关信息
                c->complexLevel = compoundComplexLevel;
                c->neededBoxNumOfEachType = compoundNeededBoxNumOfEachType;
                c->typeNumberOfBox = compoundTypeNumberOfBox;
                c->blockLenght = compoundBlockLenght;
                c->blockWidth = compoundBlockWidth;
                c->blockHigh = compoundBlockHigh;
                c->viableLength = compoundViableLength;
                c->viableWidth = compoundViableWidth;
                c->blockVolum = compoundBlockVolum;
            }
        }
    }
    return c;
}

// 生成Y方项复杂块
Block *getYdirectionComplexBlock(Block *a, Block *b, Problem *problem)
{
    Block *c = nullptr;
    // 先计算复合块的各种参数 然后再判断是否能生成
    int compoundComplexLevel = maxV(a->complexLevel, b->complexLevel) + 1;
    int *compoundNeededBoxNumOfEachType = new int[problem->typeNumberOfBox]; // 该复杂快所需要的各种类别箱子的数目(下标比表示类别)
    int compoundTypeNumberOfBox = problem->typeNumberOfBox;                  // 用于遍历
    // 计算所需的各种类别的箱子数目 是否超过总箱子数目
    int isChaoguo = 0;
    for (int i = 0; i < compoundTypeNumberOfBox; i++)
    {
        // cout << "anum:" << a -> neededBoxNumOfEachType[i] << "bnum:" << b ->neededBoxNumOfEachType[i] << "hasnum:" << problem -> boxTypeInfoList[i].boxNumber << endl;
        if (a->neededBoxNumOfEachType[i] + b->neededBoxNumOfEachType[i] > problem->boxTypeInfoList[i].boxNumber)
        {
            isChaoguo = 1;
            break;
        }
        else
        {
            compoundNeededBoxNumOfEachType[i] = a->neededBoxNumOfEachType[i] + b->neededBoxNumOfEachType[i];
        }
    }
    if (isChaoguo == 1)
    {
        delete compoundNeededBoxNumOfEachType;
        return c;
    }
    double compoundBlockLenght = maxV(a->blockLenght, b->blockLenght);    // 复杂块的长度
    double compoundBlockWidth = a->blockWidth + b->blockWidth;            // 复杂块的宽度
    double compoundBlockHigh = a->blockHigh;                              // 复杂快的高度
    double compoundViableLength = minV(a->viableLength, b->viableLength); // 可行域的长度
    double compoundViableWidth = a->viableWidth + b->viableWidth;         // 可行域的宽度
    double compoundBlockVolum = a->blockVolum + b->blockVolum;            // 复杂快的体积

    double fillRate = compoundBlockVolum / (compoundBlockLenght * compoundBlockHigh * compoundBlockWidth);
    double viableRate = (compoundViableLength * compoundViableWidth) / (compoundBlockLenght * compoundBlockWidth);

    // 判断复合块是否超出容器大小
    if (compoundBlockLenght <= problem->containerLength && compoundBlockWidth <= problem->containerWidth && compoundBlockHigh <= problem->containerHigh)
    {
        // 判断填充率和最小可行域是否满足要求是否满足要求
        if (fillRate >= MinFillRate && viableRate >= MinAreaRate)
        {
            // 判断是否满足可行放置矩阵条件
            if (a->viableWidth == a->blockWidth && b->viableWidth == b->blockWidth && a->blockHigh == b->blockHigh)
            {
                // 创建复合块c
                c = new Block();
                // 填充c的相关信息
                c->complexLevel = compoundComplexLevel;
                c->neededBoxNumOfEachType = compoundNeededBoxNumOfEachType;
                c->typeNumberOfBox = compoundTypeNumberOfBox;
                c->blockLenght = compoundBlockLenght;
                c->blockWidth = compoundBlockWidth;
                c->blockHigh = compoundBlockHigh;
                c->viableLength = compoundViableLength;
                c->viableWidth = compoundViableWidth;
                c->blockVolum = compoundBlockVolum;
            }
        }
    }
    return c;
}
// 生成Z方项复杂块
Block *getZdirectionComplexBlock(Block *a, Block *b, Problem *problem)
{
    //judge(*a,problem);
    //judge(*b,problem);
    Block *c = nullptr;
    // 先计算复合块的各种参数 然后再判断是否能生成
    int compoundComplexLevel = maxV(a->complexLevel, b->complexLevel) + 1;
    int *compoundNeededBoxNumOfEachType = new int[problem->typeNumberOfBox]; // 该复杂快所需要的各种类别箱子的数目(下标比表示类别)
    int compoundTypeNumberOfBox = problem->typeNumberOfBox;                  // 用于遍历
    // 计算所需的各种类别的箱子数目 是否超过总箱子数目
    int isChaoguo = 0;
    for (int i = 0; i < compoundTypeNumberOfBox; i++)
    {
        if (a->neededBoxNumOfEachType[i] + b->neededBoxNumOfEachType[i] > problem->boxTypeInfoList[i].boxNumber)
        {
            isChaoguo = 1;
            break;
        }
        else
        {
            compoundNeededBoxNumOfEachType[i] = a->neededBoxNumOfEachType[i] + b->neededBoxNumOfEachType[i];
        }
    }
    if (isChaoguo == 1)
    {
        delete compoundNeededBoxNumOfEachType;
        return c;
    }
    double compoundBlockLenght = a->blockLenght;               // 复杂块的长度
    double compoundBlockWidth = a->blockWidth;                 // 复杂块的宽度
    double compoundBlockHigh = a->blockHigh + b->blockHigh;    // 复杂快的高度
    double compoundViableLength = b->viableLength;             // 可行域的长度
    double compoundViableWidth = b->viableWidth;               // 可行域的宽度
    double compoundBlockVolum = a->blockVolum + b->blockVolum; // 复杂快的体积

    double fillRate = compoundBlockVolum / (compoundBlockLenght * compoundBlockHigh * compoundBlockWidth);
    double viableRate = (compoundViableLength * compoundViableWidth) / (compoundBlockLenght * compoundBlockWidth);

    // 判断复合块是否超出容器大小
    if (compoundBlockLenght <= problem->containerLength && compoundBlockWidth <= problem->containerWidth && compoundBlockHigh <= problem->containerHigh)
    {
        // 判断填充率和最小可行域是否满足要求是否满足要求
        if (fillRate >= MinFillRate && viableRate >= MinAreaRate)
        {
            // 判断是否满足可行放置矩阵条件
            if (a->viableLength >= b->blockLenght && a->viableWidth >= b->blockWidth)
            {
                // 创建复合块c
                c = new Block();
                // 填充c的相关信息
                c->complexLevel = compoundComplexLevel;
                c->neededBoxNumOfEachType = compoundNeededBoxNumOfEachType;
                c->typeNumberOfBox = compoundTypeNumberOfBox;
                c->blockLenght = compoundBlockLenght;
                c->blockWidth = compoundBlockWidth;
                c->blockHigh = compoundBlockHigh;
                c->viableLength = compoundViableLength;
                c->viableWidth = compoundViableWidth;
                c->blockVolum = compoundBlockVolum;
                // judge(*c,problem);
                // cout << "a:" <<endl;
                // a -> showBlockInfo();
                // cout << "b:" <<endl;
                // b -> showBlockInfo();
            }
        }
    }
    return c;
}

// max int 函数实现
int maxV(int a, int b)
{
    if (a >= b)
    {
        return a;
    }
    else
    {
        return b;
    }
}

// max double 函数实现
double maxV(double a, double b)
{
    if (a >= b)
    {
        return a;
    }
    else
    {
        return b;
    }
}

// min int 实现
int minV(int a, int b)
{
    if (a <= b)
    {
        return a;
    }
    else
    {
        return b;
    }
}

// min double 实现
double minV(double a, double b)
{
    if (a <= b)
    {
        return a;
    }
    else
    {
        return b;
    }
}

// 删除块列表种重复的块
void delDuplicatedBlock(vector<Block> &blockList)
{
    for (int i = 0; i < blockList.size(); i++)
    {
        Block *block1 = &blockList.at(i);
        for (int j = i + 1; j < blockList.size(); j++)
        {
            Block *block2 = &blockList.at(j);
            if (isBlockEqual(block1, block2))
            {
                // 两个块相同则删除一个
                blockList.erase(blockList.begin() + j);
                // cout << "删除了一个重复块" << endl;
            }
        }
    }
}

// 比较两个块是否相同
int isBlockEqual(Block *a, Block *b)
{
    if (a->complexLevel == b->complexLevel && a->blockLenght == b->blockLenght && a->blockWidth == b->blockWidth && a->blockHigh == b->blockHigh)
    {
        if (a->viableLength == b->viableLength && a->viableWidth == b->viableWidth && a->blockVolum == b->blockVolum)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    else
    {
        return 0;
    }
}

// vector 的排序规则
bool cmpRule(Block &a, Block &b)
{
    return a.blockVolum > b.blockVolum;
}

// 查找可以放入空间的可行块表
vector<Block> searchViableBlock(Space &space, vector<Block> &blockList, int *avalNumOfBox)
{
    vector<Block> viableBlock;
    for (int i = 0; i < blockList.size(); i++)
    {
        Block block = blockList.at(i);
        // 判断是否满足空间大小
        if (block.blockLenght <= space.spaceLength && block.blockWidth <= space.spaceWidth && block.blockHigh <= space.spaceHigh)
        {
            // 判断剩余箱子数目是否满足
            bool isOk = true;
            for (int i = 0; i < block.typeNumberOfBox; i++)
            {
                if (avalNumOfBox[i] < block.neededBoxNumOfEachType[i])
                {
                    isOk = false;
                    break;
                }
            }
            if (isOk)
            {
                viableBlock.push_back(block);
            }
        }
    }
    return viableBlock;
}

// 求剩余空间并更新空间栈
void reNewSpaceStack(stack<Space> &spaceStack, Space &space, Block &block)
{
    spaceStack.pop(); // 刚刚被使用的空间出栈
    // 求出顶部空间
    Space spaceZ;
    Space spaceX; // x方项上的剩余空间
    Space spaceY; // y方项上的剩余空间
    spaceZ.locationX = space.locationX;
    spaceZ.locationY = space.locationY;
    spaceZ.locationZ = space.locationZ + block.blockHigh;
    spaceZ.spaceLength = block.viableLength;
    spaceZ.spaceWidth = block.viableWidth;
    spaceZ.spaceHigh = space.spaceHigh - block.blockHigh;
    spaceZ.isHasTransferSpace = false;
    if (spaceZ.spaceLength <= 587 && spaceZ.spaceHigh <= 220 && spaceZ.spaceWidth <= 233)
    {
        //cout << "z空间正确" << endl;
    }
    else
    {
        cout << "z空间错误" << endl;
        spaceZ.showSpaceInfo();
        block.showBlockInfo();
    }
    // 判断右左剩余空间 哪个的长度长
    double mx = space.spaceLength - block.blockLenght;
    double my = space.spaceWidth - block.blockWidth;
    if (mx > my)
    { // 可转移空间在x方项
        spaceX.locationX = space.locationX + block.blockLenght;
        spaceX.locationY = space.locationY;
        spaceX.locationZ = space.locationZ;
        spaceX.spaceLength = space.spaceLength - block.blockLenght;
        spaceX.spaceWidth = space.spaceWidth;
        spaceX.spaceHigh = space.spaceHigh;
        spaceX.isHasTransferSpace = true;
        if (spaceX.spaceLength <= 587 && spaceX.spaceHigh <= 220 && spaceX.spaceWidth <= 233)
        {
            //cout << "x空间正确" << endl;
        }
        else
        {
            cout << "x空间错误" << endl;
        }
        spaceX.transferSpaceLocationX = spaceX.locationX;
        spaceX.transferSpaceLocationY = spaceX.locationY + block.blockWidth;
        spaceX.transferSpaceLocationZ = spaceX.locationZ;
        spaceX.transferSpaceLength = spaceX.spaceLength;
        spaceX.transferSpaceWidth = spaceX.spaceWidth - block.blockWidth;
        spaceX.transferspaceHigh = spaceX.spaceHigh;
        // Y方项
        spaceY.locationX = space.locationX;
        spaceY.locationY = space.locationY + block.blockWidth;
        spaceY.locationZ = space.locationZ;
        spaceY.spaceLength = block.blockLenght;
        spaceY.spaceWidth = space.spaceWidth - block.blockWidth;
        spaceY.spaceHigh = space.spaceHigh;
        spaceY.isHasTransferSpace = false;
        if (spaceY.spaceLength <= 587 && spaceY.spaceHigh <= 220 && spaceY.spaceWidth <= 233)
        {
            //cout << "Y空间正确" << endl;
        }
        else
        {
            cout << "Y空间错误" << endl;
        }
        spaceStack.push(spaceZ);
        spaceStack.push(spaceY);
        spaceStack.push(spaceX);
    }
    else
    { // 可转移空间在Y轴方项
        spaceY.locationX = space.locationX;
        spaceY.locationY = space.locationY + block.blockWidth;
        spaceY.locationZ = space.locationZ;
        spaceY.spaceLength = space.spaceLength;
        spaceY.spaceWidth = space.spaceWidth - block.blockWidth;
        spaceY.spaceHigh = space.spaceHigh;
        spaceY.isHasTransferSpace = true;

        spaceY.transferSpaceLocationX = space.locationX + block.blockLenght;
        spaceY.transferSpaceLocationY = space.locationY + block.blockWidth;
        spaceY.transferSpaceLocationZ = space.locationZ;
        spaceY.transferSpaceLength = space.spaceLength - block.blockLenght;
        spaceY.transferSpaceWidth = space.spaceWidth - block.blockWidth;
        spaceY.transferspaceHigh = space.spaceHigh;

        // X 方项
        spaceX.locationX = space.locationX + block.blockLenght;
        spaceX.locationY = space.locationY;
        spaceX.locationZ = space.locationZ;
        spaceX.spaceLength = space.spaceLength - block.blockLenght;
        spaceX.spaceWidth = block.blockWidth;
        spaceX.spaceHigh = space.spaceHigh;
        spaceX.isHasTransferSpace = false;

        spaceStack.push(spaceZ);
        spaceStack.push(spaceX);
        spaceStack.push(spaceY);
    }
}

// 空间转移算法
void spaceTransfer(stack<Space> &spaceStack)
{
    Space space = spaceStack.top(); // 获取栈顶元素
    // 判断是否有可转移空间
    if (space.isHasTransferSpace)
    {
        spaceStack.pop();
        Space space2 = spaceStack.top();
        // 开始进行空间转移
        if (space2.locationX + space2.spaceLength == space.transferSpaceLocationX)
        {
            if (space2.locationY == space.transferSpaceLocationY && space2.locationZ == space.transferSpaceLocationZ)
            {
                if (space2.spaceHigh == space.transferspaceHigh && space2.spaceWidth == space.transferSpaceWidth)
                {
                    // 空间转移
                    space2.spaceLength += space.transferSpaceLength;
                    spaceStack.pop();
                    spaceStack.push(space2);
                    cout << "x方向转移成功" << endl;
                }
            }
        }
        else
        {
            if (space2.locationY + space2.spaceWidth == space.transferSpaceLocationY)
            {
                if (space2.locationX == space.transferSpaceLocationX && space2.locationZ == space.transferSpaceLocationZ)
                {
                    if (space2.spaceHigh == space.transferspaceHigh && space2.spaceLength == space.transferSpaceLength)
                    {
                        // 转移空间
                        space2.spaceWidth += space.transferSpaceWidth;
                        spaceStack.pop();
                        spaceStack.push(space2);
                        cout << "y方向转移成功" << endl;
                    }
                }
            }
        }
    }
    else
    {
        spaceStack.pop(); //出栈(舍去该空间)
    }
}

// 通过启发式算法获取方案
Scheme getSchemeByHeuristic(vector<Block> &blockList, Problem *problem, Ps *ps)
{
    // 记录每个类型的箱子的可用数目
    int *avalNumOfBox = new int[problem->typeNumberOfBox];
    for (int i = 0; i < problem->typeNumberOfBox; i++)
    {
        avalNumOfBox[i] = problem->boxTypeInfoList[i].boxNumber;
    }
    // 生成剩余空间栈
    stack<Space> spaceStack;
    // 生成初始空间并入栈
    Space space;
    space.spaceLength = problem->containerLength;
    space.spaceWidth = problem->containerWidth;
    space.spaceHigh = problem->containerHigh;
    space.locationX = 0.0;
    space.locationY = 0.0;
    space.locationZ = 0.0;
    space.isHasTransferSpace = false;
    spaceStack.push(space);

    // 创建一个方案
    Scheme scheme;
    int index = 0; // 记录第几次放置

    while (!spaceStack.empty())
    {
        Space space = spaceStack.top(); // 获取栈顶元素
        // 判断空间是否正确
        if (space.spaceLength <= problem->containerLength && space.spaceHigh <= problem->containerHigh && space.spaceWidth <= problem->containerWidth)
        {
            //cout << "空间正确" << endl;
        }
        else
        {
            cout << "空间错误" << endl;
        }
        // 获取可行块表
        vector<Block> viableBlockList = searchViableBlock(space, blockList, avalNumOfBox);
        if (viableBlockList.size() != 0)
        {
            int blockIndex = ps->psList[index++]; // 获取这一次放置选择的块再viableBlockList种的下标
            while (blockIndex > viableBlockList.size() - 1)
            { // 若下标超出范围的取余
                blockIndex %= viableBlockList.size();
            }
            // 获取这个被选择放入的块
            Block selecedBlock = viableBlockList.at(blockIndex);
            // 生成一个放置
            Put put(selecedBlock, space);
            //cout << "块大小:" << selecedBlock.blockLenght << "," << selecedBlock.blockWidth << "," << selecedBlock.blockHigh << endl;
            // 把放置加入scheme
            scheme.addPutToScheme(put);
            // 更新空间 以及 箱子数目信息
            // 箱子数目更新
            for (int i = 0; i < problem->typeNumberOfBox; i++)
            {
                avalNumOfBox[i] -= selecedBlock.neededBoxNumOfEachType[i];
            }
            // 更新空间栈信息
            reNewSpaceStack(spaceStack, space, selecedBlock);
        }
        else
        {
            // 进行空间转移
            spaceTransfer(spaceStack);
        }
        ps->psLength = index; // ps遍历下标长度增加
    }
    // cout << "index:" << index << endl;
    delete avalNumOfBox;
    return scheme;
}

Scheme algorithmSA(vector<Block> &blockList, Problem *problem, Ps *ps)
{
    // 获取初始Scheme
    Scheme bestScheme = getSchemeByHeuristic(blockList, problem, ps);
    Scheme finalBestScheme = bestScheme;
    Scheme newScheme;
    // 定义一个ps2
    Ps *ps2 = new Ps(MaxSeq);

    double T = Ts;

    while (T >= Tf)
    {
        for (int i = 0; i < LengthOfMkF; i++)
        {
            srand((unsigned)time(NULL) + i); //初始化随机种子
            int k = (rand() % (ps->psLength - 1 - 0 + 1)) + 0;
            // cout << "k:" << k <<endl;
            psCopyAtoB(ps, ps2);
            srand((unsigned)time(NULL) + i + 1); //初始化随机种子
            ps2->psList[k] = (rand() % (MaxSelect - 1 - 0 + 1)) + 0;
            // cout << "kvalue:" << ps2 -> psList[k]<<endl;
            newScheme = getSchemeByHeuristic(blockList, problem, ps2);
            if (newScheme.totalVolum > bestScheme.totalVolum)
            { // 新的解好更新最好的解
                psCopyAtoB(ps2, ps);
                bestScheme = newScheme;
            }
            else
            { // 否则以一定的概率更新解
                srand((unsigned)time(NULL) + i + 2);
                double pro = rand() / double(RAND_MAX);
                if (pro < exp((newScheme.totalVolum - bestScheme.totalVolum) / T))
                {
                    psCopyAtoB(ps2, ps);
                    bestScheme = newScheme;
                }
            }
            // 记录过程中最好解
            if (bestScheme.totalVolum > finalBestScheme.totalVolum)
            {
                finalBestScheme = bestScheme;
            }
            cout << "第 " << i << "阶段最好解:" << bestScheme.totalVolum / (problem->containerHigh * problem->containerLength * problem->containerWidth) << endl;
        }

        if (isLiner)
        {
            T *= Dt;
        }
        else
        {
            T = (1 - T * Dt) * T;
        }
    }
    delete ps2;
    return finalBestScheme;
}

// ps之间的赋值操作
void psCopyAtoB(Ps *a, Ps *b)
{
    b->psLength = a->psLength;
    for (int i = 0; i < MaxSeq; i++)
    {
        b->psList[i] = a->psList[i];
    }
}

void judge(Block &block, Problem *problem){
    if(block.blockHigh > problem -> containerHigh || block.blockLenght > problem -> containerLength || block.blockWidth > problem -> containerWidth){
        cout <<"块出错大小>>>>>>>>>" << endl;
        block.showBlockInfo();
    }

    if(block.blockLenght < block.viableLength || block.blockWidth < block.viableWidth){
        cout <<"块出错可行域>>>>>>>>>----------------------------------" << endl;
        block.showBlockInfo();
    }
}