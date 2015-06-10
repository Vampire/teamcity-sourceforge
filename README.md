SourceForge Integration
=======================

This is a plugin for TeamCity that integrates SourceForge as issue tracker.



Table of Contents
-----------------
* [Installation](#installation)
* [Setup](#setup)
* [Usage](#usage)
* [License](#license)



Installation
------------

1. Download the ZIP file from the [latest release] and place it as-is into
   the `plugins` directory of your TeamCity data directory. Do not extract the ZIP file.  
   You can for example
   * put the ZIP file manually into the data directory, if you know where it is located and how to access it
   * go to `Administration -> Plugins List -> Upload plugin zip` and upload the ZIP via web interface
   * go to `Administration -> Diagnostics -> Browse Data Directory`,
     press `Upload new file` and upload the ZIP via web interface to the `plugins` directory

1. Delete the ZIP file of the old version from the `plugins` directory, if you are updating from a previous version.

1. After the ZIP file is placed where it is supposed to be, restart your TeamCity server,
   as it does not recognize plugin changes until restart.



Setup
-----

The plugin adds the issue tracker type `SourceForge` to TeamCity.

To configure a connection to SourceForge:

1. Go to `Administration -> <The project where you want to configure the connection>
   -> Issue Trackers -> Create new connection`

1. Choose `SourceForge`as type

1. Enter some display name to distinguish this connection instance from others you might configure

1. Enter the unix name of the SourceForge project from which you want to add a ticket tool.  
   This can be a project prefixed by `p/` or a user prefixed with `u/`.  
   If you press `Save`, the project is checked for existence and an error is shown if it does not exist.  
   **_Examples:_** `p/jedit`, `u/vampire0`

1. Enter the mount point of the ticket tool that you want to add.  
   If you press `Save`, the ticket tool is checked for existence in the given project
   and the valid ticket tools are listed in the error message if it does not exist.  
   **_Examples:_** `bugs`, `features`

1. Enter a Java-flavour regular expression as issue ID pattern which will be used
   to find issue IDs in commit messages and whereelse supported by TeamCity.  
   The pattern is also used to extract the actual issue ID from the match.
   If the given pattern has at least one match group, the content of the first match group is used as issue ID,
   otherwise the full match is used. The given pattern is compiled in a case-insensitive manner.  
   If you press `Save`, the pattern is validated for syntactical correctness and for not matching the empty string.  
   **_Examples:_** `bug #(\d+)`, `\d+`

1. *Optionally* enter a SourceForge search query that returns all resolved issues.  
   This query is used to determine whether an issue is to be considered resolved or not.
   This manifests in the display style of the issue popup.
   The syntax of the search query is the same as for [the search on SourceForge] itself.  
   If you press `Save`, the search query is validated for syntactical correctness.  
   **_Examples:_** `status:closed-fixed || status:closed-invalid`

1. *Optionally* enter a SourceForge search query that returns all feature request issues
   or `true` if all issues from this ticket tool are feature requests.  
   This query is used to determine whether an issue is a feature request or not.
   This manifests in the display style of the issue popup.
   The syntax of the search query is the same as for [the search on SourceForge] itself.  
   If you press `Save`, the search query is validated for syntactical correctness.  
   **_Examples:_** `true`, `label:feature`

1. *Optionally* enter how to determine the type of an issues from the configured ticket tool.  
   <a name="custom-value-syntax"></a>
   Allowed syntax for the field value:
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
   If you press `Save`, the value is validated for syntactical correctness and in the labels case
   with regular expression, that it does not match the empty string.  
   **_Examples:_** `labels:type_(.+):bug`, `custom:_type`, `feature`

1. *Optionally* enter how to determine the priority of an issues from the configured ticket tool.  
   Allowed syntax for the field value is [the same as for type](#custom-value-syntax).  
   If you press `Save`, the value is validated for syntactical correctness and in the labels case
   with regular expression, that it does not match the empty string.  
   **_Examples:_** `labels:"important\u003a .+":important: no`, `custom:_priority`, `important: no`

1. *Optionally* enter how to determine the severity of an issues from the configured ticket tool.  
   Allowed syntax for the field value is [the same as for type](#custom-value-syntax).  
   If you press `Save`, the value is validated for syntactical correctness and in the labels case
   with regular expression, that it does not match the empty string.  
   **_Examples:_** `labels:severity_(.+)`, `custom:_severity:not: severe`, `major`



Usage
-----

After the connection - or connections if you have multiple ticket tools - is configured,
the [issue tracker integration of TeamCity] can be used.

To sum up what you get, here a quick list:

* Issue mentions in commit comments are transformed into links to the issue in the issue tracker
* Next to issue mentions in commit comments is an arrow that triggers a pop-up with further
  information about the respective issue
* Build results pages get a new Tab `Issues`, that lists the issues that were mentioned
  in a check-in included in the build, if there were any
* Build configuration pages get a new Tab `Issue Log`, that lists all issues that were mentioned
  in a check-in in a list, together with the builds of that build configuration  
  You can also filter this list by build number range and whether to show only resolved issues,
  if you have set up the search query for finding resolved issues in the connection settings



License
-------

```
This project is licensed under the Apache License, Version 2.0 (the "License");
you may not use this project except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```



[latest release]: https://github.com/Vampire/teamcity-sourceforge/releases/latest
[the search on SourceForge]: https://sourceforge.net/p/allura/tickets/search_help/
[issue tracker integration of TeamCity]: https://confluence.jetbrains.com/display/TCD9/Integrating+TeamCity+with+Issue+Tracker#IntegratingTeamCitywithIssueTracker-DedicatedSupportforIssueTrackers
