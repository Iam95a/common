package com.chen;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * @author goldgreat
 * @Date 2021-12-02
 */
public class ReactorWindowDemo {
    public static void main(String[] args) throws Exception {
        DataEmitterHolder deh = new DataEmitterHolder();
        Flux<String> source = Flux.create(sink -> {

            deh.register(new DataEmitter() {
                @Override
                public void receiveData(String value) {
                    sink.next(value);
                }

                @Override
                public void complete() {
                    sink.complete();
                }
            });
        });
        source
                .map(Integer::valueOf)
                //先把数据按100ms分为一组
                .window(Duration.ofMillis(1000))
                .flatMap(a -> {
                    return a.filter(l -> l > 0).reduce(Integer::sum);
                })
                //以十个桶作为长度 1作为步长
                .window(10,1)
                .flatMap(a->a.reduce(Integer::sum))
                .map(l -> {
                    return l;
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe(System.out::println, System.out::println,
                        () -> {
                            System.out.println("down");
                        });
//        Thread.sleep(2000);
        for (int i = 0; i < 1000000; i++) {
            deh.dataEmit("a" + i);
        }
        deh.dataEmit("a");
        deh.dataEmit("b");
        Thread.sleep(2000);
    }


}
