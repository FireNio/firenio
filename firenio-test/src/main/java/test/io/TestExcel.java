package test.io;

import com.google.common.base.Strings;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author: wangkai
 **/
public class TestExcel {

    static final boolean FAST_REGION = !"true".equals(System.getProperty("fast-region.disabled"));

    public static void main(String[] args) throws Exception {

        int rows = 1_0000;
//        rows = 3;

//        String modelFile = "C://temp/chuanyin/order_info_ch_hk.xlsx";
//        Map data = createData(rows);

//        String modelFile = "C://temp/chuanyin/india_dn_report.xlsx";
//        Map    data      = createDnData(rows);

        String modelFile = "C://temp/chuanyin/test.xlsx";
        Map    data      = createTestData(rows);

        System.out.println("Start export...");
        long startTime = System.currentTimeMillis();

        FileOutputStream dataOut    = new FileOutputStream("C://temp/chuanyin/out.xlsx");
        FileInputStream  modelInput = new FileInputStream(modelFile);
        try {
            exportExcel(data, dataOut, modelInput);
        } finally {
            close(dataOut);
            close(modelInput);
        }

        long past = System.currentTimeMillis() - startTime;
        System.out.println("complete cost:" + past);

    }


    // 流式导出时需要逐行写入 sheet, 因此需要创建新的 sheet, 整体思路：
    // 遇到非循环结构则直接复制 cell 内容到新 sheet, 如果需要做值替换则进行替换，
    // 遇到循环结构则读取 meta(循环的替换行信息) 信息在循环中使用，目的是避免在循环中读取 meta, 以减少不必要的对象创建
    // 单元格合并（这里采用栈式处理）：
    // 遇到非循环结构时 pop 出单元格合并信息，并根据新 sheet 实际行数构建新的合并信息
    // 遇到循环结构时先将该循环 meta 里的合并信息 pop 出来，在循环中根据新 sheet 的实际 rowIndex 构建新的合并信息
    // 这里需要注意的是 mergeBase, 该属性作用是对新 sheet 行数进行补偿，以达到新 sheet 行号与模版行号一致，在代码块中会对该属性做出解释
    public static void exportExcel(Map data, OutputStream dataOut, InputStream modelInput) throws IOException {
        long          startTime = System.currentTimeMillis();
        XSSFWorkbook  wb        = new XSSFWorkbook(modelInput);
        SXSSFWorkbook swb       = new SXSSFWorkbook(wb, 1024);
        System.out.println("init cost: " + (System.currentTimeMillis() - startTime));
        try {
            XSSFSheet               temp          = wb.getSheetAt(0);
            SXSSFSheet              sheet         = swb.createSheet("export");
            XSSFSheet               _sheet        = wb.getSheet("export");
            CTWorksheet             ctWorksheet   = _sheet.getCTWorksheet();
            List<CellRangeAddress>  mergedRegions = temp.getMergedRegions();
            Stack<CellRangeAddress> mergeStack    = new Stack<>();
            for (int i = mergedRegions.size() - 1; i >= 0; i--) {
                mergeStack.push(mergedRegions.get(i));
            }
            int rowIndex  = 0;
            int mergeBase = 0;
            int rowCount  = temp.getLastRowNum();
            for (int i = 0; i <= rowCount; i++) {
                XSSFRow tempRow = temp.getRow(i);
                if (isList(tempRow)) {
                    String    value   = tempRow.getCell(0).getStringCellValue();
                    String    key     = value.substring(1);
                    List<Map> rowData = (List<Map>) data.get(key);
                    tempRow = temp.getRow(++i);
                    if (!mergeStack.isEmpty()) {
                        mergeStack.pop();
                    }
                    // 模板中忽略 meta 头(#list.xxx)的合并信息，对 mergeBase + 1 操作
                    mergeBase++;
                    List<String> metas = readMeta(tempRow);
                    if (rowData != null) {
                        List<CellRangeAddress> listAddress = new ArrayList<>();
                        for (; !mergeStack.isEmpty(); ) {
                            // 这里因为先判断是否需要 merge 所以 rowIndex 尚未 +1，所以采用大于等于判断
                            if (rowIndex + mergeBase >= mergeStack.peek().getLastRow()) {
                                CellRangeAddress merge = mergeStack.pop();
                                listAddress.add(merge);
                            } else {
                                break;
                            }
                        }
                        for (int j = 0; j < rowData.size(); j++) {
                            SXSSFRow row = sheet.createRow(rowIndex++);
                            Map      map = rowData.get(j);
                            for (int k = 0; k < metas.size(); k++) {
                                SXSSFCell cell      = row.createCell(k);
                                String    meta      = metas.get(k);
                                String    cellValue = null;
                                if (!Strings.isNullOrEmpty(meta)) {
                                    cellValue = getDataValue(map, meta);
                                }
                                cell.setCellValue(cellValue);
                                XSSFCell tempCell = tempRow.getCell(k);
                                if (tempCell != null) {
                                    cell.setCellStyle(tempCell.getCellStyle());
                                }
                            }
                            for (CellRangeAddress merge : listAddress) {
                                CellRangeAddress address = new CellRangeAddress(
                                        rowIndex - 1,
                                        rowIndex - 1,
                                        merge.getFirstColumn(),
                                        merge.getLastColumn());
                                addMergedRegion(sheet, ctWorksheet, address);
                            }
                            // 新 sheet 行数增加，减小 mergeBase
                            mergeBase--;
                        }
                        // 循环结构有数据，则模版循环 meta 生效，对 mergeBase + 1 操作
                        mergeBase++;
                    }
                } else {
                    short    cellNum = tempRow.getLastCellNum();
                    SXSSFRow row     = sheet.createRow(rowIndex++);
                    for (int j = 0; j < cellNum; j++) {
                        XSSFCell tempCell = tempRow.getCell(j);
                        if (tempCell != null) {
                            SXSSFCell cell      = row.createCell(j);
                            String    tempValue = tempCell.getStringCellValue();
                            if (tempValue.startsWith("#")) {
                                cell.setCellValue(getDataValue(data, tempValue.substring(1)));
                            } else {
                                cell.setCellValue(tempValue);
                            }
                            cell.setCellStyle(tempCell.getCellStyle());
                        }
                    }
                    // 这里因为 rowIndex 已经加一，所以采用大于判断
                    for (; !mergeStack.isEmpty(); ) {
                        if (rowIndex + mergeBase > mergeStack.peek().getLastRow()) {
                            int              base  = rowIndex - 1;
                            CellRangeAddress merge = mergeStack.pop();
                            CellRangeAddress address = new CellRangeAddress(
                                    base - (merge.getLastRow() - merge.getFirstRow()),
                                    base,
                                    merge.getFirstColumn(),
                                    merge.getLastColumn());
                            addMergedRegion(sheet, ctWorksheet, address);
                        } else {
                            break;
                        }
                    }
                }
            }
            // 处理 Excel 末尾行单元格合并
            for (; !mergeStack.isEmpty(); ) {
                int              base  = rowIndex - 1;
                CellRangeAddress merge = mergeStack.pop();
                CellRangeAddress address = new CellRangeAddress(
                        base,
                        base + merge.getLastRow() - merge.getFirstRow(),
                        merge.getFirstColumn(),
                        merge.getLastColumn());
                addMergedRegion(sheet, ctWorksheet, address);
            }
            swb.removeSheetAt(0);
            swb.setSheetOrder("export", 0);
            swb.setActiveSheet(0);
            swb.write(dataOut);
        } finally {
            close(swb);
            swb.dispose();
        }
    }

    static String getDataValue(Map map, String meta) {
        Object o = map.get(meta);
        if (o == null) {
            return "";
        } else {
            return o.toString();
        }
    }

    static void addMergedRegion(SXSSFSheet sheet, CTWorksheet ctSheet, CellRangeAddress region) {
        if (FAST_REGION) {
            addMergedRegion(ctSheet, region);
        } else {
            sheet.addMergedRegionUnsafe(region);
        }
    }

    static void addMergedRegion(CTWorksheet sheet, CellRangeAddress region) {
        if (region.getNumberOfCells() < 2) {
            throw new IllegalArgumentException("Merged region " + region.formatAsString() + " must contain 2 or more cells");
        }
        region.validate(SpreadsheetVersion.EXCEL2007);
        CTMergeCells ctMergeCells = sheet.isSetMergeCells() ? sheet.getMergeCells() : sheet.addNewMergeCells();
        CTMergeCell  ctMergeCell  = ctMergeCells.addNewMergeCell();
        ctMergeCell.setRef(region.formatAsString());
    }

    static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }

    static boolean isList(XSSFRow row) {
        XSSFCell cell = row.getCell(0);
        return cell != null && cell.getStringCellValue().startsWith("#list.");
    }

    static List<String> readMeta(XSSFRow row) {
        short        lastCellNum = row.getLastCellNum();
        List<String> list        = new ArrayList<>();
        for (int i = 0; i < lastCellNum; i++) {
            XSSFCell cell = row.getCell(i);
            if (cell != null) {
                String value = cell.getStringCellValue();
                if (Strings.isNullOrEmpty(value)) {
                    list.add("");
                } else {
                    list.add(value.substring(1));
                }
            } else {
                list.add("");
            }
        }
        return list;
    }

    static Map createTestData(int rows) {
        Map map = new HashMap();
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        Map lineMap1 = new HashMap();
        Map lineMap2 = new HashMap();
        Map lineMap3 = new HashMap();
        Map lineMap4 = new HashMap();

        map.put("test1", "中文。。。");
        map.put("test2", "2222");
        lineMap1.put("list.line1.test1","aaa111");
        lineMap1.put("list.line1.test2","bbb111");
        lineMap1.put("list.line1.test3","ccc111");
        lineMap2.put("list.line1.test1","ddd111");
        lineMap2.put("list.line1.test2","eee111");
        lineMap2.put("list.line1.test3","fff111");
        list1.add(lineMap1);
        list1.add(lineMap2);
        map.put("test3","3333");
        lineMap3.put("list.line2.test1","aaa222");
        lineMap3.put("list.line2.test2","bbb222");
        lineMap3.put("list.line2.test3","ccc222");
        lineMap4.put("list.line2.test1","ddd222");
        lineMap4.put("list.line2.test2","eee222");
        lineMap4.put("list.line2.test3","fff222");
        list1.add(lineMap1);
        list1.add(lineMap2);

        map.put("test4","4444");
        map.put("list.line2", list1);
        return map;
    }

    static Map createDnData(int rows) {
        Map map = new HashMap();

        List sys = new ArrayList();
        for (int i = 0; i < rows; i++) {
            Map    row   = new HashMap();
            String value = "ADD_1234";
            row.put("list.sys.order_release_name", value);
            row.put("list.sys.brand", value);
            sys.add(row);
        }
        map.put("list.sys", sys);
        return map;
    }

    static Map createData(int rows) {
        rows /= 2;
        Map map = new HashMap();
        map.put("shipment.shipment_name", "A1234");
        map.put("shipment.start_time", "20200912 17:00:00");
        map.put("shipment.servprov__servprov_xid", "B1234");

        List adds = new ArrayList();
        for (int i = 0; i < rows; i++) {
            Map    row   = new HashMap();
            String value = "ADD_1234";
            row.put("list.addrs.attribute_date1", value);
            row.put("list.addrs.attribute_number2", value);
            row.put("list.addrs.attribute_number3", value);
            row.put("list.addrs.location__attribute5", value);
            row.put("list.addrs.location__attribute6", value);
            row.put("list.addrs.location__description", value);
            adds.add(row);
        }
        map.put("list.addrs", adds);


        List dns = new ArrayList();
        for (int i = 0; i < rows; i++) {
            Map    row   = new HashMap();
            String value = "DNS1234";
            row.put("list.dns.plan_to_location_gid", value);
            row.put("list.dns.total_ship_unit_count", value);
            row.put("list.dns.total_packaging_unit_count", value);
            row.put("list.dns.location__attribute5", value);
            row.put("list.dns.location__attribute6", value);
            row.put("list.dns.location__description", value);
            dns.add(row);
        }
        map.put("list.dns", dns);

        map.put("shipment.power_unit__power_unit_xid", "W1234");
        map.put("shipment.attribute5", "E1234");
        map.put("shipment.power_unit__power_unit_num", "R1234");
        map.put("shipment.attribute13", "T1234");
        map.put("shipment.attribute3", "Y1234");
        map.put("shipment.shipment_port_no", "U1234");
        map.put("shipment.em_phone_number", "P1234");
        map.put("shipment.stock_port_no", "S1234");

        return map;
    }

}
