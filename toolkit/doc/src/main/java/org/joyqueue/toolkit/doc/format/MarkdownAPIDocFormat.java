/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.toolkit.doc.format;


import org.joyqueue.toolkit.doc.APIDoc;
import org.joyqueue.toolkit.doc.Format;
import org.joyqueue.toolkit.doc.Param;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.code.CodeBlock;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * Markdown style api doc
 *
 **/
public class MarkdownAPIDocFormat implements Format<APIDoc> {
    private final  String path="接口路径 ";
    private final  String method="请求类型 ";
    private final  String description="功能描述 ";
    private final  String parameter="参数 ";
    private final  String paramName="字段";
    private final  String paramType="类型";
    private final  String paramDesc="备注";
    private final  String example="示例: ";
    private final  String request="请求: ";
    private final  String response="响应: ";

    @Override
    public String format(String sequenceNum, APIDoc apiDoc) {
        StringBuilder apiContent=new StringBuilder();
        String head;
        if(apiDoc.getDesc()!=null&&apiDoc.getDesc().length()>0){
            String desc=apiDoc.getDesc();
            int spitStart=       desc.indexOf(",");
            if(spitStart>0){
                desc=desc.substring(0,spitStart);
            }
            head=desc;
        }else{
            head=apiDoc.getMethod();
        }
        head=sequenceNum+"."+head;
        Text h2=new Heading(head,2);
        String pathLine=path+apiDoc.getPath();
        String methodLine=method+apiDoc.getHttpMethod();
        String descLine=description+(apiDoc.getDesc()!=null?apiDoc.getDesc():"无");
        String paramLine=parameter+(apiDoc.getParams()==null?"无":"");
        List<String> items = Arrays.asList(pathLine, methodLine, descLine,paramLine);
        UnorderedList list = new UnorderedList<>(items);
        apiContent.append(h2).append("\n");
        apiContent.append(list).append("\n\n");
        if(apiDoc.getParams()!=null&&apiDoc.getParams().size()>0) {
            List<TableRow> rows = new ArrayList();
            rows.add(new TableRow(Arrays.asList(
                    paramName,
                    paramType,
                    paramDesc
            )));
            for (Param p : apiDoc.getParams()) {
                rows.add(new TableRow(Arrays.asList(
                        p.getName(),
                        p.getType(),
                        p.getComment()
                )));
            }
            List<Integer> alignments = Arrays.asList(
                    Table.ALIGN_CENTER,
                    Table.ALIGN_CENTER,
                    Table.ALIGN_CENTER
            );
            Table table = new Table(rows, alignments);
            apiContent.append(table);
        }
        apiContent.append("\n");
        apiContent.append(example);
        if(apiDoc.getiDemo()!=null) {
            apiContent.append(request).append("\n");
            apiContent.append(new CodeBlock(apiDoc.getiDemo()));
            apiContent.append("\n");
        }
        if(apiDoc.getoDemo()!=null) {
            apiContent.append(response).append("\n");
            apiContent.append(new CodeBlock(apiDoc.getoDemo()));
        }
        apiContent.append("\n");
        return apiContent.toString();
    }


}
