package gg.bibleguessr.bible.versions;

import gg.bibleguessr.bible.data_structures.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keeps track of what versions of the Bible are
 * available to the service, and notifies interested
 * classes if the list changes.
 */
public class BibleVersionMgr {

   /* ---------- CONSTANTS ---------- */

   /**
    * The logger name for this class.
    */
   public static final String LOGGER_NAME = BibleVersionMgr.class.getSimpleName();

   /* ---------- INSTANCE VARIABLES ---------- */

   /**
    * The logger for this class.
    */
   private final Logger logger;

   /**
    * Keeps track of all available versions of the
    * Bible. Map from version name to Version object.
    */
   private Map<String, Version> availableVersions;

   /**
    * All objects interested in changes to the
    * list of available versions.
    */
   private final List<VersionsUpdateListener> listeners;

   /* ---------- CONSTRUCTOR ---------- */

   /**
    * Creates a new BibleVersionMgr object.
    */
   public BibleVersionMgr() {
      this.logger = LoggerFactory.getLogger(LOGGER_NAME);
      this.availableVersions = new HashMap<>(1);
      this.listeners = new LinkedList<>();
   }

   /* ---------- METHODS ---------- */

   /**
    * Adds the given version to the list of
    * available versions, and notifies all
    * listeners of this class about the change.
    *
    * @param version The new version to add.
    */
   public synchronized void addAvailableVersion(Version version) {

      if (version == null) {
         // Invalid usage.
         return;
      }

      // Store new version in internal map
      availableVersions.put(version.getName(), version);

      // Notify all listeners of new
      // version added, with object
      for (VersionsUpdateListener listener : listeners) {
         try {
            listener.onNewVersionAdded(version);
         } catch (Exception e) {
            // If one class has an issue, we don't want the entire
            // function to stop.
            // Grab the name of the class that threw the exception and report it.
            String name = listener.getClass().getSimpleName();
            logger.error(name + " encountered an exception while adding new version.", e);
         }
      }

      // After all listeners are done with operations,
      // report success
      logger.debug("New available version added: {}", version.getName());

   }

   /**
    * Starts notifying the given object when
    * the list of available versions changes.
    *
    * @param listener The object to start
    *                 notifying.
    */
   public synchronized void addListener(VersionsUpdateListener listener) {

      if (listener == null || listeners.contains(listener)) {
         return;
      }

      listeners.add(listener);

   }

   /**
    * Attempts to get the Version object of the
    * version with the given name.
    *
    * @param versionName The name of the version to get.
    * @return The Version object of the version with the
    * given name, or <code>null</code> if no such version
    * exists.
    */
   public Version getVersionByName(String versionName) {

      if (versionName == null) {
         return null;
      }

      return availableVersions.get(versionName);

   }

   /**
    * Gets all available Bible versions.
    *
    * @return Returns all available Bible
    * versions as an unmodifiable set.
    */
   public synchronized Collection<Version> getVersions() {
      return availableVersions
            .values()
            .stream()
            .collect(Collectors.toUnmodifiableSet());
   }

   /**
    * Stops the given object from being notified
    * when the list of versions changes.
    *
    * @param listener The object to stop from
    *                 being notified.
    */
   public synchronized void removeListener(VersionsUpdateListener listener) {

      if (listener == null) {
         return;
      }

      listeners.remove(listener);

   }

   /**
    * Replaces the list of available versions with
    * the given list of available versions, and
    * notifies all listeners of this class about
    * the change.
    *
    * @param availableVersions The new list of
    *                          available versions.
    */
   public synchronized void setAvailableVersions(Map<String, Version> availableVersions) {

      if (availableVersions == null) {
         // Invalid usage.
         return;
      }

      // Update internal map
      this.availableVersions = availableVersions;

      // Create a versions object to distribute
      // to the listeners without having
      // to worry about them modifying the
      // internal map.
      Collection<Version> unmodifiableVersionsSet = getVersions();

      // Notify all listeners of versions
      // list being updated.
      for (VersionsUpdateListener listener : listeners) {
         try {
            listener.onVersionsListSet(unmodifiableVersionsSet);
         } catch (Exception e) {
            // If one class has an issue, we don't want the entire
            // function to stop.
            // Grab the name of the class that threw the exception and report it.
            String name = listener.getClass().getSimpleName();
            logger.error(name + " encountered an exception while setting new versions list.",
                  e);
         }
      }

      logger.debug("Set of available versions has been updated!");

   }

}
