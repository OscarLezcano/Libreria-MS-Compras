package com.bigobooks.purchases_service.service;

import com.bigobooks.dto.SupplierDto;
import com.bigobooks.dto.SupplierCreateRequest;
import com.bigobooks.dto.SupplierUpdateRequest;
import com.bigobooks.entities.purchases.Supplier;
import com.bigobooks.purchases_service.exception.ConflictException;
import com.bigobooks.purchases_service.exception.NotFoundException;
import com.bigobooks.purchases_service.mappers.SupplierMapper;
import com.bigobooks.purchases_service.repository.SupplierRepository;
import com.bigobooks.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
	public SupplierDto create(SupplierCreateRequest request) {
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
	@CacheEvict(value = "suppliers", key = "'supplier_' + #id")
	public SupplierDto update(Long id, SupplierUpdateRequest request) {
		log.info("[CACHE EVICT] Desalojando proveedor id={} de Redis por actualización", id);
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
	@Cacheable(value = "suppliers", key = "'supplier_' + #id")
	public SupplierDto getById(Long id) {
		log.info("[CACHE MISS / DB HIT] Buscando proveedor id={} en base de datos", id);
		return supplierMapper.toResponse(requireById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SupplierDto> list(Pageable pageable) {
		// Sin @Cacheable para cumplir la restricción de no almacenar paginaciones
		// completas
		Page<SupplierDto> page = getRepository().findAll(pageable).map(supplierMapper::toResponse);
		log.info("Listado de proveedores: pagina {} con {} resultado(s)", page.getNumber(), page.getTotalElements());
		return page;
	}

	@Override
	@Transactional
	@CacheEvict(value = "suppliers", key = "'supplier_' + #id")
	public void delete(Long id) {
		log.info("[CACHE EVICT] Desalojando proveedor id={} de Redis por borrado", id);
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