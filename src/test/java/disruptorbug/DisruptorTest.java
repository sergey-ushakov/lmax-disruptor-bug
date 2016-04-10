package disruptorbug;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
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

        disruptor = new Disruptor<>(Event::new, disruptorSize, executor, ProducerType.SINGLE, new BusySpinWaitStrategy());
        disruptor.handleEventsWith(this::handler1).then(this::handler2);
        disruptor.start();
    }

    private void handler1(Event event, long sequence, boolean endOfBatch)
    {
        try
        {
            event.testNPE();
        }
        catch (Exception e)
        {
            System.err.println("id = " + opId + " " + event);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void handler2(Event event, long sequence, boolean endOfBatch)
    {
        event.cleanup();
    }


    @Test
    public void test() throws Exception
    {
        opId = 0L;
        while (true)
        {
//            long finalOpId = opId;
            long finalOpId = System.nanoTime();
            // todo: change to int to hide bug
            // int finalOpId = (int)opId;

            Integer obj = new Integer(123);
            // todo: uncomment to hide bug
            //Object obj = new Integer(123);
            String str = "str";

            disruptor.publishEvent(
                    (event, sequence) -> event.publish(finalOpId, obj, str)
            );

            opId++;
        }

    }
}
