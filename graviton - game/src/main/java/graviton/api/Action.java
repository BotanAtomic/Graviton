package graviton.api;


/**
 * Created by Botan on 03/10/2015.
 */
public interface Action {
    boolean start();

    void cancel();

    void onFail(String args);

    void onSuccess(String args);

    int getId();

    int getAction();
}
