<%@ page import="net.kautler.teamcity.sourceforge.SourceForgeIssueFetcher" %>
<%@ include file="/include.jsp" %>

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

<jsp:useBean id="issue" scope="request" type="jetbrains.buildServer.issueTracker.IssueEx"/>
<c:set var="issueData" value="${issue.issueDataOrNull}"/>
<c:set var="fields" value="${issueData.allFields}"/>
<c:set var="votesField"><%=SourceForgeIssueFetcher.VOTES_FIELD%></c:set>
<c:set var="labelsField"><%=SourceForgeIssueFetcher.LABELS_FIELD%></c:set>
<bs:issueDetailsPopup issue="${issue}" popupClass="sourceforge">
    <jsp:attribute name="otherFields">
        <c:set var="votes" value="${fields[votesField]}"/>
        <%--
            Votes are always delivered via API, no matter whether votes are enabled
            or not. So just do not show votes for tickets where the vote sums up to 0.
         --%>
        <c:if test="${(not empty votes) and (votes ne '0')}">
            <td title="Votes">${votes}</td>
        </c:if>

        <c:set var="labels" value="${fields[labelsField]}"/>
        <c:if test="${not empty labels}">
            <td title="Labels">${labels}</td>
        </c:if>
    </jsp:attribute>
</bs:issueDetailsPopup>
