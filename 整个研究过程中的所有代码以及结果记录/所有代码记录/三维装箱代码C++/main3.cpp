#include <iostream>
#include <vector>
#include <stack>
#include "psclass.h"

vector<Ps2> getPs();
int main(){
    for(int j = 0 ; j < 1000000; j++){
        vector<Ps2> psVector =getPs();
        cout<<j<<endl;
    }
    getchar();
}

vector<Ps2> getPs(){
    Ps2 ps2;
    vector<Ps2> psVector;
    psVector.push_back(ps2);
    return psVector;
}