package com.example.poisson.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class PlatCreationDto {
    
    @NotBlank(message = "Le nom du plat est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nomPlat;
    
    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String descriptionPlat;
    
    @NotNull(message = "La date de préparation est obligatoire")
    private LocalDate datePreparationPlat;
    
    private Boolean estUtilisePlat = false;
    
    @NotNull(message = "La composition du plat est obligatoire")
    @Size(min = 1, message = "Le plat doit contenir au moins un aliment")
    private List<CompositionDto> compositions = new ArrayList<>();
    
    @Data
    public static class CompositionDto {
        
        @NotNull(message = "L'aliment est obligatoire")
        private Long idAliment;
        
        @NotNull(message = "Le poids est obligatoire")
        @Positive(message = "Le poids doit être positif")
        private BigDecimal poidsAlimentComposition;
        
        public CompositionDto() {}
        
        public CompositionDto(Long idAliment, BigDecimal poidsAlimentComposition) {
            this.idAliment = idAliment;
            this.poidsAlimentComposition = poidsAlimentComposition;
        }
    }
}

