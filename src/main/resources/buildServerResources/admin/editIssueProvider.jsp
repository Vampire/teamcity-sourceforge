<%@ include file="/include.jsp" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<%--
  ~ Copyright 2015 Björn Kautler
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<div>
    <table class="editProviderTable">
        <c:if test="${showType}">
            <tr>
                <th><label class="shortLabel">Connection Type:</label></th>
                <td>SourceForge</td>
            </tr>
        </c:if>
        <tr>
            <th><label for="name" class="shortLabel">Display Name: <l:star/></label></th>
            <td>
                <props:textProperty name="name" maxlength="100"/>
                <span id="error_name" class="error"></span>
            </td>
        </tr>
        <tr>
            <th><label for="project" class="shortLabel">SF Project: <l:star/></label></th>
            <td>
                <props:textProperty name="project" maxlength="100"/>
                <span id="error_project" class="error"></span>
                <span class="fieldExplanation">The unix name of the project, e.&nbsp;g. 'p/jedit' or 'u/vampire0'</span>
            </td>
        </tr>
        <tr>
            <th><label for="ticketTool" class="shortLabel">SF Ticket Tool: <l:star/></label></th>
            <td>
                <props:textProperty name="ticketTool" maxlength="100"/>
                <span id="error_ticketTool" class="error"></span>
                <span class="fieldExplanation">The mount point of the ticket tool</span>
            </td>
        </tr>
        <tr>
            <th><label for="pattern" class="shortLabel">Issue ID Pattern: <l:star/></label></th>
            <td>
                <props:textProperty name="pattern" maxlength="100"/>
                <span id="error_pattern" class="error"></span>
                <span class="fieldExplanation">
                    The regex pattern issue ids have to match,
                    e.&nbsp;g. '#(\d+)', the first group is taken as issue id, or the whole match if no group is found<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/>
                </span>
            </td>
        </tr>
        <tr>
            <th><label for="resolvedQuery" class="shortLabel">Resolved Query: </label></th>
            <td>
                <props:textProperty name="resolvedQuery" maxlength="500"/>
                <span id="error_resolvedQuery" class="error"></span>
                <span class="fieldExplanation">
                    A SF search query to find all tickets that are considered 'resolved',
                    e.&nbsp;g. 'status:closed-fixed || status:closed-invalid'<bs:help urlPrefix="https://sourceforge.net/p/allura/tickets" file="search_help"/>
                </span>
            </td>
        </tr>
        <tr>
            <th><label for="featureRequestQuery" class="shortLabel">Feature Request Query: </label></th>
            <td>
                <props:textProperty name="featureRequestQuery" maxlength="500"/>
                <span id="error_featureRequestQuery" class="error"></span>
                <span class="fieldExplanation">
                    A SF search query to find all tickets that are considered feature requests,
                    e.&nbsp;g. 'labels:"editor core"'<bs:help urlPrefix="https://sourceforge.net/p/allura/tickets" file="search_help"/>, or 'true' if all tickets are feature requests
                </span>
            </td>
        </tr>
        <c:set var="possibleValuesHelpLink">
            <bs:togglePopup linkText="Show possible values">
                <jsp:attribute name="content">
                    <div style="padding: 0 1em">
                        <dl>
                            <dt><b>labels:&lt;regex&gt;[:&lt;default&gt;]</b></dt>
                            <dd>
                                The value is defined by one or more labels.<br/>
                                If multiple labels are found, they are joined together with commas.<br/>
                                If no label is found, the default value is used, if one is defined.<br/>
                                The regex must not contain any colons. If you need to match a colon, use '\u003a' instead.
                                <ul>
                                    <li>If no regex is given, all labels are used, e.&nbsp;g. 'labels:' or 'labels::bug'</li>
                                    <li>
                                        If a regex without group is given, all labels matching the regex are used completely,
                                        e.&nbsp;g. 'labels:.+_bug' or 'labels:.+_bug:general_bug'<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/>
                                    </li>
                                    <li>
                                        If a regex with groups is given, all labels matching the regex are used, but only their first group,
                                        e.&nbsp;g. 'labels:type_(.+)' or 'labels:type_(.+):bug'<bs:help file="Integrating+TeamCity+with+Issue+Tracker"/>
                                    </li>
                                </ul>
                            </dd>

                            <dt><b>custom:&lt;custom field name&gt;[:&lt;default&gt;]</b></dt>
                            <dd>
                                The value is defined by the value of a custom field, e.&nbsp;g. 'custom:_type' or 'custom:_type:bug'<br/>
                                If the custom field is not found, not set or empty, the default value is used, if one is defined.
                            </dd>

                            <dt><b>&lt;fixed string&gt;</b></dt>
                            <dd>All issues have the same value defined here, e.&nbsp;g. 'bug'</dd>
                        </dl>
                    </div>
                </jsp:attribute>
            </bs:togglePopup>
        </c:set>
        <tr>
            <th><label for="type" class="shortLabel">Type: </label></th>
            <td>
                <props:textProperty name="type" maxlength="100"/>
                <span id="error_type" class="error"></span>
                <span class="fieldExplanation">The type of issues coming from this ticket tool. ${possibleValuesHelpLink}</span>
            </td>
        </tr>
        <tr>
            <th><label for="priority" class="shortLabel">Priority: </label></th>
            <td>
                <props:textProperty name="priority" maxlength="100"/>
                <span id="error_priority" class="error"></span>
                <span class="fieldExplanation">The priority of issues coming from this ticket tool. ${possibleValuesHelpLink}</span>
            </td>
        </tr>
        <tr>
            <th><label for="severity" class="shortLabel">Severity: </label></th>
            <td>
                <props:textProperty name="severity" maxlength="100"/>
                <span id="error_severity" class="error"></span>
                <span class="fieldExplanation">The severity of issues coming from this ticket tool. ${possibleValuesHelpLink}</span>
            </td>
        </tr>
    </table>
</div>
