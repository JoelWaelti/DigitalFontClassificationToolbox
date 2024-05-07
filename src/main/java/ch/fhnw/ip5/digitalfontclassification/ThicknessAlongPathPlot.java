package ch.fhnw.ip5.digitalfontclassification;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ThicknessAlongPathPlot {
    private static final List<Double> distances = Arrays.asList(
            3.7986510791366896, 4.234375, 5.585779561735134, 4.234375, 8.171521443120932, 4.25, 4.226952368919624, 4.222588375404809, 4.218634142652576, 4.221412347429532, 4.233699762514157, 4.253525725703346, 4.274482179898444, 4.2876663202014464, 4.282223578085105, 4.242327695462859, 4.186662753311294, 4.143304205727198, 4.131021615923321, 4.151743092270811, 4.19016056403243, 4.220733379685096, 4.232772553284921, 4.244425744704426, 4.254525021574087, 4.254545205793803, 4.242364102381372, 4.222544107110659, 4.206382487937991, 4.211387294123792, 5.613218486554166, 4.210876918476303, 4.206539193836011, 4.2226000699701, 4.242468704177437, 4.254693813986775, 4.254516811272684, 4.24426648430664, 4.232720480916697, 4.220804868565677, 4.190230943298409, 4.151897484662415, 4.13117853197519, 4.142735997920802, 4.18517993991167, 4.241072313381205, 4.2820479558225, 4.287626941292776, 4.274229843521891, 4.253188642560522, 4.233502355039246, 4.221376661368429, 4.218632238768149, 4.222562283342445, 4.22694523725295, 32.983179109159344, 43.09401394991777, 4.25, 4.800361812888638, 5.901001150216165, 7.746659866497336, 4.235661158992549, 4.316516842029618, 4.378093890569509, 8.145496155777213, 5.710224616233427, 11.55707059352518, 9.397374029004745, 4.3125, 0.8443274024490297, 0.0, 4.380782594909612, 4.271175425929354, 4.253831778964119, 4.234333287733068, 4.3125, 3.5493942126725244
    );

    public static void main(String[] args) throws IOException {
        barPlot();
        //linePlot();
    }

    public static void barPlot() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int i = 0; i < distances.size(); i++) {
            dataset.addValue(distances.get(i), "distances", String.valueOf(i));
        }

        JFreeChart barChartObject = ChartFactory.createBarChart(
                "Distances along the path","Segment Nr.",
                "Distance",
                dataset, PlotOrientation.VERTICAL,
                true,true,false);

        int width = 640;    /* Width of the image */
        int height = 480;   /* Height of the image */
        File barChart = new File( "BarChart.jpeg" );
        ChartUtilities.saveChartAsJPEG(barChart , barChartObject , width , height);
    }

    public static void linePlot() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int i = 0; i < distances.size(); i++) {
            dataset.addValue(distances.get(i), "distances", String.valueOf(i));
        }

        JFreeChart lineChartObject = ChartFactory.createLineChart(
                "Distances along the path","Segment Nr.",
                "Distance",
                dataset, PlotOrientation.VERTICAL,
                true,true,false);

        int width = 640;    /* Width of the image */
        int height = 480;   /* Height of the image */
        File lineChart = new File( "LineChart.jpeg" );
        ChartUtilities.saveChartAsJPEG(lineChart ,lineChartObject, width ,height);
    }
}
