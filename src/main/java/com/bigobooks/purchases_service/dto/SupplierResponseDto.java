package com.bigobooks.purchases_service.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Proveedor registrado en el sistema")
public class SupplierResponseDto {

	@Schema(description = "Identificador del proveedor", example = "1")
	private Long id;

	@Schema(description = "Nombre comercial del proveedor", example = "Distribuidora Norte S.A.")
	private String name;

	@Schema(description = "RUC o identificador fiscal", example = "20123456789")
	private String ruc;

	@Schema(description = "Fecha de alta del registro")
	private LocalDateTime createdAt;
}
