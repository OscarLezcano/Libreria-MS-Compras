package com.bigobooks.purchases_service.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para registrar una nueva compra a proveedor")
public class PurchaseCreateRequestDto {

	@NotNull(message = "warehouseId es obligatorio")
	@Schema(description = "Identificador del almacen destino", example = "1")
	private Long warehouseId;

	@NotNull(message = "supplierId es obligatorio")
	@Schema(description = "Identificador del proveedor", example = "3")
	private Long supplierId;

	@NotBlank(message = "invoiceNumber es obligatorio")
	@Schema(description = "Numero de factura del proveedor", example = "001-001-00001234")
	private String invoiceNumber;

	@NotEmpty(message = "Debe incluir al menos un detalle en la compra")
	@Valid
	@Schema(description = "Items de la compra")
	private List<PurchaseDetailRequestDto> details;
}
