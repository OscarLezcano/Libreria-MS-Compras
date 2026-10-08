package com.bigobooks.purchases_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear o actualizar un proveedor")
public class SupplierRequestDto {

	@NotBlank(message = "El nombre del proveedor es obligatorio")
	@Size(max = 255, message = "El nombre no puede superar 255 caracteres")
	@Schema(description = "Nombre comercial del proveedor", example = "Distribuidora Norte S.A.")
	private String name;

	@Size(max = 20, message = "El RUC no puede superar 20 caracteres")
	@Schema(description = "RUC o identificador fiscal", example = "20123456789")
	private String ruc;
}
