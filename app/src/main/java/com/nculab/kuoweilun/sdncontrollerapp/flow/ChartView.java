package com.nculab.kuoweilun.sdncontrollerapp.flow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChartView {
    private Context context;
    private DBflow dBflow = new DBflow();
    private XYMultipleSeriesDataset dataset;
    private XYMultipleSeriesRenderer renderer;

    public ChartView(Context context, JSONObject jsonObject) {
        this.context = context;
        this.dBflow.parse(jsonObject);
    }

    public GraphicalView getView() {
        dataset = new XYMultipleSeriesDataset();
        renderer = new XYMultipleSeriesRenderer();
        renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
        renderer.setYTitle(dBflow.precision + "分鐘內流量(KB)\"");// 設置Y軸標題
        renderer.setAxisTitleTextSize(40);// 設定軸標題文字大小
        renderer.setChartTitle(dBflow.date + "流量");// 設定圖標標題
        renderer.setChartTitleTextSize(40);// 設定圖表文字大小
        renderer.setLabelsTextSize(40);// 設定標籤大小
        renderer.setLegendTextSize(40);// 設定左下圖例文字大小 例如：第一條線、第二條線
        renderer.setPointSize(10f);// 設定每個點的大小
        renderer.setYAxisMin(0);// 設定Y軸最小值
        renderer.setYAxisMax(dBflow.max_flow * 1.1);// 設定Y軸最大值
        renderer.setXAxisMin(0);// 設定X軸最小值
        renderer.setXAxisMax(4);// 設定X軸最大值
        renderer.setXLabels(0);// 設定label
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setShowGrid(true);// 顯示網格
        renderer.setPanEnabled(true, false);
        renderer.setInScroll(true);
        renderer.setMargins(new int[]{50, 100, 150, 50});// 設定圖表的位置
        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setXAxisColor(Color.BLACK);
        renderer.setYAxisColor(Color.BLACK);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);
        addSeries();
        return showChart();
    }

    private void addSeries() {
        XYSeries min_xySeries = new XYSeries("min_flow");
        XYSeries max_xySeries = new XYSeries("max_flow");
        XYSeries avg_xySeries = new XYSeries("avg_flow");
        Log.d("timeArray: ", dBflow.timeArray.toString());
//        Log.d("min_flowArray", dBflow.min_flowArray.toString());
//        Log.d("max_flowArray", dBflow.max_flowArray.toString());
//        Log.d("avg_flowArray", dBflow.avg_flowArray.toString());
        for (int i = 0; i < dBflow.timeArray.size(); i++) {
            renderer.addXTextLabel(i, (dBflow.timeArray.get(i).substring(dBflow.timeArray.get(i).indexOf(" ") + 1)));
            min_xySeries.add(i, dBflow.min_flowArray.get(i));
            max_xySeries.add(i, dBflow.max_flowArray.get(i));
            avg_xySeries.add(i, dBflow.avg_flowArray.get(i));
        }
        dataset.addSeries(min_xySeries);
        dataset.addSeries(max_xySeries);
        dataset.addSeries(avg_xySeries);
    }

    private GraphicalView showChart() {
        XYSeriesRenderer min_xySeries = new XYSeriesRenderer();
        min_xySeries.setColor(Color.BLUE);// 設定線條顏色
        min_xySeries.setPointStyle(PointStyle.DIAMOND);// 設置為菱形
        min_xySeries.setFillPoints(true);// 設定空心或實心
        min_xySeries.setDisplayChartValues(true);// 顯示數值
        min_xySeries.setAnnotationsTextAlign(Paint.Align.RIGHT);
        min_xySeries.setChartValuesTextAlign(Paint.Align.RIGHT);
        min_xySeries.setChartValuesSpacing(20);// 顯示
        min_xySeries.setChartValuesTextSize(40);// 數值的文字大小
        min_xySeries.setLineWidth(1);// 線寬
        renderer.addSeriesRenderer(min_xySeries);

        XYSeriesRenderer max_xySeries = new XYSeriesRenderer();
        max_xySeries.setColor(Color.RED);// 設定線條顏色
        max_xySeries.setPointStyle(PointStyle.DIAMOND);// 設置為菱形
        max_xySeries.setFillPoints(true);// 設定空心或實心
        max_xySeries.setDisplayChartValues(true);// 顯示數值
        max_xySeries.setAnnotationsTextAlign(Paint.Align.RIGHT);
        max_xySeries.setChartValuesTextAlign(Paint.Align.RIGHT);
        max_xySeries.setChartValuesSpacing(20);// 顯示
        max_xySeries.setChartValuesTextSize(40);// 數值的文字大小
        max_xySeries.setLineWidth(1);// 線寬
        renderer.addSeriesRenderer(max_xySeries);

        XYSeriesRenderer avg_xySeries = new XYSeriesRenderer();
        avg_xySeries.setColor(Color.GREEN);// 設定線條顏色
        avg_xySeries.setPointStyle(PointStyle.DIAMOND);// 設置為菱形
        avg_xySeries.setFillPoints(true);// 設定空心或實心
        avg_xySeries.setDisplayChartValues(true);// 顯示數值
        avg_xySeries.setAnnotationsTextAlign(Paint.Align.RIGHT);
        avg_xySeries.setChartValuesTextAlign(Paint.Align.RIGHT);
        avg_xySeries.setChartValuesSpacing(20);// 顯示
        avg_xySeries.setChartValuesTextSize(40);// 數值的文字大小
        avg_xySeries.setLineWidth(1);// 線寬
        renderer.addSeriesRenderer(avg_xySeries);

        GraphicalView view = ChartFactory.getLineChartView(context, dataset,
                renderer);

        return view;
    }

    public class DBflow {

        public String date = "";
        public Double max_flow = 0.0;
        public ArrayList<String> timeArray = new ArrayList<String>();
        public ArrayList<Double> min_flowArray = new ArrayList<Double>();
        public ArrayList<Double> max_flowArray = new ArrayList<Double>();
        public ArrayList<Double> avg_flowArray = new ArrayList<Double>();
        public String precision = "0";

        public DBflow() {
        }

        public DBflow(JSONObject jsonObject) {
            parse(jsonObject);
        }

        public void parse(JSONObject jsonObject) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                precision = jsonObject.getString("precision");
                this.date = jsonObject.getString("date")
                        .replace(" ", "到")
                        .replace("T", " ");
                JSONArray flow = jsonObject.getJSONArray("flow");
                for (int i = 0; i < flow.length(); i++) {
                    JSONObject item = flow.getJSONObject(i);
                    Date date = new Date();
                    date.setTime(item.getLong("timestamp"));
                    timeArray.add(simpleDateFormat.format(date).replace(" ", "\n"));
                    min_flowArray.add(item.getDouble("min_flow"));
                    max_flowArray.add(item.getDouble("max_flow"));
                    avg_flowArray.add(item.getDouble("avg_flow"));
                    if (item.getDouble("max_flow") > max_flow)
                        max_flow = item.getDouble("max_flow");
                }
                Log.d("max: ", max_flow.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
