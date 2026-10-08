package com.bigobooks.purchases_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.bigobooks.entities.purchases.PurchaseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compra registrada con sus detalles")
public class PurchaseResponseDto {

	@Schema(description = "Identificador de la compra", example = "15")
	private Long id;

	@Schema(description = "Almacen destino", example = "1")
	private Long warehouseId;

	@Schema(description = "Proveedor", example = "3")
	private Long supplierId;

	@Schema(description = "Nombre del proveedor", example = "Distribuidora Norte S.A.")
	private String supplierName;

	@Schema(description = "Estado de la compra")
	private PurchaseStatus status;

	@Schema(description = "Total calculado de la compra", example = "250000")
	private Integer totalPrice;

	@Schema(description = "Numero de factura", example = "001-001-00001234")
	private String invoiceNumber;

	@Schema(description = "Lineas de detalle")
	private List<PurchaseDetailResponseDto> details;

	@Schema(description = "Fecha de registro")
	private LocalDateTime createdAt;
}
