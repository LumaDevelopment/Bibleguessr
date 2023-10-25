package gg.bibleguessr.bible.versions;

import gg.bibleguessr.bible.data_structures.Version;

import java.util.Collection;

/**
 * This is interface is implemented by classes
 * which are interested in the list of available
 * Bible versions updating.
 */
public interface VersionsUpdateListener {

    /**
     * Called by the BibleVersionMgr whenever
     * just a single version has been added.
     *
     * @param version The new version.
     */
    void onNewVersionAdded(Version version);

    /**
     * Called by the BibleVersionMgr whenever the
     * list of available Bible versions has
     * been fully set (typically on initial
     * service start).
     *
     * @param versions The new list of versions.
     */
    void onVersionsListSet(Collection<Version> versions);

}
