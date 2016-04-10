package disruptorbug;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author Pavel Rogulin at 10.04.16 0:59.
 */
public class DisruptorEmulator {
    // let's emulate two handler
    private final Consumer<Event> tester;
    private final Consumer<Event> cleaner;

    // ring buffer & size
    private final int bufferSize;
    private final Event[] ringBuffer;

    // moved after event is published, looks like head
    private volatile int published = 0;

    // moved after event is published (tester consumer is called), looks like transit position
    private volatile int tested = 0;

    // moved after event is cleaned (cleaner consumer is called), looks like tail
    private volatile int finished = 0;

    public DisruptorEmulator(int size, Consumer<Event> tester, Consumer<Event> cleaner) {
        this.tester = tester;
        this.cleaner = cleaner;
        this.ringBuffer = createRingBuffer(size);
        this.bufferSize = size;

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(this::testerThread);
        executorService.submit(this::cleanerThread);
    }

    void publishEvent(Consumer<Event> consumer) {
        int p = next(published);
        // awaiting space for publishing
        while (p == finished) {
        }
        consumer.accept(ringBuffer[published]);
        published = p;
    }

    private void testerThread() {
        while (true) {
            int h = next(tested);
            // awaiting space to move event from test to cleaner
            while (h == finished) {
            }
            //awaiting task for NPE tester
            while (published == tested) {
            }

            tester.accept(ringBuffer[tested]);
            tested = h;
        }
    }

    private void cleanerThread() {
        while (true) {
            // no condition to await free space
            //awaiting task for cleaner
            while (tested == finished) {
            }
            cleaner.accept(ringBuffer[finished]);
            finished = next(finished);
        }
    }

    // next position in cycle buffer
    private int next(int p) {
        return ++p < bufferSize ? p : 0;
    }

    // just init buffer
    private Event[] createRingBuffer(int size) {
        Event[] events = new Event[size];
        for (int i = 0; i < events.length; i++) {
            events[i] = new Event();
        }
        return events;
    }
}
