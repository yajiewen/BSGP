import problem.Box;
import problem.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author yajiewen
 * @Date 2022-04-04 17-07-28
 * @Description 更新problem 版本
*/
public class FileHelper {
    public StringBuilder filePath;// 文件路径
    public FileHelper(String filePath){
        this.filePath = new StringBuilder(filePath);
    }

    // 把文件中的问题转化为对象
    public ArrayList<Problem> getProblems() throws IOException {
        ArrayList<Problem> problemList = new ArrayList<>();
        File file = new File(filePath.toString());
        if(file.exists()){
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            int problemNum = Integer.parseInt(line.trim());

            for (int i = 0; i < problemNum; i++) {
                line = bufferedReader.readLine(); // 读取没用的一行
                line = bufferedReader.readLine().trim(); // 读取容器大小
                String[] containnerSize = line.split(" ");
                Problem problem = new Problem(Double.parseDouble(containnerSize[0]), Double.parseDouble(containnerSize[1]), Double.parseDouble(containnerSize[2]), Integer.parseInt(bufferedReader.readLine().trim()));  // 创建一个问题
                for(int j = 0; j < problem.typeNumberOfBox; j++){
                    String[] boxInfo = bufferedReader.readLine().trim().split(" ");
                    Box box = new Box();
                    box.boxType = Integer.parseInt(boxInfo[0].trim());
                    box.boxLength = Double.parseDouble(boxInfo[1].trim());
                    box.isXvertical = Integer.parseInt(boxInfo[2].trim());
                    box.boxWidth = Double.parseDouble(boxInfo[3].trim());
                    box.isYvertical = Integer.parseInt(boxInfo[4].trim());
                    box.boxHigh = Double.parseDouble(boxInfo[5].trim());
                    box.isZvertical = Integer.parseInt(boxInfo[6].trim());
                    box.boxNumber = Integer.parseInt(boxInfo[7].trim());
                    problem.boxList.add(box);
                }
                problemList.add(problem);
            }
        }else{
            System.out.println("文件不存在");
        }
        return problemList;
    }
}
