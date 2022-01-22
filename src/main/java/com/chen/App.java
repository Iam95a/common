package com.chen;

import com.chen.reactor.MyEventListener;
import com.chen.reactor.MyEventProcessor;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        MyEventProcessor myEventProcessor = new MyEventProcessor() {
            private MyEventListener<String> myEventListener;


            @Override
            public void register(MyEventListener<String> listener) {
                this.myEventListener = listener;
            }

            @Override
            public void dataChunk(String... values) {

                myEventListener.onChunkData(Arrays.asList(values));
            }

            @Override
            public void processComplete() {
                myEventListener.processComplete();

            }
        };
        Flux<String> bridge = Flux.create(sink -> {
            myEventProcessor.register(new MyEventListener<String>() {
                @Override
                public void onChunkData(List<String> data) {
                    for (String s : data) {
                        sink.next(s);
                    }
                }

                @Override
                public void processComplete() {

                    sink.complete();
                }
            });
        });
        bridge.subscribe(System.out::println, System.out::println, () -> {
            System.out.println("down");
        });
        myEventProcessor.dataChunk("foo", "bar", "2000");
        myEventProcessor.processComplete();

        Thread.sleep(2000);
    }


}
