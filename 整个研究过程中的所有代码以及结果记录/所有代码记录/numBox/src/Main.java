import problem.Box;
import problem.Problem;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        FileHelper fileHelper = new FileHelper("E:\\Standard_Datasets\\br15.txt");
        ArrayList<Problem> problems = fileHelper.getProblems();

        int totalBoxNum = 0;
        for (Problem problem : problems) {
            for(int i = 0; i < problem.typeNumberOfBox; i++){
                Box box = problem.boxList.get(i);
                totalBoxNum += box.boxNumber;
            }
        }
        System.out.println(totalBoxNum / 100);
    }
}