package emr.analytics.models.structures;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Queue that keeps the last x values, where is the capacity
 * @param <T>
 */
public class CircularQueue<T> implements Serializable {

    private final T[] queue;
    private int capacity;
    private int head;
    private int size;

    public CircularQueue(Class<T> type, int capacity){
        this.capacity = capacity;
        this.head = 0;
        this.size = 0;

        @SuppressWarnings("unchecked")
        final T[] temp = (T[])Array.newInstance(type, capacity);
        queue = temp;
    }

    public void add(T item){

        if (this.size < this.capacity){
            this.queue[this.head + this.size] = item;
            this.size++;
        }
        else{
            this.queue[this.head] = item;
            this.head = (this.head + 1) % this.capacity;
        }
    }

    public void add(T[] items){

        for (T item : items)
            this.add(item);
    }

    public void add(List<T> items){

        for (T item : items)
            this.add(item);
    }

    public T getLast(){

        T item;
        if (this.size == 0){
            item = null;
        }
        else if (this.size < this.capacity)
            item = this.queue[this.head + (this.size - 1)];
        else{
            if (this.head == 0)
                item = this.queue[this.capacity - 1];
            else
                item = this.queue[this.head - 1];
        }

        return item;
    }

    public List<T> getList(){

        List<T> items = new ArrayList<T>();
        for(int i = 0; i < this.size; i++){

            items.add(this.queue[(this.head + i) % this.capacity]);
        }

        return items;
    }

    public int getSize(){ return this.size; }
}
