#include <fstream>
#include <iostream>
#include <string>
#include <stdio.h>
#include "problemclass.h"
using namespace std;

class FileReader{
    public:
        string filePath;
        FileReader(string filePath);
        Problem* getProblems();
};
// 构造函数
FileReader::FileReader(string filePath){
    this->filePath = filePath;
}

// 获取问题列表
Problem * FileReader::getProblems(){
    int problemNum = 100; // 所有文件默认为100
    Problem *problemList = new Problem[problemNum]; // 生成问题列表
    ifstream fileIos;// 创建一个文件输入流对象
    fileIos.open(filePath); // 打开文件
    string line; // 逐行读取文件
    getline(fileIos,line); // 读取第一行的问题数

    const char splitToken = ' '; // 定义分隔符号

    for(int i = 0; i < problemNum; i++){
        double containnerInfo[3]; // 保存容器的长宽高
        int typeNumOfBox; // 该问题的箱子的类别数

        getline(fileIos,line); // 忽略第一行
        getline(fileIos,line); // 获取容器长宽高

        char p[10];
        for(int j = 0,index = 0,cindex = 0; j < line.length(); j++){ // 分别获取长宽高
            if(line[j] != splitToken){
                p[index++] = line[j];
                p[index] = '\0';
            }else{
                if(index != 0){
                    containnerInfo[cindex++] = atof(p);
                }
                index = 0;
            }
        }
        containnerInfo[2] = atof(p);
        // 获取箱子类别数
        getline(fileIos,line);
        for(int j = 0,index = 0; j < line.length(); j++){
            if(line[j] != splitToken){
                p[index++] = line[j];
                p[index] = '\0';
            }
        }
        typeNumOfBox = atoi(p);
        // 输出 容器信息和箱子类别信息
        //cout << "箱子信息:" <<containnerInfo[0] <<"," << containnerInfo[1]<< ","<< containnerInfo[2] <<'\n' << "箱子类别数:" << typeNumOfBox <<endl;
        // 把容器信息和箱子类别数存入对应的problem
        problemList[i].containerLength = containnerInfo[0];
        problemList[i].containerWidth = containnerInfo[1];
        problemList[i].containerHigh = containnerInfo[2];
        problemList[i].typeNumberOfBox = typeNumOfBox;
        problemList[i].boxTypeInfoList = new BoxTypeInfo[typeNumOfBox];

        // 遍历各种类别的箱子的信息
        double boxInfo[8];
        for(int j = 0; j < typeNumOfBox; j++){
            getline(fileIos,line);
            for(int k = 0,index = 0,cindex = 0; k < line.length(); k++){
                if(line[k] != splitToken){
                    p[index++] = line[k];
                    p[index] = '\0';
                }else{
                    if(index != 0){
                        boxInfo[cindex++] = atof(p);
                    }
                    index = 0;
                }
            }

            boxInfo[7] = atof(p);
            // 保存该类别的箱子信息
            problemList[i].boxTypeInfoList[j].boxType = boxInfo[0];
            problemList[i].boxTypeInfoList[j].boxLength = boxInfo[1];
            problemList[i].boxTypeInfoList[j].isXvertical = boxInfo[2];
            problemList[i].boxTypeInfoList[j].boxWidht = boxInfo[3];
            problemList[i].boxTypeInfoList[j].isYvertical = boxInfo[4];
            problemList[i].boxTypeInfoList[j].boxHigh = boxInfo[5];
            problemList[i].boxTypeInfoList[j].isZvertical = boxInfo[6];
            problemList[i].boxTypeInfoList[j].boxNumber = boxInfo[7];
        }
    }

    return problemList;
}