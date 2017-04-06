/*
 * Copyright 2009 Dukascopy® (Suisse) SA. All rights reserved.
 * DUKASCOPY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.myStrategies;

/**
 * Created by: S.Vishnyakov
 * Date: Nov 17, 2009
 * Time: 12:05:49 PM
 */

import com.dukascopy.api.indicators.DoubleRangeDescription;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.api.indicators.IIndicatorContext;
import com.dukascopy.api.indicators.IndicatorInfo;
import com.dukascopy.api.indicators.IndicatorResult;
import com.dukascopy.api.indicators.InputParameterInfo;
import com.dukascopy.api.indicators.IntegerRangeDescription;
import com.dukascopy.api.indicators.OptInputParameterInfo;
import com.dukascopy.api.indicators.OutputParameterInfo;

public class PriceChannelIndicator implements IIndicator {
    private IndicatorInfo indicatorInfo;
    private InputParameterInfo[] inputParameterInfos;
    private OptInputParameterInfo[] optInputParameterInfos;
    private OutputParameterInfo[] outputParameterInfos;
    private double[][][] inputs = new double[1][][];
    private int timePeriod = 1;
    private double dev = 1.00001;
    private double[][] outputs = new double[2][];

    private IIndicator maxHighIndicator;
    private IIndicator minLowIndicator;

    public void onStart(IIndicatorContext context) {
        maxHighIndicator = context.getIndicatorsProvider().getIndicator("MAX");
        minLowIndicator = context.getIndicatorsProvider().getIndicator("MIN");

        indicatorInfo = new IndicatorInfo("MYPCHANNEL", "Price Channel", "Overlap Studies",
                true, false, false, 1, 2, 2);
        inputParameterInfos = new InputParameterInfo[] {
            new InputParameterInfo("Price", InputParameterInfo.Type.PRICE)
        };

        optInputParameterInfos = new OptInputParameterInfo[] {
            new OptInputParameterInfo("Time period", OptInputParameterInfo.Type.OTHER,new IntegerRangeDescription(1, 1, 100, 1)),
            new OptInputParameterInfo("Dev", OptInputParameterInfo.Type.OTHER, new DoubleRangeDescription(1.00001, 0.00001, 2.0, 0.00001, 5))
        };
        outputParameterInfos = new OutputParameterInfo[] {new OutputParameterInfo("Up", OutputParameterInfo.Type.DOUBLE,
                OutputParameterInfo.DrawingStyle.LINE),
                new OutputParameterInfo("Low", OutputParameterInfo.Type.DOUBLE,
                OutputParameterInfo.DrawingStyle.LINE)
       };
    }

    public IndicatorResult calculate(int startIndex, int endIndex) {
        //calculating startIndex taking into account lookback value
        if (startIndex - getLookback() < 0) {
            startIndex -= startIndex - getLookback();
        }

        if (startIndex > endIndex) {
            return new IndicatorResult(0, 0);
        }

        double[] maxHigh = new double[endIndex - startIndex + 2 + getLookback()];
        double[] minLow = new double[endIndex - startIndex + 2 + getLookback()];

        // high value for max
        maxHighIndicator.setInputParameter(0, inputs[0][2]);
        // low value for min
        minLowIndicator.setInputParameter(0, inputs[0][3]);

        maxHighIndicator.setOutputParameter(0, maxHigh);
        minLowIndicator.setOutputParameter(0, minLow);

        IndicatorResult dmaxHighResult = maxHighIndicator.calculate(startIndex - 1, endIndex);
        IndicatorResult dminLowResult = minLowIndicator.calculate(startIndex - 1, endIndex);

        int i, k;
        for (i = 1, k = dmaxHighResult.getNumberOfElements(); i < k; i++) {
            //Inputs: 0 open, 1 close, 2 high, 3 low, 4 volume
            outputs[0][i - 1] = maxHigh[i]*dev;
            outputs[1][i - 1] = minLow[i]/dev;

        }
        return new IndicatorResult(startIndex, i - 1);
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
        return timePeriod;
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
        inputs[index] = (double[][]) array;
    }

    public void setOptInputParameter(int index, Object value) {
        switch (index) {                
                case 0:
                    timePeriod = (Integer) value;
                    maxHighIndicator.setOptInputParameter(0, timePeriod);
                    minLowIndicator.setOptInputParameter(0, timePeriod);
                    break;                     
                case 1:
                    dev = (Double) value;                                    
                    break;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
    }

    public void setOutputParameter(int index, Object array) {
        outputs[index] = (double[]) array;
    }
}