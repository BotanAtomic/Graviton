package graviton.common;

import lombok.Data;

/**
 * Created by Botan on 22/06/2015.
 */
@Data
public class Pair<T, L> {
    private T key;
    private L value;

    public Pair(T key, L value) {
        this.key = key;
        this.value = value;
    }
}
