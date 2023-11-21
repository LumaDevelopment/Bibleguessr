/**
 * To allow for a React component to subscribe to changes in individual data structures, extend the Subscribable interface..
 * @link https://react.dev/reference/react/useSyncExternalStore
 */
export class Subscribable {
   /**
    * A list of callback functions that is provided when the subscribe function is called.
    */
   listeners: Function[] = []
   subscribe = (listenerToAdd: Function) => {
      this.listeners = [...this.listeners, listenerToAdd];
      return () => {
         this.listeners = this.listeners.filter(currentListener => currentListener !== listenerToAdd);
      };
   }
   emitChange = () => {
      for (let listenerToUpdate of this.listeners) {
         listenerToUpdate();
      }
   }
}