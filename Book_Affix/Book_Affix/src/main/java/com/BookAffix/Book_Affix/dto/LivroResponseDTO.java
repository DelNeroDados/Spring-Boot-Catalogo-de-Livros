package com.BookAffix.Book_Affix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LivroResponseDTO {

    @JsonProperty("results")
    private List<LivroDTO> results;

    public List<LivroDTO> getResults() {
        return results;
    }

    public void setResults(List<LivroDTO> results) {
        this.results = results;
    }
}
