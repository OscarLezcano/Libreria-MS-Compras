package com.bigobooks.purchases_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Linea de detalle al registrar una compra")
public class PurchaseDetailRequestDto {

	@NotNull(message = "bookId es obligatorio")
	@Schema(description = "Identificador del libro", example = "42")
	private Long bookId;

	@NotBlank(message = "bookName es obligatorio")
	@Schema(description = "Nombre del libro al momento de la compra", example = "Clean Code")
	private String bookName;

	@NotNull(message = "quantity es obligatorio")
	@Min(value = 1, message = "quantity debe ser mayor a 0")
	@Schema(description = "Cantidad comprada", example = "10")
	private Integer quantity;

	@NotNull(message = "unitCost es obligatorio")
	@Min(value = 1, message = "unitCost debe ser mayor a 0")
	@Schema(description = "Costo unitario en la moneda del sistema", example = "25000")
	private Long unitCost;
}
