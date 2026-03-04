package br.com.sebratel.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriacaoDeMassivaInputDTO {
    @JsonProperty("start_date")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    @JsonProperty("start_time")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;
    private Integer[] accessPointIds;
    private Integer[] slotOlt;
    private Integer[] portaOlt;
    private Integer[] addressListId;
    private Integer companyPlaceId = 1;
    private Integer assignmentTypeId;
    private String assignmentDescription;
    private LocalDate maintenanceDate;
    private LocalTime maintenanceTime;
    @Nullable
    private Integer sendEmail;
    @Nullable
    private Integer sendSms;
    @Nullable
    private Integer emailModelId;
    @Nullable
    private Integer returnEmailModelId;
    @Nullable
    private Integer sendPush;
    @Nullable
    private Integer pushModelId;
    @Nullable
    private Integer returnPushModelId;
}
