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

        @JsonProperty("confessionsByGrade")
        List<GradeConfessionStatistic> confessionsByGrade,
        
        @JsonProperty("topLocations")
        List<LocationStatistic> topLocations,

        @JsonProperty("classStatistics")
        List<ClassStatistic> classStatistics
) {
    public record GradeStatistic(
            @JsonProperty("gradeName")
            String gradeName,
            
            @JsonProperty("count")
            int count
    ) {}

    public record GradeConfessionStatistic(
            @JsonProperty("gradeName")
            String gradeName,

            @JsonProperty("confessions")
            List<ConfessionStatistic> confessions
    ) {}

    public record ConfessionStatistic(
            @JsonProperty("confessionCode")
            String confessionCode,

            @JsonProperty("confessionName")
            String confessionName,

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

    public record ClassStatistic(
            @JsonProperty("classId")
            Integer classId,

            @JsonProperty("className")
            String className,

            @JsonProperty("totalStudents")
            int totalStudents,

            @JsonProperty("maleStudents")
            int maleStudents,

            @JsonProperty("femaleStudents")
            int femaleStudents,

            @JsonProperty("grades")
            List<GradeStatistic> grades,

            @JsonProperty("specialNeeds")
            List<SpecialNeedStatistic> specialNeeds,

            @JsonProperty("nationalities")
            List<NationalityStatistic> nationalities
    ) {}

    public record SpecialNeedStatistic(
            @JsonProperty("specialNeedCode")
            String specialNeedCode,

            @JsonProperty("specialNeedName")
            String specialNeedName,

            @JsonProperty("count")
            int count
    ) {}

        public record NationalityStatistic(
                        @JsonProperty("nationalityCode")
                        String nationalityCode,

                        @JsonProperty("nationalityName")
                        String nationalityName,

                        @JsonProperty("count")
                        int count,

                        @JsonProperty("maleCount")
                        int maleCount,

                        @JsonProperty("femaleCount")
                        int femaleCount
        ) {}
}
