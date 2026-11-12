package com.fullstack.desafio.endereco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(

        @Schema(description = "Preenchido pelo ViaCEP a partir do CEP", example = "Praça da Sé")
        @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres")
        String rua,

        @Schema(description = "Número do imóvel", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O número é obrigatório")
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        String numero,

        @Schema(description = "Complemento do endereço", example = "Apto 12")
        @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
        String complemento,

        @Schema(description = "Preenchido pelo ViaCEP a partir do CEP", example = "Sé")
        @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
        String bairro,

        @Schema(description = "Preenchido pelo ViaCEP a partir do CEP", example = "São Paulo")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Schema(description = "UF preenchida pelo ViaCEP", example = "SP")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "O estado deve ser a sigla da UF com 2 letras (ex.: SP)")
        String estado,

        @Schema(description = "CEP brasileiro; ViaCEP valida e fornece os dados do endereço", example = "01001-000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido. Informe 8 dígitos (ex.: 13201-000)")
        String cep
) {
}
