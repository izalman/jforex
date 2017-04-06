/*
 * Copyright 2009 DukascopyР’В® (Suisse) SA. All rights reserved.
 * DUKASCOPY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.myStrategies;

import com.dukascopy.api.IConsole;
import com.dukascopy.api.IIndicators;
import java.awt.Color;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IIndicatorsProvider;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

import static com.dukascopy.api.indicators.OutputParameterInfo.DrawingStyle.*;
import static com.dukascopy.api.indicators.OutputParameterInfo.Type.*;

public class RSISinglaArrows implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private static IConsole console;
    //Price includes 5 arrays: open, close, high, low, volume
    private double[][][] inputsPriceArr = new double[1][][]; 
    //price array depending on AppliedPrice
    private double[][][] cciInputsDouble = new double[1][][]; 
    private double[][] outputs = new double[2][];

    IIndicator cciIndicator;
    IIndicator stochIndicator;
    int cciTimePeriod = 14;
    int fastK = 5;
    int slowK = 5;
    int slowD = 5;
    IIndicators.MaType slowKMa = IIndicators.MaType.SMA;
    IIndicators.MaType slowDMa = IIndicators.MaType.SMA;
    
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
        
        int optInputsCount = 2;// Indicator input parameters count
        
        indicatorInfo = new IndicatorInfo("RSI_Signals", "RSI signals", "Custom indicators", true, false, false, 2, optInputsCount, 2);
        inputParameterInfos = new InputParameterInfo[] { 
                new InputParameterInfo("Price arrays", InputParameterInfo.Type.PRICE),
                new InputParameterInfo("Price double", InputParameterInfo.Type.DOUBLE)
            };
        optInputParameterInfos = new OptInputParameterInfo[] { 
            new OptInputParameterInfo("CCI time period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(cciTimePeriod, 1, 200, 1)), 
            new OptInputParameterInfo("Fast K%", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(fastK, 1, 200, 1))
        };
        
        
        
        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Maximums", DOUBLE, ARROW_SYMBOL_DOWN) {{ setColor(Color.RED); }},
                new OutputParameterInfo("Minimums", DOUBLE, ARROW_SYMBOL_UP) {{ setColor(Color.BLUE); }} };

        IIndicatorsProvider indicatorsProvider = context.getIndicatorsProvider();
        cciIndicator = indicatorsProvider.getIndicator("CCI");
        
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        //println("startIndex: "+startIndex);
        //println("endIndex: "+endIndex);
        
        // calculating rsi
        double[] rsiOutput = new double[endIndex - startIndex + 1];
        cciIndicator.setInputParameter(0, cciInputsDouble[0]);
        cciIndicator.setOutputParameter(0, rsiOutput); 
        cciIndicator.calculate(startIndex, endIndex);

        int i, j;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            //place down signal on the high price of the corresponding bar
            //outputs[DOWN][j] = rsiOutput[j] < 0 ? inputsPriceArr[0][HIGH][i] : Double.NaN;
            //place up signal on the low price of the corresponding bar
            //outputs[UP][j] = rsiOutput[j] > 0 ? inputsPriceArr[0][LOW][i] : Double.NaN;
            if (j > 0) {
                outputs[DOWN][j] = (rsiOutput[j-1] > 0 && rsiOutput[j] < 0 )? inputsPriceArr[0][HIGH][i] : Double.NaN;
                outputs[UP][j] = (rsiOutput[j-1] < 0 && rsiOutput[j] > 0) ? inputsPriceArr[0][LOW][i] : Double.NaN;
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
        return cciTimePeriod;
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
        if(index == 0) {
            cciInputsDouble[0] = (double[][]) array;
            inputsPriceArr[0] = (double[][]) array;
        //else if(index == 1)
        //    inputsDouble[0] = (double[]) array;
        }
    }

    public void setOptInputParameter(int index, Object value) {
        if (index == 0) {
            //set rsi time period
            cciTimePeriod = (Integer) value;
            cciIndicator.setOptInputParameter(0, (Integer) value);
        }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}