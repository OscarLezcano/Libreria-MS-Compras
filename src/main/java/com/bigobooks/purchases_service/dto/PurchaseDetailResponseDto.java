package com.bigobooks.purchases_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Linea de detalle de una compra")
public class PurchaseDetailResponseDto {

	@Schema(description = "Identificador del detalle", example = "100")
	private Long id;

	@Schema(description = "Identificador del libro", example = "42")
	private Long bookId;

	@Schema(description = "Nombre del libro", example = "Clean Code")
	private String bookName;

	@Schema(description = "Cantidad", example = "10")
	private Integer quantity;

	@Schema(description = "Costo unitario", example = "25000")
	private Long unitCost;

	@Schema(description = "Subtotal de la linea (quantity * unitCost)", example = "250000")
	private Long lineTotal;
}
