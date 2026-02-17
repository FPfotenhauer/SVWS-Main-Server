package de.schultraeger.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Computed school statistics aggregated from SVWS statistics endpoint.
 */
public record SchuleStatistikenGesamt(
        @JsonProperty("totalStudents")
        int totalStudents,
        
        @JsonProperty("maleStudents")
        int maleStudents,
        
        @JsonProperty("femaleStudents")
        int femaleStudents,
        
        @JsonProperty("studentsWithSpecialNeeds")
        int studentsWithSpecialNeeds,
        
        @JsonProperty("studentsWithMigrationBackground")
        int studentsWithMigrationBackground,
        
        @JsonProperty("abiStudentsEligible")
        int abiStudentsEligible,
        
        @JsonProperty("abiStudentsPassed")
        int abiStudentsPassed,
        
        @JsonProperty("studentsByGrade")
        List<GradeStatistic> studentsByGrade,
        
        @JsonProperty("topLocations")
        List<LocationStatistic> topLocations
) {
    public record GradeStatistic(
            @JsonProperty("gradeName")
            String gradeName,
            
            @JsonProperty("count")
            int count
    ) {}
    
    public record LocationStatistic(
            @JsonProperty("locationName")
            String locationName,
            
            @JsonProperty("postalCode")
            String postalCode,
            
            @JsonProperty("count")
            int count
    ) {}
}
