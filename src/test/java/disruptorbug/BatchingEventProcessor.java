package disruptorbug;


import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;


class BatchingEventProcessor<T> implements EventProcessor
{

    private final Sequence sequence;

    private final SequenceBarrier barrier;

    private final BatchHandler<T> batchHandler;

    private DataProvider<T> dataProvider;

    private RingBuffer<StatusChangedEvent> ringBuffer;

    private long startTime;



    BatchingEventProcessor(SequenceBarrier barrier, DataProvider<T> dataProvider, BatchHandler<T> batchHandler)
    {
        this.barrier = barrier;
        this.dataProvider = dataProvider;
        this.batchHandler = batchHandler;
        sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
 
        this.startTime = System.currentTimeMillis();
        ringBuffer = RingBuffer.createSingleProducer(StatusChangedEvent::new, 1024);
        // todo: uncomment to hide bug 
        //ringBuffer = RingBuffer.createMultiProducer(StatusChangedEvent::new, 1024);
    }


    @Override
    public Sequence getSequence()
    {
        return sequence;
    }


    @Override
    public void halt()
    {
        barrier.alert();
    }


    @Override
    public boolean isRunning()
    {
        return true;
    }


    @Override
    public void run()
    {
        long nextSequence = sequence.get() + 1L;
        while (true)
        {
            try
            {
                currentStateChanged();
                final long availableSequence = barrier.waitFor(nextSequence);

                if (nextSequence <= availableSequence)
                {
                    currentStateChanged();

                    batchHandler.processBatch(nextSequence, availableSequence, dataProvider);

                    nextSequence = availableSequence + 1;
                }
                sequence.set(availableSequence);
            }
            catch (Exception e)
            {
            }
        }
    }

    void currentStateChanged()
    {
        long currentTime = System.currentTimeMillis();

        if (startTime != currentTime)
        {
            ringBuffer.publishEvent((event, sequence) -> event.finishedAt = currentTime);
        }

        startTime = currentTime;
    }

}
