#include <iostream>
#include <vector>
#include <stack>
#include "psclass.h"

vector<Ps> getPs(vector<Ps> *psList, stack<Ps> * psStack);
int main(){
    vector<Ps> psList;
    stack<Ps> psStack;
    vector<Ps> psVector = getPs(&psList, &psStack);
    for(int i = 0; i < psList.size(); i++){
        Ps *ps = &psList.at(i);
        cout << ps ->psLength<< endl;
    }
    cout << psVector.at(0).psLength << endl;
    Ps ps1(100);
    ps1.psLength++;
    Ps ps2 = ps1;
    cout << ps2.psLength << endl;
    getchar();
}

vector<Ps> getPs(vector<Ps> *psList, stack<Ps> * psStack){
    Ps ps(200);
    psList -> push_back(ps);
    psStack -> push(ps);
    vector<Ps> psVector;
    psVector.push_back(ps);
    return psVector;
}