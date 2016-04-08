package disruptorbug;


class Event
{
    long id;
    volatile Object obj;
    String str;

    // debug timings
    long publishStarted;
    long publishFinished;
    long cleanupStarted;
    long cleanupFinished;


    @Override
    public String toString()
    {
        return "Event{" +
                "id=" + id +
                ", obj=" + obj +
                ", str='" + str + '\'' +
                ", publishStarted=" + publishStarted +
                ", publishFinished=" + publishFinished +
                ", cleanupStarted=" + cleanupStarted +
                ", cleanupFinished=" + cleanupFinished +
                '}';
    }


    synchronized void publish(long id, Object obj, String str)
    {
        publishStarted = System.nanoTime();
        this.id = id;
        this.obj = obj;
        // todo: uncomment this line to get differ 'magic' opId count
        // this.str = str;
        publishFinished = System.nanoTime();
    }

    void testNPE()
    {
        obj.toString();
    }

    synchronized void cleanup()
    {
        cleanupStarted = System.nanoTime();
        id = 0;
        obj = null;
        str = null;
        cleanupFinished = System.nanoTime();
    }
}
