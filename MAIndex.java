/*
 * Copyright 2009 DukascopyР вЂ™Р’В® (Suisse) SA. All rights reserved.
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
import com.dukascopy.api.indicators.IntegerListDescription;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;


public class MAIndex implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private IIndicatorsProvider indicatorsProvider;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private static IConsole console;
    //Price includes 5 arrays: open, close, high, low, volume
    private double[][][] inputsPriceArr = new double[1][][]; 
    //price array depending on AppliedPrice
    private double[][] cciInputsDouble = new double[1][];
    private double[][] outputs = new double[1][];

    private int maType;
    
    IIndicator maIndicator;
    int maTimePeriod = 14;
    int maIndexPeriod = 14;
   
    
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
        
        int optInputsCount = 3;// Indicator input parameters count
        int optOutputsCount = 1;
        int numberOfInputs = 2;
        boolean unstablePeriod = true;
        boolean overChart = false;
        boolean overVolumes = true;

        indicatorInfo = new IndicatorInfo("MAIndex", "RSI signals", "Custom indicators", overChart, overVolumes, unstablePeriod, numberOfInputs, optInputsCount, optOutputsCount);
        inputParameterInfos = new InputParameterInfo[] { 
                new InputParameterInfo("Price arrays", InputParameterInfo.Type.PRICE),
                new InputParameterInfo("Price double", InputParameterInfo.Type.DOUBLE)
            };
        int[] maValues = new int[IIndicators.MaType.values().length];
            String[] maNames = new String[IIndicators.MaType.values().length];
            for (int i = 0; i < maValues.length; i++) {
                maValues[i] = i;
                maNames[i] = IIndicators.MaType.values()[i].name();
            }
        optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("Period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(maTimePeriod, 1, 2000, 1)),                                
                new OptInputParameterInfo("MA type", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(IIndicators.MaType.SMA.ordinal(), maValues, maNames)),
                new OptInputParameterInfo("Index Period", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(maIndexPeriod, 1, 2000, 1))
        };
        
        outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Output", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE)           
            };

        indicatorsProvider = context.getIndicatorsProvider();
        maIndicator = indicatorsProvider.getIndicator("MA");
        
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        //println("startIndex: "+startIndex);
        //println("endIndex: "+endIndex);
        
        // calculating rsi
        double[] rsiOutput = new double[endIndex - startIndex + 1];
        double[] calculations = new double[endIndex - startIndex + 1];
        maIndicator.setInputParameter(0, cciInputsDouble[0]);
        maIndicator.setOutputParameter(0, rsiOutput);
        maIndicator.calculate(startIndex, endIndex);


        int i, j, c;
        double ima, sum;
        for (i = startIndex, j = 0; i <= endIndex; i++, j++) {
            if (j > maIndexPeriod) {
                sum = 0d;
                for (c = 0; c < maIndexPeriod; c++) {
                    sum+=rsiOutput[j-c];
                }
                ima = rsiOutput[j]/(sum/maIndexPeriod)-1;
                //calculations[j] = ima;
                //ima = (ima-calculations[j-1])/calculations[j-1];
                outputs[DOWN][j] = ima;
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
        return maTimePeriod;
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
                inputsPriceArr[0] = (double[][]) array;
                break;                    
            case 1:
                cciInputsDouble[0] = (double[]) array;                                      
                break;              
            default:
                throw new ArrayIndexOutOfBoundsException(index);
            }
    }

    public void setOptInputParameter(int index, Object value) {
         switch (index) {                
            case 0:
                maTimePeriod = (Integer) value; 
                maIndicator.setOptInputParameter(0, maTimePeriod);
                break;                    
            case 1:
                maType = (Integer) value;
                maIndicator.setOptInputParameter(1, maType);                                      
                break;
            case 2:
                maIndexPeriod = (Integer) value;                                      
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(index);
            }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}