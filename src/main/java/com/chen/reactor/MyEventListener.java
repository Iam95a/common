package com.chen.reactor;

import java.util.List;

/**
 * @author goldgreat
 * @Date 2021-12-02
 */
public interface MyEventListener <T> {


    void onChunkData(List<T> data);

    void processComplete();
}
