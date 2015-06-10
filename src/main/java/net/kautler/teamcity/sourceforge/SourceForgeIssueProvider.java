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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueData;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.errors.NotFoundException;
import jetbrains.buildServer.issueTracker.errors.RetrieveIssueException;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import net.kautler.teamcity.sourceforge.model.DataVehicle;
import net.kautler.teamcity.sourceforge.model.Project;
import net.kautler.teamcity.sourceforge.model.Ticket;
import net.kautler.teamcity.sourceforge.model.Tool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * An {@code IssueProvider}, that provides SourceForge issues and validates the settings for the issue tracker that is edited.
 */
public class SourceForgeIssueProvider extends AbstractIssueProvider {
    private static final String MOUNT_POINT_PATTERN = "[a-zA-Z0-9-]+";
    private static final String PROJECT_PATTERN = "(?:u|p)/" + MOUNT_POINT_PATTERN;
    private static final Gson GSON = new Gson();

    public SourceForgeIssueProvider(String type, IssueFetcher fetcher) {
        super(type, fetcher);
    }

    @NotNull
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return new PropertiesProcessor() {
            @Override
            public Collection<InvalidProperty> process(Map<String, String> properties) {
                // check the standard properties, that are used in the superclass
                List<InvalidProperty> result = new ArrayList<InvalidProperty>(SourceForgeIssueProvider.super.getPropertiesProcessor().process(properties));

                SourceForgeIssueFetcher sfFetcher = (SourceForgeIssueFetcher) SourceForgeIssueProvider.this.myFetcher;

                String projectName = null;
                InputStream projectStream = null;
                if (properties.containsKey("project")) {
                    projectName = properties.get("project");
                    if (projectName.length() == 0) {
                        result.add(new InvalidProperty("project", "SF project must be specified"));
                    } else if (!projectName.matches(PROJECT_PATTERN)) {
                        result.add(new InvalidProperty("project", "SF project may only contain letters, digits and dashes"));
                    } else {
                        try {
                            // request the project from the API to see if it exists
                            projectStream = sfFetcher.fetchHttpFile(sfFetcher.getProjectUrl(projectName, true));
                        } catch (NotFoundException e) {
                            result.add(new InvalidProperty("project", "The specified SF project could not be found"));
                        } catch (RetrieveIssueException e) {
                            result.add(new InvalidProperty("project", format("A valid SF project must be specified [%s]", e.getMessage())));
                        } catch (IOException e) {
                            result.add(new InvalidProperty("project", format("A valid SF project must be specified [%s]", e.getMessage())));
                        }
                    }
                }

                String ticketToolName = null;
                boolean validTicketTool = false;
                if (properties.containsKey("ticketTool")) {
                    ticketToolName = properties.get("ticketTool");
                    if (ticketToolName.length() == 0) {
                        result.add(new InvalidProperty("ticketTool", "The Ticket tool mount point must be specified"));
                    } else if (!ticketToolName.matches(MOUNT_POINT_PATTERN)) {
                        result.add(new InvalidProperty("ticketTool", "Ticket tool mount point may only contain letters, digits and dashes"));
                    } else {
                        // if a valid project was specified, the stream to the API was opened,
                        // so we can decode the project JSON to verify the specified ticket tool mount point
                        if (projectStream != null) {
                            // decode the project JSON
                            Project project = GSON.fromJson(new InputStreamReader(projectStream), Project.class);

                            // search through the ticket tools of the project for the specified mount point and build
                            // a list of valid ticket tool mount points for the error message if the mount point is not valid
                            StringBuilder validTicketToolsBuilder = new StringBuilder();
                            List<Tool> tools = new ArrayList<Tool>(project.getTools());
                            // sort the found tools, so that they are listed alphabetically in the error message
                            sort(tools, new Comparator<Tool>() {
                                @Override
                                public int compare(Tool o1, Tool o2) {
                                    return o1.getMountPoint().compareTo(o2.getMountPoint());
                                }
                            });
                            for (Tool tool : tools) {
                                if (tool.getName().equals("tickets")) {
                                    if (tool.getMountPoint().equals(ticketToolName)) {
                                        validTicketTool = true;
                                        break;
                                    } else {
                                        validTicketToolsBuilder.append(tool.getMountPoint()).append(", ");
                                    }
                                }
                            }
                            if (!validTicketTool) {
                                String validTicketTools = validTicketToolsBuilder.substring(0, validTicketToolsBuilder.length() - 2);
                                result.add(new InvalidProperty("ticketTool", "The specified ticket tool mount point does not exist in the specified SF project, "
                                                                             + "valid ticket tool mount points are: " + validTicketTools));
                            }
                        } else {
                            result.add(new InvalidProperty("ticketTool", "SF project is not valid, ticket tool cannot be verified"));
                        }
                    }
                }

                if (properties.containsKey("resolvedQuery") && isNotEmpty(properties.get("resolvedQuery"))) {
                    if (projectStream == null) {
                        result.add(new InvalidProperty("resolvedQuery", "SF project and is not valid, resolved query cannot be verified"));
                    } else if (!validTicketTool) {
                        result.add(new InvalidProperty("resolvedQuery", "Ticket tool is not valid, resolved query cannot be verified"));
                    } else {
                        String resolvedQuery = properties.get("resolvedQuery");
                        try {
                            // trigger the specified search through the API to validate the syntactical correctness of the
                            // specified search query. Add the condition that ticket_num equals 1 to speed up the search,
                            // as we are only interested in syntax here, not in the actual result.
                            sfFetcher.fetchHttpFile(sfFetcher.getSearchUrl(projectName, ticketToolName, format("(%s) && ticket_num:1", resolvedQuery)));
                        } catch (RetrieveIssueException e) {
                            result.add(new InvalidProperty("resolvedQuery", format("A valid SF search query must be specified [%s]", e.getMessage())));
                        } catch (IOException e) {
                            result.add(new InvalidProperty("resolvedQuery", format("A valid SF search query must be specified [%s]", e.getMessage())));
                        }
                    }
                }

                if (properties.containsKey("featureRequestQuery") && isNotEmpty(properties.get("featureRequestQuery"))) {
                    String featureRequestQuery = properties.get("featureRequestQuery");
                    // a value of "true" means that all tickets from this ticket tool are feature requests
                    // this is useful if you have separate ticket tools for bugs and feature requests
                    if (!featureRequestQuery.equals("true")) {
                        if (projectStream == null) {
                            result.add(new InvalidProperty("featureRequestQuery", "SF project and is not valid, feature request query cannot be verified"));
                        } else if (!validTicketTool) {
                            result.add(new InvalidProperty("featureRequestQuery", "Ticket tool is not valid, feature request query cannot be verified"));
                        } else {
                            try {
                                // trigger the specified search through the API to validate the syntactical correctness of the
                                // specified search query. Add the condition that ticket_num equals 1 to speed up the search,
                                // as we are only interested in syntax here, not in the actual result.
                                sfFetcher.fetchHttpFile(sfFetcher.getSearchUrl(projectName, ticketToolName, format("(%s) && ticket_num:1", featureRequestQuery)));
                            } catch (RetrieveIssueException e) {
                                result.add(new InvalidProperty("featureRequestQuery", format("A valid SF search query or 'true' must be specified [%s]", e.getMessage())));
                            } catch (IOException e) {
                                result.add(new InvalidProperty("featureRequestQuery", format("A valid SF search query or 'true' must be specified [%s]", e.getMessage())));
                            }
                        }
                    }
                }

                validateCustomValueField(properties, "type", result);
                validateCustomValueField(properties, "priority", result);
                validateCustomValueField(properties, "severity", result);

                return result;
            }

            /**
             * Validates the correctness of a custom value field.
             * <p>
             * Allowed syntax can be seen at {@link SourceForgeIssueFetcher#getCustomValue(String, Ticket)}
             * <p>
             * This method verifies that a given regex for the labels case is valid and does not match the empty string
             * and that for the custom case a custom field name is given.
             *
             * @param properties the properties object where the field is stored
             * @param fieldName  the name of the field that is to be validated
             * @param result     the result list to which violations should be added
             */
            private void validateCustomValueField(@NotNull Map<String, String> properties, @NotNull String fieldName, @NotNull List<InvalidProperty> result) {
                if (properties.containsKey(fieldName) && isNotEmpty(properties.get(fieldName))) {
                    String fieldValue = properties.get(fieldName);
                    String[] fieldValueParts = fieldValue.split(":", 3);
                    // if there is at least one colon, otherwise it is a fixed string setting
                    if (fieldValueParts.length > 1) {
                        if (fieldValueParts[0].equals("labels")) {
                            if (isNotEmpty(fieldValueParts[1]) && safeCompile(fieldValueParts[1]).equals(EMPTY_PATTERN)) {
                                result.add(new InvalidProperty(fieldName, "A correct regex pattern or nothing must be specified as second colon-separated token for 'labels:'"));
                            }
                        } else if (fieldValueParts[0].equals("custom") && isEmpty(fieldValueParts[1])) {
                            result.add(new InvalidProperty(fieldName, "A custom field name must be specified as second colon-separated token for 'custom:'"));
                        }
                    }
                }
            }
        };
    }

    @Override
    public void setProperties(@NotNull Map<String, String> map) {
        super.setProperties(map);
        // encode all data that we need in the issue fetcher into
        // a JSON string and transport it via the host variable
        myHost = new DataVehicle(myProperties.get("project"),
                                 myProperties.get("ticketTool"),
                                 myProperties.get("resolvedQuery"),
                                 myProperties.get("featureRequestQuery"),
                                 myProperties.get("type"),
                                 myProperties.get("priority"),
                                 myProperties.get("severity")).toJson();
    }

    @Override
    public boolean isBatchFetchSupported() {
        return true;
    }

    @Nullable
    @Override
    public Map<String, IssueData> findIssuesByIds(@NotNull Collection<String> ids) {
        return findIssuesByIdsImpl(ids);
    }

    @NotNull
    @Override
    protected String extractId(@NotNull String match) {
        Matcher matcher = myPattern.matcher(match);
        if (!matcher.matches()) {
            throw new AssertionError(format("Match '%s' should match the pattern '%s', but does not", match, myPattern));
        }
        // use the first group if present, or the whole match otherwise
        if (matcher.groupCount() > 0) {
            return matcher.group(1);
        } else {
            return matcher.group();
        }
    }

    /**
     * Does the same as {@link AbstractIssueProvider#safeCompile(String)}.
     * This method is just present to expose the functionality to other classes in this package.
     *
     * @param pattern the pattern to compile
     * @return a compiled pattern
     */
    @NotNull
    static Pattern safeCompilePattern(@NotNull String pattern) {
        return safeCompile(pattern);
    }
}
