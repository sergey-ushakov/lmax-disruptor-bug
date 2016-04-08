package disruptorbug;


class Event
{
    long id;
    Object obj;
    String str;


    @Override
    public String toString()
    {
        return "Event{" +
                "id=" + id +
                ", obj=" + obj +
                ", str='" + str + '\'' +
                '}';
    }
}
