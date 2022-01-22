package com.chen;

/**
 * @author goldgreat
 * @Date 2021-11-28
 */
public interface DataEmitter {

    void receiveData(String value);

    void complete();
}
