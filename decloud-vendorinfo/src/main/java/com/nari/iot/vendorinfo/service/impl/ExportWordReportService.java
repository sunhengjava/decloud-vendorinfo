package com.nari.iot.vendorinfo.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.nari.iot.vendorinfo.common.CommonInterface;
import com.nari.iot.vendorinfo.common.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.style.RtfFont;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ExportWordReportService {
    @Autowired
    CommonInterface commonInterface;

    static final String ZPURL="http://ggzj-center.oss-hn-1-a.ops.sgmc.sgcc.com.cn/%E9%85%8D%E7%BD%91%E6%88%91%E6%9D%A5%E4%BF%9D/";
    static final String FILEPATH=System.getenv("D5000_HOME")+File.separator+"yzzjar"+File.separator+"zdys"+File.separator+"tempFile";
    static final String IMGPATH=System.getenv("D5000_HOME")+File.separator+"yzzjar"+File.separator+"zdys"+File.separator+"tempImg";
    public  String exportWord(HttpServletRequest request) throws Exception{
        String devLabel=request.getParameter("esn");

        // 设置纸张大小
        Document document = new Document(PageSize.A4);
        // 建立一个书写器(Writer)与document对象关联，通过书写器(Writer)可以将文档写入到磁盘中
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fileNameDoc = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss")+".doc";

        //        File file = new File("E:/mdTest/cs1result1.doc");
        File file = new File(FILEPATH+File.separator+fileNameDoc);
        RtfWriter2.getInstance(document, new FileOutputStream(file));
        document.open();
        // 设置中文字体
        BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA,
                BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        // 标题字体风格(加粗)
        Font titleFont = new Font(bfChinese, 12, Font.BOLD);
        // 正文字体风格
        Font contextFont = new Font(bfChinese, 10, Font.NORMAL);

        String sql1="select n1.dev_name,n1.dev_label,n3.name,n1.data_time||'',n1.is_pass,n1.img_url," +
              // " is_online_result,report_msg_result,param_set_result,data_measure_result,dev_count_result,remote_control_result,remark" +
                " is_online_result,report_msg_result,1,data_measure_result,dev_count_result,remote_control_result,remark" +
                " from D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n1\n" +
                "inner join d5000.iot_device n2 on n1.dev_label=n2.dev_label\n" +
                "left join d5000.dms_tr_device n3 on n2.rely_id=n3.id\n" +
                "where n1.dev_label='"+devLabel+"'";
        List<Object[]> devList1 = commonInterface.selectListBySql(sql1);
        log.info("sql1"+sql1);
        String  titleText=devList1.get(0)[0]+"终端验收详情";
        //报告标题
        Paragraph title = new Paragraph(titleText);
        // 设置标题格式对齐方式
        title.setAlignment(Element.ALIGN_CENTER);
        title.setFont(titleFont);
        document.add(title);

        String jl="";
        if (devList1.get(0)[4]==null||devList1.get(0)[4].equals("2")){
            jl="不涉及";
        } else if(devList1.get(0)[4].toString().equals("0")){
            jl="不通过";
        } else {
            jl="通过";
        }
        String textDetail="终端名称："+devList1.get(0)[0]+" \n"+
                "终端标识："+devList1.get(0)[1]+" \n"+
                "所属配变："+devList1.get(0)[2]+" \n"+
                "验收时间："+devList1.get(0)[3]+" \n"+
                "验收结论："+jl+" \n";
        //终端信息
        Paragraph contextDetail = setStyle(textDetail);
        contextDetail.setFont(titleFont);
        document.add(contextDetail);



        List<String> tableNameList = new ArrayList<>();
        tableNameList.add("DMS_ONLINE_CHECK_RESULT_ACCEPT_DETAIL");
        tableNameList.add("DMS_REPORT_CHECK_RESULT_ACCEPT_DETAIL");
      //  tableNameList.add("DMS_PARAM_CHECK_RESULT_DETAIL");
        tableNameList.add("DMS_MEASURE_CHECK_RESULT_ACCEPT_DETAIL");
        tableNameList.add("DMS_DEVCOUNT_CHECK_RESULT_ACCEPT_DETAIL");
        //剔除遥控验收结果
//        tableNameList.add("DMS_CONTROL_CHECK_RESULT_DETAIL");
        List<String> titleList = new ArrayList<>();
        titleList.add("1、 边子设备在线校验"+"  \t"+judgeRs(devList1.get(0)[6]));
        titleList.add("2、 报文上送校验"+"  \t"+judgeRs(devList1.get(0)[7]));
        //titleList.add("3、 参数下发校验"+"  \t"+judgeRs(devList1.get(0)[8]));
        titleList.add("3、 数据召测校验"+"  \t"+judgeRs(devList1.get(0)[9]));
        titleList.add("4、 子设备数量校验"+"  \t"+judgeRs(devList1.get(0)[10]));
        //剔除遥控验收结果
//        titleList.add("6、 遥控校验"+"  \t"+judgeRs(devList1.get(0)[11]));
        titleList.add("5、 现场照片");
        titleList.add("6、 现场备注");


        int k=0;
        for (String tableName:tableNameList) {
            String context="";
            String sql="select n1.dev_type,n1.dev_label,n1.pd_name,n1.device_mode_name,n1.is_pass,dbms_lob.substr(data_result,4000)" +
                    " from D5000."+tableName+" n1\n" +
                    " inner join D5000.DMS_TERM_CHECK_RESULT_ACCEPT_INFO n2 on n1.direct_id=n2.id\n" +
                    " where n2.dev_label='"+devLabel+"'";
            List<Object[]> devList = commonInterface.selectListBySql(sql);
            if(tableName.equals("DMS_DEVCOUNT_CHECK_RESULT_DETAIL")){
                System.out.println("输出结果"+sql);
            }
            //验收标题
            Paragraph contextT = setStyle(titleList.get(k++));
            contextT.setFont(titleFont);
            document.add(contextT);

            Table table = new Table(4);
            int withs[] = { 12, 56, 20, 12 };
            if (k==4) {
                table = new Table(5);
                withs = new int[]{ 12,40,18,18,12 };
            }
            /** 设置每列所占比例 author:yyli Sep 15, 2010 */
            table.setWidths(withs);
            /** 表格所占页面宽度 author:yyli Sep 15, 2010 */
            table.setWidth(100);
            /** 居中显示 author:yyli Sep 15, 2010 */
            table.setAlignment(Element.ALIGN_CENTER);
            /** 自动填满 author:yyli Sep 15, 2010 */
            table.setAutoFillEmptyCells(true);
            table = createTableTitle(table,k);
            for (Object[] devlist:devList) {
                table = createTable(devlist, table,k);
//              context+=devType+"  \t"+devlist[2]+"  \t"+judgeRs(devlist[4])+" \n";
            }
            //验收列表
            document.add(table);
        }

        //照片标题
//        Paragraph contextImg = setStyle(titleList.get(6));
        Paragraph contextImg = setStyle(titleList.get(4));
        contextImg.setFont(titleFont);
        document.add(contextImg);
        List<String> zpList =new ArrayList<>();
        if (devList1.get(0)[5]!=null) {
            zpList= Arrays.asList(devList1.get(0)[5].toString().split(";"));
        }
        int m=0;
        for ( String zp:zpList) {
            String fileName = CommonUtil.getDayStrBeforeOrAfter(0, "yyyyMMddHHmmss")+(m++)+".jpg";
            String imgUrl=ZPURL+zp;
            String b1 = CommonUtil.encodeImageToBase64(new URL(imgUrl));
            base64StringToImage(b1,IMGPATH+File.separator+fileName);

            Image img = Image.getInstance(IMGPATH+File.separator+fileName);
            img.setAbsolutePosition(0, 0);
            img.setAlignment(Image.LEFT);// 设置图片显示位置

            img.scaleAbsolute(300, 200);// 直接设定显示尺寸
            //img.scalePercent(10);//表示显示的大小为原尺寸的50%
            // // img.scalePercent(25, 12);//图像高宽的显示比例
            // // img.setRotation(30);//图像旋转一定角度
            document.add(img);

        }


        //验收反馈标题
//        Paragraph contextRemarkT = setStyle(titleList.get(7));
        Paragraph contextRemarkT = setStyle(titleList.get(5));
        contextRemarkT.setFont(titleFont);
        document.add(contextRemarkT);
        //验收反馈描述
        Paragraph contextRemark = setStyle(devList1.get(0)[12]+" \n");
        contextRemark.setFont(contextFont);
        if (devList1.get(0)[12]!=null){
            document.add(contextRemark);
        }

        document.close();
        return fileNameDoc;
    }

    /**
     * 生成表格数据
     * @param devlist
     * @param table
     * @param k
     * @return
     * @throws Exception
     */
    public Table createTable(Object[] devlist,Table table,int k) throws Exception {
        /** 正文字体 author:yyli Sep 15, 2010 */
        RtfFont contextFont = new RtfFont("仿宋_GB2312", 9, Font.NORMAL, Color.BLACK);
//        Table table = new Table(4);
        if (k!=4) {
            String devType="";
            if (devlist[0].toString().equals("1")){
                devType="边设备";
            } else {
                devType="子设备";
            }
            Cell cell0 = new Cell(new Phrase(devType, contextFont));
            cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell0);

            String devLabel=devlist[1]==null?"-":devlist[1].toString();
            Cell cell1 = new Cell(new Phrase(devLabel, contextFont));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell1);

            String pdName=devlist[2]==null?"-":devlist[2].toString();
            Cell cell2 = new Cell(new Phrase(pdName, contextFont));
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell2);

            String isPass="";
            if (devlist[4].toString().equals("2")) {
                isPass="不涉及";
            }else if (devlist[4].toString().equals("0")){
                isPass="未通过";
            } else {
                isPass="通过";
            }
            Cell cell4 = new Cell(new Phrase(isPass, contextFont));
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell4);
        } else {
            List<String> devtype = new ArrayList<>();
            devtype.add("nonreactivenum");
            devtype.add("voltagetapnum");
            devtype.add("oilsensornum");
            devtype.add("lowbranchnum");

            List<String> devTypeName = new ArrayList<>();
            devTypeName.add("无功补偿电容器");
            devTypeName.add("有载调压控制器");
            devTypeName.add("配变油温");
            devTypeName.add("低压开关");

            Map<String, Object> dataResult = JSONObject.parseObject(devlist[5].toString());
            Map<String, Object> fieldSum = (Map<String, Object>) dataResult.get("fieldSum");
            Map<String, Object> indirectSum = (Map<String, Object>) dataResult.get("indirectSum");

            if( fieldSum.containsKey("humitureNum")&&indirectSum.containsKey("humitureNum")){
                devTypeName.add("台区温湿度");
                devtype.add("humitureNum");
            }
            int i=0;
            for (String dtype:devtype) {
                String devType="子设备";
                Cell cell0 = new Cell(new Phrase(devType, contextFont));
                cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell0);

                String pdName=devTypeName.get(i++);
                Cell cell1 = new Cell(new Phrase(pdName, contextFont));
                cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell1);

                String sbNum= fieldSum.get(dtype)==null? "0": fieldSum.get(dtype).toString();
                Cell cell2 = new Cell(new Phrase(sbNum, contextFont));
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell2);

                String sjNum= indirectSum.get(dtype)==null? "0": indirectSum.get(dtype).toString();
                Cell cell3 = new Cell(new Phrase(sjNum, contextFont));
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell3);

                String isPass="";
                if (sbNum.equals(sjNum)) {
                    isPass="通过";
                } else {
                 if(sbNum.equals("0")){
                     isPass="未涉及";
                 }else{
                     isPass="未通过";
                 }

                }
                Cell cell4 = new Cell(new Phrase(isPass, contextFont));
                cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell4);
            }


        }

        return table;
    }

    /**
     * 生成表格表头
     * @param table
     * @param k
     * @return
     * @throws Exception
     */
    public Table createTableTitle(Table table,int k) throws Exception {
        /** 正文字体 author:yyli Sep 15, 2010 */
        RtfFont contextFont = new RtfFont("仿宋_GB2312", 9, Font.NORMAL, Color.BLACK);
//        Table table = new Table(4);
        if (k!=4) {
            Cell cell0 = new Cell(new Phrase("设备类型", contextFont));
            cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell0);

            Cell cell1 = new Cell(new Phrase("设备标识ESN码", contextFont));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell1);

            Cell cell2 = new Cell(new Phrase("产品名称", contextFont));
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell2);

            Cell cell4 = new Cell(new Phrase("是否通过", contextFont));
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell4);
        } else {
            Cell cell0 = new Cell(new Phrase("设备类型", contextFont));
            cell0.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell0.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell0);

            Cell cell1 = new Cell(new Phrase("产品名称", contextFont));
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell1);

            Cell cell2 = new Cell(new Phrase("上报数量", contextFont));
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell2);

            Cell cell3 = new Cell(new Phrase("实际数量", contextFont));
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell3);

            Cell cell4 = new Cell(new Phrase("是否通过", contextFont));
            cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell4);

        }

        return table;
    }
    /**
     * 文本样式设置
     * @param text
     * @return
     */
    public Paragraph setStyle(String text){
        Paragraph contextList = new Paragraph(text);
        // 正文格式左对齐
        contextList.setAlignment(Element.ALIGN_LEFT);
        // context.setFont(contextFont);
        // 离上一段落（标题）空的行数
        contextList.setSpacingBefore(5);
        // 设置第一行空的列数
        contextList.setFirstLineIndent(20);
        return contextList;
    }

    /**
     * 判断是否通过
     * @param obj
     * @return
     */
    public String judgeRs(Object obj){
        String result="";
        if (obj==null){
            result="未通过";
        } else if (obj.toString().equals("1")){
            result="通过";
        } else if (obj.toString().equals("2")){
            result="未涉及";
        } else {
            result="未通过";
        }

        return result;
    }

    /**
     * 照片编码转换为照片
     * @param base64String
     * @param pathname
     */
    public void base64StringToImage(String base64String,String pathname) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes1 = decoder.decodeBuffer(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            BufferedImage bi1 = ImageIO.read(bais);
            File f1 = new File(pathname);
            ImageIO.write(bi1, "jpg", f1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
