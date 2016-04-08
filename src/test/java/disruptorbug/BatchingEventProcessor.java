package disruptorbug;


import java.util.function.Consumer;

import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;


class BatchingEventProcessor<T> implements EventProcessor
{

    private final Sequence sequence;

    private final SequenceBarrier barrier;

    private final Consumer<Event> consumer;

    private DataProvider<Event> dataProvider;



    BatchingEventProcessor(SequenceBarrier barrier, DataProvider<Event> dataProvider, Consumer<Event> consumer)
    {
        this.barrier = barrier;
        this.dataProvider = dataProvider;
        this.consumer = consumer;
        sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
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
                final long availableSequence = barrier.waitFor(nextSequence);

                if (nextSequence <= availableSequence)
                {
                    for (long i = nextSequence; i <= nextSequence; i++)
                    {
                        consumer.accept(dataProvider.get(i));
                    }

                    nextSequence = availableSequence + 1;
                    sequence.set(availableSequence);
                }
            }
            catch (Exception e)
            {
            }
        }
    }

}
