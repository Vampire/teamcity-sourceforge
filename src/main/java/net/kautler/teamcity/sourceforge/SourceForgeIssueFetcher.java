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

package net.kautler.teamcity.sourceforge;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.errors.RetrieveIssueException;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import net.kautler.teamcity.sourceforge.model.SearchResult;
import net.kautler.teamcity.sourceforge.model.Ticket;
import net.kautler.teamcity.sourceforge.model.TicketWrapper;
import org.apache.commons.httpclient.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static jetbrains.buildServer.issueTracker.IssueData.PRIORITY_FIELD;
import static jetbrains.buildServer.issueTracker.IssueData.SEVERITY_FIELD;
import static jetbrains.buildServer.issueTracker.IssueData.STATE_FIELD;
import static jetbrains.buildServer.issueTracker.IssueData.SUMMARY_FIELD;
import static jetbrains.buildServer.issueTracker.IssueData.TYPE_FIELD;
import static net.kautler.teamcity.sourceforge.SourceForgeIssueProvider.safeCompilePattern;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getFeatureRequestQuery;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getPriority;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getProject;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getResolvedQuery;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getSeverity;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getTicketTool;
import static net.kautler.teamcity.sourceforge.model.DataVehicle.getType;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;

/**
 * An {@code IssueFetcher}, that fetches SourceForge issues.
 */
public class SourceForgeIssueFetcher extends AbstractIssueFetcher {
    public static final String LABELS_FIELD = "Labels";
    public static final String VOTES_FIELD = "Votes";
    private static final Gson GSON = new Gson();

    public SourceForgeIssueFetcher(@NotNull EhCacheUtil cacheUtil) {
        super(cacheUtil);
    }

    @NotNull
    @Override
    public IssueData getIssue(@NotNull final String dataVehicleJson, @NotNull final String id, @Nullable Credentials credentials) throws Exception {
        final String issueUrl = getIssueUrl(dataVehicleJson, id, true);
        return getFromCacheOrFetch(issueUrl, new FetchFunction() {
            @NotNull
            @Override
            public IssueData fetch() throws IOException {
                InputStream issueStream = fetchHttpFile(issueUrl);
                Ticket ticket = GSON.fromJson(new InputStreamReader(issueStream), TicketWrapper.class).getTicket();
                return getIssueData(ticket, dataVehicleJson);
            }
        });
    }

    @NotNull
    @Override
    public String getUrl(@NotNull String dataVehicleJson, @NotNull String id) {
        return getIssueUrl(dataVehicleJson, id, false);
    }

    /**
     * Constructs the URL to the issue with the specified ID, either as browsing variant, or as API variant.
     *
     * @param dataVehicleJson the {@code JSON} representation of the data vehicle transporting the configuration data
     * @param id              the ID of the issue to construct the URL for
     * @param rest            whether to build the browsing variant ({@code false}) or the API variant ({@code true})
     * @return the constructed URL as string
     */
    @NotNull
    private String getIssueUrl(@NotNull String dataVehicleJson, @NotNull String id, boolean rest) {
        return format("%s/%s", getTicketToolUrl(getProject(dataVehicleJson), getTicketTool(dataVehicleJson), rest), id);
    }

    /**
     * Constructs the URL to the search through the API for the specified project and ticket tool and with the specified search query.
     * The search query is automatically URL encoded and must not be already encoded.
     *
     * @param project    the project to construct the URL for
     * @param ticketTool the ticket tool to construct the URL for
     * @param query      the search query to constuct the URL for
     * @return the constructed URL as string
     */
    @NotNull
    String getSearchUrl(@NotNull String project, @NotNull String ticketTool, @NotNull String query) {
        try {
            return format("%s/search?q=%s", getTicketToolUrl(project, ticketTool, true), URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 should be supported on all JVMs", e);
        }
    }

    /**
     * Constructs the URL to the specified ticket tool in the specified project, either as browsing variant, or as API variant.
     *
     * @param project    the project to construct the URL for
     * @param ticketTool the ticket tool to construct the URL for
     * @param rest       whether to build the browsing variant ({@code false}) or the API variant ({@code true})
     * @return the constructed URL as string
     */
    @NotNull
    private String getTicketToolUrl(@NotNull String project, @NotNull String ticketTool, boolean rest) {
        return format("%s/%s", getProjectUrl(project, rest), ticketTool);
    }

    /**
     * Constructs the URL to the specified project, either as browsing variant, or as API variant.
     *
     * @param project the project to construct the URL for
     * @param rest    whether to build the browsing variant ({@code false}) or the API variant ({@code true})
     * @return the constructed URL as string
     */
    @NotNull
    String getProjectUrl(@NotNull String project, boolean rest) {
        return format("https://sourceforge.net/%s%s", rest ? "rest/" : "", project);
    }

    /**
     * Fetches the issues corresponding to the specified IDs that are not cached already as one batch operation
     * from the remote issue tracker.
     *
     * @param dataVehicleJson the {@code JSON} representation of the data vehicle transporting the configuration data
     * @param ids             the IDs to fetch the issues for
     * @param credentials     the credentials to use for authentication
     * @return the fetched issues
     */
    @Nullable
    @Override
    public Collection<IssueData> getIssuesInBatch(@NotNull final String dataVehicleJson, @NotNull Collection<String> ids, @Nullable Credentials credentials) {
        return super.defaultGetIssuesInBatch(dataVehicleJson, ids, new BatchFetchFunction() {
            @NotNull
            @Override
            public List<IssueData> batchFetch(@NotNull Collection<String> ids) {
                try {
                    StringBuilder queryBuilder = new StringBuilder();
                    for (String id : ids) {
                        queryBuilder.append("ticket_num:").append(id).append(" || ");
                    }
                    if (ids.size() > 0) {
                        queryBuilder.delete(queryBuilder.length() - 4, queryBuilder.length());
                    }
                    String searchUrl = getSearchUrl(getProject(dataVehicleJson), getTicketTool(dataVehicleJson), queryBuilder.toString());
                    InputStream issueStream = fetchHttpFile(searchUrl);
                    Collection<Ticket> tickets = GSON.fromJson(new InputStreamReader(issueStream), SearchResult.class).getTickets();
                    List<IssueData> result = new ArrayList<IssueData>(tickets.size());
                    for (Ticket ticket : tickets) {
                        result.add(getIssueData(ticket, dataVehicleJson));
                    }
                    return result;
                } catch (IOException e) {
                    return emptyList();
                }
            }
        });
    }

    /**
     * Transforms a {@code Ticket} into an {@code IssueData}.
     *
     * @param ticket          the ticket to be transformed
     * @param dataVehicleJson the {@code JSON} representation of the data vehicle transporting the configuration data
     * @return the transformed issue data
     */
    @NotNull
    private IssueData getIssueData(@NotNull Ticket ticket, @NotNull String dataVehicleJson) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(TYPE_FIELD, getCustomValue(getType(dataVehicleJson), ticket));
        data.put(SUMMARY_FIELD, ticket.getSummary());
        data.put(STATE_FIELD, ticket.getStatus());
        data.put(PRIORITY_FIELD, getCustomValue(getPriority(dataVehicleJson), ticket));
        data.put(SEVERITY_FIELD, getCustomValue(getSeverity(dataVehicleJson), ticket));
        data.put(VOTES_FIELD, String.valueOf(ticket.getVotes()));
        data.put(LABELS_FIELD, join(ticket.getLabels().iterator(), ", "));

        String ticketNum = ticket.getTicketNum();

        boolean resolved = false;
        String resolvedQuery = getResolvedQuery(dataVehicleJson);
        if (isNotBlank(resolvedQuery)) {
            resolved = checkSearchCondition(dataVehicleJson, ticketNum, resolvedQuery, false);
        }

        boolean featureRequest = false;
        String featureRequestQuery = getFeatureRequestQuery(dataVehicleJson);
        if ("true".equals(featureRequestQuery)) {
            featureRequest = true;
        } else if (isNotEmpty(featureRequestQuery)) {
            featureRequest = checkSearchCondition(dataVehicleJson, ticketNum, featureRequestQuery, false);
        }

        return new IssueData(ticketNum, data, resolved, featureRequest, getUrl(dataVehicleJson, ticketNum));
    }

    /**
     * Retrieve the custom value for some field from the specified ticket.
     * <p/>
     * Allowed syntax for the field value:
     * <p><dl>
     * <dt><b>labels:&lt;regex&gt;[:&lt;default&gt;]</b></dt>
     * <dd>
     * The value is defined by one or more labels.<br/>
     * If multiple labels are found, they are joined together with commas.<br/>
     * If no label is found, the default value is used, if one is defined.<br/>
     * The regex must not contain any colons. If you need to match a colon, use '\u005c003a' instead.
     * <ul>
     * <li>If no regex is given, all labels are used, e.&nbsp;g. 'labels:' or 'labels::bug'</li>
     * <li>
     * If a regex without group is given, all labels matching the regex are used completely,
     * e.&nbsp;g. 'labels:.+_bug' or 'labels:.+_bug:general_bug'<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/>
     * </li>
     * <li>
     * If a regex with groups is given, all labels matching the regex are used, but only their first group,
     * e.&nbsp;g. 'labels:type_(.+)' or 'labels:type_(.+):bug'<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/>
     * </li>
     * </ul>
     * </dd>
     * <dt><b>custom:&lt;custom field name&gt;[:&lt;default&gt;]</b></dt>
     * <dd>
     * The value is defined by the value of a custom field, e.&nbsp;g. 'custom:_type' or 'custom:_type:bug'<br/>
     * If the custom field is not found, not set or empty, the default value is used, if one is defined.
     * </dd>
     * <dt><b>&lt;fixed string&gt;</b></dt>
     * <dd>All issues have the same value defined here, e.&nbsp;g. 'bug'</dd>
     * </dl>
     *
     * @param fieldValue the custom value field specification
     * @param ticket     the ticket to retrieve the data from
     * @return the retrieved custom value
     */
    @Nullable
    private String getCustomValue(@NotNull String fieldValue, @NotNull Ticket ticket) {
        String[] fieldValueParts = fieldValue.split(":", 3);

        // if there is no colon, this is the fixed string case, so simple return the specification
        if (fieldValueParts.length == 1) {
            return fieldValue;
        }

        if (fieldValueParts[0].equals("labels")) {
            String labelRegex = fieldValueParts[1];
            if (isEmpty(labelRegex)) {
                // if no regex is given, just use all labels
                String labels = join(ticket.getLabels().iterator(), ", ");
                if (isNotBlank(labels)) {
                    return labels;
                }
                // if there are no labels present, return the default value if specified
                if (fieldValueParts.length > 2) {
                    return fieldValueParts[2];
                }
            } else {
                // if there is a regex given, search all matching labels
                Pattern labelPattern = safeCompilePattern(labelRegex);
                List<String> matchingLabels = new ArrayList<String>();
                for (String label : ticket.getLabels()) {
                    Matcher labelMatcher = labelPattern.matcher(label);
                    if (labelMatcher.matches()) {
                        // use the first group if present, or the whole match otherwise
                        if (labelMatcher.groupCount() > 0) {
                            matchingLabels.add(labelMatcher.group(1));
                        } else {
                            matchingLabels.add(labelMatcher.group());
                        }
                    }
                }
                // join the matched labels together
                String labels = join(matchingLabels.iterator(), ", ");
                if (isNotBlank(labels)) {
                    return labels;
                }
                // if no labels were found, return the default value if specified
                if (fieldValueParts.length > 2) {
                    return fieldValueParts[2];
                }
            }
        } else if (fieldValueParts[0].equals("custom")) {
            String customFieldValue = ticket.getCustomFields().get(fieldValueParts[1]);
            if (isNotBlank(customFieldValue)) {
                return customFieldValue;
            }
            // if the custom field is not found, not set or empty, return the default value if specified
            if (fieldValueParts.length > 2) {
                return fieldValueParts[2];
            }
        } else {
            // there is a colon present, but none of the defined prefixes matches,
            // so we are in the fixed string case again, simply return specification
            return fieldValue;
        }

        return null;
    }

    /**
     * Check whether the ticket with the specified ticket number is included in the specified search query.
     *
     * @param dataVehicleJson the {@code JSON} representation of the data vehicle transporting the configuration data
     * @param ticketNum       the number of the ticket to check against the search query
     * @param searchQuery     the search query to test the ticket against
     * @param defaultValue    the default value that should be returned if there was an unexpected server error
     * @return whether the ticket is included in the search query or the default value in case of server error
     */
    private boolean checkSearchCondition(@NotNull String dataVehicleJson, @NotNull String ticketNum, @NotNull String searchQuery, boolean defaultValue) {
        try {
            String searchUrl = getSearchUrl(getProject(dataVehicleJson), getTicketTool(dataVehicleJson), format("(%s) && ticket_num:%s", searchQuery, ticketNum));
            InputStream searchResultStream = fetchHttpFile(searchUrl);
            SearchResult searchResult = GSON.fromJson(new InputStreamReader(searchResultStream), SearchResult.class);
            return searchResult.didFind();
        } catch (RetrieveIssueException e) {
            return defaultValue;
        } catch (IOException e) {
            return defaultValue;
        }
    }

    /**
     * Does the same as {@link AbstractIssueFetcher#fetchHttpFile(String, Credentials)} with {@code null} as second parameter.
     * This method is mainly present to expose the functionality to other classes in this package.
     *
     * @param url the url of file to fetch
     * @return result input stream, or null in case of HTTP error
     *
     * @throws IOException if I/O error occurs
     */
    @NotNull
    InputStream fetchHttpFile(@NotNull String url) throws IOException {
        return super.fetchHttpFile(url, null);
    }
}
