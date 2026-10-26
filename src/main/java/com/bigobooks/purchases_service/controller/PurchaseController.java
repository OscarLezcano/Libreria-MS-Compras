package com.bigobooks.purchases_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.bigobooks.dto.Envelope;
import com.bigobooks.util.Envelopes;
import com.bigobooks.util.Pageables;
import com.bigobooks.dto.PurchaseCreateRequest;
import com.bigobooks.dto.PurchaseDto;
import com.bigobooks.dto.PurchaseStatusChange;
import com.bigobooks.entities.purchases.PurchaseStatus;
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

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/purchases")
@Validated
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Registro de compras a proveedores, consulta, cambio de estado y baja logica")
public class PurchaseController {

	private final Pageables pageables;
	private final PurchaseService purchaseService;

	@Value("${app.pagination.page-size:25}")
	private int defaultPageSize;
	@Value("${app.pagination.max-page-size:100}")
	private int maxPageSize;

	@Operation(summary = "Crear compra", description = "Registra una compra con detalles y calcula el total automaticamente")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Compra creada", content = @Content(schema = @Schema(implementation = Envelope.class))),
			@ApiResponse(responseCode = "400", description = "Datos invalidos"),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
	})
	// Crear compra: reservado para ADMIN o VENDEDOR
	@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
	@PostMapping
	public ResponseEntity<Envelope> create(@Valid @RequestBody PurchaseCreateRequest request) {
		PurchaseDto created = purchaseService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new Envelope().data(created));
	}

	@Operation(summary = "Obtener compra por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Compra encontrada", content = @Content(schema = @Schema(implementation = Envelope.class))),
			@ApiResponse(responseCode = "404", description = "Compra no encontrada")
	})

	// Consultar por ID: ADMIN o VENDEDOR
	@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Envelope> getById(
			@Parameter(description = "Identificador de la compra") @PathVariable Long id) {
		return ResponseEntity.ok(new Envelope().data(purchaseService.getById(id)));
	}


	@Operation(summary = "Listar compras", description = "Listado paginado con filtros opcionales")
	@ApiResponse(responseCode = "200", description = "Pagina de compras")
	// Listado paginado: ADMIN o VENDEDOR
	@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
	@GetMapping
	public ResponseEntity<Envelope> list(
			@Parameter(description = "Filtrar por estado") @RequestParam(required = false) PurchaseStatus status,
			@Parameter(description = "Filtrar por almacen") @RequestParam(required = false) Long warehouseId,
			@Parameter(description = "Filtrar por proveedor") @RequestParam(required = false) Long supplierId,
			@Parameter(description = "Filtrar compras que incluyan este libro") @RequestParam(required = false) Long bookId,
			@Parameter(description = "Busqueda parcial por numero de factura") @RequestParam(required = false) String invoiceNumber,
			@Parameter(description = "Fecha de creacion desde (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
			@Parameter(description = "Fecha de creacion hasta (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int size) {
		Pageable pageable = pageables.of(page, size);
		Page<PurchaseDto> result = purchaseService.list(status, warehouseId, supplierId, bookId, invoiceNumber,
				createdFrom, createdTo,
				pageable);
		return ResponseEntity.ok(new Envelope()
				.data(result.getContent())
				.pagination(Envelopes.pagination(result)));
	}

	@Operation(summary = "Actualizar estado de la compra", description = "Transiciones: PENDING -> RECEIVED o CANCELLED")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Estado actualizado", content = @Content(schema = @Schema(implementation = Envelope.class))),
			@ApiResponse(responseCode = "404", description = "Compra no encontrada"),
			@ApiResponse(responseCode = "409", description = "Transicion de estado no permitida")
	})

	// Cambiar estado: solo ADMIN
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{id}/status")
	public ResponseEntity<Envelope> updateStatus(
			@Parameter(description = "Identificador de la compra") @PathVariable Long id,
			@Valid @RequestBody PurchaseStatusChange request) {
		return ResponseEntity.ok(new Envelope().data(purchaseService.updateStatus(id, request)));
	}

	@Operation(summary = "Eliminar compra", description = "Borrado logico de la compra")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Compra eliminada"),
			@ApiResponse(responseCode = "404", description = "Compra no encontrada")
	})

	// Eliminar compra: solo ADMIN
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@Parameter(description = "Identificador de la compra") @PathVariable Long id) {
		purchaseService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
