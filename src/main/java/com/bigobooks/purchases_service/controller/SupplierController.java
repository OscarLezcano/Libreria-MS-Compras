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
import com.bigobooks.dto.SupplierDto;
import com.bigobooks.dto.SupplierCreateRequest;
import com.bigobooks.dto.SupplierUpdateRequest;
import com.bigobooks.purchases_service.service.SupplierService;

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
@RequestMapping("/api/v1/suppliers")
@Validated
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Alta, consulta, actualizacion y baja logica de proveedores")
public class SupplierController {

	private final Pageables pageables;
	private final SupplierService supplierService;

	@Value("${app.pagination.page-size:25}")
	private int defaultPageSize;
	@Value("${app.pagination.max-page-size:100}")
	private int maxPageSize;

	@Operation(summary = "Crear proveedor", description = "Registra un nuevo proveedor en el sistema")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Proveedor creado", content = @Content(schema = @Schema(implementation = Envelope.class))),
			@ApiResponse(responseCode = "400", description = "Datos invalidos"),
			@ApiResponse(responseCode = "409", description = "Nombre de proveedor duplicado")
	})

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<Envelope> create(@Valid @RequestBody SupplierCreateRequest request) {
		SupplierDto created = supplierService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new Envelope().data(created));
	}

	@Operation(summary = "Actualizar proveedor", description = "Actualiza nombre y RUC de un proveedor existente")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Proveedor actualizado", content = @Content(schema = @Schema(implementation = Envelope.class))),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado"),
			@ApiResponse(responseCode = "409", description = "Nombre de proveedor duplicado")
	})

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<Envelope> update(
			@Parameter(description = "Identificador del proveedor") @PathVariable Long id,
			@Valid @RequestBody SupplierUpdateRequest request) {
		return ResponseEntity.ok(new Envelope().data(supplierService.update(id, request)));
	}

	@Operation(summary = "Obtener proveedor por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Proveedor encontrado", content = @Content(schema = @Schema(implementation = Envelope.class))),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
	})
	@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Envelope> getById(
			@Parameter(description = "Identificador del proveedor") @PathVariable Long id) {
		return ResponseEntity.ok(new Envelope().data(supplierService.getById(id)));
	}

	@Operation(summary = "Listar proveedores", description = "Listado paginado de proveedores activos (no eliminados)")
	@ApiResponse(responseCode = "200", description = "Pagina de proveedores")
	@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
	@GetMapping
	public ResponseEntity<Envelope> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int size) {
		Pageable pageable = pageables.of(page, size);
		Page<SupplierDto> result = supplierService.list(pageable);
		return ResponseEntity.ok(new Envelope()
				.data(result.getContent())
				.pagination(Envelopes.pagination(result)));
	}

	@Operation(summary = "Eliminar proveedor", description = "Borrado logico del proveedor")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Proveedor eliminado"),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
	})
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@Parameter(description = "Identificador del proveedor") @PathVariable Long id) {
		supplierService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
