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
    
    // AJOUT : Lipides et vitamines totaux
    private BigDecimal lipidesTotalPlat;
    private BigDecimal vitaminesTotalPlat;
    
    private BigDecimal proteinesParKgPlat;
    private BigDecimal glucidesParKgPlat;
    
    // AJOUT : Lipides et vitamines par kg
    private BigDecimal lipidesParKgPlat;
    private BigDecimal vitaminesParKgPlat;
    
    private LocalDate datePreparationPlat;
    private Boolean estUtilisePlat;
    private LocalDateTime dateCreationPlat;
    private List<CompositionResponseDto> compositions;
    
    // Statistiques d'utilisation
    private Long nombreUtilisations;
    private BigDecimal gainTotalGenere;
    
    @Data
    public static class CompositionResponseDto {
        private Long idCompositionPlat;
        private Long idAliment;
        private String nomAliment;
        private BigDecimal prixKgAliment;
        private BigDecimal proteinesParKgAliment;
        private BigDecimal glucidesParKgAliment;
        
        // AJOUT : Lipides et vitamines par kg
        private BigDecimal lipidesParKgAliment;
        private BigDecimal vitaminesParKgAliment;
        
        private BigDecimal stockAliment;
        private BigDecimal poidsAlimentComposition;
        private BigDecimal coutAlimentComposition;
        private BigDecimal proteinesComposition;
        private BigDecimal glucidesComposition;
        
        // AJOUT : Lipides et vitamines dans la composition
        private BigDecimal lipidesComposition;
        private BigDecimal vitaminesComposition;
        
        private LocalDateTime dateCreationComposition;
    }
}

