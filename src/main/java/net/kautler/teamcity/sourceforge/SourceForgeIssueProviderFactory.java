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

import jetbrains.buildServer.issueTracker.AbstractIssueProviderFactory;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.issueTracker.IssueProvider;
import org.jetbrains.annotations.NotNull;

/**
 * A factory that creates {@link SourceForgeIssueProvider}s with the given {@code IssueFetcher} from the constructor.
 */
public class SourceForgeIssueProviderFactory extends AbstractIssueProviderFactory {
    protected SourceForgeIssueProviderFactory(@NotNull IssueFetcher fetcher) {
        super(fetcher, "sourceforge", "SourceForge");
    }

    @NotNull
    @Override
    public IssueProvider createProvider() {
        return new SourceForgeIssueProvider(getType(), myFetcher);
    }
}
