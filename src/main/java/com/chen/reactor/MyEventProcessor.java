package com.chen.reactor;

/**
 * @author goldgreat
 * @Date 2021-12-02
 */
public interface MyEventProcessor {
    void register(MyEventListener<String> listener);

    void dataChunk(String... values);

    void processComplete();
}
