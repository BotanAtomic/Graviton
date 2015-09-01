package graviton.common;

import lombok.Data;

import java.util.List;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Pair<T, L> {
    T first;
    L second;

    public Pair(T first, L second) {
        this.first = first;
        this.second = second;
    }
}
