package com.bigobooks.purchases_service.service;

import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.purchases_service.dto.SupplierRequestDto;
import com.bigobooks.purchases_service.dto.SupplierResponseDto;
import com.bigobooks.purchases_service.exception.ConflictException;
import com.bigobooks.purchases_service.exception.NotFoundException;
import com.bigobooks.purchases_service.mappers.SupplierMapper;
import com.bigobooks.purchases_service.repository.SupplierRepository;
import com.bigobooks.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SupplierServiceImpl extends BaseService<Supplier, SupplierRepository> implements SupplierService {

	private final SupplierMapper supplierMapper;

	public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
		super(supplierRepository);
		this.supplierMapper = supplierMapper;
	}

	@Override
	@Transactional
	public SupplierResponseDto create(SupplierRequestDto request) {
		String name = normalizeName(request.getName());
		assertNameAvailable(name, null);

		Supplier supplier = new Supplier();
		supplier.setName(name);
		supplier.setRuc(normalizeRuc(request.getRuc()));

		Supplier saved = save(supplier);
		log.info("Proveedor id={} creado (nombre={})", saved.getId(), saved.getName());
		return supplierMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public SupplierResponseDto update(Long id, SupplierRequestDto request) {
		Supplier supplier = requireById(id);
		String name = normalizeName(request.getName());
		assertNameAvailable(name, id);

		supplier.setName(name);
		supplier.setRuc(normalizeRuc(request.getRuc()));

		Supplier saved = save(supplier);
		log.info("Proveedor id={} actualizado (nombre={})", saved.getId(), saved.getName());
		return supplierMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public SupplierResponseDto getById(Long id) {
		return supplierMapper.toResponse(requireById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SupplierResponseDto> list(Pageable pageable) {
		Page<SupplierResponseDto> page = getRepository().findAll(pageable).map(supplierMapper::toResponse);
		log.info("Listado de proveedores: pagina {} con {} resultado(s)", page.getNumber(), page.getTotalElements());
		return page;
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Supplier supplier = requireById(id);
		supplier.setDeleted(true);
		save(supplier);
		log.info("Proveedor id={} eliminado (borrado logico, nombre={})", supplier.getId(), supplier.getName());
	}

	private Supplier requireById(Long id) {
		return findById(id).orElseThrow(() -> new NotFoundException("Proveedor no encontrado con id: " + id));
	}

	private String normalizeName(String name) {
		return name != null ? name.trim() : null;
	}

	private String normalizeRuc(String ruc) {
		return ruc != null ? ruc.trim() : null;
	}

	private void assertNameAvailable(String name, Long currentId) {
		getRepository().findByNameIgnoreCase(name).ifPresent(existing -> {
			if (currentId == null || !existing.getId().equals(currentId)) {
				throw new ConflictException("Ya existe un proveedor con el nombre: " + name);
			}
		});
	}
}