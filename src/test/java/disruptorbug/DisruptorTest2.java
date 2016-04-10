package disruptorbug;


import org.junit.Before;
import org.junit.Test;


public class DisruptorTest2 {

    private DisruptorEmulator disruptor;

    private volatile long opId;


    @Before
    public void setUp() throws Exception {
        disruptor = new DisruptorEmulator(4, this::method1, this::method2);
    }


    private void method1(Event event) {
        try {
            event.testNPE();
        } catch (Exception e) {
            System.err.println("id = " + opId + " " + event);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void method2(Event event) {
        event.cleanup();
    }

    @Test
    public void test() throws Exception {
        opId = 0L;
        while (true) {
            long finalOpId = opId;
            Integer obj = new Integer(123);
            String str = "str";
            disruptor.publishEvent(
                    event -> event.publish(finalOpId, obj, str)
            );
            opId++;
        }

    }
}
