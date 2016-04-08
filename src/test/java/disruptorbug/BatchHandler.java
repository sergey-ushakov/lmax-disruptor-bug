package disruptorbug;


import java.util.function.Consumer;

import com.lmax.disruptor.DataProvider;


class BatchHandler<T>
{
    private final Consumer<T> consumer;


    BatchHandler(Consumer<T> consumer)
    {
        this.consumer = consumer;
    }


    public void processBatch(long begin, long end, DataProvider<T> buffer)
    {
        try
        {
            for (long i = begin; i <= end; i++)
            {
                consumer.accept(buffer.get(i));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
