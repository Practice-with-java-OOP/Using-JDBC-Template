package com.jidian.cosalon.migration.pos365.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    @JsonProperty("UserId")
    private String userId;
    @JsonProperty("SessionId")
    private String sessionId;
    @JsonProperty("UserName")
    private String userName;
    @JsonProperty("DisplayName")
    private String displayName;
    @JsonProperty("ProfileUrl")
    private String profileUrl;
    @JsonProperty("Roles")
    private List<String> roles;
}
