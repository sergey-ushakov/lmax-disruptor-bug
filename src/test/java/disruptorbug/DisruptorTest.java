package disruptorbug;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventProcessorFactory;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.Before;
import org.junit.Test;


public class DisruptorTest
{
    private static final int disruptorSize = 4;

    private Disruptor<Event> disruptor;

    private volatile long opId;


    @Before
    public void setUp() throws Exception
    {
        ExecutorService executor = Executors.newCachedThreadPool();

        disruptor = new Disruptor<>(Event::new, disruptorSize, executor, ProducerType.MULTI, new SleepingWaitStrategy());

        BatchHandler<Event> bh1 = new BatchHandler<>(this::method1);
        BatchHandler<Event> bh2 = new BatchHandler<>(this::method2);

        disruptor
                .handleEventsWith(
                        new BatchingEventProcessor<>(disruptor.getRingBuffer().newBarrier(), disruptor.getRingBuffer(), bh1)
                )
                .then(
                        (EventProcessorFactory<Event>) (
                        ringBuffer, barrierSequences) ->
                        new BatchingEventProcessor<>(ringBuffer.newBarrier(barrierSequences), ringBuffer, bh2)
                );

        disruptor.start();
    }


    private void method1(Event event)
    {
        try
        {
            event.obj.toString();
        }
        catch (Exception e)
        {
            System.err.println("id = " + opId);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void method2(Event event)
    {
        try
        {
            event.id = 0;
            event.obj = null;
            event.str = null;
        }
        catch (Exception e)
        {
            System.err.println("id = " + opId);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Test
    public void test() throws Exception
    {
        opId = 0L;
        while (true)
        {
            long finalOpId = opId;
            Integer obj = new Integer(123);
            String str = "str";

            disruptor.publishEvent((event, sequence) -> {
                event.id = finalOpId;
                event.obj = obj;
                event.str = str;
            });
            opId++;
        }

    }
}
