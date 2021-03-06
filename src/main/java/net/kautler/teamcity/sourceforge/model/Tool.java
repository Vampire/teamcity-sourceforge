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

/**
 * A SourceForge tool as returned via the API with selected fields.
 */
public class Tool {
    private String mount_point;
    private String name;

    public String getMountPoint() {
        return mount_point;
    }

    public String getName() {
        return name;
    }
}
