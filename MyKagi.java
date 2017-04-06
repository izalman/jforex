/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myStrategies;

        
import com.dukascopy.api.IConsole;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.indicators.DoubleRangeDescription;
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
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

    
    /**
     * <b>NOTE: </b> The calculate logic of this indicator is implemented in JavaScript.
     * Please, update the corresponding JS code in case of updating of this class.
     * 
     * @author anatoly.pokusayev
     *
     */

     enum REVERSAL_MODE {
        PIPS(0),    
        PERCENT(1);  

         public final int index;

        private REVERSAL_MODE(int index) {
            this.index = index;
        }
    };

    

    public class MyKagi implements IIndicator {
                
        private int pips = 35;
        private double percent = 0.01;
        private int shiftWeight = -2;
        private int mode = REVERSAL_MODE.PIPS.index;
        private boolean firstRun = true;
        private double reversal = 0;
        private IIndicatorsProvider indicatorsProvider;        
        private IndicatorInfo indicatorInfo;
        private InputParameterInfo[] inputParameterInfos;
        private OutputParameterInfo[] outputParameterInfos;
        private OptInputParameterInfo[] optInputParameterInfos;
        private static PrintStream out;
        private IConsole console;
        private double[][] inputs = new double[1][];
        private double[][] outputs = new double[1][];  // 0-Line
        
        private double KagiBuffer[];
        private double Buffer[];
        
         //Method prints message to console
        public void println(String message) {
            out.println(message);
            //System.out.println(message);
        }

         //Method prints message to console
        public void print(String message) {
            out.print(message);
            //System.out.print(message);
        }
    
        public void onStart(IIndicatorContext context) { 
            indicatorsProvider = context.getIndicatorsProvider();
            console = context.getConsole();
            out = console.getOut();
            indicatorInfo = new IndicatorInfo("MyKagi", "MyKagi", "Custom", true, false, true, 1, 4, 1);
            inputParameterInfos = new InputParameterInfo[] {new InputParameterInfo("Price", InputParameterInfo.Type.DOUBLE)};
            
            if (context.getFeedDescriptor()!=null) {
                inputParameterInfos[0].setInstrument(context.getFeedDescriptor().getInstrument());
            } else {
                inputParameterInfos[0].setInstrument(Instrument.EURUSD);  
            }
            
            int[] RValues = new int[REVERSAL_MODE.values().length];
            String[] RNames = new String[REVERSAL_MODE.values().length];
            for (REVERSAL_MODE mode: REVERSAL_MODE.values()) {
                RValues[mode.index] = mode.index;
                RNames[mode.index] = mode.name();
            }
            
            optInputParameterInfos = new OptInputParameterInfo[] {
                new OptInputParameterInfo("Mode", OptInputParameterInfo.Type.OTHER, new IntegerListDescription(REVERSAL_MODE.PIPS.index, RValues, RNames)),                               
                new OptInputParameterInfo("Pips", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(35, 1, 1000, 1)),
                new OptInputParameterInfo("Percents", OptInputParameterInfo.Type.OTHER, new DoubleRangeDescription(0.01, 0.01, 1.00, 0.01, 2)),
                new OptInputParameterInfo("Weight", OptInputParameterInfo.Type.OTHER, new IntegerRangeDescription(0, 0, 30, 1))
                    
            };
            outputParameterInfos = new OutputParameterInfo[] {
                new OutputParameterInfo("Output", OutputParameterInfo.Type.DOUBLE, OutputParameterInfo.DrawingStyle.LINE)           
            };
        }
        
        public IndicatorResult calculate(int startIndex, int endIndex) {            
            if (startIndex - getLookback() < 0) {
                startIndex -= startIndex - getLookback();
            }
            
            if (startIndex > endIndex) {
                return new IndicatorResult(0, 0);
            }
            
            if (Buffer!=null && endIndex > (Buffer.length-10)) {
                firstRun = true;
            }
            //println("START: "+String.valueOf(startIndex));
            //println("END: "+String.valueOf(endIndex));

            int resIndex =0; 
            for (int i = startIndex; i <= endIndex; i++) {
                outputs[0][resIndex] = Double.NaN;
            }
            

            int shift = endIndex-1; 
            double[] price = inputs[0];
            
            if (endIndex != startIndex && endIndex >=10 && firstRun) {
                firstRun = false;
                KagiBuffer[endIndex] = price[endIndex];
                reversal = getReversal(price[endIndex]);
                    
                    for (int i = endIndex; i >= startIndex; i--) {
                        //println("REVERSAL INDX: "+i+" PRICE: "+price[i] +" SHIFT: "+shift);
                        if (i+shiftWeight <=endIndex) {
                            while (shift >= startIndex && Math.abs(price[shift]-price[i+shiftWeight]) < reversal) {
                                KagiBuffer[shift] = price[i+shiftWeight];
                                shift--;
                            }
                        } else {
                            while (shift >= startIndex && Math.abs(price[shift]-price[i]) < reversal) {
                                KagiBuffer[shift] = price[i]; 
                                shift--;
                            }
                        }
                        if (shift>=startIndex) {

                            if (i+shiftWeight <=endIndex) {
                                KagiBuffer[shift] = price[i+shiftWeight];
                            } else { KagiBuffer[shift] = price[i]; }
 
                            i=shift;
                            shift--;
                        } else {
                            break;
                        }  
                    }

                    Buffer = KagiBuffer;
                    
            } else if (endIndex >=10 && !firstRun) {
                int j = KagiBuffer.length-1;
                int b = Buffer.length-1;
                KagiBuffer[j] = Buffer[b];
                if (b+shiftWeight <Buffer.length-1) {
                    if (Math.abs(Buffer[b+shiftWeight] - price[endIndex]) > reversal) {
                            reversal = getReversal(price[endIndex]);
                            Buffer[b] = price[endIndex];
                            KagiBuffer[j] = Buffer[b];
                    }
                } else {
                    if (Math.abs(Buffer[b] - price[endIndex]) > reversal) {
                            reversal = getReversal(price[endIndex]);
                            Buffer[b] = price[endIndex];
                            KagiBuffer[j] = Buffer[b];
                    }
                }
                j--;
                b--;
                while (j >0) {
                        j--;
                        b--;
                        if (b+shiftWeight <Buffer.length-1) {
                            Buffer[b] = Buffer[b+1+shiftWeight];
                            KagiBuffer[j] = Buffer[b];
                        } else {
                            Buffer[b] = Buffer[b+1];
                            KagiBuffer[j] = Buffer[b];
                        }
                }

            }
            
            for (resIndex = 0; resIndex <= KagiBuffer.length-1; resIndex++) {
                   outputs[0][resIndex] = KagiBuffer[resIndex];
            }
            
            
                
            return new IndicatorResult(startIndex, resIndex);
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
    
        public OutputParameterInfo getOutputParameterInfo(int index) {
            if (index <= outputParameterInfos.length) {
                return outputParameterInfos[index];
            }
            return null;
        }
    
        public void setInputParameter(int index, Object array) {
            inputs[index] = (double[]) array;
        }
    
        public void setOutputParameter(int index, Object array) {
            outputs[0] = (double[]) array;
            KagiBuffer = (double[]) array;
        }
    
        public OptInputParameterInfo getOptInputParameterInfo(int index) {
            if (index <= optInputParameterInfos.length) {
                return optInputParameterInfos[index];
            }
            return null;
        }
    
        public void setOptInputParameter(int index, Object value) {            
            switch (index) {                
                case 0:
                    mode = (Integer)value;
                    break;                    
                case 1:
                    pips = (Integer)value;                                        
                    break;
                case 2:
                    percent = (Double)value;
                    break;
                case 3:
                    shiftWeight = (Integer)value;
                    break;
                default:
                    throw new ArrayIndexOutOfBoundsException(index);
            }
            
        }
        
        
        private double getReversal(double price) {

            if ( mode == REVERSAL_MODE.PIPS.index ) {
               return(pips*inputParameterInfos[0].getInstrument().getPipValue());
            }
            //println("PRICE: "+price);
            //println("INSTRUMENT: "+inputParameterInfos[0].getInstrument()); 
            return BigDecimal.valueOf((price/100)*percent).setScale(inputParameterInfos[0].getInstrument().getPipScale()+1, RoundingMode.FLOOR).doubleValue();

        }
        
        
        
        
        
        
        
        
        
        
    }
