package event;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShipComparison {
    static class Ship{
        double hp, maxspeed, hydrogenfuel, ifcsDiff;
        public Ship(double hp, double maxspeed, double hydrogenfuel, double ifcsDiff){
            this.hp = hp;
            this.maxspeed = maxspeed;
            this.hydrogenfuel = hydrogenfuel;
            this.ifcsDiff = ifcsDiff;
        }
    }
    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Geb dein Schiff ein: ");
        String ourShipName = scanner.nextLine();
        System.out.print("Geb den Namen des gegnerischen Schiff: ");
        String enemyShipName = scanner.nextLine();
        try {
            FileInputStream file = new FileInputStream(new File("/Users/lorenzogiacomelli/Downloads/BCRBot-master/src/main/resources/Database Ships.xlsx"));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndex = new HashMap <>();
            for (Cell cell : headerRow){
                columnIndex.put(cell.getStringCellValue(), cell.getColumnIndex());
            }

            Ship ourShip = null;
            Ship enemyShip = null;
            
            for (int r = 2; r <= sheet.getLastRowNum(); r++){
                Row row = sheet.getRow(r);
                if (row != null) {
                    String shipName = row.getCell(0).getStringCellValue().trim();
                    if(ourShipName.equals(shipName)) {
                        ourShip = extractShipData(row, columnIndex);
                    } else if (enemyShipName.equals (shipName)){
                        enemyShip = extractShipData(row, columnIndex);
                    }

                    if (ourShip != null && enemyShip != null) {
                        break;
                    }
                }
            }
            workbook.close();
            file.close();
            compareShips(ourShip, enemyShip);
            createRadarChart(ourShip, enemyShip);
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double getNumericCellValue(Cell cell){
        if (cell == null) {
            throw new IllegalArgumentException("Cell nichts ");
        }
        try {
            if (cell.getCellType() == CellType.STRING){
                String value = cell.getStringCellValue();
                try {
                    return Double.parseDouble(value);
                } catch(NumberFormatException e) {
                    throw new IllegalArgumentException("Will numeric finden aber finde was anderes :/ : " + value + " hier " + cell.getAddress().formatAsString());
                }
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else {
                throw new IllegalArgumentException("der typ geht nicht lul: " + cell.getCellType() + " hier " + cell.getAddress().formatAsString());
            }
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("kann nicht lesen " + cell.getAddress().formatAsString() + "wegen dem hier: " + e.getMessage());
        }
    }




    private static Ship extractShipData(Row row, Map<String, Integer > columnIndex) {
        double hp = getNumericCellValue(row.getCell(columnIndex.get("HP")));
        double maxspeed = getNumericCellValue(row.getCell(columnIndex.get("Max speed")));
        double hydrogenfuel = getNumericCellValue(row.getCell(columnIndex.get("Hydrogen capacity")));
        double pitchmax = getNumericCellValue(row.getCell(columnIndex.get("IFCS pitch max")));
        double yawmax = getNumericCellValue(row.getCell(columnIndex.get("IFCS yaw max")));
        double rollmax = getNumericCellValue(row.getCell(columnIndex.get("IFCS roll max")));

        double ifcsDiff = Math.abs((pitchmax + yawmax + rollmax) / 3);
        return new Ship(hp, maxspeed, hydrogenfuel, ifcsDiff);
    }




    private static void compareShips(Ship ourShip, Ship enemyShip) {
        //System.out.print("Comparison HP: " + (ourShip.hp > enemyShip.hp ? 1 : 0));
        //System.out.print("Comparison max speed: "+ (ourShip.maxspeed > enemyShip.maxspeed ? 1:0));
        //System.out.print("Comparison hydrogenfuel: " + (ourShip.hydrogenfuel > enemyShip.hydrogenfuel ? 1:0));
        //System.out.print("Comparison ifcsDiff: " + (ourShip.ifcsDiff > enemyShip.ifcsDiff ? 1:0));
        System.out.print("info: " + ourShip.hp + " " + ourShip.maxspeed + " " + ourShip.hydrogenfuel + " " + ourShip.ifcsDiff +" " + enemyShip.ifcsDiff + " " + enemyShip.hp + " " + enemyShip.maxspeed + " " + enemyShip.hydrogenfuel);
        
    }

        
    
    private static void createRadarChart(Ship ourShip, Ship enemyShip) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "Our Ship";
        String series2 = "Enemy Ship";

        dataset.addValue(ourShip.hp, series1, "HP");
        dataset.addValue(ourShip.maxspeed, series1, "Max speed");
        dataset.addValue(ourShip.hydrogenfuel, series1, "Hydrogen Fuel");
        dataset.addValue(ourShip.ifcsDiff, series1, "IFCS Diff");

        dataset.addValue(enemyShip.hp, series2, "HP");
        dataset.addValue(enemyShip.maxspeed, series2, "Max speed");
        dataset.addValue(enemyShip.hydrogenfuel, series2, "Hydrogen Fuel");
        dataset.addValue(enemyShip.ifcsDiff, series2, "IFCS Diff");


        for (Object series : dataset.getRowKeys()){
            for (Object category : dataset.getColumnKeys()) {
                Number value = dataset.getValue((Comparable)series, (Comparable)category);
                System.out.println (series + " (" + category + "): " + value);

            }
        }
        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setStartAngle(90);
        plot.setInteriorGap(0.25);
        plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        plot.setOutlineVisible(false);
        plot.setSeriesPaint(0, new Color(0, 0, 255, 128)); 
        plot.setSeriesPaint(1, new Color(255, 0, 0, 128)); 
        plot.setWebFilled(true);
        plot.setBaseSeriesOutlinePaint(Color.GRAY);
        plot.setBaseSeriesOutlineStroke(new BasicStroke(1.0f));
        plot.setMaxValue(0.5);

        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.setLabelPaint(Color.DARK_GRAY);
        plot.setBackgroundPaint(new Color(255, 255, 255, 100));
        plot.setOutlinePaint(null);
        
        JFreeChart chart = new JFreeChart("Radar Chart", plot);
        chart.setBackgroundPaint(Color.WHITE);


        try {
            ChartUtils.saveChartAsPNG(new File("RadarChart.jpeg"), chart, 500, 500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

