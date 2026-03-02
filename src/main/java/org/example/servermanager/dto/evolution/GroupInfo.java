package org.example.servermanager.dto.evolution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupInfo {

    private String id;
    private String subject;
    private Integer size;
    private String owner;
    private String desc;
}
