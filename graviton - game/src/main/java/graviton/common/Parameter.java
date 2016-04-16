package graviton.common;

import lombok.Data;

/**
 * Created by Botan on 15/04/2016.
 */
@Data
public class Parameter<A,B,C,D> {
    private A first;
    private B second;
    private C third;
    private D fourth;

    public Parameter(A first, B second,C third, D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }
}
