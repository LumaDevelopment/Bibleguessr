/**
 * To allow for a React component to subscribe to changes in individual data structures, extend the Subscribable interface..
 * @link https://react.dev/reference/react/useSyncExternalStore
 */
export class Subscribable {
    /**
     * A list of callback functions that is provided when the subscribe function is called.
     */
    listeners: Function[] = []
    subscribe = (newListener: Function) => {
        this.listeners = [...this.listeners, newListener];
        return () => {
            this.listeners = this.listeners.filter(l => l !== newListener);
        };
    }
    emitChange = () => {
        for (let listener of this.listeners) {
            listener();
        }
    }
}