package com.korimart.f12;

import java.util.Collection;
import java.util.Comparator;

public enum LinearTimeHelper {
    INSTANCE;

    public <T> int indexOf(Collection<T> list, T matching, Comparator<T> comparator){
        int i = 0;
        for (T e : list){
            if (comparator.compare(e, matching) == 0)
                return i;
            i++;
        }

        return -1;
    }

    public <T, S> int indexOf(Collection<T> list, S matching, Comparator2<T, S> comparator){
        int i = 0;
        for (T e : list){
            if (comparator.compare(e, matching) == 0)
                return i;
            i++;
        }

        return -1;
    }
}

interface Comparator2<T, S> {
    int compare(T t, S s);
}
