package disruptorbug;


class Event
{
    long id;
    Object obj;
    String str;

    // debug timings
    long publishStarted;
    long publishFinished;
    long cleanupStarted;
    long cleanupFinished;
    long testStarted;
    long testFinished;


    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", obj=" + obj +
                ", str='" + str + '\'' +
                ", publishStarted=" + publishStarted +
                ", publishFinished=" + publishFinished +
                ", cleanupStarted=" + cleanupStarted +
                ", cleanupFinished=" + cleanupFinished +
                ", testStarted=" + testStarted +
                ", testFinished=" + testFinished +
                '}';
    }

    synchronized // CR
    void publish(long id, Object obj, String str)
    {
        publishStarted = System.nanoTime();
        this.id = id;
        this.obj = obj;
        this.str = str;
        publishFinished = System.nanoTime();
    }

    synchronized // CR
    void testNPE()
    {
        testStarted = System.nanoTime();
        obj.toString();
        testFinished = System.nanoTime();
    }

    synchronized // CR
    void cleanup()
    {
        cleanupStarted = System.nanoTime();
        id = 0;
        obj = null;
        str = null;
        cleanupFinished = System.nanoTime();
    }
}
