/**
 * To allow for a React component to subscribe to changes in individual data structures, extend the Subscribable interface..
 * 
 * This allows for data changes in the application to be propagated across the application from one central store.
 * 
 * Rather than using useStates in every component and passing down that data.
 * 
 * @link https://react.dev/reference/react/useSyncExternalStore
 */
export class Subscribable {
   /**
    * A list of callback functions that is provided when the subscribe function is called.
    */
   listeners: Function[] = []
   /**
    * Allows for a variable in a component to subscribe to this data structure. 
    * 
    * @param listenerToAdd A getter function for a attribute in the data structure.
    * @returns A function that unsubscribes this listener when the component is un-rendered.
    */
   subscribe = (listenerToAdd: Function) => {
      this.listeners = [...this.listeners, listenerToAdd];
      return () => {
         this.listeners = this.listeners.filter(currentListener => currentListener !== listenerToAdd);
      };
   }
   /**
    * Whenever a setter is used on the data structure, call emit change to notify the subscriptions in the render loop.
    */
   emitChange = () => {
      for (let listenerToUpdate of this.listeners) {
         listenerToUpdate();
      }
   }
}