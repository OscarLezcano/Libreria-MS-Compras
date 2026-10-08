package com.bigobooks.purchases_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.purchases_service.dto.SupplierRequestDto;
import com.bigobooks.purchases_service.dto.SupplierResponseDto;
import com.bigobooks.purchases_service.exception.ConflictException;
import com.bigobooks.purchases_service.exception.NotFoundException;
import com.bigobooks.purchases_service.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

	private final SupplierRepository supplierRepository;

	@Override
	@Transactional
	public SupplierResponseDto create(SupplierRequestDto request) {
		String name = normalizeName(request.getName());
		assertNameAvailable(name, null);

		Supplier supplier = new Supplier();
		supplier.setName(name);
		supplier.setRuc(normalizeRuc(request.getRuc()));

		Supplier saved = supplierRepository.save(supplier);
		log.info("Proveedor id={} creado (nombre={})", saved.getId(), saved.getName());
		return toResponse(saved);
	}

	@Override
	@Transactional
	public SupplierResponseDto update(Long id, SupplierRequestDto request) {
		Supplier supplier = requireById(id);
		String name = normalizeName(request.getName());
		assertNameAvailable(name, id);

		supplier.setName(name);
		supplier.setRuc(normalizeRuc(request.getRuc()));

		Supplier saved = supplierRepository.save(supplier);
		log.info("Proveedor id={} actualizado (nombre={})", saved.getId(), saved.getName());
		return toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public SupplierResponseDto getById(Long id) {
		return toResponse(requireById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SupplierResponseDto> list(Pageable pageable) {
		Page<SupplierResponseDto> page = supplierRepository.findAll(pageable).map(this::toResponse);
		log.info("Listado de proveedores: pagina {} con {} resultado(s)", page.getNumber(), page.getTotalElements());
		return page;
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Supplier supplier = requireById(id);
		supplier.setDeleted(true);
		supplierRepository.save(supplier);
		log.info("Proveedor id={} eliminado (borrado logico, nombre={})", supplier.getId(), supplier.getName());
	}

	private Supplier requireById(Long id) {
		return supplierRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("No existe el proveedor con id " + id));
	}

	private void assertNameAvailable(String name, Long excludeId) {
		boolean taken = excludeId == null
				? supplierRepository.existsByName(name)
				: supplierRepository.existsByNameAndIdNot(name, excludeId);
		if (taken) {
			throw new ConflictException("Ya existe un proveedor con el nombre " + name);
		}
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
		}
		return name.trim();
	}

	private String normalizeRuc(String ruc) {
		if (ruc == null || ruc.isBlank()) {
			return null;
		}
		return ruc.trim();
	}

	private SupplierResponseDto toResponse(Supplier supplier) {
		return SupplierResponseDto.builder()
				.id(supplier.getId())
				.name(supplier.getName())
				.ruc(supplier.getRuc())
				.createdAt(supplier.getCreatedAt())
				.build();
	}
}
