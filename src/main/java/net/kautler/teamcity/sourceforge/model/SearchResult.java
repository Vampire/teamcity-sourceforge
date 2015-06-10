/*
 * Copyright 2015 Björn Kautler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kautler.teamcity.sourceforge.model;

import java.util.Collection;

/**
 * A SourceForge search result as returned via the API with selected fields.
 */
public class SearchResult {
    private Collection<Ticket> tickets;

    public Collection<Ticket> getTickets() {
        return tickets;
    }

    /**
     * Returns whether any tickets were found by the search which is represented by this search result.
     *
     * @return whether any tickets were found by the search which is represented by this search result
     */
    public boolean didFind() {
        return !tickets.isEmpty();
    }
}
