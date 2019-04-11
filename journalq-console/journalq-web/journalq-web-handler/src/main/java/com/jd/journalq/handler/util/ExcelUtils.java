/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.handler.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangkepeng on 15-7-13.
 */
public class ExcelUtils {
    /** Excel文件的存放位置。注意是正斜线*/
    private static final int maxRow = 150;
    private static final int maxRange = 50;

    /**
     * @return  ip list
     **/
    public static List<String> readIp(InputStream inputStream) throws Exception {
        List<String> readAllIps = new ArrayList<String>();
        Workbook workbook = null;
        try{
            try {
                workbook = new HSSFWorkbook(inputStream);
            }catch (Exception e){
                workbook = new XSSFWorkbook(inputStream);
            }
            // 创建对工作表的引用。
            // 按索引引用
            Sheet sheet = workbook.getSheetAt(0);
            // 读取单元数据
            for (int i = 0; i < maxRow; i++){
                for(int j = 0; j < maxRange; j++){
                    Row row = sheet.getRow(i);
                    if(row == null){
                        break;
                    }
                    Cell cell = row.getCell(j);
                    // 输出单元内容，cell.getStringCellValue()就是取所在单元的值
                    if(cell == null){
                        continue;
                    }
                    if(isIp(cell.getStringCellValue())){
                        readAllIps.add(cell.getStringCellValue());
                    }
                }
            }
        }catch (Exception e){
            throw e;
        }
        return readAllIps;
    }

    public static String trimSpaces(String IP) {//去掉IP字符串前后所有的空格
        while (IP.startsWith(" ")) {
            IP = IP.substring(1, IP.length()).trim();
        }
        while (IP.endsWith(" ")) {
            IP = IP.substring(0, IP.length() - 1).trim();
        }
        return IP;
    }

    public static boolean isIp(String IP) {//判断是否是一个IP
        boolean b = false;
        IP = trimSpaces(IP);
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

}
