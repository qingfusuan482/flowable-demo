package org.example.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProcessDiagramVO {
    private String bpmnXml;
    private List<String> activeTaskIds;
    private List<String> finishedTaskIds;
}
