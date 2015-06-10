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
import java.util.Map;

/**
 * A SourceForge ticket as returned via the API with selected fields.
 */
public class Ticket {
    private String status;
    private String ticket_num;
    private String summary;
    private Map<String, String> custom_fields;
    private int votes_down;
    private int votes_up;
    private Collection<String> labels;

    public String getStatus() {
        return status;
    }

    public String getTicketNum() {
        return ticket_num;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, String> getCustomFields() {
        return custom_fields;
    }

    /**
     * Calculates the sum of votes for this ticket and returns the result.
     * The downvotes are subtracted from the upvotes and the result is returned.
     *
     * @return the sum of the votes for this ticket
     */
    public int getVotes() {
        return votes_up - votes_down;
    }

    public Collection<String> getLabels() {
        return labels;
    }
}
