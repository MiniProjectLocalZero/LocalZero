package se.mau.localzero.auth.observer;

public interface Observable {
    void attach(SessionObserver observer);
    void detach(SessionObserver observer);
    void notifyAllObservers(SessionEvent event);
}
