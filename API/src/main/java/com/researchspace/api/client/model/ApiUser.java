/**
 * RSpace API
 * Access your RSpace documents programmatically.   All requests require authentication using an API key set as the value of the header `RSpace-API-Key`. 
 *
 * OpenAPI spec version: 0.1
 * Contact: support@researchspace.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.researchspace.api.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of a User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(value={"id", "username", "email", "firstName", "lastName"})
public class ApiUser {
    
    @JsonProperty("id")
    private Long id = null;

    @JsonProperty("username")
    private String username = null;

    @JsonProperty("email")
    private String email = null;

    @JsonProperty("firstName")
    private String firstName = null;

    @JsonProperty("lastName")
    private String lastName = null;

}
