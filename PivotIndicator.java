package com.myStrategies;



import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.Period;
import com.dukascopy.api.indicators.BooleanOptInputDescription;
import com.dukascopy.api.indicators.IDrawingIndicator;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorDrawingSupport;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo.DrawingStyle;
import com.dukascopy.api.indicators.OutputParameterInfo.Type;
import com.dukascopy.api.indicators.PeriodListDescription;

/**
 * Created by: S.Vishnyakov
 * Date: Oct 21, 2009
 * Time: 1:59:16 PM
 */
public class PivotIndicator implements IIndicator, IDrawingIndicator {
    
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT 0"));
    }

    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    
    private IBar[][] inputs = new IBar[2][];
    
    private double[][] outputs = new double[18][];
    private PivotLevel[] finishedCalculationOutputs, drawingOutputs;
    private InputParameterInfo dailyInput;
    private DecimalFormat decimalFormat;
    
    private final GeneralPath generalPath = new GeneralPath(); 
    private List<Point> tmpHandlesPoints = new ArrayList<Point>();
    
    private boolean showHistoricalLevels = false;
    
    private IIndicatorContext context;
    
    private int maxDistanceBetweenTwoSeparators;
    private int lastCalculatedOutputSize = Integer.MIN_VALUE;
    
    private List<Period> periods = new ArrayList<Period>();
    
    private class PivotLevel {
        private long barTime;
        private double[] values = new double[18];
        private long timeOnChart = -1;
        
        private int x = -1;
        
        public long getBarTime() {
            return barTime;
        }
        public void setBarTime(long time) {
            this.barTime = time;
        }
        public double[] getValues() {
            return values;
        }
        
        public int getX() {
            return x;
        }
        public void setX(int x) {
            this.x = x;
        }
        
        public void setTimeOnChart(long timeOnChart) {
            this.timeOnChart = timeOnChart;
        }
        
        public long getTime(){
            return timeOnChart <= 0 ? barTime : timeOnChart;
        }
    }    
    
    public void onStart(IIndicatorContext context) {
        this.context = context;
        
        indicatorInfo = new IndicatorInfo("MYFIBPIVOT", "Pivot", "Overlap Studies", true, false, true, 2, 2, 18);
        indicatorInfo.setSparseIndicator(true);
        indicatorInfo.setRecalculateAll(true);
        
        dailyInput = new InputParameterInfo("Input data", InputParameterInfo.Type.BAR);
        dailyInput.setPeriod(Period.DAILY);
        dailyInput.setFilter(Filter.WEEKENDS);        
        inputParameterInfos = new InputParameterInfo[] {
            new InputParameterInfo("Main Input data", InputParameterInfo.Type.BAR),
            dailyInput
        };

        for (Period p : Period.values()){
            if (p.isTickBasedPeriod() || p.equals(Period.ONE_YEAR)){
                continue;
            }
            periods.add(p);
        }
        
        optInputParameterInfos = new OptInputParameterInfo[] {
              new OptInputParameterInfo("Period", OptInputParameterInfo.Type.OTHER, 
                      new PeriodListDescription(Period.DAILY, periods.toArray(new Period[periods.size()]))),
              new OptInputParameterInfo("Show historical levels", OptInputParameterInfo.Type.OTHER, new BooleanOptInputDescription(showHistoricalLevels))
        };

        outputParameterInfos = new OutputParameterInfo[] {
            createOutputParameterInfo("Central Point (P)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Resistance (R1)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Support (S1)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Resistance (R2)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Support (S2)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Resistance (R3)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Support (S3)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Resistance (R4)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Support (S4)", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point P-R1", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point P-S1", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point R1-R2", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point S1-S2", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point R2-R3", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point S2-S3", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point R3-R4", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Mid Point S3-S4", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
            createOutputParameterInfo("Separators", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LEVEL_LINE, true),
        };
        
        decimalFormat = new DecimalFormat("0.00000");
    }
    
    private OutputParameterInfo createOutputParameterInfo(String name, Type type, DrawingStyle drawingStyle, final boolean showOutput) {
        return new OutputParameterInfo(name, type, drawingStyle, false){{
            setDrawnByIndicator(true);
            setShowOutput(showOutput);
        }};
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        resetOutputs(outputs);
        
        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        IndicatorResult result = new IndicatorResult(startIndex, endIndex - startIndex + 1);
        if(inputs[0] == null) {
            return result;
        }
        
        int leftIndexForIndicatorPeriod = getLeftBarIndexForIndicatorPeriod(startIndex) - 1;
        int rightIndexForIndicatorPeriod = getRightBarIndexForIndicatorPeriod(endIndex);
        
        if (leftIndexForIndicatorPeriod < 0) {
            leftIndexForIndicatorPeriod = 0;
        }
        if (rightIndexForIndicatorPeriod < 0) {
            rightIndexForIndicatorPeriod = inputs[1].length - 1;
        }
        
        if (leftIndexForIndicatorPeriod < 0 || rightIndexForIndicatorPeriod < 0) {
            /*
             * Not enough data for calculations
             */
            return result;
        }
        else {
            IBar previousBar = null;
            
            PivotLevel[] innerOutputs = new PivotLevel[rightIndexForIndicatorPeriod - leftIndexForIndicatorPeriod + 1];
            
            for (int i = leftIndexForIndicatorPeriod; i <= rightIndexForIndicatorPeriod; i++) {
                IBar currentBar = inputs[1][i];
                
                if (previousBar != null) {
                    int chartPeriodBarIndex = i - leftIndexForIndicatorPeriod;
                    
                    if (innerOutputs[chartPeriodBarIndex] == null) {
                        innerOutputs[chartPeriodBarIndex] = new PivotLevel();
                    }
                    
                    calculateAndSetupPivotValue(innerOutputs, chartPeriodBarIndex, previousBar, currentBar);                    
                }
                previousBar = currentBar;
            }
            
            fillOutput(innerOutputs);
            
            lastCalculatedOutputSize = endIndex - startIndex + 1;
            
            synchronized(this) {
                finishedCalculationOutputs = innerOutputs;
            }
            
            return result;
        }
    }
    
    private void fillOutput(PivotLevel[] innerOutputs){
        if(inputs[0] == null) {
            return;
        }

        for (int i = 1; i < innerOutputs.length; i++){
            
            int currentInputIndex = getTimeIndex(innerOutputs[i].getBarTime(), inputs[0]);
            if(currentInputIndex < 0) {
                // if time of first input is after start of last innerOutput
                for (int j = 0; j < indicatorInfo.getNumberOfOutputs() - 1; j++) {
                    for (int k = 0; k < outputs[0].length; k++) {
                        outputs[j][k] = innerOutputs[innerOutputs.length - 1].getValues()[j];
                    }

                }

            } else {
                if (currentInputIndex < outputs[0].length){
                    outputs[indicatorInfo.getNumberOfOutputs() - 1][currentInputIndex] = 0;
                    innerOutputs[i].setTimeOnChart(inputs[0][currentInputIndex].getTime());

                    for (int j = 0; j < indicatorInfo.getNumberOfOutputs() - 1; j++) {
                        outputs[j][currentInputIndex] = innerOutputs[i].getValues()[j];

                        PivotLevel prevPivotLevel = innerOutputs[i - 1];
                        int prevInputIndex = prevPivotLevel == null ? -1 : getTimeIndex(prevPivotLevel.getBarTime(), inputs[0]);
                        if (prevInputIndex > -1 && prevInputIndex < outputs[j].length){
                            for (int k = prevInputIndex + 1; k < currentInputIndex; k++){
                                outputs[j][k] = prevPivotLevel.getValues()[j];
                            }
                        }

                        if (i == innerOutputs.length - 1){
                            for (int k = currentInputIndex + 1; k < outputs[0].length; k++){
                                outputs[j][k] = innerOutputs[i].getValues()[j];
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void resetOutputs(double[][] outputs) {
        if (outputs != null) {
            for (double[] otpts : outputs) {
                if (otpts != null) {
                    for (int i = 0; i < otpts.length; i++) {
                        otpts[i] = Double.NaN;
                    }
                }
            }
        }
    }
    
    private void calculateAndSetupPivotValue(
            PivotLevel[] innerOutputs,            
            int chartPeriodBarIndex,
            IBar previousBar,
            IBar currentBar
    ) {
        // P
        double p = (previousBar.getClose() + previousBar.getHigh() + previousBar.getLow())/3;
        double r1 = p + 0.382 * (previousBar.getHigh() - previousBar.getLow());
        double s1 = p - 0.382 * (previousBar.getHigh() - previousBar.getLow());
        double r2 = p + 0.618 * (previousBar.getHigh() - previousBar.getLow());
        double s2 = p - 0.618 * (previousBar.getHigh() - previousBar.getLow());
        double r3 = p + previousBar.getHigh() - previousBar.getLow();
        double s3 = p - (previousBar.getHigh() - previousBar.getLow());
        double r4 = p + 1.382 * (previousBar.getHigh() - previousBar.getLow());
        double s4 = p - 1.382 * (previousBar.getHigh() - previousBar.getLow());
        
        innerOutputs[chartPeriodBarIndex].getValues()[0] = p;
        // R1
        innerOutputs[chartPeriodBarIndex].getValues()[1] = r1;
        // S1
        innerOutputs[chartPeriodBarIndex].getValues()[2] = s1;
        // R2
        innerOutputs[chartPeriodBarIndex].getValues()[3] = r2;
        // S2
        innerOutputs[chartPeriodBarIndex].getValues()[4] = s2;
        // R3
        innerOutputs[chartPeriodBarIndex].getValues()[5] = r3;
        // S3
        innerOutputs[chartPeriodBarIndex].getValues()[6] = s3;
        // R4
        innerOutputs[chartPeriodBarIndex].getValues()[7] = r4;
        // S4
        innerOutputs[chartPeriodBarIndex].getValues()[8] = s4;
        
        //Mid Points
        //P-R1
        innerOutputs[chartPeriodBarIndex].getValues()[9] = p + (r1 - p) / 2;
        //P-S1
        innerOutputs[chartPeriodBarIndex].getValues()[10] = p - (p - s1) / 2;
        //R1-R2
        innerOutputs[chartPeriodBarIndex].getValues()[11] = r1 + (r2 - r1) / 2;
        //S1-S2
        innerOutputs[chartPeriodBarIndex].getValues()[12] = s2 + (s1 - s2) / 2;
        //R2-R3
        innerOutputs[chartPeriodBarIndex].getValues()[13] = r2 + (r3 - r2) / 2;
        //S2-S3
        innerOutputs[chartPeriodBarIndex].getValues()[14] = s3 + (s2 - s3) / 2;
        //R3-R4
        innerOutputs[chartPeriodBarIndex].getValues()[15] = r3 + (r4 - r3) / 2;
        //S3-S4
        innerOutputs[chartPeriodBarIndex].getValues()[16] = s4 + (s3 - s4) / 2;
        
        innerOutputs[chartPeriodBarIndex].setBarTime(currentBar.getTime());
    }
    
    private int getTimeIndex(long time, IBar[] source) {
        if (source == null) {
            return -1;
        }

        int curIndex = 0;
        int upto = source.length;
        
        while (curIndex < upto) {
            int midIndex = (curIndex + upto) / 2;
            int nextToMidIndex = midIndex + 1;
            
            IBar midBar = source[midIndex];
            IBar nextToMidBar = nextToMidIndex >= 0 && nextToMidIndex < source.length ? source[nextToMidIndex] : null;
                       
            if (midBar.getTime() == time) {
                return midIndex;
            }
            else if (nextToMidBar != null && midBar.getTime() < time && time <= nextToMidBar.getTime()){
                if (time == nextToMidBar.getTime()){
                    return nextToMidIndex;
                }
                else {
                    if (Math.abs(midBar.getTime() - time) < context.getFeedDescriptor().getPeriod().getInterval()){
                        return midIndex;
                    }
                    else {
                        return nextToMidIndex;
                    }
                }
            }
            else if (time < midBar.getTime()) {
                upto = midIndex;
            } 
            else if (time > midBar.getTime()) {
                curIndex = midIndex + 1;
            } 
        }
        
        return -1;
    }
    
    private int getRightBarIndexForIndicatorPeriod(int index) {
        int result = -1;
        if(inputs[0] == null) {
            return -1;
        }

        if (index >= 0 && index < inputs[0].length) {
            IBar bar = inputs[0][index];
            if (bar != null) {
                long time = bar.getTime();
                result = getTimeOrAfterTimeIndex(time, inputs[1]);
            }
        }
        
        return result;
    }
  
    private int getLeftBarIndexForIndicatorPeriod(int index) {
        int result = -1;
        if(inputs[0] == null) {
            return -1;
        }
        
        if (index >= 0 && index < inputs[0].length) {
            IBar bar = inputs[0][index];
            if (bar != null) {
                long time = bar.getTime();
                result = getTimeOrBeforeTimeIndex(time, inputs[1]);
            }
        }
        
        return result;
    }
    
    private int getTimeOrAfterTimeIndex(long time, IBar[] source) {
        if (source == null) {
            return -1;
        }

        int curIndex = 0;
        int upto = source.length;
        
        while (curIndex < upto) {
            int midIndex = (curIndex + upto) / 2;
            int nextToMidIndex = midIndex + 1;
            
            IBar midBar = source[midIndex];
            IBar nextToMidBar = nextToMidIndex >= 0 && nextToMidIndex < source.length ? source[nextToMidIndex] : null;
                       
            if (midBar.getTime() == time) {
                return midIndex;
            }
            else if (nextToMidBar != null && midBar.getTime() < time && time <= nextToMidBar.getTime()) {
                return nextToMidIndex;
            }
            else if (time < midBar.getTime()) {
                upto = midIndex;
            } 
            else if (time > midBar.getTime()) {
                curIndex = midIndex + 1;
            } 
        }

        return -1;
    }

    private int getTimeOrBeforeTimeIndex(long time, IBar[] source) {
        if (source == null) {
            return -1;
        }

        int curIndex = 0;
        int upto = source.length;
        
        while (curIndex < upto) {
            int midIndex = (curIndex + upto) / 2;
            int previousToMidIndex = midIndex - 1;
            
            IBar midBar = source[midIndex];
            IBar previousToMidBar = previousToMidIndex >= 0 && previousToMidIndex < source.length ? source[previousToMidIndex] : null;
            
            if (midBar.getTime() == time) {
                return midIndex;
            }
            else if (previousToMidBar != null && previousToMidBar.getTime() <= time && time < midBar.getTime()) {
                return previousToMidIndex;
            }
            else if (time < midBar.getTime()) {
                upto = midIndex;
            } 
            else if (time > midBar.getTime()) {
                curIndex = midIndex + 1;
            } 
        }

        return -1;
    }

    public IndicatorInfo getIndicatorInfo() {
        return indicatorInfo;
    }

    public InputParameterInfo getInputParameterInfo(int index) {
        if (index <= inputParameterInfos.length) {
            return inputParameterInfos[index];
        }
        return null;
    }

    public int getLookback() {
        return 0;
    }

    public int getLookforward() {
        return 0;
    }

    public OptInputParameterInfo getOptInputParameterInfo(int index) {
        if (index <= optInputParameterInfos.length) {
            return optInputParameterInfos[index];
        }
        return null;
    }

    public OutputParameterInfo getOutputParameterInfo(int index) {
        if (index <= outputParameterInfos.length) {
            return outputParameterInfos[index];
        }
        return null;
    }

    public void setInputParameter(int index, Object array) {
        inputs[index] = (IBar[]) array;
    }

    public void setOptInputParameter(int index, Object value) {
        if (index == 0) {
            Period period;
            if (value instanceof Integer){
                period = mapToPredefPeriodOrdinal((Integer)value);
            }
            else {
                   period = (Period)value;
            }                    
            
            if (! periods.contains(period)){
                throw new IllegalArgumentException("Period not supported");
            }
                        
            dailyInput.setPeriod(period);
        }
        else if (index == 1) {
            showHistoricalLevels = Boolean.valueOf(String.valueOf(value)).booleanValue();
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }

    @Override
    public Point drawOutput(
            Graphics g,
            int outputIdx,
            Object values,
            Color color,
            Stroke stroke,
            IIndicatorDrawingSupport indicatorDrawingSupport,
            List<Shape> shapes,
            Map<Color, List<Point>> handles
    ) {
        switch (outputIdx) {
            case 0 : color = Color.BLUE; break;
            case 1 : color = Color.MAGENTA; break;
            case 2 : color = Color.RED; break;
            case 3 : color = Color.MAGENTA; break;
            case 4 : color = Color.RED; break;
            case 5 : color = Color.MAGENTA; break;
            case 6 : color = Color.RED; break;
            case 7 : color = Color.MAGENTA; break;
            case 8 : color = Color.RED; break;
            default: color = Color.GRAY; break;
        }

        
        doDrawOutput(g, outputIdx, values, color, stroke, indicatorDrawingSupport, shapes, handles);
        return null;
    }
    
    private void doDrawOutput(
            Graphics g,
            int outputIdx,
            Object values,
            Color color,
            Stroke stroke,
            IIndicatorDrawingSupport indicatorDrawingSupport,
            List<Shape> shapes,
            Map<Color, List<Point>> handles
    ) {
        synchronized(this) {
            drawingOutputs = finishedCalculationOutputs;
        }
        
        if (drawingOutputs == null) {
            return;
        }

        tmpHandlesPoints.clear();
        
        if (values != null && stroke != null) {
            double[] output = (double[]) values;
            
            if (output.length != lastCalculatedOutputSize) {
                /*
                 * This means that data was changed, but not calculated yet - can not draw yet
                 */
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g;
            generalPath.reset();
            g2.setColor(color);
            g2.setStroke(stroke);
            
            IBar[] chartData = inputs[0];
            int si = 0, ei = drawingOutputs.length - 1;
            int firstCandleIndex = indicatorDrawingSupport.getIndexOfFirstCandleOnScreen();
            int lastCandleIndex = firstCandleIndex + indicatorDrawingSupport.getNumberOfCandlesOnScreen() - 1;
            
            if (firstCandleIndex <= chartData.length && lastCandleIndex < chartData.length){
                
                if (drawingOutputs[ei] != null && drawingOutputsOutOfScope(chartData, firstCandleIndex, drawingOutputs)){
                    si = drawingOutputs.length > 1 ? ei - 1 : ei;
                }
                else {
                
                    IBar siBar = firstCandleIndex > 0 ?  chartData[firstCandleIndex - 1] : chartData[firstCandleIndex];            
                    IBar eiBar = lastCandleIndex < chartData.length - 1 ? chartData[lastCandleIndex + 1] : chartData[lastCandleIndex];
                    
                    si = drawingOutputs.length - 1;
                    while (si > 0){
                        if (drawingOutputs[si] == null){
                            si--;
                            continue;
                        }
                        
                        if (drawingOutputs[si].getTime() >= siBar.getTime()){
                            si--;
                        }
                        else {
                            break;
                        }
                    }
                    
                    ei = si;
                    while (ei < drawingOutputs.length - 1){
                        if (drawingOutputs[ei] == null){
                            ei++;
                            continue;
                        }
                        
                        if (drawingOutputs[ei].getTime() <= eiBar.getTime()){
                            ei++;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            
            if (outputIdx == getFirstEnabledOutputIndex()){
                maxDistanceBetweenTwoSeparators = calculateMaxDistanceBetweenTwoSeparators(drawingOutputs, indicatorDrawingSupport, si, ei);
            }
        
            int fontSize = calculateFontSize(maxDistanceBetweenTwoSeparators, (int)indicatorDrawingSupport.getCandleWidthInPixels());
            boolean drawValues = canDrawValues(fontSize);
            
            if (outputIdx == outputs.length-1) {
                if (drawValues) {
                    drawSeparators(
                            drawingOutputs,
                            indicatorDrawingSupport, 
                            generalPath,
                            maxDistanceBetweenTwoSeparators,
                            si,
                            ei
                    );
                }
            }
            else {
                drawPivotLevels(
                        g2,
                        outputIdx,
                        drawingOutputs,
                        indicatorDrawingSupport, 
                        generalPath,
                        fontSize,
                        drawValues,
                        maxDistanceBetweenTwoSeparators,
                        si,
                        ei
                );
            }
            
            g2.draw(generalPath);
            
            shapes.add((Shape) generalPath.clone()); // cloning path, so when checking for intersection each indicator has its own path
            handles.put(color, new ArrayList<Point>(tmpHandlesPoints));
        }
    }

    private void drawSeparators(
            PivotLevel[] innerOutputs,
            IIndicatorDrawingSupport indicatorDrawingSupport,
            GeneralPath generalPath,
            int maxDistanceBetweenTwoSeparators,
            int si,
            int ei
    ) {
        if (innerOutputs == null) {
            return;
        }
        
        int maxWidth = indicatorDrawingSupport.getChartWidth() + maxDistanceBetweenTwoSeparators;
        int maxHeight = indicatorDrawingSupport.getChartHeight();
        
        Integer lastSeparatorX = null;
        
        for (int i = ei; i >= si; i--) {    
            if(innerOutputs[i] == null){
                continue;
            }
            int x = innerOutputs[i].getX() == -1 ? indicatorDrawingSupport.getXForTime(innerOutputs[i].getTime()) : innerOutputs[i].getX();
            
            if (lastSeparatorX == null) {
                lastSeparatorX = new Integer(x);
            }
            
            if (x < 0) {
                /*
                 * Drawing is from right to left
                 * Stop drawing if we are out of screen
                 */
                break;
            }
            
            drawSeparator(
                    generalPath,
                    x,
                    maxWidth,
                    maxHeight
            );
            
            if (!showHistoricalLevels) {
                /*
                 * Don't draw separators further if the user doesn't want them
                 */
                break;
            }
        }
        
        if (indicatorDrawingSupport.isTimeAggregatedPeriod()) {
            drawSeparator(
                    generalPath,
                    (lastSeparatorX == null ? 0 : lastSeparatorX.intValue()) + maxDistanceBetweenTwoSeparators,
                    maxWidth,
                    maxHeight
            );
        }

    }

    private int calculateMaxDistanceBetweenTwoSeparators(
            PivotLevel[] innerOutputs,
            IIndicatorDrawingSupport indicatorDrawingSupport,
            int si, int ei
    ) {
        int maxDistance = Integer.MIN_VALUE;
        
        if (innerOutputs == null) {
            return maxDistance;
        }
        
        int previousX = -1;
        
        for (int i = si; i <= ei; i++) {
            if (innerOutputs[i] == null){
                continue;
            }
            
            int x = indicatorDrawingSupport.getXForTime(innerOutputs[i].getTime());
            innerOutputs[i].setX(x);
            
            if (i > si && previousX != -1) {
                if (x != previousX) {
                    int distance = Math.abs(x - previousX);
                    
                    if (maxDistance < distance) {
                        maxDistance = distance;
                    }
                }
                if (!showHistoricalLevels){
                    int lastInnerOutputIndex = innerOutputs.length - 1; 
                    if (innerOutputs[lastInnerOutputIndex] != null){
                        innerOutputs[lastInnerOutputIndex].setX(indicatorDrawingSupport.getXForTime(innerOutputs[lastInnerOutputIndex].getTime()));
                    }
                    break;
                }
            }
            previousX = x;
        }
        return maxDistance;
    }

    private void drawPivotLevels(
            Graphics2D g2,
            int outputIdx,
            PivotLevel[] innerOutputs,
            IIndicatorDrawingSupport indicatorDrawingSupport,
            GeneralPath generalPath,
            int fontSize,
            boolean drawValues,
            int maxDistanceBetweenTwoSeparators,
            int si,
            int ei
    ) {
        g2.setFont(new Font(g2.getFont().getName(), g2.getFont().getStyle(), fontSize));
        
        int maxX = indicatorDrawingSupport.getChartWidth() + maxDistanceBetweenTwoSeparators; //JFOREX-2432
        int minX = -maxDistanceBetweenTwoSeparators;
        
        Integer previousX = null;
        
        for (int i = ei; i >= si; i--) {
            if (innerOutputs[i] == null){
                continue;
            }
            
            double d = innerOutputs[i].getValues()[outputIdx];
            
            int x = innerOutputs[i].getX() == -1 ? indicatorDrawingSupport.getXForTime(innerOutputs[i].getTime()) : innerOutputs[i].getX();
            int y = (int)indicatorDrawingSupport.getYForValue(d);
            
            if (previousX == null) {
                if (indicatorDrawingSupport.isTimeAggregatedPeriod()) {
                    previousX = new Integer(x + maxDistanceBetweenTwoSeparators);
                } else {
                    previousX = indicatorDrawingSupport.getChartWidth();
                }
            }
            
            if (
                    (y >= 0 && y <= indicatorDrawingSupport.getChartHeight()) &&
                    ! (previousX < 0 && x < 0) &&
                    ! (previousX > indicatorDrawingSupport.getChartWidth() && x > indicatorDrawingSupport.getChartWidth()) &&
                    (
                            (minX <= previousX.intValue() && previousX.intValue() <= maxX) ||
                            (minX <= x && x <= maxX)
                    )
            ) {
                previousX = Math.max(0, previousX);
                previousX = Math.min(indicatorDrawingSupport.getChartWidth(), previousX);
                x = Math.max(0, x);
                x = Math.min(indicatorDrawingSupport.getChartWidth(), x);
                
                generalPath.moveTo(previousX.intValue(), y);
                generalPath.lineTo(x, y);
                
                if (drawValues) {
                    String valueStr = decimalFormat.format(d);
                    String lineCode = getLineCodeText(outputIdx);
                    String result = lineCode + ": " + valueStr;
                    
                    int lineCodeX = x + 1;
                    int distance = Math.abs(x - previousX.intValue());
                    int newFontSize = calculateFontSize(distance, (int)indicatorDrawingSupport.getCandleWidthInPixels());
                    boolean canDrawValues = this.canDrawValues(newFontSize);
                    
                    if (canDrawValues) {
                        if (newFontSize != fontSize) {
                            fontSize = newFontSize;
                            g2.setFont(new Font(g2.getFont().getName(), g2.getFont().getStyle(), fontSize));
                        }
                        g2.drawString(result, lineCodeX, y - 2);
                    }
                }
                
                if (!showHistoricalLevels) {
                    break;
                }
            }
            else if (y < 0 || y > indicatorDrawingSupport.getChartHeight() ||
                    (previousX < 0 && x < 0) ||
                    (previousX > indicatorDrawingSupport.getChartWidth() && x > indicatorDrawingSupport.getChartWidth()) ||
                    x > maxX ||
                    previousX.intValue() > maxX
            ) {
                /*
                 *    the last actual period is out of screen => don't draw anything if showHistoricalLevels is disabled 
                 */                 
                if (!showHistoricalLevels) {
                    break;
                }
            }
            
            previousX = Integer.valueOf(x);
        }
    }
    
    private boolean canDrawValues(int fontSize) {
        final int MIN_FONT_SIZE = 4;
        
        if (fontSize <= MIN_FONT_SIZE) {
            return false;
        }
        return true;
    }

    private int calculateFontSize(
            int spaceBetweenTwoSeparators,
            int candleWidthInPixels
    ) {
        
        final int MAX_FONT_SIZE = 12;
        final int DIVISION_COEF = 7;
        
        spaceBetweenTwoSeparators /= DIVISION_COEF;
        spaceBetweenTwoSeparators = spaceBetweenTwoSeparators < 0 ? candleWidthInPixels : spaceBetweenTwoSeparators;
        
        return spaceBetweenTwoSeparators > MAX_FONT_SIZE ? MAX_FONT_SIZE : spaceBetweenTwoSeparators;
    }

    private void drawSeparator(
            GeneralPath generalPath,
            int x,
            int maxWidth,
            int maxHeight
    ) {
        if (0 <= x && x <= maxWidth) {
            generalPath.moveTo(x, 0);
            generalPath.lineTo(x, maxHeight);
            
            tmpHandlesPoints.add(new Point(x, 5));
            tmpHandlesPoints.add(new Point(x, maxHeight/2));
            tmpHandlesPoints.add(new Point(x, maxHeight - 5));
        }
    }

    private String getLineCodeText(int outputIdx) {
        String lineCode = "";
        switch (outputIdx) {
            case 0 : lineCode = "P"; break;
            case 1 : lineCode = "R1"; break;
            case 2 : lineCode = "S1"; break;
            case 3 : lineCode = "R2"; break;
            case 4 : lineCode = "S2"; break;
            case 5 : lineCode = "R3"; break;
            case 6 : lineCode = "S3"; break;
            case 7 : lineCode = "R4"; break;
            case 8 : lineCode = "S4"; break;
            case 9 : lineCode = "P-R1"; break;
            case 10 : lineCode = "P-S1"; break;
            case 11 : lineCode = "R1-R2"; break;
            case 12 : lineCode = "S1-S2"; break;
            case 13 : lineCode = "R2-R3"; break;
            case 14 : lineCode = "S2-S3"; break;
            case 15 : lineCode = "R3-R4"; break;
            case 16 : lineCode = "S3-S4"; break;
            default: throw new IllegalArgumentException("Illegal outputIdx - " + outputIdx);
        }
        return lineCode;
    }
    
    private int getFirstEnabledOutputIndex(){
        for (int i = 0; i < getIndicatorInfo().getNumberOfOutputs(); i++){
            if (outputParameterInfos[i].isShowOutput()) {
                return i; 
            }
        }
        return -1;
    }
    
    protected boolean drawingOutputsOutOfScope(IBar[] chartData, int firstCandleIndex, PivotLevel[] drawingOutputs){
        boolean res = chartData[firstCandleIndex].getTime() > drawingOutputs[drawingOutputs.length - 1].getTime();
        
        return res;
    }
    private static Period mapToPredefPeriodOrdinal(int oldPeriod){
        Period res;
        
        switch (oldPeriod) {
        case 0:
            res = Period.ONE_MIN;
            break;
        case 1:
            res = Period.FIVE_MINS;
            break;
        case 2:
            res = Period.TEN_MINS;
            break;
        case 3:
            res = Period.FIFTEEN_MINS;
            break;
        case 4:
            res = Period.THIRTY_MINS;
            break;
        case 5:
            res = Period.ONE_HOUR;
            break;
        case 6:
            res = Period.FOUR_HOURS;
            break;
        case 7:
            res = Period.DAILY;
            break;
        case 8:
            res = Period.WEEKLY;
            break;
        case 9:
            res = Period.MONTHLY;
            break;

        default:
            res = Period.DAILY;
        }
        
        return res;
    }
}