package info.rsdev.playlists.dao;

import info.rsdev.playlists.domain.ChartsItem;

public interface Initializable {

    /**
     * prepare an existing data store to persist {@link ChartsItem} instances. If the data store is already setup, this
     * method will do nothing
     *
     * @return true if the persistence store was newly created, false otherwise
     */
    boolean setupStoreWhenNeeded();

}
