package com.bigobooks.purchases_service.dto;

import com.bigobooks.entities.purchases.PurchaseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Nuevo estado de la compra")
public class PurchaseStatusUpdateRequestDto {

	@NotNull(message = "status es obligatorio")
	@Schema(description = "Estado destino (PENDING, RECEIVED, CANCELLED)", example = "RECEIVED")
	private PurchaseStatus status;
}
