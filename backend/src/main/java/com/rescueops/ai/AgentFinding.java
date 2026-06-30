package com.rescueops.ai;

import com.rescueops.entity.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentFinding {
    private Severity severity;
    private String rootCause;
    private Integer confidence;
    private String suggestedFix;
    private String summary;
}
