package com.subway.analyticsservice.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "stations")
public class StationSearchDocument {

    @Id
    private String id;  // "강남역-2" 형식

    @Field(type = FieldType.Text, analyzer = "standard")
    private String stationName;  // "강남역"

    @Field(type = FieldType.Keyword)
    private String lineNumber;  // "2"

    @Field(type = FieldType.Text)
    private String lineName;  // "2호선"
}