package com.gy.vam.uhflib;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by RuanJian-GuoYong on 2017/9/28.
 */


public class EpcDataConvertUtils {
    private static final String TAG = "EpcDataConvertUtils";

    public static final int PRE_LENGTH = 2;

    //解析操作
    public static String getReadableDecodedEpcData(String strHexEpc){
        return getIDByEpcData(strHexEpc);
    }

    /**
     * [解析操作]
     * 格式规范性校验，校验不通过:1.十六进制数的长度是否为24，2.头两个二进制数是否小于或等于（24-2）
     *
     * 把EPC上的Hex进制的字符转换成身份识别码
     * 14-4e524942-21183916C030-00
     * */
    public static String getIDByEpcData(String strHex){
        String strHexSkipBlanks = strHex.replace(" ","");
        //获取有效数据位数(这里需要做数值校验，如果头两位为AF呢，就不知用字符转int了)
        String ss = "0x"+strHexSkipBlanks.substring(0,PRE_LENGTH);
        int validLength = Integer.decode(ss);
        System.out.println("有效长度位值 = "+validLength);

        if (!isValidEPCSkipBlanks(strHexSkipBlanks)){
            return "无效数据";
        }
        //获取到有效的EPC区数据（排除前两位的有效位计数值）
        String validStr = strHexSkipBlanks.substring(2,validLength+2);

        //获取厂家名称代号的十六进制码,TODO:转换为字符型
        String mid_factoryHex = validStr.substring(0,8);
        StringBuilder mid_factoryStr = new StringBuilder();

        byte[] mid_factoryBytes = hexStr2Bytes(mid_factoryHex);
        String mid_fac = new String(mid_factoryBytes);
        mid_factoryStr.append(mid_fac);

        //获取后边的十六进制码
        String mid_code = validStr.substring(8, validLength);//8是厂家编号占的位数

        mid_factoryStr.append(mid_code);

        return mid_factoryStr.toString();
    }
    /**
     * bytes字符串转换为Byte值
     * @param  src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m=0,n=0;
        int l=src.length()/2;
        //System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++)
        {
            m=i*2+1;
            n=m+1;
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
        }
        return ret;
    }

    /**
     * @param strHexSkipBlanks
     * 通过不带空格间隔的十六进制的EPC数据 @strHexSkipBlanks来判断这个电子标签是否是有效的数据，
     * 即可不可以对应一个台账信息..
     * 14-4e524942-21183916C030-00
     * 判断条件：
     * a.前两个十六进制字符0-1转换成十进制表示有效数据位，需要>字符(去空格)的长度-2；
     * b.后八个十六进制字符2-9转化成四个字母后表示厂家名称的简称，且转换出来的字母都必须为大写
     * c.有效位起始点-有效位终止点,需要都是十六进制的数值
     * */
    public static boolean isValidEPCSkipBlanks(String strHexSkipBlanks){
        boolean isValid = true;
        //获取有效数据位数(这里需要做数值校验，如果头两位为AF呢，就不知用字符转int了)
        String ss = "0x"+strHexSkipBlanks.substring(0,PRE_LENGTH);
        int validLength = Integer.decode(ss);//有效长度必须得大于或等于8，即厂家的代号必须要有，4个字母。
        //不带“0x”的话可以用:Integer.parseInt("F2", 16);

        if(validLength+2>strHexSkipBlanks.length()){
            /*Log.d(TAG, "isValidEPCSkipBlanks:*有效位超出最大支持值* (validLength+2)="+(validLength+2)
                    +"，strHexSkipBlanks.length()="+strHexSkipBlanks.length());*/
            System.out.println(""+"isValidEPCSkipBlanks:*有效位超出最大支持值* (validLength+2)="+(validLength+2)
                    +"，strHexSkipBlanks.length()="+strHexSkipBlanks.length());
            isValid &= false;
            return isValid;
        } else {
        }

        //获取到有效的EPC区数据
        String validStr = strHexSkipBlanks.substring(2,validLength+2);

        //获取厂家名称代号的十六进制码,TODO:转换为字符型
        String mid_factoryHex = validStr.substring(0,8);
        byte[] mid_factoryBytes = hexStr2Bytes(mid_factoryHex);
        //b判断厂家代号是不是都是大写字母,有一个不是即false
        for (int i = 0; i < mid_factoryBytes.length ; i++) {
            if (mid_factoryBytes[i]<0x41 || mid_factoryBytes[i]>0x5a){
                isValid &= false;
                break;
            }
        }
        //c.识别码编号是否为0-F(十六进制)[必须validLength大于8的时候才会有这一步]
        if(validLength > 8){
            String after_codeHex = strHexSkipBlanks.substring(8+2, validLength+2);
            System.out.println(after_codeHex);
            String regex="^[A-Fa-f0-9]+$";
            //String regex="^[A-Fa-f0-9]+$";
            if(!after_codeHex.matches(regex)){
                isValid &= false;
            }
        }

        return isValid;
    }

    /**
     * @param strHex
     * 通过带空格间隔的十六进制的EPC数据 @strHex来判断这个电子标签是否是有效的数据，
     * 即可不可以对应一个台账信息.
     * */
    public static boolean isValidEPCNotSkipBlanks(String strHex){
        //获取有效数据位数
        return isValidEPCSkipBlanks(strHex.replace(" ",""));
    }
}
