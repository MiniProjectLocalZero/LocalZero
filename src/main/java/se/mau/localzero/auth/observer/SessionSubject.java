package se.mau.localzero.auth.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SessionSubject implements Observable {
    private List<SessionObserver> observers = new ArrayList<>();

    public SessionSubject() {
        this.observers.add(new FileLoggerObserver());
    }

    @Override
    public void attach(SessionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(SessionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyAllObservers(SessionEvent event) {
        for (SessionObserver observer : observers) {
            observer.update(event);
        }
    }
}
