package mylib;

/**
 * Created by RuanJian-GuoYong on 2017/8/4.
 */

public class GyFunction {
    static {
        System.loadLibrary("serial_port");
    }
    public native String getString();
}
