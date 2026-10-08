package com.bigobooks.purchases_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bigobooks.purchases_service.dto.SupplierRequestDto;
import com.bigobooks.purchases_service.dto.SupplierResponseDto;
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

	private final SupplierService supplierService;

	@Operation(summary = "Crear proveedor", description = "Registra un nuevo proveedor en el sistema")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Proveedor creado",
					content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Datos invalidos"),
			@ApiResponse(responseCode = "409", description = "Nombre de proveedor duplicado")
	})
	@PostMapping
	public ResponseEntity<SupplierResponseDto> create(@Valid @RequestBody SupplierRequestDto request) {
		SupplierResponseDto created = supplierService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@Operation(summary = "Actualizar proveedor", description = "Actualiza nombre y RUC de un proveedor existente")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Proveedor actualizado",
					content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado"),
			@ApiResponse(responseCode = "409", description = "Nombre de proveedor duplicado")
	})
	@PutMapping("/{id}")
	public ResponseEntity<SupplierResponseDto> update(
			@Parameter(description = "Identificador del proveedor") @PathVariable Long id,
			@Valid @RequestBody SupplierRequestDto request) {
		return ResponseEntity.ok(supplierService.update(id, request));
	}

	@Operation(summary = "Obtener proveedor por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Proveedor encontrado",
					content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
	})
	@GetMapping("/{id}")
	public ResponseEntity<SupplierResponseDto> getById(
			@Parameter(description = "Identificador del proveedor") @PathVariable Long id) {
		return ResponseEntity.ok(supplierService.getById(id));
	}

	@Operation(summary = "Listar proveedores", description = "Listado paginado de proveedores activos (no eliminados)")
	@ApiResponse(responseCode = "200", description = "Pagina de proveedores")
	@GetMapping
	public ResponseEntity<Page<SupplierResponseDto>> list(Pageable pageable) {
		return ResponseEntity.ok(supplierService.list(pageable));
	}

	@Operation(summary = "Eliminar proveedor", description = "Borrado logico del proveedor")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Proveedor eliminado"),
			@ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@Parameter(description = "Identificador del proveedor") @PathVariable Long id) {
		supplierService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
