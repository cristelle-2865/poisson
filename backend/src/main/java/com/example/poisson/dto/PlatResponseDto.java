package com.example.poisson.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlatResponseDto {
    
    private Long idPlat;
    private String nomPlat;
    private String descriptionPlat;
    private BigDecimal poidsTotalPlat;
    private BigDecimal coutTotalPlat;
    private BigDecimal proteinesTotalPlat;
    private BigDecimal glucidesTotalPlat;
    private BigDecimal proteinesParKgPlat;
    private BigDecimal glucidesParKgPlat;
    private LocalDate datePreparationPlat;
    private Boolean estUtilisePlat;
    private LocalDateTime dateCreationPlat;
    private List<CompositionResponseDto> compositions;
    
    @Data
    public static class CompositionResponseDto {
        private Long idCompositionPlat;
        private Long idAliment;
        private String nomAliment;
        private BigDecimal prixKgAliment;
        private BigDecimal proteinesParKgAliment;
        private BigDecimal glucidesParKgAliment;
        private BigDecimal stockAliment;
        private BigDecimal poidsAlimentComposition;
        private BigDecimal coutAlimentComposition;
        private BigDecimal proteinesComposition;
        private BigDecimal glucidesComposition;
        private LocalDateTime dateCreationComposition;
    }
}


