package bgu.spl.mics;

public class RequestCompleted<T> implements Message {

    private Request<T> completed;     //CONTAIN THE REQUST THAT IS COMPLEATED
    private T result;

    public RequestCompleted(Request<T> completed, T result) {
        this.completed = completed;
        this.result = result;
    }

    public Request getCompletedRequest() {
        return completed;
    }

    public T getResult() {
        return result;
    }

}
