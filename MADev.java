/*
 * Copyright 2009 DukascopyР вЂ™Р’В® (Suisse) SA. All rights reserved.
 * DUKASCOPY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.myStrategies;

import com.dukascopy.api.IConsole;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.indicators.DoubleRangeDescription;
import java.awt.Color;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerListDescription;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;
import static com.dukascopy.api.indicators.OutputParameterInfo.DrawingStyle.ARROW_SYMBOL_DOWN;
import static com.dukascopy.api.indicators.OutputParameterInfo.DrawingStyle.ARROW_SYMBOL_UP;
import static com.dukascopy.api.indicators.OutputParameterInfo.Type.DOUBLE;


public class MADev implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private IIndicatorsProvider indicatorsProvider;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private static IConsole console;
    //Price includes 5 arrays: open, close, high, low, volume
    private double[][][] inputsPriceArr1 = new double[1][][];
    private double[][][] inputsPriceArr2 = new double[1][][]; 
    //price array depending on AppliedPrice
    private double[][] ma1InputsDouble = new double[1][];
    private double[][] ma2InputsDouble = new double[1][];
    private double[][] outputs = new double[2][];

    
 
    IIndicator ma1Indicator;
    IIndicator ma2Indicator;
    private int ma1TimePeriod = 14;
    private int ma1Type;
    private int ma2TimePeriod = 14;
    private int ma2Type;
    private double delta = 0.00195;
    private double multiplier = 39.2;
    private int powNum = 3;
    
    
    //output indices
    private static final int DOWN = 0;
    private static final int UP = 1;
    //input indices
    private static final int HIGH = 2;
    private static final int LOW = 3;
    
    
     //Method prints message to console
    public static void println(String message) {
        console.getOut().println(message);
        //System.out.println(message);
    }
    
     //Method prints message to console
    public static void print(String message) {
        console.getOut().print(message);
        //System.out.print(message);
    }

    public void onStart(IIndicatorContext context) {
        this.console = context.getConsole();
        
        int optInputsCount = 7;// Indicator input parameters count
        int optOutputsCount = 2;
        int numberOfInputs = 4;
        boolean unstablePeriod = true;
        boolean overChart = true;
        boolean overVolumes = false;

        indicatorInfo = new IndicatorInfo("MADev", "MADev Signals", "Custom indicators", overChart, overVolumes, unstablePeriod, numberOfInputs, optInputsCount, optOutputsCount);
        inputParameterInfos = new InputParameterInfo[] { 
                new InputParameterInfo("Price arrays1", InputParameterInfo.Type.PRICE),
                new InputParameterInfo("Price double1", InputParameterInfo.Type.DOUBLE),
                new InputParameterInfo("Price arrays2", InputParameterInfo.Type.PRICE),
                new InputParameterInfo("Price double2", InputParameterInfo.Type.DOUBLE)
            };
        int[] maValues = new int[IIndicators.MaType.values().length];
            String[] maNames = new String[IIndicators.MaType.values().length];
            for (int i = 0; i < maValues.length; i++) {
                maValues[i] = i;
                maNames[i] = IIndicators.MaType.values()[i].name();
            }
        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("MA_1 Period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(ma1TimePeriod, 1, 2000, 1)),                                
                new OptInputParameterInfo("MA_1 Type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(IIndicators.MaType.SMA.ordinal(), maValues, maNames)),
                new OptInputParameterInfo("MA_2 Period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(ma2TimePeriod, 1, 2000, 1)),                                
                new OptInputParameterInfo("MA_2 Type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(IIndicators.MaType.SMA.ordinal(), maValues, maNames)),
                new OptInputParameterInfo("Delta", OptInputParameterInfo.Type.OTHER, new DoubleRangeDescription(delta, 0.00001, 1, 0.00001, 5)),
                new OptInputParameterInfo("Multiplier", OptInputParameterInfo.Type.OTHER, new DoubleRangeDescription(multiplier, 0.1, 1000.0, 0.1, 1)),
                new OptInputParameterInfo("PowNum", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(powNum, 1, 100, 1))
        };
        
        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Maximums", DOUBLE, OutputParameterInfo.DrawingStyle.ARROW_SYMBOL_DOWN) {{ setColor(Color.RED); }},
                new OutputParameterInfo("Minimums", DOUBLE, OutputParameterInfo.DrawingStyle.ARROW_SYMBOL_UP) {{ setColor(Color.BLUE); }}           
            };

        indicatorsProvider = context.getIndicatorsProvider();
        ma1Indicator = indicatorsProvider.getIndicator("MA");
        ma2Indicator = indicatorsProvider.getIndicator("MA");
        
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        //println("startIndex: "+startIndex);
        //println("endIndex: "+endIndex);
        
        // calculating rsi
        double[] ma1Output = new double[endIndex - startIndex + 1];
        double[] ma2Output = new double[endIndex - startIndex + 1];
        ma1Indicator.setInputParameter(0, ma1InputsDouble[0]);
        ma1Indicator.setOutputParameter(0, ma1Output);
        ma1Indicator.calculate(startIndex, endIndex);
        
        ma2Indicator.setInputParameter(0, ma2InputsDouble[0]);
        ma2Indicator.setOutputParameter(0, ma2Output);
        ma2Indicator.calculate(startIndex, endIndex);

        double hi = 0;
        double lo = 0;
        int i, j, c;
        double ima, sum;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            if (j > 0) {
                double px=Math.pow(multiplier*(ma1Output[j]-ma2Output[j]),powNum);
                
                if (px > hi) {
                    hi = px; lo = hi-delta;
                    outputs[DOWN][j] = inputsPriceArr1[0][HIGH][i];
                }
                if (px < lo) {
                    lo = px; hi = lo+delta;
                    outputs[UP][j] = inputsPriceArr1[0][LOW][i];
                }
                
                //println(String.valueOf(rsiOutput[j]) +" " + String.valueOf(outputs[DOWN][j]));
            }
        }

        return new IndicatorResult(startIndex, endIndex-startIndex + 1); 
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
        if (ma1TimePeriod > ma2TimePeriod) {
            return ma1TimePeriod;
        } else {
            return ma2TimePeriod;
        }
        
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
        switch (index) {                
            case 0:
                inputsPriceArr1[0] = (double[][]) array;
                break;                    
            case 1:
                ma1InputsDouble[0] = (double[]) array;
                break;
            case 2:
                inputsPriceArr2[0] = (double[][]) array;
                break;                    
            case 3:
                ma2InputsDouble[0] = (double[]) array;
                break;
            default:
                println("setInputParameter Out Of Index: "+index);
                break;
            }
    }

    public void setOptInputParameter(int index, Object value) {
         switch (index) {                
            case 0:
                ma1TimePeriod = (Integer) value; 
                ma1Indicator.setOptInputParameter(0, ma1TimePeriod);
                break;                    
            case 1:
                ma1Type = (Integer) value;
                ma1Indicator.setOptInputParameter(1, ma1Type);                                      
                break;
            case 2:
                ma2TimePeriod = (Integer) value; 
                ma2Indicator.setOptInputParameter(0, ma2TimePeriod);
                break;                    
            case 3:
                ma2Type = (Integer) value;
                ma2Indicator.setOptInputParameter(1, ma2Type);                                      
                break;
            case 4:
                delta = (Double) value;                              
                break;
            case 5:
                multiplier = (Double) value;                              
                break;
            case 6:
                powNum = (Integer) value;                              
                break;
            default:
                println("setOptInputParameter Out Of Index: "+index);
                break;
            }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}