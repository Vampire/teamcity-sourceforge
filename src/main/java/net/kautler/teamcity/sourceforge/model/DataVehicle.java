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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

/**
 * A vehicle to transport various data from the {@code IssueProvider} to the {@code IssueFetcher} in one {@code String}.
 * The data of this vehicle is represented as {@code JSON} string.
 */
public class DataVehicle {
    private static final Gson GSON = new Gson();
    private static Map<String, DataVehicle> cache = new ConcurrentHashMap<String, DataVehicle>();

    private String project;
    private String ticketTool;
    private String resolvedQuery;
    private String featureRequestQuery;
    private String type;
    private String priority;
    private String severity;

    public DataVehicle(String project, String ticketTool, String resolvedQuery, String featureRequestQuery, String type, String priority, String severity) {
        this.project = project;
        this.ticketTool = ticketTool;
        this.resolvedQuery = resolvedQuery;
        this.featureRequestQuery = featureRequestQuery;
        this.type = type;
        this.priority = priority;
        this.severity = severity;
    }

    /**
     * Generates and returns the {@code JSON} representation of this data vehicle.
     *
     * @return the {@code JSON} representation of this data vehicle
     */
    public String toJson() {
        String dataVehicleJson = GSON.toJson(this);
        cache.put(dataVehicleJson, this);
        return dataVehicleJson;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its project value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the project value of the given data vehicle
     */
    public static String getProject(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).project;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.project;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its ticket tool value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the ticket tool value of the given data vehicle
     */
    public static String getTicketTool(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).ticketTool;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.ticketTool;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its resolved query value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the resolved query value of the given data vehicle
     */
    public static String getResolvedQuery(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).resolvedQuery;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.resolvedQuery;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its feature request query value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the feature request query value of the given data vehicle
     */
    public static String getFeatureRequestQuery(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).featureRequestQuery;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.featureRequestQuery;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its type value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the type value of the given data vehicle
     */
    public static String getType(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).type;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.type;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its priority value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the priority value of the given data vehicle
     */
    public static String getPriority(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).priority;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.priority;
    }

    /**
     * Decodes the given {@code JSON} representation of a data vehicle and returns its severity value.
     *
     * @param dataVehicleJson the {@code JSON} representation of a data vehicle
     * @return the severity value of the given data vehicle
     */
    public static String getSeverity(String dataVehicleJson) {
        if (cache.containsKey(dataVehicleJson)) {
            return cache.get(dataVehicleJson).severity;
        }
        DataVehicle dataVehicle = GSON.fromJson(dataVehicleJson, DataVehicle.class);
        cache.put(dataVehicleJson, dataVehicle);
        return dataVehicle.severity;
    }
}
