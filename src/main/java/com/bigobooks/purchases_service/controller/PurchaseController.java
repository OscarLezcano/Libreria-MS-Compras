package com.bigobooks.purchases_service.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bigobooks.entities.purchases.PurchaseStatus;
import com.bigobooks.purchases_service.dto.PurchaseCreateRequestDto;
import com.bigobooks.purchases_service.dto.PurchaseResponseDto;
import com.bigobooks.purchases_service.dto.PurchaseStatusUpdateRequestDto;
import com.bigobooks.purchases_service.service.PurchaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/purchases")
@Validated
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Registro de compras a proveedores, consulta, cambio de estado y baja logica")
public class PurchaseController {

	private final PurchaseService purchaseService;

	@Operation(summary = "Crear compra", description = "Registra una compra con detalles y calcula el total automaticamente")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Compra creada",
					content = @Content(schema = @Schema(implementation = PurchaseResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos invalidos"),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
	})
	@PostMapping
	public ResponseEntity<PurchaseResponseDto> create(@Valid @RequestBody PurchaseCreateRequestDto request) {
		PurchaseResponseDto created = purchaseService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@Operation(summary = "Obtener compra por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Compra encontrada",
					content = @Content(schema = @Schema(implementation = PurchaseResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "Compra no encontrada")
	})
	@GetMapping("/{id}")
	public ResponseEntity<PurchaseResponseDto> getById(
			@Parameter(description = "Identificador de la compra") @PathVariable Long id) {
		return ResponseEntity.ok(purchaseService.getById(id));
	}

	@Operation(summary = "Listar compras", description = "Listado paginado con filtros opcionales")
	@ApiResponse(responseCode = "200", description = "Pagina de compras")
	@GetMapping
	public ResponseEntity<Page<PurchaseResponseDto>> list(
			@Parameter(description = "Filtrar por estado") @RequestParam(required = false) PurchaseStatus status,
			@Parameter(description = "Filtrar por almacen") @RequestParam(required = false) Long warehouseId,
			@Parameter(description = "Filtrar por proveedor") @RequestParam(required = false) Long supplierId,
			@Parameter(description = "Filtrar compras que incluyan este libro") @RequestParam(required = false) Long bookId,
			@Parameter(description = "Busqueda parcial por numero de factura") @RequestParam(required = false) String invoiceNumber,
			@Parameter(description = "Fecha de creacion desde (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
			@Parameter(description = "Fecha de creacion hasta (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
			Pageable pageable) {
		return ResponseEntity.ok(
				purchaseService.list(status, warehouseId, supplierId, bookId, invoiceNumber, createdFrom, createdTo,
						pageable));
	}

	@Operation(summary = "Actualizar estado de la compra", description = "Transiciones: PENDING -> RECEIVED o CANCELLED")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Estado actualizado",
					content = @Content(schema = @Schema(implementation = PurchaseResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "Compra no encontrada"),
			@ApiResponse(responseCode = "409", description = "Transicion de estado no permitida")
	})
	@PatchMapping("/{id}/status")
	public ResponseEntity<PurchaseResponseDto> updateStatus(
			@Parameter(description = "Identificador de la compra") @PathVariable Long id,
			@Valid @RequestBody PurchaseStatusUpdateRequestDto request) {
		return ResponseEntity.ok(purchaseService.updateStatus(id, request));
	}

	@Operation(summary = "Eliminar compra", description = "Borrado logico de la compra")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Compra eliminada"),
			@ApiResponse(responseCode = "404", description = "Compra no encontrada")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@Parameter(description = "Identificador de la compra") @PathVariable Long id) {
		purchaseService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
